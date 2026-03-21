package com.jiedan.dto.ai;

import lombok.Data;
import java.util.List;

@Data
public class AnalyzeTasksResponse {
    private List<PhaseAnalysis> phases;

    @Data
    public static class PhaseAnalysis {
        private Integer phase;
        private String phaseName;
        private List<String> targetFiles;
        private List<String> keywords;
    }
}
