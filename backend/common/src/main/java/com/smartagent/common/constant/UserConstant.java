package com.smartagent.common.constant;

/**
 * 用户常量
 * 定义用户相关的常量
 *
 * @author SmartAgent
 * @since 1.0.0
 */
public class UserConstant {

    /**
     * 用户Token Redis键前缀
     */
    public static final String USER_TOKEN_KEY = "user:token:%s:%s";

    /**
     * 用户Refresh Token Redis键前缀
     */
    public static final String USER_REFRESH_KEY = "user:refresh:%s:%s";

    public static final String TOKEN_HEADER = "Authorization";

    public  static final String TOKEN_PREFIX = "Bearer ";

    public static final String DEVICE_TYPE_HEADER = "Device-Type";

    /**
     * 访问令牌过期时间（小时）
     */
    public static final int ACCESS_TOKEN_EXPIRE = 1;

    /**
     * 刷新令牌过期时间（天）
     */
    public static final int REFRESH_TOKEN_EXPIRE = 7;

    /**
     * 设备类型 - Web
     */
    public static final String DEVICE_TYPE_WEB = "web";

    /**
     * 设备类型 - 移动端
     */
    public static final String DEVICE_TYPE_MOBILE = "mobile";

    /**
     * 用户状态 - 正常
     */
    public static final int USER_STATUS_NORMAL = 1;

    /**
     * 用户状态 - 禁用
     */
    public static final int USER_STATUS_DISABLED = 0;
}
