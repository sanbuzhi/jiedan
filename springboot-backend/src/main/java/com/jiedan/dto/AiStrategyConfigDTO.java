package com.jiedan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * AI策略配置DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiStrategyConfigDTO {

    private Long id;

    /**
     * 接口代码，唯一标识
     */
    private String apiCode;

    /**
     * 接口名称
     */
    private String apiName;

    /**
     * AI提供商
     */
    private String provider;

    /**
     * AI模型
     */
    private String model;

    /**
     * 温度参数
     */
    private Double temperature;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 接口描述
     */
    private String description;

    /**
     * 图标名称
     */
    private String icon;

    /**
     * 排序顺序
     */
    private Integer sortOrder;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}
