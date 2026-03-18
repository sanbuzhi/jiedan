package com.jiedan.service.ai;

import com.jiedan.service.ai.DependencyAnalyzer.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * 自动更新服务
 * 当检测到接口变更时，自动生成更新建议并触发重新生成
 */
@Slf4j
@Service
public class AutoUpdateService {

    @Autowired
    private DependencyAnalyzer dependencyAnalyzer;

    @Autowired
    private ModularCodeGenerator modularCodeGenerator;

    @Autowired
    private AiService aiService;

    @Data
    public static class UpdateTask {
        private String taskId;
        private String projectId;
        private String triggerModule;
        private List<String> changedFiles;
        private List<String> changedClasses;
        private UpdateStatus status;
        private List<UpdateStep> steps;
        private long createdAt;
        private long completedAt;
        private String errorMessage;
    }

    @Data
    public static class UpdateStep {
        private int stepNumber;
        private String description;
        private StepStatus status;
        private String result;
        private long startTime;
        private long endTime;
    }

    public enum UpdateStatus {
        PENDING, ANALYZING, GENERATING_UPDATES, APPLYING_UPDATES, COMPLETED, FAILED
    }

    public enum StepStatus {
        PENDING, IN_PROGRESS, COMPLETED, FAILED, SKIPPED
    }

    private final Map<String, UpdateTask> taskCache = new HashMap<>();
    private static final String UPDATE_CACHE_DIR = "./cache/updates/";

    /**
     * 检测变更并创建更新任务
     */
    public UpdateTask detectChangesAndCreateTask(String projectId, String moduleName, 
                                                  String projectPath) {
        log.info("检测模块变更: projectId={}, module={}", projectId, moduleName);
        
        // 获取变更的文件列表（这里简化处理，实际可能需要Git diff）
        List<String> changedFiles = detectChangedFiles(projectId, moduleName, projectPath);
        
        if (changedFiles.isEmpty()) {
            log.info("未检测到变更");
            return null;
        }

        // 提取变更的类
        List<String> changedClasses = extractChangedClasses(changedFiles);
        
        // 创建更新任务
        UpdateTask task = new UpdateTask();
        task.setTaskId(UUID.randomUUID().toString());
        task.setProjectId(projectId);
        task.setTriggerModule(moduleName);
        task.setChangedFiles(changedFiles);
        task.setChangedClasses(changedClasses);
        task.setStatus(UpdateStatus.PENDING);
        task.setCreatedAt(System.currentTimeMillis());
        task.setSteps(createUpdateSteps());
        
        // 缓存任务
        taskCache.put(task.getTaskId(), task);
        saveTaskToFile(task);
        
        log.info("创建更新任务: taskId={}, 变更文件数={}", task.getTaskId(), changedFiles.size());
        return task;
    }

    /**
     * 执行更新任务
     */
    public UpdateTask executeUpdateTask(String taskId) {
        final UpdateTask[] taskHolder = new UpdateTask[1];
        taskHolder[0] = taskCache.get(taskId);
        if (taskHolder[0] == null) {
            taskHolder[0] = loadTaskFromFile(taskId);
            if (taskHolder[0] == null) {
                log.error("未找到更新任务: {}", taskId);
                return null;
            }
            taskCache.put(taskId, taskHolder[0]);
        }

        if (taskHolder[0].getStatus() == UpdateStatus.COMPLETED || 
            taskHolder[0].getStatus() == UpdateStatus.FAILED) {
            log.warn("任务已结束，无法执行: {}", taskId);
            return taskHolder[0];
        }

        try {
            // 步骤1: 分析影响
            executeStep(taskHolder[0], 1, new StepExecutor() {
                @Override
                public String execute() {
                    return analyzeImpact(taskHolder[0]);
                }
            });
            
            // 步骤2: 生成更新建议
            executeStep(taskHolder[0], 2, new StepExecutor() {
                @Override
                public String execute() {
                    return generateUpdateSuggestions(taskHolder[0]);
                }
            });
            
            // 步骤3: 应用更新
            executeStep(taskHolder[0], 3, new StepExecutor() {
                @Override
                public String execute() {
                    return applyUpdates(taskHolder[0]);
                }
            });
            
            taskHolder[0].setStatus(UpdateStatus.COMPLETED);
            taskHolder[0].setCompletedAt(System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("更新任务执行失败", e);
            taskHolder[0].setStatus(UpdateStatus.FAILED);
            taskHolder[0].setErrorMessage(e.getMessage());
        }

        saveTaskToFile(taskHolder[0]);
        return taskHolder[0];
    }

    /**
     * 分析变更影响
     */
    private String analyzeImpact(UpdateTask task) {
        task.setStatus(UpdateStatus.ANALYZING);
        
        ImpactAnalysisResult impactResult = dependencyAnalyzer.analyzeImpact(
            task.getProjectId(),
            task.getTriggerModule(),
            task.getChangedClasses()
        );

        // 如果没有受影响模块，跳过更新
        if (impactResult.getImpactedModules().isEmpty()) {
            return "无受影响模块，无需更新";
        }

        // 将结果附加到任务中（通过JSON序列化保存）
        return String.format("发现 %d 个受影响模块", impactResult.getImpactedModules().size());
    }

    /**
     * 生成更新建议
     */
    private String generateUpdateSuggestions(UpdateTask task) {
        task.setStatus(UpdateStatus.GENERATING_UPDATES);
        
        ImpactAnalysisResult impactResult = dependencyAnalyzer.analyzeImpact(
            task.getProjectId(),
            task.getTriggerModule(),
            task.getChangedClasses()
        );

        List<String> suggestions = dependencyAnalyzer.generateUpdateSuggestions(impactResult);
        
        // 为每个受影响的模块生成具体的更新提示词
        for (ImpactedModule impacted : impactResult.getImpactedModules()) {
            String updatePrompt = generateModuleUpdatePrompt(
                task.getTriggerModule(),
                task.getChangedClasses(),
                impacted
            );
            
            // 保存更新提示词供后续使用
            saveUpdatePrompt(task.getTaskId(), impacted.getModuleName(), updatePrompt);
        }

        return String.format("生成了 %d 个模块的更新建议", impactResult.getImpactedModules().size());
    }

    /**
     * 应用更新
     */
    private String applyUpdates(UpdateTask task) {
        task.setStatus(UpdateStatus.APPLYING_UPDATES);
        
        ImpactAnalysisResult impactResult = dependencyAnalyzer.analyzeImpact(
            task.getProjectId(),
            task.getTriggerModule(),
            task.getChangedClasses()
        );

        int successCount = 0;
        int failCount = 0;

        // 按严重程度排序，优先更新严重依赖
        List<ImpactedModule> sortedModules = impactResult.getImpactedModules();
        
        for (ImpactedModule impacted : sortedModules) {
            try {
                boolean success = updateModule(task, impacted);
                if (success) {
                    successCount++;
                } else {
                    failCount++;
                }
            } catch (Exception e) {
                log.error("更新模块失败: {}", impacted.getModuleName(), e);
                failCount++;
            }
        }

        return String.format("更新完成: 成功 %d, 失败 %d", successCount, failCount);
    }

    /**
     * 更新单个模块
     */
    private boolean updateModule(UpdateTask task, ImpactedModule impacted) {
        String moduleName = impacted.getModuleName();
        log.info("更新模块: {}", moduleName);

        // 加载更新提示词
        String updatePrompt = loadUpdatePrompt(task.getTaskId(), moduleName);
        if (updatePrompt == null) {
            log.warn("未找到更新提示词: {}", moduleName);
            return false;
        }

        // 加载模块现有代码作为上下文
        Map<String, String> existingCode = loadExistingModuleCode(task.getProjectId(), moduleName);

        // 构建生成提示词
        String prompt = buildUpdateGenerationPrompt(
            updatePrompt,
            existingCode,
            impacted
        );

        // 调用AI生成更新
        try {
            // 构建生成请求 - 使用模块化生成方式
            com.jiedan.dto.ai.GenerateCodeRequest request = new com.jiedan.dto.ai.GenerateCodeRequest();
            request.setProjectId(task.getProjectId());
            request.setRequirementDoc(prompt);
            request.setTaskDoc("更新模块: " + moduleName);
            request.setModuleName(moduleName);
            
            com.jiedan.dto.ai.GenerateCodeResponse response = aiService.generateCode(request);
            
            if (!response.isSuccess()) {
                log.error("AI生成失败: {}", response.getExplanation());
                return false;
            }
            
            // 解析生成的代码
            Map<String, String> generatedFiles = modularCodeGenerator.parseGeneratedCode(response.getCode());
            
            if (generatedFiles.isEmpty()) {
                log.warn("模块 {} 更新未生成代码", moduleName);
                return false;
            }

            // 保存生成的代码（使用merge策略）
            String outputPath = "./generated/" + task.getProjectId() + "/" + moduleName;
            modularCodeGenerator.saveGeneratedFiles(generatedFiles, outputPath, "merge");
            
            log.info("模块 {} 更新成功，生成了 {} 个文件", moduleName, generatedFiles.size());
            return true;
            
        } catch (Exception e) {
            log.error("模块 {} 更新失败", moduleName, e);
            return false;
        }
    }

    /**
     * 生成模块更新提示词
     */
    private String generateModuleUpdatePrompt(String changedModule, 
                                               List<String> changedClasses,
                                               ImpactedModule impacted) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("=== 模块更新任务 ===\n\n");
        
        prompt.append("变更来源模块: ").append(changedModule).append("\n");
        prompt.append("变更类: ").append(String.join(", ", changedClasses)).append("\n");
        prompt.append("需要更新的模块: ").append(impacted.getModuleName()).append("\n");
        prompt.append("影响类型: ").append(impacted.getImpactType()).append("\n");
        prompt.append("严重程度: ").append(impacted.getSeverity()).append("/5\n\n");
        
        prompt.append("需要进行的更新:\n");
        for (String change : impacted.getRequiredChanges()) {
            prompt.append("  - ").append(change).append("\n");
        }
        prompt.append("\n");
        
        prompt.append("请根据上述变更信息，更新模块代码以保持一致性。\n");
        prompt.append("输出格式要求:\n");
        prompt.append("===FILE:文件路径===\n");
        prompt.append("文件内容\n");
        prompt.append("===END===\n\n");
        
        return prompt.toString();
    }

    /**
     * 构建更新生成提示词
     */
    private String buildUpdateGenerationPrompt(String updatePrompt,
                                                Map<String, String> existingCode,
                                                ImpactedModule impacted) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append(updatePrompt);
        
        // 添加现有代码作为参考
        if (!existingCode.isEmpty()) {
            prompt.append("\n=== 现有代码参考 ===\n");
            prompt.append("（请基于这些代码进行更新，保持原有结构和风格）\n\n");
            
            // 只添加关键文件，避免超出token限制
            int fileCount = 0;
            for (Map.Entry<String, String> entry : existingCode.entrySet()) {
                if (fileCount >= 5) break; // 最多5个参考文件
                
                String filePath = entry.getKey();
                String content = entry.getValue();
                
                // 截断过长的文件
                if (content.length() > 2000) {
                    content = content.substring(0, 2000) + "\n... (truncated)";
                }
                
                prompt.append("===FILE:").append(filePath).append("===\n");
                prompt.append(content).append("\n");
                prompt.append("===END===\n\n");
                
                fileCount++;
            }
        }
        
        return prompt.toString();
    }

    /**
     * 检测变更的文件
     */
    private List<String> detectChangedFiles(String projectId, String moduleName, String projectPath) {
        List<String> changedFiles = new ArrayList<>();
        
        // 这里简化处理，实际应该使用Git diff或文件哈希比对
        // 检查模块目录中最近修改的文件
        String modulePath = projectPath + "/" + getModuleDirectory(moduleName);
        File moduleDir = new File(modulePath);
        
        if (moduleDir.exists()) {
            long oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000);
            
            collectRecentFiles(moduleDir, changedFiles, oneHourAgo);
        }
        
        return changedFiles;
    }

    /**
     * 收集最近修改的文件
     */
    private void collectRecentFiles(File dir, List<String> files, long since) {
        File[] fileList = dir.listFiles();
        if (fileList == null) return;

        for (File file : fileList) {
            if (file.isDirectory()) {
                String name = file.getName();
                if (!name.equals("node_modules") && !name.equals("target") 
                    && !name.equals(".git") && !name.equals("cache")) {
                    collectRecentFiles(file, files, since);
                }
            } else if (file.lastModified() > since) {
                files.add(file.getAbsolutePath());
            }
        }
    }

    /**
     * 提取变更的类名
     */
    private List<String> extractChangedClasses(List<String> changedFiles) {
        List<String> classes = new ArrayList<>();
        
        for (String filePath : changedFiles) {
            if (filePath.endsWith(".java")) {
                // 从文件路径提取类名
                String fileName = new File(filePath).getName();
                String className = fileName.replace(".java", "");
                classes.add(className);
            } else if (filePath.endsWith(".js") || filePath.endsWith(".ts")) {
                String fileName = new File(filePath).getName();
                String moduleName = fileName.replaceAll("\\.(js|ts)$", "");
                classes.add(moduleName);
            }
        }
        
        return classes;
    }

    /**
     * 获取模块目录名
     */
    private String getModuleDirectory(String moduleName) {
        switch (moduleName) {
            case "backend": return "springboot-backend";
            case "frontend": return "miniprogram";
            case "database": return "database";
            case "config": return "config";
            default: return moduleName;
        }
    }

    /**
     * 加载模块现有代码
     */
    private Map<String, String> loadExistingModuleCode(String projectId, String moduleName) {
        Map<String, String> code = new HashMap<>();
        String modulePath = "./generated/" + projectId + "/" + moduleName;
        
        File moduleDir = new File(modulePath);
        if (!moduleDir.exists()) {
            return code;
        }

        collectCodeFiles(moduleDir, code, modulePath);
        return code;
    }

    /**
     * 收集代码文件内容
     */
    private void collectCodeFiles(File dir, Map<String, String> code, String basePath) {
        File[] fileList = dir.listFiles();
        if (fileList == null) return;

        for (File file : fileList) {
            if (file.isDirectory()) {
                collectCodeFiles(file, code, basePath);
            } else if (modularCodeGenerator.isCodeFile(file.getName())) {
                try {
                    String content = new String(Files.readAllBytes(file.toPath()));
                    String relativePath = file.getAbsolutePath().substring(basePath.length() + 1);
                    code.put(relativePath, content);
                } catch (IOException e) {
                    log.warn("读取文件失败: {}", file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 创建更新步骤
     */
    private List<UpdateStep> createUpdateSteps() {
        List<UpdateStep> steps = new ArrayList<>();
        
        UpdateStep step1 = new UpdateStep();
        step1.setStepNumber(1);
        step1.setDescription("分析变更影响范围");
        step1.setStatus(StepStatus.PENDING);
        steps.add(step1);
        
        UpdateStep step2 = new UpdateStep();
        step2.setStepNumber(2);
        step2.setDescription("生成更新建议");
        step2.setStatus(StepStatus.PENDING);
        steps.add(step2);
        
        UpdateStep step3 = new UpdateStep();
        step3.setStepNumber(3);
        step3.setDescription("应用更新到受影响模块");
        step3.setStatus(StepStatus.PENDING);
        steps.add(step3);
        
        return steps;
    }

    /**
     * 执行步骤
     */
    private void executeStep(UpdateTask task, int stepNumber, StepExecutor executor) {
        UpdateStep step = task.getSteps().get(stepNumber - 1);
        step.setStatus(StepStatus.IN_PROGRESS);
        step.setStartTime(System.currentTimeMillis());
        
        try {
            String result = executor.execute();
            step.setResult(result);
            step.setStatus(StepStatus.COMPLETED);
        } catch (Exception e) {
            step.setResult("失败: " + e.getMessage());
            step.setStatus(StepStatus.FAILED);
            throw e;
        } finally {
            step.setEndTime(System.currentTimeMillis());
        }
    }

    @FunctionalInterface
    private interface StepExecutor {
        String execute();
    }

    /**
     * 保存更新提示词
     */
    private void saveUpdatePrompt(String taskId, String moduleName, String prompt) {
        try {
            File dir = new File(UPDATE_CACHE_DIR + taskId);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            
            File file = new File(dir, moduleName + "_prompt.txt");
            Files.write(file.toPath(), prompt.getBytes());
        } catch (IOException e) {
            log.error("保存更新提示词失败", e);
        }
    }

    /**
     * 加载更新提示词
     */
    private String loadUpdatePrompt(String taskId, String moduleName) {
        try {
            File file = new File(UPDATE_CACHE_DIR + taskId + "/" + moduleName + "_prompt.txt");
            if (file.exists()) {
                return new String(Files.readAllBytes(file.toPath()));
            }
        } catch (IOException e) {
            log.error("加载更新提示词失败", e);
        }
        return null;
    }

    /**
     * 保存任务到文件
     */
    private void saveTaskToFile(UpdateTask task) {
        // 简化实现，实际应该使用JSON序列化
        // 这里仅作演示
    }

    /**
     * 从文件加载任务
     */
    private UpdateTask loadTaskFromFile(String taskId) {
        // 简化实现
        return null;
    }

    /**
     * 获取任务状态
     */
    public UpdateTask getTaskStatus(String taskId) {
        UpdateTask task = taskCache.get(taskId);
        if (task == null) {
            task = loadTaskFromFile(taskId);
        }
        return task;
    }

    /**
     * 列出项目的所有更新任务
     */
    public List<UpdateTask> listProjectTasks(String projectId) {
        return taskCache.values().stream()
            .filter(task -> task.getProjectId().equals(projectId))
            .sorted((a, b) -> Long.compare(b.getCreatedAt(), a.getCreatedAt()))
            .collect(java.util.stream.Collectors.toList());
    }
}
