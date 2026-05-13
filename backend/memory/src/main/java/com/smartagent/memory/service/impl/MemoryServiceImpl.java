package com.smartagent.memory.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.smartagent.memory.entity.MemoryCurrentEntity;
import com.smartagent.memory.mapper.MemoryCurrentMapper;
import com.smartagent.common.model.ContextMessage;
import com.smartagent.common.model.MemorySnapshot;
import com.smartagent.memory.service.MemoryContextBuilder;
import com.smartagent.memory.service.MemoryService;
import com.smartagent.memory.service.MemorySummaryProcessor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class MemoryServiceImpl implements MemoryService {

    @Value("${memory.summary.trigger-round-threshold:8}")
    private int summaryTriggerRoundThreshold;

    @Resource
    private MemoryContextBuilder contextBuilder;

    @Resource
    private MemorySummaryProcessor summaryProcessor;

    @Resource
    private MemoryCurrentMapper memoryCurrentMapper;

    @Override
    public String buildContext(Long sessionId, String currentUserMessage, List<ContextMessage> recentMessages) {
        return contextBuilder.build(sessionId, currentUserMessage, recentMessages);
    }

    @Override
    public void triggerAsyncSummary(Long sessionId, Long userId, int currentRound) {
        int summarizedRound = summaryProcessor.getSummarizedRound(sessionId);
        int unsummarizedRounds = currentRound - summarizedRound;

        if (unsummarizedRounds < Math.max(1, summaryTriggerRoundThreshold)) {
            return;
        }

        log.info("Summary threshold reached: sessionId={}, currentRound={}, summarizedRound={}, unsummarized={}",
                sessionId, currentRound, summarizedRound, unsummarizedRounds);
    }

    @Override
    public MemorySnapshot getCurrent(Long sessionId) {
        LambdaQueryWrapper<MemoryCurrentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryCurrentEntity::getSessionId, sessionId).last("limit 1");
        MemoryCurrentEntity entity = memoryCurrentMapper.selectOne(wrapper);

        if (entity == null) {
            return null;
        }

        return MemorySnapshot.builder()
                .sessionId(entity.getSessionId())
                .userId(entity.getUserId())
                .summaryContent(entity.getSummaryContent())
                .summarizedRound(entity.getSummarizedRound())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    @Override
    public void processSummary(Long sessionId, Long userId, int targetRound, List<ContextMessage> messages) {
        summaryProcessor.process(sessionId, userId, targetRound, messages);
    }

    @Override
    public int getSummarizedRound(Long sessionId) {
        return summaryProcessor.getSummarizedRound(sessionId);
    }

    @Override
    public void clearMemory(Long sessionId) {
        LambdaQueryWrapper<MemoryCurrentEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MemoryCurrentEntity::getSessionId, sessionId);
        MemoryCurrentEntity entity = memoryCurrentMapper.selectOne(wrapper);
        if (entity != null) {
            memoryCurrentMapper.deleteById(entity.getId());
            log.info("Memory cleared for sessionId={}", sessionId);
        }
    }
}
