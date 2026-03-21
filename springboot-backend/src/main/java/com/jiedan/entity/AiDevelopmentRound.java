package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_development_round")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDevelopmentRound {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "phase_id", nullable = false)
    private Long phaseId;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "PROCESSING";

    @Column(name = "input_prompt", columnDefinition = "LONGTEXT")
    private String inputPrompt;

    @Column(name = "output_content", columnDefinition = "LONGTEXT")
    private String outputContent;

    @Column(name = "tokens_used")
    private Integer tokensUsed = 0;

    @Column(name = "files_count")
    private Integer filesCount = 0;

    @Column(name = "continuation")
    private Integer continuation = 0;

    @Column(name = "finish_reason", length = 20)
    private String finishReason;

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
