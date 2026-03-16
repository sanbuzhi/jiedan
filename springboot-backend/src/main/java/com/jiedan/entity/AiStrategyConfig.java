package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI策略配置实体类
 */
@Entity
@Table(name = "ai_strategy_config")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiStrategyConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 接口代码，唯一标识
     */
    @Column(name = "api_code", nullable = false, unique = true, length = 50)
    private String apiCode;

    /**
     * 接口名称
     */
    @Column(name = "api_name", nullable = false, length = 100)
    private String apiName;

    /**
     * AI提供商
     */
    @Column(name = "provider", nullable = false, length = 50)
    private String provider;

    /**
     * AI模型
     */
    @Column(name = "model", nullable = false, length = 100)
    private String model;

    /**
     * 温度参数
     */
    @Column(name = "temperature", nullable = false)
    private Double temperature;

    /**
     * 最大token数
     */
    @Column(name = "max_tokens", nullable = false)
    private Integer maxTokens;

    /**
     * 是否启用
     */
    @Column(name = "enabled", nullable = false)
    private Boolean enabled;

    /**
     * 接口描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 图标名称
     */
    @Column(name = "icon", length = 50)
    private String icon;

    /**
     * 排序顺序
     */
    @Column(name = "sort_order")
    private Integer sortOrder;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
