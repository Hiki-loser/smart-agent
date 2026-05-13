package com.smartagent.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContextUtils {

    private UserContextUtils() {
    }
    public static Long getUserId() {
        HttpServletRequest request = currentRequest();
        if (request == null) return null;
        String header = request.getHeader("X-User-Id");
        return header != null && !header.isBlank() ? Long.valueOf(header) : null;
    }

    public static String getUsername() {
        HttpServletRequest request = currentRequest();
        return request != null ? request.getHeader("X-Username") : null;
    }

    private static HttpServletRequest currentRequest() {
        ServletRequestAttributes attr =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attr != null ? attr.getRequest() : null;
    }
}
