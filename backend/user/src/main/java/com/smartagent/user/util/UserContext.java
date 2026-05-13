package com.smartagent.user.util;

/**
 * 用户上下文工具类
 * 用于存储当前登录用户的信息
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public class UserContext {

    /**
     * 用户ID ThreadLocal
     */
    private static final ThreadLocal<Long> userIdThreadLocal = new ThreadLocal<>();

    /**
     * 设置用户ID
     *
     * @param userId 用户ID
     */
    public static void setUserId(Long userId) {
        userIdThreadLocal.set(userId);
    }

    /**
     * 获取用户ID
     *
     * @return 用户ID
     */
    public static Long getUserId() {
        return userIdThreadLocal.get();
    }

    /**
     * 清除上下文
     */
    public static void clear() {
        userIdThreadLocal.remove();
    }
}
