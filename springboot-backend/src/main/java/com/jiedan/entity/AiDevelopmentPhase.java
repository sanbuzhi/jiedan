package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_development_phase")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDevelopmentPhase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, length = 50)
    private String projectId;

    @Column(name = "phase", nullable = false)
    private Integer phase;

    @Column(name = "phase_name", length = 100)
    private String phaseName;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "NOT_STARTED";

    @Column(name = "total_rounds")
    private Integer totalRounds = 0;

    @Column(name = "total_files")
    private Integer totalFiles = 0;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "summary", columnDefinition = "LONGTEXT")
    private String summary;

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
