package com.smartagent.memory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.memory.entity.MemoryCurrentEntity;
import com.smartagent.memory.entity.MemoryHistoryEntity;
import com.smartagent.memory.mapper.MemoryCurrentMapper;
import com.smartagent.memory.mapper.MemoryHistoryMapper;
import com.smartagent.common.model.ContextMessage;
import com.smartagent.model.config.AIModelConfig;
import com.smartagent.model.service.ModelService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class MemorySummaryProcessor {

    @Resource
    private MemoryCurrentMapper memoryCurrentMapper;

    @Resource
    private MemoryHistoryMapper memoryHistoryMapper;

    @Resource
    private ModelService modelService;

    @Resource
    private AIModelConfig aiModelConfig;

    public void process(Long sessionId, Long userId, int targetRound, List<ContextMessage> messages) {
        if (sessionId == null || messages == null || messages.isEmpty()) {
            return;
        }

        MemoryCurrentEntity currentMemory = getCurrentMemory(sessionId);
        int summarizedRound = currentMemory == null || currentMemory.getSummarizedRound() == null
                ? 0 : currentMemory.getSummarizedRound();

        if (targetRound <= summarizedRound) {
            return;
        }

        String summaryInput = buildSummaryInput(currentMemory, messages);
        String summaryResult = callModelSummary(summaryInput, sessionId);
        if (summaryResult == null || summaryResult.isBlank()) {
            log.warn("Summary result is empty, sessionId={}", sessionId);
            return;
        }

        int startRound = summarizedRound + 1;
        upsertCurrentMemory(sessionId, userId, currentMemory, targetRound, summaryResult);
        insertHistory(sessionId, userId, startRound, targetRound, summaryResult);
    }

    public int getSummarizedRound(Long sessionId) {
        MemoryCurrentEntity currentMemory = getCurrentMemory(sessionId);
        if (currentMemory == null || currentMemory.getSummarizedRound() == null) {
            return 0;
        }
        return currentMemory.getSummarizedRound();
    }

    private MemoryCurrentEntity getCurrentMemory(Long sessionId) {
        LambdaQueryWrapper<MemoryCurrentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryCurrentEntity::getSessionId, sessionId).last("limit 1");
        return memoryCurrentMapper.selectOne(wrapper);
    }

    private String buildSummaryInput(MemoryCurrentEntity currentMemory,
                                     List<ContextMessage> messages) {
        StringBuilder builder = new StringBuilder();
        builder.append("历史摘要:\n");
        if (currentMemory == null
                || currentMemory.getSummaryContent() == null
                || currentMemory.getSummaryContent().isBlank()) {
            builder.append("暂无\n\n");
        } else {
            builder.append(currentMemory.getSummaryContent().trim()).append("\n\n");
        }

        builder.append("新增对话:\n");
        for (ContextMessage message : messages) {
            builder.append(message.role())
                    .append(": ")
                    .append(message.content())
                    .append("\n");
        }
        builder.append("\n请基于历史摘要和新增对话生成新的完整摘要。");
        return builder.toString();
    }

    private String callModelSummary(String summaryInput, Long sessionId) {
        try {
            com.smartagent.model.dto.ModelRequest request = new com.smartagent.model.dto.ModelRequest();
            String modelName = aiModelConfig.getChatModelName();
            request.setModelName(modelName != null && !modelName.isBlank() ? modelName : "deepseek-v4-pro");
            request.setMessages(List.of(
                    new com.smartagent.model.dto.ModelRequest.Message("user", summaryInput, null)
            ));
            com.smartagent.model.dto.ModelResponse response = modelService.chat(request);
            return response != null ? response.getContent() : null;
        } catch (Exception e) {
            log.error("Failed to call model for summary, sessionId={}, error={}", sessionId, e.getMessage(), e);
            return null;
        }
    }

    private void upsertCurrentMemory(Long sessionId, Long userId,
                                     MemoryCurrentEntity currentMemory,
                                     int targetRound, String summaryResult) {
        if (currentMemory == null) {
            currentMemory = new MemoryCurrentEntity();
            currentMemory.setSessionId(sessionId);
            currentMemory.setUserId(userId);
            currentMemory.setSummaryContent(summaryResult);
            currentMemory.setSummarizedRound(targetRound);
            currentMemory.setCreatedAt(LocalDateTime.now());
            currentMemory.setUpdatedAt(LocalDateTime.now());
            memoryCurrentMapper.insert(currentMemory);
        } else {
            currentMemory.setUserId(userId);
            currentMemory.setSummaryContent(summaryResult);
            currentMemory.setSummarizedRound(targetRound);
            currentMemory.setUpdatedAt(LocalDateTime.now());
            memoryCurrentMapper.updateById(currentMemory);
        }
    }

    private void insertHistory(Long sessionId, Long userId,
                               int startRound, int endRound, String summaryResult) {
        MemoryHistoryEntity history = new MemoryHistoryEntity();
        history.setSessionId(sessionId);
        history.setUserId(userId);
        history.setStartRound(startRound);
        history.setEndRound(endRound);
        history.setSummaryContent(summaryResult);
        history.setCreatedAt(LocalDateTime.now());
        memoryHistoryMapper.insert(history);
    }
}
