package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhaseProgressResponse {
    private String projectId;
    private Integer phase;
    private String content;
    private LocalDateTime lastUpdatedAt;
}
