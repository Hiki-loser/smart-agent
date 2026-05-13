package com.smartagent.gateway.filter;

import com.smartagent.gateway.constant.GatewayConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * 限流过滤器
 */
@Component
public class RateLimitFilter implements GlobalFilter, Ordered {

    @Autowired
    private ReactiveRedisTemplate<String, String> redisTemplate;

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

        // 获取用户 ID 或 IP
        String userId = request.getHeaders().getFirst(GatewayConstant.USER_ID_HEADER);
        String key;

        if (userId != null) {
            // 使用用户 ID 进行限流
            key = GatewayConstant.USER_RATE_LIMIT_PREFIX + userId;
        } else {
            // 使用 IP 进行限流
            String ip = request.getRemoteAddress().getAddress().getHostAddress();
            key = GatewayConstant.IP_RATE_LIMIT_PREFIX + ip;
        }

        // 限流逻辑：1分钟最多100个请求
        return redisTemplate.opsForValue().increment(key)
                .flatMap(count -> {
                    // 设置过期时间（1分钟）
                    if (count == 1) {
                        return redisTemplate.expire(key, Duration.ofMinutes(1))
                                .then(Mono.just(count));
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    if (count > 100) {
                        // 限流
                        ServerHttpResponse response = exchange.getResponse();
                        response.setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return response.setComplete();
                    }
                    // 继续执行过滤器链
                    return chain.filter(exchange);
                });
    }

    /**
     * 获取过滤器优先级
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getOrder() {
        return 12;
    }

}
