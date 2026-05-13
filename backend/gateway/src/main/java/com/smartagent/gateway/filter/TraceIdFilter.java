package com.smartagent.gateway.filter;

import com.smartagent.gateway.constant.GatewayConstant;
import com.smartagent.gateway.util.TraceIdUtil;
import org.slf4j.MDC;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 追踪 ID 过滤器
 */
@Component
public class TraceIdFilter implements GlobalFilter, Ordered {

    /**
     * 执行过滤器逻辑
     *
     * @param exchange 服务交换对象
     * @param chain    过滤器链
     * @return 响应式结果
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // 检查请求头是否已有 traceId
        String traceId = request.getHeaders().getFirst(GatewayConstant.TRACE_ID);
        if (traceId == null) {
            // 生成新的 traceId
            traceId = TraceIdUtil.generateTraceId();
        }

        // 将 traceId 写入请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(GatewayConstant.TRACE_ID, traceId)
                .build();

        // 将 traceId 写入 MDC（日志）
        MDC.put(GatewayConstant.TRACE_ID, traceId);

        // 继续执行过滤器链
        return chain.filter(exchange.mutate().request(modifiedRequest).build())
                // 完成后清理 MDC
                .doFinally(signalType -> MDC.remove(GatewayConstant.TRACE_ID));
    }

    /**
     * 获取过滤器优先级
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getOrder() {
        return 5;
    }

}
