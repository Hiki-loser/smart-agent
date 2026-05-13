package com.smartagent.user.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * API Key 实体类
 * 用于存储用户的API Key信息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
@TableName("api_key")
public class ApiKey {

    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * API Key值（UUID格式）
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

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}