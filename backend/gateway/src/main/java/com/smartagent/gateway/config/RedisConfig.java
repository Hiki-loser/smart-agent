package com.smartagent.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 */
@Configuration
public class RedisConfig {

    /**
     * 配置响应式 Redis 模板
     *
     * @param factory Redis 连接工厂
     * @return 响应式 Redis 模板
     */

    @Primary
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate (ReactiveRedisConnectionFactory factory) {
        // 字符串序列化器
        StringRedisSerializer keySerializer = new StringRedisSerializer();
        // JSON 序列化器
        Jackson2JsonRedisSerializer<String> valueSerializer = new Jackson2JsonRedisSerializer<>(String.class);

        // 配置序列化上下文
        RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder = 
                RedisSerializationContext.newSerializationContext(keySerializer);
        RedisSerializationContext<String, String> context = builder.value(valueSerializer).build();

        // 创建并返回响应式 Redis 模板
        return new ReactiveRedisTemplate<>(factory, context);
    }

}
