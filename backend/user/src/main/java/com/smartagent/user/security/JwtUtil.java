package com.smartagent.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT 工具类
 * 用于生成、解析和校验 JWT 令牌
 */
@Component
public class JwtUtil {

    /**
     * 密钥（建议长度 >= 32）
     */
    @Value("${jwt.secret:smart-agent-secret-key-smart-agent-2026}")
    private String secret;

    /**
     * 访问令牌过期时间（小时）
     */
    @Value("${jwt.access-token-expire:1}")
    private long accessTokenExpire;

    /**
     * 刷新令牌过期时间（天）
     */
    @Value("${jwt.refresh-token-expire:7}")
    private long refreshTokenExpire;

    /**
     * 获取签名 key（核心改动）
     */
    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成访问令牌
     */
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "access");

        return generateToken(claims, accessTokenExpire * 3600 * 1000);
    }

    /**
     * 生成刷新令牌
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");

        return generateToken(claims, refreshTokenExpire * 24 * 3600 * 1000);
    }

    /**
     * 生成令牌（核心改动）
     */
    private String generateToken(Map<String, Object> claims, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .setIssuedAt(new Date())
                .signWith(getKey(), SignatureAlgorithm.HS256) // ✅ 新写法
                .compact();
    }

    /**
     * 解析令牌（核心改动）
     */
    public Claims parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey()) // ✅ 新写法
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * 获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = parseToken(token);
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 token
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}