package com.smartagent.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 登录日志实体
 * 对应数据库 user_login_log 表
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
@TableName("user_login_log")
public class UserLoginLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 用户名
     */
    @TableField("username")
    private String username;

    /**
     * IP地址
     */
    @TableField("ip")
    private String ip;

    /**
     * 设备信息
     */
    @TableField("device")
    private String device;

    /**
     * 状态（1成功 0失败）
     */
    @TableField("status")
    private Integer status;

    /**
     * 消息
     */
    @TableField("message")
    private String message;

    /**
     * 登录时间
     */
    @TableField(value = "login_time", fill = FieldFill.INSERT)
    private LocalDateTime loginTime;
}
