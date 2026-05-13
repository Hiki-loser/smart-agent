package com.smartagent.common.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * 雪花算法 ID 生成器
 * 生成全局唯一的 Long 类型 ID，适合分布式环境下的主键生成
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
public class SnowflakeIdUtils {

    /**
     * 机器ID（0-31）
     */
    private final long workerId;

    /**
     * 数据中心ID（0-31）
     */
    private final long datacenterId;

    /**
     * 序列号（0-4095）
     */
    private long sequence = 0L;

    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;

    /**
     * 机器ID位数
     */
    private static final long WORKER_ID_BITS = 5L;

    /**
     * 数据中心ID位数
     */
    private static final long DATACENTER_ID_BITS = 5L;

    /**
     * 序列号位数
     */
    private static final long SEQUENCE_BITS = 12L;

    /**
     * 机器ID最大值
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);

    /**
     * 数据中心ID最大值
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);

    /**
     * 序列号最大值
     */
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BITS);

    /**
     * 时间戳左移位数
     */
    private static final long TIMESTAMP_LEFT_SHIFT = WORKER_ID_BITS + DATACENTER_ID_BITS + SEQUENCE_BITS;

    /**
     * 数据中心ID左移位数
     */
    private static final long DATACENTER_ID_LEFT_SHIFT = WORKER_ID_BITS + SEQUENCE_BITS;

    /**
     * 机器ID左移位数
     */
    private static final long WORKER_ID_LEFT_SHIFT = SEQUENCE_BITS;

    /**
     * 纪元时间戳（2024-01-01 00:00:00）
     */
    private static final long EPOCH = 1704067200000L;

    /**
     * 构造方法
     *
     * @param workerId     机器ID
     * @param datacenterId 数据中心ID
     */
    public SnowflakeIdUtils(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("workerId can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenterId can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        log.info("SnowflakeIdUtils initialized with workerId: {}, datacenterId: {}", workerId, datacenterId);
    }

    /**
     * 生成下一个唯一ID
     *
     * @return 唯一ID
     */
    public synchronized long nextId() {
        long timestamp = System.currentTimeMillis();

        // 时钟回拨处理
        if (timestamp < lastTimestamp) {
            log.error("Clock moved backwards. Refusing to generate id for {} milliseconds", lastTimestamp - timestamp);
            throw new RuntimeException(String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }

        // 同一毫秒内，序列号递增
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            // 序列号溢出，等待下一毫秒
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            // 不同毫秒，序列号重置
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        // 组合ID：时间戳 + 数据中心ID + 机器ID + 序列号
        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
               (datacenterId << DATACENTER_ID_LEFT_SHIFT) |
               (workerId << WORKER_ID_LEFT_SHIFT) |
               sequence;
    }

    /**
     * 等待到下一毫秒
     *
     * @param lastTimestamp 上次时间戳
     * @return 新的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis();
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis();
        }
        return timestamp;
    }

    /**
     * 单例实例
     */
    private static class SingletonHolder {
        private static final SnowflakeIdUtils INSTANCE = new SnowflakeIdUtils(1, 1);
    }

    /**
     * 获取单例实例
     *
     * @return SnowflakeIdUtils实例
     */
    public static SnowflakeIdUtils getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * 生成ID的静态方法
     *
     * @return 唯一ID
     */
    public static long generateId() {
        return getInstance().nextId();
    }
}