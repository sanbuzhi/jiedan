package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectStatusResponse {
    private String projectId;
    private String projectName;
    private String status;
    private Integer currentPhase;
    private String currentPhaseName;
    private Integer totalPhases;
    private Integer progress;
    private List<PhaseStatusDto> phases;
    private Integer totalFiles;
    private Integer totalRounds;
    private String error;
    private LocalDateTime startedAt;
    private LocalDateTime estimatedCompletionTime;
    private LocalDateTime updatedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PhaseStatusDto {
        private Integer phase;
        private String name;
        private String status;
        private Integer totalRounds;
        private Integer totalFiles;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private String duration;
        private Integer currentRound;
        private List<String> recentFiles;
    }
}
