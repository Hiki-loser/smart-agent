package com.smartagent.common.annotation;

import java.lang.annotation.*;

/**
 * 操作日志注解
 * 标注在Controller方法上，通过AOP自动记录接口调用日志
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Log {

    /**
     * 操作标题
     *
     * @return 操作标题
     */
    String title() default "";

    /**
     * 操作类型
     *
     * @return 操作类型
     */
    String type() default "操作";

    /**
     * 是否记录请求参数
     *
     * @return 是否记录请求参数
     */
    boolean recordParams() default true;

    /**
     * 是否记录响应结果
     *
     * @return 是否记录响应结果
     */
    boolean recordResult() default true;
}