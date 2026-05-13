package com.smartagent.chat.controller;

import com.smartagent.chat.dto.ChatRequestDTO;
import com.smartagent.chat.dto.CreateSessionDTO;
import com.smartagent.chat.service.ChatService;
import com.smartagent.chat.vo.MessageVO;
import com.smartagent.chat.vo.SessionVO;

import com.smartagent.common.enums.ResultCode;
import com.smartagent.common.model.ApiResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天控制器
 * 处理聊天相关的 HTTP 请求
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    /**
     * 创建会话
     *
     * @param dto 创建会话参数
     * @return 会话信息
     */
    @PostMapping("/sessions")
    public ApiResponse<SessionVO> createSession(@RequestBody CreateSessionDTO dto) {
        try {
            SessionVO sessionVO = chatService.createSession(dto);
            return ApiResponse.success(sessionVO);
        } catch (Exception e) {
            log.error("Failed to create session: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.SERVER_ERROR);
        }
    }

    /**
     * 发送消息（SSE 流式）
     *
     * @param dto 聊天请求参数
     * @return SseEmitter
     */
    @PostMapping("/messages")
    public SseEmitter sendMessage(@RequestBody ChatRequestDTO dto) {
        return chatService.sendMessage(dto);
    }

    /**
     * 获取会话列表
     *
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public ApiResponse<List<SessionVO>> getSessionList() {
        try {
            List<SessionVO> sessionVOs = chatService.getSessionList();
            return ApiResponse.success(sessionVOs);
        } catch (Exception e) {
            log.error("Failed to get session list: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.SERVER_ERROR);
        }
    }

    /**
     * 获取会话历史消息
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public ApiResponse<List<MessageVO>> getSessionHistory(@PathVariable("sessionId") Long sessionId) {
        try {
            List<MessageVO> messageVOs = chatService.getSessionHistory(sessionId);
            return ApiResponse.success(messageVOs);
        } catch (Exception e) {
            log.error("Failed to get session history: {}", e.getMessage(), e);
            return ApiResponse.error(ResultCode.NOT_FOUND);
        }
    }
}
