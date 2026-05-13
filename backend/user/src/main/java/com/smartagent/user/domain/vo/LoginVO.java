package com.smartagent.user.domain.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录VO
 * 用于返回登录成功后的令牌信息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class LoginVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 访问令牌
     */
    private String accessToken;

    /**
     * 刷新令牌
     */
    private String refreshToken;

    /**
     * 令牌类型
     */
    private String tokenType;

    /**
     * 过期时间（秒）
     */
    private long expiresIn;

    /**
     * 用户信息
     */
    private UserVO user;
}
