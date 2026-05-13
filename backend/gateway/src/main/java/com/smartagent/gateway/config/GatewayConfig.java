package com.smartagent.gateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 网关配置类
 */
@Configuration
public class GatewayConfig {

    /**
     * 配置路由规则
     *
     * @param builder 路由构建器
     * @return 路由定位器
     */
    @Bean
    public RouteLocator routeLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // 用户服务路由
                .route("user-service", r -> r
                        .path("/api/user/**")
                        .uri("lb://smart-agent-user"))

                // 聊天服务路由
                .route("chat-service", r -> r
                        .path("/api/chat/**")
                        .uri("lb://smart-agent-chat"))

                // 核心服务路由
                .route("core-service", r -> r
                        .path("/api/core/**")
                        .uri("lb://smart-agent-core"))
                        
                // 知识库服务路由
                .route("knowledge-service", r -> r
                        .path("/api/knowledge/**")
                        .uri("lb://smart-agent-knowledge"))
                .build();
    }

}
