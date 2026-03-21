package com.tongquyouyi.utils;

import org.springframework.stereotype.Component;

/**
 * 雪花算法ID生成器
 */
@Component
public class IdWorker {
    
    /**
     * 开始时间戳（2023-01-01 00:00:00）
     */
    private static final long START_TIMESTAMP = 1672531200000L;
    
    /**
     * 机器ID所占的位数
     */
    private static final long WORKER_ID_BITS = 5L;
    
    /**
     * 数据中心ID所占的位数
     */
    private static final long DATACENTER_ID_BITS = 5L;
    
    /**
     * 支持的最大机器ID
     */
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    
    /**
     * 支持的最大数据中心ID
     */
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    
    /**
     * 序列所占的位数
     */
    private static final long SEQUENCE_BITS = 12L;
    
    /**
     * 机器ID向左移位数
     */
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    
    /**
     * 数据中心ID向左移位数
     */
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    
    /**
     * 时间戳向左移位数
     */
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    /**
     * 生成序列的掩码
     */
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    /**
     * 工作机器ID(0~31)
     */
    private long workerId;
    
    /**
     * 数据中心ID(0~31)
     */
    private long datacenterId;
    
    /**
     * 毫秒内序列(0~4095)
     */
    private long sequence = 0L;
    
    /**
     * 上次生成ID的时间戳
     */
    private long lastTimestamp = -1L;
    
    public IdWorker() {
        this(0, 0);
    }
    
    /**
     * 构造函数
     * @param workerId 工作ID (0~31)
     * @param datacenterId 数据中心ID (0~31)
     */
    public IdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }
    
    /**
     * 获得下一个ID (该方法是线程安全的)
     * @return SnowflakeId
     */
    public synchronized long nextId() {
        long timestamp = timeGen();
        
        // 如果当前时间小于上一次ID生成的时间戳，说明系统时钟回退过，抛出异常
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                String.format("Clock moved backwards. Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
        
        // 如果是同一时间生成的，则进行毫秒内序列
        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            // 毫秒内序列溢出
            if (sequence == 0) {
                // 阻塞到下一个毫秒,获得新的时间戳
                timestamp = tilNextMillis(lastTimestamp);
            }
        }
        // 时间戳改变，毫秒内序列重置
        else {
            sequence = 0L;
        }
        
        // 上次生成ID的时间戳
        lastTimestamp = timestamp;
        
        // 移位并通过或运算拼到一起组成64位的ID
        return ((timestamp - START_TIMESTAMP) << TIMESTAMP_LEFT_SHIFT)
            | (datacenterId << DATACENTER_ID_SHIFT)
            | (workerId << WORKER_ID_SHIFT)
            | sequence;
    }
    
    /**
     * 返回以毫秒为单位的当前时间
     * @return 当前时间(毫秒)
     */
    protected long timeGen() {
        return System.currentTimeMillis();
    }
    
    /**
     * 阻塞到下一个毫秒，直到获得新的时间戳
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 当前时间戳
     */
    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }
}