package com.smartagent.chat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 聊天请求DTO
 * 用于接收聊天消息的请求参数
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class ChatRequestDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 代理类型
     */
    private String agentType;
}
