package com.smartagent.common.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * JSON 工具类
 * 用于 JSON 序列化和反序列化
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
public class JsonUtils {

    /**
     * ObjectMapper 实例
     */
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /**
     * 对象转 JSON 字符串
     *
     * @param obj 对象
     * @return JSON 字符串
     */
    public static String toJson(Object obj) {
        try {
            return OBJECT_MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("Object to JSON error: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * JSON 字符串转对象
     *
     * @param json JSON 字符串
     * @param clazz 目标类
     * @param <T> 目标类型
     * @return 目标对象
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            log.error("JSON to Object error: {}", e.getMessage(), e);
            return null;
        }
    }
}
