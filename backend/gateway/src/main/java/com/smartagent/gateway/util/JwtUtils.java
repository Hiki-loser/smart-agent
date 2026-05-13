package com.smartagent.gateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * JWT 工具类
 */
@Component
public class JwtUtils {

    /**
     * 密钥（建议长度 >= 32）
     * 从配置中获取密钥，默认值为 "smart-agent-secret-key-smart-agent-2026"
     */
    @Value("${jwt.secret:smart-agent-secret-key-smart-agent-2026}")
    private String secret;
    /**
     * 解析 JWT 获取用户 ID
     *
     * @param token JWT 令牌
     * @return 用户 ID，解析失败返回 null
     */
    public Long getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(secret.getBytes(StandardCharsets.UTF_8))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", Long.class);
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }

}
