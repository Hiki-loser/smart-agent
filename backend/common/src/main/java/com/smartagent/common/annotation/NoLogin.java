package com.smartagent.common.annotation;

import java.lang.annotation.*;

/**
 * 无需登录注解
 * 标记接口无需登录，用于拦截器识别白名单接口
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoLogin {

}
