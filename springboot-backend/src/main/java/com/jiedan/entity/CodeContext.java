package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 代码上下文实体（代码摘要）
 */
@Entity
@Table(name = "code_context", indexes = {
    @Index(name = "idx_project_id", columnList = "project_id"),
    @Index(name = "idx_task_id", columnList = "task_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeContext {

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
     * 类名
     */
    @Column(name = "class_name", length = 200)
    private String className;

    /**
     * public方法签名列表（JSON格式）
     */
    @Column(name = "public_methods", columnDefinition = "TEXT")
    private String publicMethods;

    /**
     * 依赖的其他类（JSON格式）
     */
    @Column(name = "dependencies", columnDefinition = "TEXT")
    private String dependencies;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
