package com.smartagent.common.handler;

import com.smartagent.common.enums.ResultCode;
import com.smartagent.common.exception.BizException;
import com.smartagent.common.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * 全局异常处理器
 * 统一处理系统中的异常
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理业务异常
     *
     * @param e 业务异常
     * @return ApiResponse
     */
    @ExceptionHandler(BizException.class)
    public ApiResponse<?> handleBizException(BizException e) {
        String traceId = UUID.randomUUID().toString();
        log.error("[BizException] traceId: {}, code: {}, message: {}", traceId, e.getCode(), e.getMessage(), e);
        return ApiResponse.error(e.getCode(), e.getMessage(), traceId);
    }

    /**
     * 处理运行时异常
     *
     * @param e 运行时异常
     * @return ApiResponse
     */
    @ExceptionHandler(RuntimeException.class)
    public ApiResponse<?> handleRuntimeException(RuntimeException e) {
        String traceId = UUID.randomUUID().toString();
        log.error("[RuntimeException] traceId: {}, message: {}", traceId, e.getMessage(), e);
        return ApiResponse.error(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage(), traceId);
    }

    /**
     * 处理所有异常
     *
     * @param e 异常
     * @return ApiResponse
     */
    @ExceptionHandler(Exception.class)
    public ApiResponse<?> handleException(Exception e) {
        String traceId = UUID.randomUUID().toString();
        log.error("[Exception] traceId: {}, message: {}", traceId, e.getMessage(), e);
        return ApiResponse.error(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage(), traceId);
    }

    /**
     * 获取当前请求路径
     *
     * @return 请求路径
     */
    private String getCurrentRequestPath() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getRequestURI();
        }
        return "unknown";
    }
}
