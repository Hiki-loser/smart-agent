package com.smartagent.memory.service;

import com.smartagent.common.model.ContextMessage;
import com.smartagent.common.model.MemorySnapshot;

import java.util.List;

public interface MemoryService {

    /**
     * 构建上下文消息（用于发给 AI 的 prompt）
     *
     * @param sessionId          会话 ID
     * @param currentUserMessage 当前用户消息
     * @param recentMessages     最近的对话消息（由调用方提供）
     * @return 完整的上下文 prompt 文本
     */
    String buildContext(Long sessionId, String currentUserMessage, List<ContextMessage> recentMessages);

    /**
     * 触发异步摘要（通过 RocketMQ）
     *
     * @param sessionId   会话 ID
     * @param userId      用户 ID
     * @param currentRound 当前轮次
     */
    void triggerAsyncSummary(Long sessionId, Long userId, int currentRound);

    /**
     * 获取当前记忆快照
     *
     * @param sessionId 会话 ID
     * @return 记忆快照，不存在时返回 null
     */
    MemorySnapshot getCurrent(Long sessionId);

    /**
     * 处理摘要（由 MQ 消费者或 API 调用）
     *
     * @param sessionId   会话 ID
     * @param userId      用户 ID
     * @param targetRound 目标轮次
     * @param messages    待摘要的消息列表
     */
    void processSummary(Long sessionId, Long userId, int targetRound, List<ContextMessage> messages);

    /**
     * 获取已摘要的轮次
     *
     * @param sessionId 会话 ID
     * @return 已摘要轮次，无记录时返回 0
     */
    int getSummarizedRound(Long sessionId);

    /**
     * 清除会话记忆
     *
     * @param sessionId 会话 ID
     */
    void clearMemory(Long sessionId);
}
