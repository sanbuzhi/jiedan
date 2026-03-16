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
 * 任务状态实体
 */
@Entity
@Table(name = "task_status", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_task_id", columnList = "task_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 项目ID
     */
    @Column(name = "project_id", length = 64, nullable = false)
    private String projectId;

    /**
     * 任务ID
     */
    @Column(name = "task_id", length = 64, nullable = false)
    private String taskId;

    /**
     * 任务名称
     */
    @Column(name = "task_name", length = 200)
    private String taskName;

    /**
     * 任务类型：design/frontend/backend/api/test/doc
     */
    @Column(name = "task_type", length = 50)
    private String taskType;

    /**
     * 任务状态：PENDING/IN_PROGRESS/COMPLETED/FAILED
     */
    @Column(name = "state", length = 50)
    private String state;

    /**
     * 优先级：P0/P1/P2
     */
    @Column(name = "priority", length = 10)
    private String priority;

    /**
     * 依赖任务ID列表（JSON格式）
     */
    @Column(name = "dependencies", columnDefinition = "TEXT")
    private String dependencies;

    /**
     * 生成代码的存储路径
     */
    @Column(name = "generated_code_path", length = 500)
    private String generatedCodePath;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    @Builder.Default
    private Integer retryCount = 0;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
