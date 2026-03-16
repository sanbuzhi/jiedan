package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * AI验证记录实体
 * 记录Feedback Shadow的验证历史
 */
@Entity
@Table(name = "ai_validation_record", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_task_id", columnList = "task_id"),
    @Index(name = "idx_decision", columnList = "decision")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiValidationRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目ID
     */
    @Column(name = "project_id", length = 64)
    private String projectId;

    /**
     * 任务ID
     */
    @Column(name = "task_id", length = 64)
    private String taskId;

    /**
     * 验证类型: DOCUMENT/CODE/SECURITY
     */
    @Column(name = "validation_type", length = 50)
    private String validationType;

    /**
     * 文档类型: PRD/TASK/CODE/TEST
     */
    @Column(name = "document_type", length = 50)
    private String documentType;

    /**
     * 决策: ALLOW/REPAIR/REJECT
     */
    @Column(name = "decision", length = 20)
    private String decision;

    /**
     * 质量评分(0-100)
     */
    @Column(name = "score")
    private Integer score;

    /**
     * 发现的问题（JSON格式）
     */
    @Column(name = "issues", columnDefinition = "TEXT")
    private String issues;

    /**
     * 改进建议（JSON格式）
     */
    @Column(name = "suggestions", columnDefinition = "TEXT")
    private String suggestions;

    /**
     * Token使用量
     */
    @Column(name = "token_usage")
    private Integer tokenUsage;

    /**
     * 响应时间(毫秒)
     */
    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
