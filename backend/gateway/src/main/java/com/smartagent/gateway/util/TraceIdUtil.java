package com.smartagent.gateway.util;

import java.util.UUID;

/**
 * 追踪 ID 工具类
 */
public class TraceIdUtil {

    /**
     * 生成追踪 ID
     *
     * @return 追踪 ID（UUID 去掉 -）
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

}
