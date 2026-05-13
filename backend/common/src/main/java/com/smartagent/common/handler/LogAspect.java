package com.smartagent.common.handler;

import com.smartagent.common.annotation.Log;
import com.smartagent.common.utils.JsonUtils;
import com.smartagent.common.utils.UserContextUtils;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * 操作日志切面
 * 处理@Log注解，记录接口调用日志
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    /**
     * 切入点
     */
    @Pointcut("@annotation(com.smartagent.common.annotation.Log)")
    public void logPointcut() {
    }

    /**
     * 环绕通知
     *
     * @param joinPoint 连接点
     * @return 方法返回值
     * @throws Throwable 异常
     */
    @Around("logPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        // 开始时间
        LocalDateTime startTime = LocalDateTime.now();
        // 生成日志ID
        String logId = UUID.randomUUID().toString();
        // 获取请求信息
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        // 获取方法签名
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        // 获取@Log注解
        Log logAnnotation = method.getAnnotation(Log.class);
        
        // 构建日志信息
        StringBuilder logInfo = new StringBuilder();
        logInfo.append("[LogAspect] logId: " + logId);
        logInfo.append(" | title: " + logAnnotation.title());
        logInfo.append(" | type: " + logAnnotation.type());
        logInfo.append(" | uri: " + request.getRequestURI());
        logInfo.append(" | method: " + request.getMethod());
        logInfo.append(" | ip: " + request.getRemoteAddr());
        logInfo.append(" | user: " + UserContextUtils.getUsername());
        
        // 记录请求参数
        if (logAnnotation.recordParams()) {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                try {
                    String params = JsonUtils.toJson(args);
                    logInfo.append(" | params: " + params);
                } catch (Exception e) {
                    log.warn("Failed to serialize params", e);
                }
            }
        }
        
        log.info(logInfo.toString());
        
        Object result = null;
        try {
            // 执行方法
            result = joinPoint.proceed();
            // 记录响应结果
            if (logAnnotation.recordResult()) {
                try {
                    String resultStr = JsonUtils.toJson(result);
                    log.info("[LogAspect] logId: {} | result: {}", logId, resultStr);
                } catch (Exception e) {
                    log.warn("Failed to serialize result", e);
                }
            }
        } catch (Throwable e) {
            // 记录异常
            log.error("[LogAspect] logId: {} | error: {}", logId, e.getMessage(), e);
            throw e;
        } finally {
            // 计算耗时
            long cost = ChronoUnit.MILLIS.between(startTime, LocalDateTime.now());
            log.info("[LogAspect] logId: {} | cost: {}ms", logId, cost);
        }
        
        return result;
    }
}