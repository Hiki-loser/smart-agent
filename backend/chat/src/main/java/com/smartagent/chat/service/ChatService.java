package com.smartagent.chat.service;

import com.smartagent.chat.dto.ChatRequestDTO;
import com.smartagent.chat.dto.CreateSessionDTO;
import com.smartagent.chat.vo.MessageVO;
import com.smartagent.chat.vo.SessionVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

/**
 * 聊天服务接口
 * 定义聊天相关的核心业务逻辑
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public interface ChatService {

    /**
     * 发送消息（SSE 流式）
     *
     * @param dto 聊天请求参数
     * @return SseEmitter
     */
    SseEmitter sendMessage( ChatRequestDTO dto);

    /**
     * 创建会话
     *
     * @param dto 创建会话参数
     * @return 会话信息
     */
    SessionVO createSession(CreateSessionDTO dto);

    /**
     * 获取会话历史
     *
     * @return 消息列表
     */
    List<MessageVO> getSessionHistory(Long sessionId);

    /**
     * 获取用户会话列表
     *
     * @return 会话列表
     */
    List<SessionVO> getSessionList();
}
