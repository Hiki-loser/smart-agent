package com.smartagent.chat.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 会话VO
 * 用于返回会话信息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class SessionVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 会话ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 会话标题
     */
    private String title;

    /**
     * 代理类型
     */
    private String agentType;

    /**
     * 消息数量
     */
    private Integer messageCount;

    /**
     * 会话轮数
     */
    private Integer roundCount;

    /**
     * 最后消息时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastMessageAt;

    /**
     * 是否建议新建会话
     */
    private Boolean shouldCreateNewSession;

    /**
     * 会话提醒
     */
    private String sessionHint;
}
