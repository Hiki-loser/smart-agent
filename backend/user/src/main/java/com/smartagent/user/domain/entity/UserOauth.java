package com.smartagent.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 第三方登录实体
 * 对应数据库 user_oauth 表
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
@TableName("user_oauth")
public class UserOauth implements Serializable {

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
     * 第三方类型（github/wechat/google）
     */
    @TableField("oauth_type")
    private String oauthType;

    /**
     * 第三方唯一ID
     */
    @TableField("oauth_id")
    private String oauthId;

    /**
     * 访问令牌
     */
    @TableField("access_token")
    private String accessToken;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
