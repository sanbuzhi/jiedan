package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云OSS配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.ali.oss")
public class AliOssProperties {

    /**
     * OSS端点
     */
    private String endpoint;

    /**
     * 访问密钥ID
     */
    private String accessKeyId;

    /**
     * 访问密钥密钥
     */
    private String accessKeySecret;

    /**
     * 存储桶名称
     */
    private String bucketName;

}