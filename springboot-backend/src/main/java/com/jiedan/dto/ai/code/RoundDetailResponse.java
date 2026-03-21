package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoundDetailResponse {
    private String projectId;
    private Integer phase;
    private Integer round;
    private String status;
    private String inputSummary;
    private String outputSummary;
    private List<FileInfo> files;
    private Integer tokensUsed;
    private Integer continuationCount;
    private String finishReason;
    private String duration;
    private LocalDateTime completedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FileInfo {
        private String path;
        private Long size;
    }
}
