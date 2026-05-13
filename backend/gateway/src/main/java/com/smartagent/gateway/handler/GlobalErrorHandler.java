package com.smartagent.gateway.handler;

import com.smartagent.gateway.constant.GatewayConstant;
import com.smartagent.gateway.util.TraceIdUtil;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalErrorHandler {

    /**
     * 处理响应状态异常
     *
     * @param exchange 服务交换对象
     * @param ex       异常对象
     * @return 响应式结果
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<Void> handleResponseStatusException(ServerWebExchange exchange, ResponseStatusException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(ex.getStatusCode());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", ex.getStatusCode().value());
        errorResponse.put("message", ex.getReason());
        errorResponse.put("traceId", getTraceId(exchange));

        // 写入响应
        return response.writeWith(Mono.just(response.bufferFactory().wrap(
                toJson(errorResponse).getBytes())));
    }

    /**
     * 处理未找到异常
     *
     * @param exchange 服务交换对象
     * @param ex       异常对象
     * @return 响应式结果
     */
    @ExceptionHandler(NotFoundException.class)
    public Mono<Void> handleNotFoundException(ServerWebExchange exchange, NotFoundException ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.NOT_FOUND);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.NOT_FOUND.value());
        errorResponse.put("message", "Resource not found");
        errorResponse.put("traceId", getTraceId(exchange));

        // 写入响应
        return response.writeWith(Mono.just(response.bufferFactory().wrap(
                toJson(errorResponse).getBytes())));
    }

    /**
     * 处理系统异常
     *
     * @param exchange 服务交换对象
     * @param ex       异常对象
     * @return 响应式结果
     */
    @ExceptionHandler(Exception.class)
    public Mono<Void> handleException(ServerWebExchange exchange, Exception ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // 构建错误响应
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("code", HttpStatus.INTERNAL_SERVER_ERROR.value());
        errorResponse.put("message", "Internal server error");
        errorResponse.put("traceId", getTraceId(exchange));

        // 写入响应
        return response.writeWith(Mono.just(response.bufferFactory().wrap(
                toJson(errorResponse).getBytes())));
    }

    /**
     * 获取 traceId
     *
     * @param exchange 服务交换对象
     * @return traceId
     */
    private String getTraceId(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String traceId = request.getHeaders().getFirst(GatewayConstant.TRACE_ID);
        return traceId != null ? traceId : TraceIdUtil.generateTraceId();
    }

    /**
     * 将对象转换为 JSON 字符串
     *
     * @param object 对象
     * @return JSON 字符串
     */
    private String toJson(Object object) {
        // 简单实现，实际项目中应使用 Jackson 等序列化库
        if (object instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) object;
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(",");
                }
                sb.append("\"").append(entry.getKey()).append("\":");
                if (entry.getValue() instanceof String) {
                    sb.append("\"").append(entry.getValue()).append("\"");
                } else {
                    sb.append(entry.getValue());
                }
                first = false;
            }
            sb.append("}");
            return sb.toString();
        }
        return "{}";
    }

}
