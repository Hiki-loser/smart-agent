package com.smartagent.common.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.smartagent.common.enums.ResultCode;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 统一接口返回结构
 * 封装所有接口的返回数据
 *
 * @param <T> 数据类型
 * @author SmartAgent
 * @since 1.0.0
 */
@Data
public class ApiResponse<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 返回码
     */
    private int code;

    /**
     * 返回消息
     */
    private String message;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 链路追踪 ID
     */
    private String traceId;

    /**
     * 构造方法
     */
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
        this.traceId = UUID.randomUUID().toString();
    }

    /**
     * 构造方法
     *
     * @param code    返回码
     * @param message 返回消息
     * @param data    返回数据
     */
    private ApiResponse(int code, String message, T data) {
        this();
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造方法
     *
     * @param code     返回码
     * @param message  返回消息
     * @param data     返回数据
     * @param traceId  链路追踪 ID
     */
    private ApiResponse(int code, String message, T data, String traceId) {
        this(code, message, data);
        this.traceId = traceId;
    }

    /**
     * 成功响应
     *
     * @param data 返回数据
     * @param <T>  数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应
     *
     * @param <T> 数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success() {
        return success(null);
    }

    /**
     * 错误响应
     *
     * @param code    错误码
     * @param message 错误消息
     * @param <T>     数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    /**
     * 错误响应
     *
     * @param code     错误码
     * @param message  错误消息
     * @param traceId  链路追踪 ID
     * @param <T>      数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(int code, String message, String traceId) {
        return new ApiResponse<>(code, message, null, traceId);
    }

    /**
     * 错误响应
     *
     * @param resultCode 结果码枚举
     * @param <T>        数据类型
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(ResultCode resultCode) {
        return error(resultCode.getCode(), resultCode.getMessage());
    }
}

