package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhaseStatusResponse {
    private String projectId;
    private Integer phase;
    private String phaseName;
    private String status;
    private String sessionId;
    private Integer currentRound;
    private Integer totalRounds;
    private Integer totalFiles;
    private List<GeneratedFileDto> generatedFiles;
    private List<String> pendingFiles;
    private LocalDateTime startTime;
    private LocalDateTime updatedAt;
    private Integer progress;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeneratedFileDto {
        private String path;
        private Long size;
        private String type;
        private Integer round;
        private Boolean complete;
    }
}
