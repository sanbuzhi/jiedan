package com.tongquyouyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 文件配置类
 *
 * @author tongquyouyi
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "tongquyouyi.file")
public class FileProperties {

    /**
     * 本地临时上传路径
     */
    private String uploadPath;

    /**
     * 最大图片大小（字节）
     */
    private Long maxImageSize;

    /**
     * 最大Excel大小（字节）
     */
    private Long maxExcelSize;

}