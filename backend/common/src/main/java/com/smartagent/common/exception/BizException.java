package com.smartagent.common.exception;

import com.smartagent.common.enums.ResultCode;

/**
 * 业务异常
 * 继承 RuntimeException，用于业务逻辑异常
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public class BizException extends RuntimeException {

    /**
     * 错误码
     */
    private final int code;

    /**
     * 构造方法
     *
     * @param code    错误码
     * @param message 错误消息
     */
    public BizException(int code, String message) {
        super(message);
        this.code = code;
    }

    /**
     * 构造方法
     *
     * @param resultCode 结果码枚举
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 构造方法
     *
     * @param resultCode 结果码枚举
     * @param message    错误消息
     */
    public BizException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }
}
