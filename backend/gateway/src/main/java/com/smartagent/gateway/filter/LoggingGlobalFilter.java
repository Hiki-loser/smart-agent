package com.smartagent.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 日志全局过滤器
 */
@Component
public class LoggingGlobalFilter implements GlobalFilter, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(LoggingGlobalFilter.class);

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
        long startTime = System.currentTimeMillis();

        // 记录请求信息
        logger.info("Request: {} {}", request.getMethod(), request.getPath());

        // 继续执行过滤器链
        return chain.filter(exchange)
                // 完成后记录响应时间
                .doFinally(signalType -> {
                    long endTime = System.currentTimeMillis();
                    long responseTime = endTime - startTime;
                    logger.info("Response: {} {} - {}ms", 
                            request.getMethod(), 
                            request.getPath(), 
                            responseTime);
                });
    }

    /**
     * 获取过滤器优先级
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getOrder() {
        return 15;
    }

}
