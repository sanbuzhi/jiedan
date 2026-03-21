package com.tongquyouyi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池配置类
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    
    /**
     * 核心线程数
     */
    private static final int CORE_POOL_SIZE = 10;
    
    /**
     * 最大线程数
     */
    private static final int MAX_POOL_SIZE = 50;
    
    /**
     * 队列容量
     */
    private static final int QUEUE_CAPACITY = 100;
    
    /**
     * 线程存活时间（秒）
     */
    private static final int KEEP_ALIVE_SECONDS = 60;
    
    /**
     * 线程名称前缀
     */
    private static final String THREAD_NAME_PREFIX = "tqy-exec-";
    
    /**
     * 通用线程池
     */
    @Bean("taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * 邮件发送线程池
     */
    @Bean("emailExecutor")
    public ThreadPoolTaskExecutor emailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("tqy-email-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
    
    /**
     * 短信发送线程池
     */
    @Bean("smsExecutor")
    public ThreadPoolTaskExecutor smsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(50);
        executor.setKeepAliveSeconds(30);
        executor.setThreadNamePrefix("tqy-sms-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }
}