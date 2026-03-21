package com.tongquyouyi.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * 应用启动监听器
 */
@Slf4j
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {
    
    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {
        log.info("==============================================");
        log.info("应用启动成功！");
        log.info("应用名称：{}", event.getApplicationContext().getApplicationName());
        log.info("应用版本：{}", event.getApplicationContext().getEnvironment().getProperty("spring.application.version", "1.0.0"));
        log.info("当前环境：{}", event.getApplicationContext().getEnvironment().getActiveProfiles());
        log.info("==============================================");
    }
}