package com.jiedan.controller.ai;

import com.jiedan.dto.ApiResponse;
import com.jiedan.dto.ai.code.*;
import com.jiedan.entity.ProjectStatus;
import com.jiedan.repository.ProjectStatusRepository;
import com.jiedan.service.ai.code.CodeGenerationOrchestrator;
import com.jiedan.service.ai.code.CodeQualityChecker;
import com.jiedan.service.ai.ModularCodeGenerator;
import com.jiedan.service.ai.TaskDocumentParser;
import com.jiedan.service.ai.DependencyAnalyzer;
import com.jiedan.service.ai.AutoUpdateService;
import com.jiedan.service.ai.LargeProjectGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    private final ModularCodeGenerator modularCodeGenerator;
    private final TaskDocumentParser taskDocumentParser;
    private final DependencyAnalyzer dependencyAnalyzer;
    private final AutoUpdateService autoUpdateService;
    private final LargeProjectGenerator largeProjectGenerator;

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

    // ========== 【步骤6】模块化生成接口 ==========

    /**
     * 解析任务书，获取模块列表
     */
    @PostMapping("/parse-task")
    public ResponseEntity<ApiResponse<TaskDocumentParser.ParseResult>> parseTaskDocument(
            @RequestBody ParseTaskRequest request) {
        log.info("解析任务书, projectId: {}", request.getProjectId());

        TaskDocumentParser.ParseResult result = taskDocumentParser.parse(request.getTaskDoc());

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(result.getErrorMessage()));
        }
    }

    /**
     * 生成完整项目（按模块顺序）
     */
    @PostMapping("/generate-project/{projectId}")
    public ResponseEntity<ApiResponse<ModularCodeGenerator.ProjectGenerationResult>> generateProject(
            @PathVariable String projectId,
            @RequestBody GenerateProjectRequest request) {
        log.info("生成完整项目, projectId: {}", projectId);

        ModularCodeGenerator.ProjectGenerationResult result = modularCodeGenerator.generateProject(
                projectId,
                request.getRequirementDoc(),
                request.getTaskDoc());

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(result.getErrorMessage()));
        }
    }

    /**
     * 生成单个模块
     */
    @PostMapping("/generate-module/{projectId}")
    public ResponseEntity<ApiResponse<ModularCodeGenerator.ModuleGenerationResult>> generateModule(
            @PathVariable String projectId,
            @RequestBody GenerateModuleRequest request) {
        log.info("生成单个模块, projectId: {}, module: {}", projectId, request.getModuleName());

        ModularCodeGenerator.ModuleGenerationResult result = modularCodeGenerator.generateSingleModule(
                projectId,
                request.getRequirementDoc(),
                request.getTaskDoc(),
                request.getModuleName(),
                request.getGeneratedModulesSummary());

        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(result));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(result.getErrorMessage()));
        }
    }

    // ========== 模块化生成请求DTO ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ParseTaskRequest {
        private String projectId;
        private String taskDoc;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerateProjectRequest {
        private String requirementDoc;
        private String taskDoc;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class GenerateModuleRequest {
        private String requirementDoc;
        private String taskDoc;
        private String moduleName;
        private Map<String, String> generatedModulesSummary;
    }

    // ========== 【步骤10-11】大项目生成和依赖分析接口 ==========

    /**
     * 启动大项目分批次生成
     */
    @PostMapping("/large-project/start/{projectId}")
    public ResponseEntity<ApiResponse<LargeProjectGenerator.BatchStatus>> startLargeProjectGeneration(
            @PathVariable String projectId,
            @RequestBody LargeProjectRequest request) {
        log.info("启动大项目分批次生成, projectId: {}", projectId);

        LargeProjectGenerator.BatchStatus status = largeProjectGenerator.startGeneration(
                projectId,
                request.getRequirementDoc(),
                request.getTaskDoc());

        if (status.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(status));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error(status.getErrorMessage()));
        }
    }

    /**
     * 执行下一批次生成
     */
    @PostMapping("/large-project/next/{projectId}")
    public ResponseEntity<ApiResponse<LargeProjectGenerator.BatchStatus>> executeNextBatch(
            @PathVariable String projectId,
            @RequestBody LargeProjectRequest request) {
        log.info("执行下一批次生成, projectId: {}", projectId);

        LargeProjectGenerator.BatchStatus status = largeProjectGenerator.executeNextBatch(
                projectId,
                request.getRequirementDoc(),
                request.getTaskDoc());

        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(status));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("批次执行失败"));
        }
    }

    /**
     * 获取批次生成状态
     */
    @GetMapping("/large-project/status/{projectId}")
    public ResponseEntity<ApiResponse<LargeProjectGenerator.BatchStatus>> getBatchStatus(
            @PathVariable String projectId) {
        log.info("获取批次生成状态, projectId: {}", projectId);

        LargeProjectGenerator.BatchStatus status = largeProjectGenerator.getBatchStatus(projectId);

        if (status != null) {
            return ResponseEntity.ok(ApiResponse.success(status));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 分析项目依赖关系
     */
    @PostMapping("/analyze-dependencies/{projectId}")
    public ResponseEntity<ApiResponse<DependencyAnalyzer.DependencyGraph>> analyzeDependencies(
            @PathVariable String projectId,
            @RequestBody AnalyzeDependenciesRequest request) {
        log.info("分析项目依赖关系, projectId: {}", projectId);

        DependencyAnalyzer.DependencyGraph graph = dependencyAnalyzer.analyzeProject(
                projectId,
                request.getProjectPath());

        if (graph.getModules().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("依赖分析失败，未找到模块"));
        }

        return ResponseEntity.ok(ApiResponse.success(graph));
    }

    /**
     * 分析变更影响
     */
    @PostMapping("/analyze-impact/{projectId}")
    public ResponseEntity<ApiResponse<DependencyAnalyzer.ImpactAnalysisResult>> analyzeImpact(
            @PathVariable String projectId,
            @RequestBody AnalyzeImpactRequest request) {
        log.info("分析变更影响, projectId: {}, module: {}", projectId, request.getChangedModule());

        DependencyAnalyzer.ImpactAnalysisResult result = dependencyAnalyzer.analyzeImpact(
                projectId,
                request.getChangedModule(),
                request.getChangedClasses());

        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * 创建自动更新任务
     */
    @PostMapping("/auto-update/create/{projectId}")
    public ResponseEntity<ApiResponse<AutoUpdateService.UpdateTask>> createAutoUpdateTask(
            @PathVariable String projectId,
            @RequestBody CreateUpdateTaskRequest request) {
        log.info("创建自动更新任务, projectId: {}, module: {}", projectId, request.getModuleName());

        AutoUpdateService.UpdateTask task = autoUpdateService.detectChangesAndCreateTask(
                projectId,
                request.getModuleName(),
                request.getProjectPath());

        if (task != null) {
            return ResponseEntity.ok(ApiResponse.success(task));
        } else {
            return ResponseEntity.ok(ApiResponse.success("未检测到变更", null));
        }
    }

    /**
     * 执行自动更新任务
     */
    @PostMapping("/auto-update/execute/{taskId}")
    public ResponseEntity<ApiResponse<AutoUpdateService.UpdateTask>> executeAutoUpdateTask(
            @PathVariable String taskId) {
        log.info("执行自动更新任务, taskId: {}", taskId);

        AutoUpdateService.UpdateTask task = autoUpdateService.executeUpdateTask(taskId);

        if (task != null) {
            return ResponseEntity.ok(ApiResponse.success(task));
        } else {
            return ResponseEntity.badRequest().body(ApiResponse.error("任务执行失败"));
        }
    }

    /**
     * 获取更新任务状态
     */
    @GetMapping("/auto-update/status/{taskId}")
    public ResponseEntity<ApiResponse<AutoUpdateService.UpdateTask>> getUpdateTaskStatus(
            @PathVariable String taskId) {
        log.info("获取更新任务状态, taskId: {}", taskId);

        AutoUpdateService.UpdateTask task = autoUpdateService.getTaskStatus(taskId);

        if (task != null) {
            return ResponseEntity.ok(ApiResponse.success(task));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ========== 大项目和依赖分析请求DTO ==========

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class LargeProjectRequest {
        private String requirementDoc;
        private String taskDoc;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyzeDependenciesRequest {
        private String projectPath;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class AnalyzeImpactRequest {
        private String changedModule;
        private List<String> changedClasses;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class CreateUpdateTaskRequest {
        private String moduleName;
        private String projectPath;
    }
}
