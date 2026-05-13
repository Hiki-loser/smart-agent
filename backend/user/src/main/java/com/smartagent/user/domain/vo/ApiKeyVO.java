package com.smartagent.user.domain.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key 视图对象
 * 用于返回给前端的API Key信息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class ApiKeyVO {

    /**
     * API Key ID
     */
    private Long id;

    /**
     * API Key值
     */
    private String keyValue;

    /**
     * Key备注名
     */
    private String name;

    /**
     * 状态：1=有效，0=已吊销
     */
    private Integer status;

    /**
     * 过期时间，NULL=永不过期
     */
    private LocalDateTime expireAt;

    /**
     * 最后使用时间
     */
    private LocalDateTime lastUsedAt;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}