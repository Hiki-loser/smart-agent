package com.smartagent.gateway.constant;

import org.springframework.beans.factory.annotation.Value;

/**
 * 网关常量类
 */
public class GatewayConstant {

    /**
     * Token 头信息
     */
    public static final String TOKEN_HEADER = "Authorization";

    /**
     * Token 前缀
     */
    public static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 追踪 ID 头信息
     */
    public static final String TRACE_ID = "X-Trace-Id";

    /**
     * 用户 ID 头信息
     */
    public static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 白名单路径
     */
    public static final String[] WHITE_LIST = {
            "/api/user/login",
            "/api/user/register",
            "/api/user/refresh",
            "swagger-ui/**",
            "v3/api-docs/**",
            "doc.html"
    };

    /**
     * Redis 限流前缀
     */
    public static final String RATE_LIMIT_PREFIX = "rate_limit:";

    /**
     * 用户级限流前缀
     */
    public static final String USER_RATE_LIMIT_PREFIX = RATE_LIMIT_PREFIX + "user:";

    /**
     * IP 级限流前缀
     */
    public static final String IP_RATE_LIMIT_PREFIX = RATE_LIMIT_PREFIX + "ip:";

}
