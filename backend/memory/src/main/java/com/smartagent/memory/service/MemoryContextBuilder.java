package com.smartagent.memory.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.memory.entity.MemoryCurrentEntity;
import com.smartagent.memory.mapper.MemoryCurrentMapper;
import com.smartagent.common.model.ContextMessage;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MemoryContextBuilder {

    @Value("${memory.context.recent-message-limit:12}")
    private int recentMessageLimit;

    @Resource
    private MemoryCurrentMapper memoryCurrentMapper;

    public String build(Long sessionId, String currentUserMessage, List<ContextMessage> recentMessages) {
        MemoryCurrentEntity currentMemory = getCurrentMemory(sessionId);

        StringBuilder builder = new StringBuilder();

        if (currentMemory != null
                && currentMemory.getSummaryContent() != null
                && !currentMemory.getSummaryContent().isBlank()) {
            builder.append("[历史总结]\n")
                    .append(currentMemory.getSummaryContent().trim())
                    .append("\n\n");
        }

        List<ContextMessage> limitedMessages = recentMessages;
        if (recentMessages != null && recentMessages.size() > recentMessageLimit) {
            limitedMessages = recentMessages.subList(
                    recentMessages.size() - recentMessageLimit, recentMessages.size());
        }

        if (limitedMessages != null && !limitedMessages.isEmpty()) {
            builder.append("[最近对话]\n");
            for (ContextMessage message : limitedMessages) {
                builder.append(message.role())
                        .append(": ")
                        .append(message.content())
                        .append("\n");
            }
            builder.append("\n");
        }

        builder.append("[当前用户问题]\n").append(currentUserMessage);
        return builder.toString();
    }

    MemoryCurrentEntity getCurrentMemory(Long sessionId) {
        LambdaQueryWrapper<MemoryCurrentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryCurrentEntity::getSessionId, sessionId).last("limit 1");
        return memoryCurrentMapper.selectOne(wrapper);
    }
}
