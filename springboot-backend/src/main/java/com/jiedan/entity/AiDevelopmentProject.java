package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_development_project")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDevelopmentProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, unique = true, length = 50)
    private String projectId;

    @Column(name = "project_name", nullable = false, length = 100)
    private String projectName;

    @Column(name = "task_doc", columnDefinition = "LONGTEXT")
    private String taskDoc;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "NOT_STARTED";

    @Column(name = "current_phase")
    private Integer currentPhase = 1;

    @Column(name = "progress")
    private Integer progress = 0;

    @Column(name = "total_files")
    private Integer totalFiles = 0;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
