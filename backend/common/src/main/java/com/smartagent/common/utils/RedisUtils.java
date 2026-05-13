package com.smartagent.common.utils;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redis工具类
 * 对Redisson客户端的简单封装，提供常用的缓存操作方法
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Component
@ConditionalOnBean(RedissonClient.class)
public class RedisUtils {

    /**
     * Redisson客户端
     */
    private final RedissonClient redissonClient;

    /**
     * 构造方法
     *
     * @param redissonClient Redisson客户端
     */
    public RedisUtils(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    /**
     * 设置缓存
     *
     * @param key     键
     * @param value   值
     * @param ttl     过期时间
     * @param unit    时间单位
     * @param <T>     值类型
     */
    public <T> void set(String key, T value, long ttl, TimeUnit unit) {
        redissonClient.getBucket(key).set(value, ttl, unit);
    }

    /**
     * 获取缓存
     *
     * @param key 键
     * @param <T> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) redissonClient.getBucket(key).get();
    }

    /**
     * 删除缓存
     *
     * @param key 键
     * @return 是否删除成功
     */
    public boolean del(String key) {
        return redissonClient.getBucket(key).delete();
    }

    /**
     * 设置缓存（如果不存在）
     * 分布式锁常用
     *
     * @param key     键
     * @param value   值
     * @param ttl     过期时间
     * @param <T>     值类型
     * @return 是否设置成功
     */
    public <T> boolean setIfAbsent(String key, T value, long ttl) {
        return redissonClient.getBucket(key).trySet(value, ttl, TimeUnit.SECONDS);
    }

    /**
     * 原子递增
     * 限流用
     *
     * @param key 键
     * @return 递增后的值
     */
    public Long incr(String key) {
        return redissonClient.getAtomicLong(key).incrementAndGet();
    }

    /**
     * 原子递减
     *
     * @param key 键
     * @return 递减后的值
     */
    public Long decr(String key) {
        return redissonClient.getAtomicLong(key).decrementAndGet();
    }

    /**
     * 获取分布式锁
     *
     * @param lockKey 锁键
     * @param leaseTime 租约时间
     * @param unit 时间单位
     * @return 锁对象
     */
    public RLock getLock(String lockKey, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        lock.lock(leaseTime, unit);
        return lock;
    }

    /**
     * 尝试获取分布式锁
     *
     * @param lockKey 锁键
     * @param waitTime 等待时间
     * @param leaseTime 租约时间
     * @param unit 时间单位
     * @return 是否获取成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime, TimeUnit unit) {
        RLock lock = redissonClient.getLock(lockKey);
        try {
            return lock.tryLock(waitTime, leaseTime, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lockKey 锁键
     */
    public void unlock(String lockKey) {
        RLock lock = redissonClient.getLock(lockKey);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 释放分布式锁
     *
     * @param lock 锁对象
     */
    public void unlock(RLock lock) {
        if (lock != null && lock.isHeldByCurrentThread()) {
            lock.unlock();
        }
    }

    /**
     * 设置列表元素
     *
     * @param key 键
     * @param value 值
     * @param <T> 值类型
     */
    public <T> void lpush(String key, T value) {
        redissonClient.getList(key).add(0, value);
    }

    /**
     * 获取列表范围
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     * @param <T> 值类型
     * @return 列表元素
     */
    @SuppressWarnings("unchecked")
    public <T> java.util.List<T> lrange(String key, int start, int end) {
        return (java.util.List<T>) redissonClient.getList(key).subList(start, end + 1);
    }

    /**
     * 获取列表长度
     *
     * @param key 键
     * @return 列表长度
     */
    public long llen(String key) {
        return redissonClient.getList(key).size();
    }

    /**
     * 修剪列表
     *
     * @param key 键
     * @param start 开始索引
     * @param end 结束索引
     */
    public void ltrim(String key, int start, int end) {
        redissonClient.getList(key).trim(start, end);
    }

    /**
     * 设置哈希表字段
     *
     * @param key 键
     * @param field 字段
     * @param value 值
     * @param <T> 值类型
     */
    public <T> void hset(String key, String field, T value) {
        redissonClient.getMap(key).put(field, value);
    }

    /**
     * 获取哈希表字段
     *
     * @param key 键
     * @param field 字段
     * @param <T> 值类型
     * @return 值
     */
    @SuppressWarnings("unchecked")
    public <T> T hget(String key, String field) {
        return (T) redissonClient.getMap(key).get(field);
    }

    /**
     * 删除哈希表字段
     *
     * @param key 键
     * @param field 字段
     * @return 是否删除成功
     */
    public boolean hdel(String key, String field) {
        return redissonClient.getMap(key).remove(field) != null;
    }

    /**
     * 设置缓存过期时间
     *
     * @param key 键
     * @param ttl 过期时间
     * @param unit 时间单位
     * @return 是否设置成功
     */
    public boolean expire(String key, long ttl, TimeUnit unit) {
        return redissonClient.getBucket(key).expire(ttl, unit);
    }

    /**
     * 获取键的剩余过期时间
     *
     * @param key 键
     * @return 剩余过期时间（毫秒）
     */
    public long ttl(String key) {
        return redissonClient.getBucket(key).remainTimeToLive();
    }

    /**
     * 检查键是否存在
     *
     * @param key 键
     * @return 是否存在
     */
    public boolean exists(String key) {
        return redissonClient.getBucket(key).isExists();
    }
}