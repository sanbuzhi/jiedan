package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 生成的代码文件实体
 */
@Entity
@Table(name = "generated_files", indexes = {
    @Index(name = "idx_project_task", columnList = "project_id, task_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedFileEntity {

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
    @Column(name = "task_id", length = 64)
    private String taskId;

    /**
     * 文件路径
     */
    @Column(name = "file_path", length = 500)
    private String filePath;

    /**
     * 文件内容
     */
    @Column(name = "file_content", columnDefinition = "LONGTEXT")
    private String fileContent;

    /**
     * 编程语言
     */
    @Column(name = "language", length = 50)
    private String language;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
