package com.smartagent.user.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户更新DTO
 * 用于接收用户更新请求参数
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class UserUpdateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像URL
     */
    private String avatar;
}
