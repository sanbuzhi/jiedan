package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 用户反馈实体
 */
@Entity
@Table(name = "user_feedback", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_task_id", columnList = "task_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFeedback {

    @Id
    @Column(name = "feedback_id", length = 64)
    private String feedbackId;

    /**
     * 项目ID
     */
    @Column(name = "project_id", length = 64, nullable = false)
    private String projectId;

    /**
     * 任务ID（可选）
     */
    @Column(name = "task_id", length = 64)
    private String taskId;

    /**
     * 反馈类型：CODE_ISSUE/DESIGN_ISSUE/FUNCTION_MISSING/OTHER
     */
    @Column(name = "feedback_type", length = 50)
    private String feedbackType;

    /**
     * 问题描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 问题文件路径列表（JSON格式）
     */
    @Column(name = "affected_files", columnDefinition = "TEXT")
    private String affectedFiles;

    /**
     * 期望的修复方式
     */
    @Column(name = "expected_fix", columnDefinition = "TEXT")
    private String expectedFix;

    /**
     * 严重程度：CRITICAL/HIGH/MEDIUM/LOW
     */
    @Column(name = "severity", length = 20)
    private String severity;

    /**
     * 反馈来源：USER/TEST/AUTO
     */
    @Column(name = "source", length = 20)
    private String source;

    /**
     * 处理状态：PENDING/PROCESSING/COMPLETED/FAILED
     */
    @Column(name = "status", length = 20)
    private String status;

    /**
     * 修复结果
     */
    @Column(name = "repair_result", columnDefinition = "TEXT")
    private String repairResult;

    /**
     * 修复后的文件路径
     */
    @Column(name = "repaired_files", columnDefinition = "TEXT")
    private String repairedFiles;

    /**
     * AI修复尝试次数
     */
    @Column(name = "repair_attempts")
    @Builder.Default
    private Integer repairAttempts = 0;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
