package com.smartagent.common.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 错误响应结构
 * 封装错误响应的详细信息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class ErrorResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 错误码
     */
    private int code;

    /**
     * 错误消息
     */
    private String message;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 时间戳
     */
    private LocalDateTime timestamp;

    /**
     * 构造方法
     *
     * @param code      错误码
     * @param message   错误消息
     * @param path      请求路径
     * @param timestamp 时间戳
     */
    public ErrorResponse(int code, String message, String path, LocalDateTime timestamp) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    /**
     * 构造方法
     */
    public ErrorResponse() {
    }
}
