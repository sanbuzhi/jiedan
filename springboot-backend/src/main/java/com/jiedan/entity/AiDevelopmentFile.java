package com.jiedan.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ai_development_file")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiDevelopmentFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "project_id", nullable = false, length = 50)
    private String projectId;

    @Column(name = "phase", nullable = false)
    private Integer phase;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize = 0L;

    @Column(name = "file_type", length = 20)
    private String fileType;

    @Column(name = "is_complete")
    private Boolean isComplete = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
