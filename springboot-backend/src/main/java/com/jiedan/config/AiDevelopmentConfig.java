package com.jiedan.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai-development")
public class AiDevelopmentConfig {

    private Integer maxRetries = 30;
    private Integer maxTokens = 32000;
    private Integer maxContinuations = 5;
    private Integer maxRoundsPerPhase = 20;
    private Integer noNewFileRoundsThreshold = 5;
    private Double coverageThreshold = 0.8;
    private Integer baseDelayMs = 1000;

    @Data
    public static class PhaseConfig {
        private Integer phase;
        private String name;
        private List<String> keywords;
        private List<String> targetFiles;
        private Integer estimatedFiles;
        private String phaseTask;

        public PhaseConfig(Integer phase, String name, List<String> keywords,
                          List<String> targetFiles, Integer estimatedFiles) {
            this.phase = phase;
            this.name = name;
            this.keywords = keywords;
            this.targetFiles = targetFiles;
            this.estimatedFiles = estimatedFiles;
        }

        public String getPhaseDirName() {
            return phase + "_" + name.replace("开发", "").replace("/", "_");
        }
    }

    public PhaseConfig getPhaseConfig(Integer phase) {
        throw new UnsupportedOperationException("PHASE_CONFIGS已弃用，阶段配置应从任务书AI分析获取");
    }

    public int getTotalPhases() {
        throw new UnsupportedOperationException("PHASE_CONFIGS已弃用，阶段数量应从任务书AI分析获取");
    }
}
