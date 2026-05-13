package com.smartagent.memory.controller;

import com.smartagent.common.model.ApiResponse;
import com.smartagent.common.utils.UserContextUtils;
import com.smartagent.common.model.ContextMessage;
import com.smartagent.common.model.MemoryContextRequest;
import com.smartagent.common.model.MemorySnapshot;
import com.smartagent.common.model.MemorySummaryRequest;
import com.smartagent.memory.service.MemoryService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/memory")
public class MemoryController {

    @Resource
    private MemoryService memoryService;

    /**
     * 构建上下文 Prompt
     */
    @PostMapping("/context/build")
    public ApiResponse<String> buildContext(@RequestBody MemoryContextRequest request) {
        if (request.getSessionId() == null || request.getCurrentUserMessage() == null) {
            return ApiResponse.error(400, "sessionId and currentUserMessage are required");
        }

        List<ContextMessage> recentMessages = request.getRecentMessages() != null
                ? request.getRecentMessages() : List.of();

        String context = memoryService.buildContext(
                request.getSessionId(), request.getCurrentUserMessage(), recentMessages);
        return ApiResponse.success(context);
    }

    /**
     * 同步处理摘要（调用方提供消息列表）
     */
    @PostMapping("/summary")
    public ApiResponse<Void> processSummary(@RequestBody MemorySummaryRequest request) {
        if (request.getSessionId() == null || request.getMessages() == null) {
            return ApiResponse.error(400, "sessionId and messages are required");
        }

        Long userId = request.getUserId() != null ? request.getUserId() : UserContextUtils.getUserId();
        int targetRound = request.getCurrentRound() != null ? request.getCurrentRound() : 0;

        Thread.startVirtualThread(() -> {
            try {
                memoryService.processSummary(request.getSessionId(), userId, targetRound, request.getMessages());
            } catch (Exception e) {
                log.error("Failed to process summary for sessionId={}", request.getSessionId(), e);
            }
        });

        return ApiResponse.success(null);
    }

    /**
     * 获取当前记忆快照
     */
    @GetMapping("/{sessionId}")
    public ApiResponse<MemorySnapshot> getMemory(@PathVariable Long sessionId) {
        MemorySnapshot snapshot = memoryService.getCurrent(sessionId);
        return ApiResponse.success(snapshot);
    }

    /**
     * 清除会话记忆
     */
    @DeleteMapping("/{sessionId}")
    public ApiResponse<Void> clearMemory(@PathVariable Long sessionId) {
        memoryService.clearMemory(sessionId);
        return ApiResponse.success(null);
    }
}
