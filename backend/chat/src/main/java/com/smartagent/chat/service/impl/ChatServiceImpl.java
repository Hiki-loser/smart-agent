package com.smartagent.chat.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.chat.dto.ChatRequestDTO;
import com.smartagent.chat.dto.CreateSessionDTO;
import com.smartagent.chat.entity.LlmMessageEntity;
import com.smartagent.chat.entity.LlmSessionEntity;
import com.smartagent.chat.entity.SessionEntity;
import com.smartagent.chat.entity.UserMessageEntity;
import com.smartagent.chat.feign.MemoryFeignClient;
import com.smartagent.chat.mapper.LlmMessageMapper;
import com.smartagent.chat.mapper.LlmSessionMapper;
import com.smartagent.chat.mapper.SessionMapper;
import com.smartagent.chat.mapper.UserMessageMapper;
import com.smartagent.chat.producer.RocketMQProducer;
import com.smartagent.chat.service.ChatMessageLoader;
import com.smartagent.chat.service.ChatService;
import com.smartagent.chat.vo.MessageVO;
import com.smartagent.chat.vo.SessionVO;
import com.smartagent.common.event.MessageArchiveEvent;
import com.smartagent.common.model.ApiResponse;
import com.smartagent.common.model.ContextMessage;
import com.smartagent.common.model.MemoryContextRequest;
import com.smartagent.common.model.MemorySummaryRequest;
import com.smartagent.common.utils.UserContextUtils;
import com.smartagent.model.dto.AiStreamUsageMetadata;
import com.smartagent.model.dto.ModelRequest;
import com.smartagent.model.config.AIModelConfig;
import com.smartagent.model.dto.StreamChunk;
import com.smartagent.model.service.ModelService;
import com.smartagent.model.utils.TokenCountUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final long SSE_TIMEOUT = 30 * 60 * 1000;

    @Resource
    private SessionMapper sessionMapper;

    @Resource
    private UserMessageMapper userMessageMapper;

    @Resource
    private LlmMessageMapper llmMessageMapper;

    @Resource
    private LlmSessionMapper llmSessionMapper;

    @Resource
    private RocketMQProducer rocketMQProducer;

    @Resource
    private MemoryFeignClient memoryFeignClient;

    @Resource
    private ChatMessageLoader chatMessageLoader;

    @Resource
    private ModelService modelService;

    @Resource
    private AIModelConfig aiModelConfig;

    @Value("${chat.memory.recent-message-limit:12}")
    private int recentMessageLimit;

    @Value("${chat.memory.summary-session-round-warning:200}")
    private int summarySessionRoundWarning;

    @Override
    public SseEmitter sendMessage(ChatRequestDTO dto) {

        Long userId = UserContextUtils.getUserId();
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT);

        Thread.startVirtualThread(() -> {

            StringBuilder fullResponse = new StringBuilder();
            AtomicReference<AiStreamUsageMetadata> usageRef = new AtomicReference<>();
            AtomicBoolean llmSessionSaved = new AtomicBoolean(false);
            LocalDateTime startedAt = LocalDateTime.now();

            try {
                if (dto == null || dto.getSessionId() == null || dto.getContent() == null || dto.getContent().isBlank()) {
                    emitter.send(SseEmitter.event().data("{\"error\": \"请求参数不合法\"}"));
                    emitter.complete();
                    return;
                }

                // ================== 1. 校验会话 ==================
                SessionEntity session = sessionMapper.selectById(dto.getSessionId());
                if (session == null) {
                    emitter.send(SseEmitter.event().data("{\"error\": \"会话不存在\"}"));
                    emitter.complete();
                    return;
                }

                if (!session.getUserId().equals(userId)) {
                    emitter.send(SseEmitter.event().data("{\"error\": \"无权限访问该会话\"}"));
                    emitter.complete();
                    return;
                }

                int nextRoundNo = (session.getRoundCount() == null ? 0 : session.getRoundCount()) + 1;

                // ================== 2. 构建上下文（调用 memory 模块） ==================
                final String promptWithMemory = buildPromptWithMemory(dto);
                final Long sessionId = dto.getSessionId();

                // ================== 3. 保存用户消息 ==================
                UserMessageEntity userMessage = new UserMessageEntity();
                userMessage.setSessionId(dto.getSessionId());
                userMessage.setUserId(userId);
                userMessage.setRoundNo(nextRoundNo);
                userMessage.setContent(dto.getContent());
                userMessage.setTokens(TokenCountUtils.countTokens(dto.getContent()));
                userMessage.setCreatedAt(LocalDateTime.now());
                userMessageMapper.insert(userMessage);

                // ================== 4. 更新会话 ==================
                session.setMessageCount(session.getMessageCount() + 1);
                session.setLastMessageAt(LocalDateTime.now());
                sessionMapper.updateById(session);

                // ================== 5. 调用 model 模块流式对话 ==================
                String systemPrompt = loadSystemPrompt();
                ModelRequest modelRequest = new ModelRequest();
                String modelName = aiModelConfig.resolvedStreamingChatModelName();
                modelRequest.setModelName(modelName != null && !modelName.isBlank() ? modelName : "deepseek-v4-pro");
                modelRequest.setMessages(List.of(
                        new ModelRequest.Message("system", systemPrompt, null),
                        new ModelRequest.Message("user", promptWithMemory, null)
                ));

                modelService.chatStream(modelRequest)
                        .doOnNext(chunk -> {
                            try {
                                if (chunk.getContent() != null) {
                                    fullResponse.append(chunk.getContent());
                                    emitter.send(SseEmitter.event().data(chunk.getContent()));
                                }
                                if (chunk.getUsage() != null) {
                                    usageRef.set(chunk.getUsage());
                                }
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                        .doOnError(error -> {
                            log.error("Model stream error: {}", error.getMessage(), error);
                            if (!llmSessionSaved.get()) {
                                persistFailedLlmSession(dto.getSessionId(), userMessage.getId(), session.getAgentType(),
                                        promptWithMemory, fullResponse.toString(), error.getMessage(), startedAt);
                                llmSessionSaved.set(true);
                            }
                            emitter.completeWithError(error);
                        })
                        .doOnComplete(() -> {
                            try {
                                AiStreamUsageMetadata usageMetadata = usageRef.get();

                                // ================== 6. 保存 AI 回复 ==================
                                LlmMessageEntity aiMessage = buildLlmMessage(dto.getSessionId(), nextRoundNo,
                                        session.getAgentType(), promptWithMemory, fullResponse.toString(), usageMetadata);
                                llmMessageMapper.insert(aiMessage);

                                LlmSessionEntity llmSession = buildSuccessfulLlmSession(dto.getSessionId(),
                                        userMessage.getId(), aiMessage.getId(), session.getAgentType(), promptWithMemory,
                                        fullResponse.toString(), usageMetadata, startedAt);
                                llmSessionMapper.insert(llmSession);
                                llmSessionSaved.set(true);

                                // ================== 7. 更新会话 ==================
                                session.setMessageCount(session.getMessageCount() + 1);
                                session.setRoundCount(nextRoundNo);
                                session.setLastMessageAt(LocalDateTime.now());
                                sessionMapper.updateById(session);

                                // ================== 8. 触发记忆摘要（调用 memory 模块） ==================
                                triggerSummaryIfNeeded(dto.getSessionId(), userId, nextRoundNo);

                                // ================== 9. MQ 归档 ==================
                                MessageArchiveEvent archiveEvent = new MessageArchiveEvent();
                                archiveEvent.setSessionId(dto.getSessionId());
                                archiveEvent.setContent(fullResponse.toString());
                                archiveEvent.setUserId(userId);
                                rocketMQProducer.sendMessageArchiveEvent(archiveEvent);

                                // ================== 10. 结束 SSE ==================
                                emitter.complete();

                            } catch (Exception e) {
                                log.error("Failed on complete: {}", e.getMessage(), e);
                                emitter.completeWithError(e);
                            }
                        })
                        .blockLast();

            } catch (Exception e) {

                log.error("Failed to process chat message: {}", e.getMessage(), e);

                try {
                    emitter.send(SseEmitter.event().data("{\"error\": \"处理消息失败\"}"));
                } catch (IOException ex) {
                    log.error("Failed to send error message: {}", ex.getMessage(), ex);
                }

                if (!llmSessionSaved.get()) {
                    Long safeSessionId = dto == null ? null : dto.getSessionId();
                    String safeAgentType = dto == null ? null : dto.getAgentType();
                    String safeContent = dto == null ? "" : dto.getContent();
                    if (safeSessionId != null) {
                        persistFailedLlmSession(safeSessionId, null, safeAgentType, safeContent,
                                fullResponse.toString(), e.getMessage(), startedAt);
                    }
                }

                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    @Override
    @Transactional
    public SessionVO createSession(CreateSessionDTO dto) {
        Long userId = UserContextUtils.getUserId();
        SessionEntity session = new SessionEntity();
        session.setUserId(userId);
        session.setTitle(dto.getTitle());
        session.setAgentType(dto.getAgentType());
        session.setMessageCount(0);
        session.setRoundCount(0);
        session.setStatus(1);
        session.setCreatedAt(LocalDateTime.now());
        session.setLastMessageAt(LocalDateTime.now());
        sessionMapper.insert(session);

        SessionVO sessionVO = new SessionVO();
        BeanUtils.copyProperties(session, sessionVO);
        return sessionVO;
    }

    @Override
    public List<MessageVO> getSessionHistory(Long sessionId) {
        Long userId = UserContextUtils.getUserId();
        SessionEntity session = sessionMapper.selectById(sessionId);
        if (session == null || !session.getUserId().equals(userId)) {
            return new ArrayList<>();
        }

        List<MessageVO> messageVOs = new ArrayList<>();

        LambdaQueryWrapper<UserMessageEntity> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(UserMessageEntity::getSessionId, sessionId)
                .orderByAsc(UserMessageEntity::getCreatedAt);

        for (UserMessageEntity message : userMessageMapper.selectList(userQuery)) {
            MessageVO messageVO = new MessageVO();
            messageVO.setRole("USER");
            messageVO.setContent(message.getContent());
            messageVO.setCompletionTokens(message.getTokens());
            messageVO.setTotalTokens(message.getTokens());
            messageVO.setCreatedAt(message.getCreatedAt());
            messageVOs.add(messageVO);
        }

        LambdaQueryWrapper<LlmMessageEntity> llmQuery = new LambdaQueryWrapper<>();
        llmQuery.eq(LlmMessageEntity::getSessionId, sessionId)
                .orderByAsc(LlmMessageEntity::getCreatedAt);

        for (LlmMessageEntity message : llmMessageMapper.selectList(llmQuery)) {
            MessageVO messageVO = new MessageVO();
            messageVO.setRole("ASSISTANT");
            messageVO.setContent(message.getContent());
            messageVO.setModelName(message.getModelName());
            messageVO.setPromptTokens(message.getPromptTokens());
            messageVO.setCompletionTokens(message.getCompletionTokens());
            messageVO.setTotalTokens(message.getTotalTokens());
            messageVO.setFinishReason(message.getFinishReason());
            messageVO.setCreatedAt(message.getCreatedAt());
            messageVOs.add(messageVO);
        }

        messageVOs.sort(Comparator.comparing(MessageVO::getCreatedAt));
        return messageVOs;
    }

    @Override
    public List<SessionVO> getSessionList() {
        Long userId = UserContextUtils.getUserId();
        LambdaQueryWrapper<SessionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SessionEntity::getUserId, userId)
                .orderByDesc(SessionEntity::getLastMessageAt);

        List<SessionEntity> sessions = sessionMapper.selectList(queryWrapper);

        List<SessionVO> sessionVOs = new ArrayList<>();
        for (SessionEntity session : sessions) {
            SessionVO sessionVO = new SessionVO();
            BeanUtils.copyProperties(session, sessionVO);
            int roundCount = session.getRoundCount() == null ? 0 : session.getRoundCount();
            boolean shouldWarn = roundCount >= Math.max(1, summarySessionRoundWarning);
            sessionVO.setShouldCreateNewSession(shouldWarn);
            if (shouldWarn) {
                sessionVO.setSessionHint("当前会话历史较长，建议新建会话以获得更稳定的回复效果");
            }
            sessionVOs.add(sessionVO);
        }

        return sessionVOs;
    }

    private String buildPromptWithMemory(ChatRequestDTO dto) {
        try {
            List<ContextMessage> recentMessages = chatMessageLoader.loadRecent(dto.getSessionId(), recentMessageLimit);
            MemoryContextRequest contextRequest = new MemoryContextRequest();
            contextRequest.setSessionId(dto.getSessionId());
            contextRequest.setCurrentUserMessage(dto.getContent());
            contextRequest.setRecentMessages(recentMessages);

            ApiResponse<String> contextResponse = memoryFeignClient.buildContext(contextRequest);
            if (contextResponse != null && contextResponse.getCode() == 200) {
                return contextResponse.getData();
            }
        } catch (Exception e) {
            log.warn("Failed to call memory context/build, using raw message. sessionId={}, error={}",
                    dto.getSessionId(), e.getMessage());
        }
        return dto.getContent();
    }

    private void triggerSummaryIfNeeded(Long sessionId, Long userId, int currentRound) {
        try {
            List<ContextMessage> roundMessages = chatMessageLoader.loadByRoundRange(sessionId, 1, currentRound);
            if (roundMessages.isEmpty()) {
                return;
            }
            MemorySummaryRequest summaryRequest = new MemorySummaryRequest();
            summaryRequest.setSessionId(sessionId);
            summaryRequest.setUserId(userId);
            summaryRequest.setCurrentRound(currentRound);
            summaryRequest.setMessages(roundMessages);
            memoryFeignClient.processSummary(summaryRequest);
        } catch (Exception e) {
            log.warn("Failed to trigger memory summary, sessionId={}, error={}", sessionId, e.getMessage());
        }
    }

    private String loadSystemPrompt() {
        try {
            InputStream is = getClass().getClassLoader()
                    .getResourceAsStream("streamChat-system-prompt.md");
            if (is == null) {
                return "";
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Failed to load system prompt", e);
            return "";
        }
    }

    private LlmMessageEntity buildLlmMessage(Long sessionId, Integer roundNo, String defaultModelName, String prompt,
                                             String completion, AiStreamUsageMetadata usageMetadata) {
        int promptTokens = usageMetadata != null && usageMetadata.getPromptTokens() != null
                ? usageMetadata.getPromptTokens() : TokenCountUtils.countTokens(prompt);
        int completionTokens = usageMetadata != null && usageMetadata.getCompletionTokens() != null
                ? usageMetadata.getCompletionTokens() : TokenCountUtils.countTokens(completion);
        int totalTokens = usageMetadata != null && usageMetadata.getTotalTokens() != null
                ? usageMetadata.getTotalTokens() : promptTokens + completionTokens;

        LlmMessageEntity aiMessage = new LlmMessageEntity();
        aiMessage.setSessionId(sessionId);
        aiMessage.setRoundNo(roundNo);
        aiMessage.setModelName(usageMetadata != null && usageMetadata.getModelName() != null
                ? usageMetadata.getModelName() : defaultModelName);
        aiMessage.setContent(completion);
        aiMessage.setPromptTokens(promptTokens);
        aiMessage.setCompletionTokens(completionTokens);
        aiMessage.setTotalTokens(totalTokens);
        aiMessage.setFinishReason(usageMetadata != null ? usageMetadata.getFinishReason() : "STOP");
        aiMessage.setCreatedAt(LocalDateTime.now());
        return aiMessage;
    }

    private LlmSessionEntity buildSuccessfulLlmSession(Long sessionId, Long userMessageId, Long llmMessageId,
                                                       String defaultModelName, String prompt, String completion,
                                                       AiStreamUsageMetadata usageMetadata, LocalDateTime startedAt) {
        int promptTokens = usageMetadata != null && usageMetadata.getPromptTokens() != null
                ? usageMetadata.getPromptTokens() : TokenCountUtils.countTokens(prompt);
        int completionTokens = usageMetadata != null && usageMetadata.getCompletionTokens() != null
                ? usageMetadata.getCompletionTokens() : TokenCountUtils.countTokens(completion);
        int totalTokens = usageMetadata != null && usageMetadata.getTotalTokens() != null
                ? usageMetadata.getTotalTokens() : promptTokens + completionTokens;

        LlmSessionEntity llmSession = new LlmSessionEntity();
        llmSession.setSessionId(sessionId);
        llmSession.setUserMessageId(userMessageId);
        llmSession.setLlmMessageId(llmMessageId);
        llmSession.setModelName(usageMetadata != null && usageMetadata.getModelName() != null
                ? usageMetadata.getModelName() : defaultModelName);
        llmSession.setPromptTokens(promptTokens);
        llmSession.setCompletionTokens(completionTokens);
        llmSession.setTotalTokens(totalTokens);
        llmSession.setFinishReason(usageMetadata != null ? usageMetadata.getFinishReason() : "STOP");
        llmSession.setStatus("SUCCESS");
        llmSession.setStartedAt(startedAt);
        llmSession.setCompletedAt(LocalDateTime.now());
        llmSession.setCreatedAt(LocalDateTime.now());
        return llmSession;
    }

    private void persistFailedLlmSession(Long sessionId, Long userMessageId, String modelName,
                                         String prompt, String partialCompletion,
                                         String errorMessage, LocalDateTime startedAt) {
        int promptTokens = TokenCountUtils.countTokens(prompt);
        int completionTokens = TokenCountUtils.countTokens(partialCompletion);

        LlmSessionEntity llmSession = new LlmSessionEntity();
        llmSession.setSessionId(sessionId);
        llmSession.setUserMessageId(userMessageId);
        llmSession.setModelName(modelName);
        llmSession.setPromptTokens(promptTokens);
        llmSession.setCompletionTokens(completionTokens);
        llmSession.setTotalTokens(promptTokens + completionTokens);
        llmSession.setFinishReason("ERROR");
        llmSession.setStatus("FAILED");
        llmSession.setErrorMessage(errorMessage);
        llmSession.setStartedAt(startedAt);
        llmSession.setCompletedAt(LocalDateTime.now());
        llmSession.setCreatedAt(LocalDateTime.now());
        llmSessionMapper.insert(llmSession);
    }
}
