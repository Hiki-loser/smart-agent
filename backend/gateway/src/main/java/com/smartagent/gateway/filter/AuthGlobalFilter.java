package com.smartagent.gateway.filter;

import com.smartagent.gateway.constant.GatewayConstant;
import com.smartagent.gateway.util.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证全局过滤器
 */
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtils jwtUtils;

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
        String path = request.getPath().value();

        // 检查是否在白名单中
        if (isInWhiteList(path)) {
            return chain.filter(exchange);
        }

        // 获取 Authorization 头
        String authHeader = request.getHeaders().getFirst(GatewayConstant.TOKEN_HEADER);
        if (authHeader == null || !authHeader.startsWith(GatewayConstant.TOKEN_PREFIX)) {
            return unauthorized(exchange);
        }

        // 提取 token
        String token = authHeader.substring(GatewayConstant.TOKEN_PREFIX.length());
        Long userId = jwtUtils.getUserId(token);

        // 校验 token
        if (userId == null) {
            return unauthorized(exchange);
        }

        // 将用户 ID 写入请求头
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(GatewayConstant.USER_ID_HEADER, userId.toString())
                .build();

        // 继续执行过滤器链
        return chain.filter(exchange.mutate().request(modifiedRequest).build());
    }

    /**
     * 检查路径是否在白名单中
     *
     * @param path 请求路径
     * @return 是否在白名单中
     */

    private boolean isInWhiteList(String path) {
        for (String whitePath : GatewayConstant.WHITE_LIST) {
            if (path.startsWith(whitePath)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回未授权响应
     *
     * @param exchange 服务交换对象
     * @return 响应式结果
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }

    /**
     * 获取过滤器优先级
     *
     * @return 优先级（数字越小优先级越高）
     */
    @Override
    public int getOrder() {
        return 10;
    }

}
