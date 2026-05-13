package com.smartagent.common.enums;

import lombok.Getter;

/**
 * 统一系统返回码
 * 定义系统中所有的返回码和对应消息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Getter
public enum ResultCode {

    /**
     * 成功
     */
    SUCCESS(200, "success"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未登录
     */
    UNAUTHORIZED(401, "未登录"),

    /**
     * 权限不足
     */
    FORBIDDEN(403, "权限不足"),

    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),

    /**
     * 请求方法不支持
     */
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),

    /**
     * 服务器异常
     */
    SERVER_ERROR(500, "服务器异常");

    /**
     * 返回码
     * -- GETTER --
     *  获取返回码
     *
     * @return 返回码

     */
    private final int code;

    /**
     * 返回消息
     * -- GETTER --
     *  获取返回消息
     *
     * @return 返回消息

     */
    private final String message;

    /**
     * 构造方法
     *
     * @param code    返回码
     * @param message 返回消息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

}
