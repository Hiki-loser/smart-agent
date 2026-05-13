package com.smartagent.chat.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 创建会话DTO
 * 用于接收创建会话的请求参数
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class CreateSessionDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 代理类型
     */
    private String agentType;

    /**
     * 知识库ID
     */
    private Long knowledgeBaseId;
}
