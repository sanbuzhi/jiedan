package com.jiedan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 火山引擎AI配置属性类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai.huoshan")
public class HuoshanAiProperties {

    /**
     * API Key
     */
    private String apiKey;

    /**
     * 基础URL
     */
    private String baseUrl = "https://ark.cn-beijing.volces.com/api/coding/v3";

    /**
     * 默认模型
     */
    private String defaultModel = "doubao-seed-2.0-code";

    /**
     * 超时时间（毫秒）
     */
    private int timeout = 30000;
}
