package com.smartagent.chat.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.chat.entity.LlmMessageEntity;
import com.smartagent.chat.entity.UserMessageEntity;
import com.smartagent.chat.mapper.LlmMessageMapper;
import com.smartagent.chat.mapper.UserMessageMapper;
import com.smartagent.common.model.ContextMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class ChatMessageLoader {

    @Resource
    private UserMessageMapper userMessageMapper;

    @Resource
    private LlmMessageMapper llmMessageMapper;

    public List<ContextMessage> loadRecent(Long sessionId, int limit) {
        List<ContextMessage> messages = new ArrayList<>();

        LambdaQueryWrapper<UserMessageEntity> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(UserMessageEntity::getSessionId, sessionId)
                .orderByDesc(UserMessageEntity::getCreatedAt)
                .last("limit " + limit);
        for (UserMessageEntity entity : userMessageMapper.selectList(userQuery)) {
            messages.add(new ContextMessage("用户", entity.getContent(), entity.getCreatedAt()));
        }

        LambdaQueryWrapper<LlmMessageEntity> llmQuery = new LambdaQueryWrapper<>();
        llmQuery.eq(LlmMessageEntity::getSessionId, sessionId)
                .orderByDesc(LlmMessageEntity::getCreatedAt)
                .last("limit " + limit);
        for (LlmMessageEntity entity : llmMessageMapper.selectList(llmQuery)) {
            messages.add(new ContextMessage("助手", entity.getContent(), entity.getCreatedAt()));
        }

        messages.sort(Comparator.comparing(ContextMessage::createdAt));
        if (messages.size() > limit) {
            messages = messages.subList(messages.size() - limit, messages.size());
        }
        return messages;
    }

    public List<ContextMessage> loadByRoundRange(Long sessionId, int startRound, int endRound) {
        List<ContextMessage> messages = new ArrayList<>();

        LambdaQueryWrapper<UserMessageEntity> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(UserMessageEntity::getSessionId, sessionId)
                .ge(UserMessageEntity::getRoundNo, startRound)
                .le(UserMessageEntity::getRoundNo, endRound)
                .orderByAsc(UserMessageEntity::getCreatedAt);
        for (UserMessageEntity entity : userMessageMapper.selectList(userQuery)) {
            messages.add(new ContextMessage("用户", entity.getContent(), entity.getCreatedAt()));
        }

        LambdaQueryWrapper<LlmMessageEntity> llmQuery = new LambdaQueryWrapper<>();
        llmQuery.eq(LlmMessageEntity::getSessionId, sessionId)
                .ge(LlmMessageEntity::getRoundNo, startRound)
                .le(LlmMessageEntity::getRoundNo, endRound)
                .orderByAsc(LlmMessageEntity::getCreatedAt);
        for (LlmMessageEntity entity : llmMessageMapper.selectList(llmQuery)) {
            messages.add(new ContextMessage("助手", entity.getContent(), entity.getCreatedAt()));
        }

        messages.sort(Comparator.comparing(ContextMessage::createdAt));
        return messages;
    }
}
