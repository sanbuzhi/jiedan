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
 * 项目状态实体
 */
@Entity
@Table(name = "project_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatus {

    @Id
    @Column(name = "project_id", length = 64)
    private String projectId;

    /**
     * 项目状态
     */
    @Column(name = "state", length = 50)
    private String state;

    /**
     * 当前阶段
     */
    @Column(name = "current_phase", length = 200)
    private String currentPhase;

    /**
     * 当前执行的任务ID
     */
    @Column(name = "current_task_id", length = 64)
    private String currentTaskId;

    /**
     * 已完成任务ID列表（JSON格式）
     */
    @Column(name = "completed_tasks", columnDefinition = "TEXT")
    private String completedTasks;

    /**
     * 待执行任务ID列表（JSON格式）
     */
    @Column(name = "pending_tasks", columnDefinition = "TEXT")
    private String pendingTasks;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
