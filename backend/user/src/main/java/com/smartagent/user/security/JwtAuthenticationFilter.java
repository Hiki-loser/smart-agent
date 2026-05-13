package com.smartagent.user.security;

import com.smartagent.common.constant.SecurityConstant;
import com.smartagent.user.util.UserContext;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            // 1. 获取 Authorization header
            String token = request.getHeader(SecurityConstant.TOKEN_HEADER);

            // 2. 去掉 Bearer 前缀
            if (token != null && token.startsWith(SecurityConstant.TOKEN_PREFIX)) {
                token = token.substring(SecurityConstant.TOKEN_PREFIX.length());
            }

            // 3. 校验 token
            if (token != null && jwtUtil.validateToken(token)) {

                Long userId = jwtUtil.getUserIdFromToken(token);

                if (userId != null &&
                        SecurityContextHolder.getContext().getAuthentication() == null) {

                    // 4. 放入 ThreadLocal（你自己项目用的）
                    UserContext.setUserId(userId);

                    // 5. 构建 Authentication
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userId,
                                    null,
                                    null // ⚠️ 这里可以扩展权限
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. 放入 SecurityContext（核心！！）
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }

        } catch (Exception e) {
            log.error("JWT 解析失败", e);
        }

        // 7. 放行请求（必须！）
        filterChain.doFilter(request, response);

        // 8. 清理 ThreadLocal（防止内存泄漏）
        UserContext.clear();
    }
}