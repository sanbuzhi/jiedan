package com.jiedan.dto.ai.code;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 代码生成请求DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateCodeRequest {

    /**
     * 生成类型
     */
    private GenerateType generateType;

    // ========== 项目信息 ==========
    private String projectId;
    private String projectType;
    private String frontendFramework;
    private String backendFramework;

    // ========== 类型2：任务开发 ==========
    private String taskId;
    private String taskName;
    private String taskDescription;
    private String taskType;
    private List<String> taskDependencies;

    // ========== 上下文（类型2和3） ==========
    private String prdSummary;
    private List<CodeSummary> contextSummaries;
    private List<CodeSummary> dependencySummaries;
    private CodeStyle codeStyle;

    // ========== 类型3：Feedback修复 ==========
    private String previousCode;
    private List<CompilationError> previousErrors;
    private String feedbackShadowReport;
    private Integer fixAttempt;
}
