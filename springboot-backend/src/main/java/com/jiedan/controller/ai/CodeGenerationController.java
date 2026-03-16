package com.jiedan.controller.ai;

import com.jiedan.dto.ApiResponse;
import com.jiedan.dto.ai.code.*;
import com.jiedan.entity.ProjectStatus;
import com.jiedan.repository.ProjectStatusRepository;
import com.jiedan.service.ai.code.CodeGenerationOrchestrator;
import com.jiedan.service.ai.code.CodeQualityChecker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码生成控制器（极简版 - 直接生成完整项目）
 * 提供AI代码生成相关接口
 */
@Slf4j
@RestController
@RequestMapping("/api/ai/code")
@RequiredArgsConstructor
public class CodeGenerationController {

    private final CodeGenerationOrchestrator codeGenerationOrchestrator;
    private final CodeQualityChecker codeQualityChecker;
    private final ProjectStatusRepository projectStatusRepository;

    /**
     * 直接生成完整项目代码
     * AI生成包含脚手架和业务代码的完整项目
     */
    @PostMapping("/generate/{projectId}")
    public ResponseEntity<ApiResponse<GenerateCodeResponse>> generateCompleteProject(
            @PathVariable String projectId,
            @RequestBody GenerateCodeRequest request) {
        log.info("生成完整项目代码, projectId: {}, projectType: {}", projectId, request.getProjectType());

        GenerateCodeResponse response = codeGenerationOrchestrator.generateCompleteProject(
                projectId,
                request.getProjectType(),
                request.getPrdDocument(),
                request.getTaskDocument());

        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getErrorMessage()));
        }
    }

    /**
     * 执行完整开发流程（极简版）
     * 直接生成完整项目 → 质量门禁
     */
    @PostMapping("/develop/{projectId}")
    public ResponseEntity<ApiResponse<DevelopmentResponse>> executeFullDevelopment(
            @PathVariable String projectId,
            @RequestBody DevelopmentRequest request) {
        log.info("执行完整开发流程, projectId: {}", projectId);

        try {
            codeGenerationOrchestrator.executeFullDevelopmentFlow(
                    projectId,
                    request.getProjectType(),
                    request.getPrdDocument(),
                    request.getTaskDocument());

            DevelopmentResponse response = DevelopmentResponse.builder()
                    .success(true)
                    .message("开发流程已启动")
                    .projectId(projectId)
                    .build();

            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (Exception e) {
            log.error("开发流程失败", e);
            DevelopmentResponse response = DevelopmentResponse.builder()
                    .success(false)
                    .message("开发流程失败: " + e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getMessage()));
        }
    }

    /**
     * 获取项目状态
     */
    @GetMapping("/status/{projectId}")
    public ResponseEntity<ApiResponse<ProjectStatus>> getProjectStatus(@PathVariable String projectId) {
        log.info("获取项目状态, projectId: {}", projectId);

        ProjectStatus status = projectStatusRepository.findByProjectId(projectId)
                .orElse(null);

        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(status));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 执行质量门禁检查
     */
    @PostMapping("/quality-check/{projectId}")
    public ResponseEntity<ApiResponse<CodeQualityChecker.QualityCheckResult>> performQualityCheck(
            @PathVariable String projectId,
            @RequestBody QualityCheckRequest request) {
        log.info("执行质量门禁检查, projectId: {}", projectId);

        CodeQualityChecker.QualityCheckResult result = codeGenerationOrchestrator.performQualityGateCheck(
                projectId, request.getProjectType(), request.getFiles());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 修复代码
     */
    @PostMapping("/repair/{projectId}")
    public ResponseEntity<ApiResponse<GenerateCodeResponse>> repairCode(
            @PathVariable String projectId,
            @RequestBody RepairRequest request) {
        log.info("修复代码, projectId: {}", projectId);

        GenerateCodeResponse response = codeGenerationOrchestrator.repairCode(
                projectId,
                request.getProjectType(),
                request.getPrdDocument(),
                request.getTaskDocument(),
                request.getErrorReport());

        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(response));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(response.getErrorMessage()));
        }
    }

    // ========== 请求/响应DTO ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerateCodeRequest {
        private String projectType;
        private String prdDocument;
        private String taskDocument;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DevelopmentRequest {
        private String projectType;
        private String prdDocument;
        private String taskDocument;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DevelopmentResponse {
        private boolean success;
        private String message;
        private String projectId;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QualityCheckRequest {
        private String projectType;
        private List<GeneratedFile> files;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class RepairRequest {
        private String projectType;
        private String prdDocument;
        private String taskDocument;
        private String errorReport;
    }
}
