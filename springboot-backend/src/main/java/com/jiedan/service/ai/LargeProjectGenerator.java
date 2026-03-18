package com.jiedan.service.ai;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 【步骤10】大型项目生成器
 * 支持50万token的大型项目分批次生成
 * 每批次2-3个模块，支持断点续传
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LargeProjectGenerator {

    private final ModularCodeGenerator modularCodeGenerator;
    private final TaskDocumentParser taskDocumentParser;

    // 批次状态缓存：projectId -> BatchStatus
    private final Map<String, BatchStatus> batchStatusCache = new ConcurrentHashMap<>();

    /**
     * 批次状态
     */
    public static class BatchStatus {
        private String projectId;
        private List<Batch> batches;
        private int currentBatchIndex;
        private boolean completed;
        private String errorMessage;
        private long startTime;
        private long endTime;

        public BatchStatus(String projectId) {
            this.projectId = projectId;
            this.batches = new ArrayList<>();
            this.currentBatchIndex = 0;
            this.completed = false;
            this.startTime = System.currentTimeMillis();
        }

        // Getters and setters
        public String getProjectId() { return projectId; }
        public List<Batch> getBatches() { return batches; }
        public int getCurrentBatchIndex() { return currentBatchIndex; }
        public void setCurrentBatchIndex(int index) { this.currentBatchIndex = index; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        public long getStartTime() { return startTime; }
        public void setStartTime(long startTime) { this.startTime = startTime; }
        public long getEndTime() { return endTime; }
        public void setEndTime(long endTime) { this.endTime = endTime; }

        /**
         * 获取总体进度（0-100）
         */
        public int getProgress() {
            if (batches.isEmpty()) return 0;
            if (completed) return 100;
            return (currentBatchIndex * 100) / batches.size();
        }

        /**
         * 获取已生成的模块摘要
         */
        public Map<String, String> getGeneratedModulesSummary() {
            Map<String, String> summary = new HashMap<>();
            for (int i = 0; i < currentBatchIndex && i < batches.size(); i++) {
                Batch batch = batches.get(i);
                for (ModularCodeGenerator.ModuleGenerationResult result : batch.getResults()) {
                    if (result.isSuccess()) {
                        summary.put(result.getModuleName(), result.getCodeSummary());
                    }
                }
            }
            return summary;
        }

        /**
         * 是否成功（无错误）
         */
        public boolean isSuccess() {
            return errorMessage == null || errorMessage.isEmpty();
        }

        /**
         * 获取总批次数
         */
        public int getTotalBatches() {
            return batches.size();
        }

        /**
         * 获取已完成的模块列表
         */
        public List<String> getCompletedModules() {
            List<String> completed = new ArrayList<>();
            for (int i = 0; i < currentBatchIndex && i < batches.size(); i++) {
                Batch batch = batches.get(i);
                for (ModularCodeGenerator.ModuleGenerationResult result : batch.getResults()) {
                    if (result.isSuccess()) {
                        completed.add(result.getModuleName());
                    }
                }
            }
            return completed;
        }
    }

    /**
     * 批次信息
     */
    public static class Batch {
        private int index;
        private String name;
        private List<String> moduleNames;
        private List<ModularCodeGenerator.ModuleGenerationResult> results;
        private boolean completed;
        private String errorMessage;

        public Batch(int index, String name, List<String> moduleNames) {
            this.index = index;
            this.name = name;
            this.moduleNames = moduleNames;
            this.results = new ArrayList<>();
            this.completed = false;
        }

        // Getters and setters
        public int getIndex() { return index; }
        public String getName() { return name; }
        public List<String> getModuleNames() { return moduleNames; }
        public List<ModularCodeGenerator.ModuleGenerationResult> getResults() { return results; }
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }

    /**
     * 启动大型项目生成
     * @param projectId 项目ID
     * @param requirementDoc 需求书
     * @param taskDoc 任务书
     * @return 批次状态
     */
    public BatchStatus startGeneration(String projectId, String requirementDoc, String taskDoc) {
        log.info("启动大型项目生成, projectId: {}", projectId);

        // 1. 解析任务书（传入projectId以保存章节文件）
        TaskDocumentParser.ParseResult parseResult = taskDocumentParser.parse(taskDoc, projectId);
        if (!parseResult.isSuccess()) {
            BatchStatus status = new BatchStatus(projectId);
            status.setErrorMessage("任务书解析失败: " + parseResult.getErrorMessage());
            return status;
        }

        List<TaskDocumentParser.ModuleInfo> modules = parseResult.getModules();
        if (modules.isEmpty()) {
            BatchStatus status = new BatchStatus(projectId);
            status.setErrorMessage("未找到任何模块");
            return status;
        }

        log.info("任务书解析完成，共 {} 个模块", modules.size());

        // 2. 划分批次（每批次2-3个模块）
        BatchStatus status = new BatchStatus(projectId);
        List<Batch> batches = createBatches(modules);
        status.getBatches().addAll(batches);

        log.info("项目划分为 {} 个批次", batches.size());

        // 3. 保存状态到缓存
        batchStatusCache.put(projectId, status);

        // 4. 保存状态到文件（用于断点续传）
        saveStatusToFile(status);

        return status;
    }

    /**
     * 执行下一批次
     * @param projectId 项目ID
     * @return 更新后的状态
     */
    public BatchStatus executeNextBatch(String projectId, String requirementDoc, String taskDoc) {
        BatchStatus status = batchStatusCache.get(projectId);
        if (status == null) {
            // 尝试从文件恢复状态
            status = loadStatusFromFile(projectId);
            if (status == null) {
                log.error("未找到项目状态, projectId: {}", projectId);
                return null;
            }
            batchStatusCache.put(projectId, status);
        }

        if (status.isCompleted()) {
            log.info("项目已 completed, projectId: {}", projectId);
            return status;
        }

        int currentIndex = status.getCurrentBatchIndex();
        if (currentIndex >= status.getBatches().size()) {
            status.setCompleted(true);
            status.setEndTime(System.currentTimeMillis());
            saveStatusToFile(status);
            return status;
        }

        Batch batch = status.getBatches().get(currentIndex);
        log.info("执行批次 {}/{}: {}", currentIndex + 1, status.getBatches().size(), batch.getName());

        // 获取已生成模块摘要
        Map<String, String> generatedSummary = status.getGeneratedModulesSummary();

        // 生成批次中的每个模块
        for (String moduleName : batch.getModuleNames()) {
            log.info("生成模块: {}", moduleName);

            ModularCodeGenerator.ModuleGenerationResult result =
                    modularCodeGenerator.generateSingleModule(
                            projectId,
                            requirementDoc,
                            taskDoc,
                            moduleName,
                            generatedSummary
                    );

            batch.getResults().add(result);

            if (result.isSuccess()) {
                generatedSummary.put(moduleName, result.getCodeSummary());
                log.info("模块生成成功: {}", moduleName);
            } else {
                log.error("模块生成失败: {}, 错误: {}", moduleName, result.getErrorMessage());
                batch.setErrorMessage("模块 " + moduleName + " 生成失败: " + result.getErrorMessage());
                // 继续生成其他模块
            }
        }

        batch.setCompleted(true);
        status.setCurrentBatchIndex(currentIndex + 1);

        // 检查是否全部完成
        if (status.getCurrentBatchIndex() >= status.getBatches().size()) {
            status.setCompleted(true);
            status.setEndTime(System.currentTimeMillis());
            log.info("项目生成完成, projectId: {}, 耗时: {}ms",
                    projectId, status.getEndTime() - status.getStartTime());
        }

        // 保存状态
        saveStatusToFile(status);

        return status;
    }

    /**
     * 获取项目状态
     */
    public BatchStatus getStatus(String projectId) {
        BatchStatus status = batchStatusCache.get(projectId);
        if (status == null) {
            status = loadStatusFromFile(projectId);
        }
        return status;
    }

    /**
     * 获取批次状态（兼容方法）
     */
    public BatchStatus getBatchStatus(String projectId) {
        return getStatus(projectId);
    }

    /**
     * 创建批次
     * 策略：
     * 1. 数据库模块单独一个批次（优先级最高）
     * 2. 配置模块一个批次
     * 3. 后端模块每2-3个一批次
     * 4. 前端模块每2-3个一批次
     */
    private List<Batch> createBatches(List<TaskDocumentParser.ModuleInfo> modules) {
        List<Batch> batches = new ArrayList<>();

        // 按类型分组
        List<TaskDocumentParser.ModuleInfo> databaseModules = new ArrayList<>();
        List<TaskDocumentParser.ModuleInfo> configModules = new ArrayList<>();
        List<TaskDocumentParser.ModuleInfo> backendModules = new ArrayList<>();
        List<TaskDocumentParser.ModuleInfo> frontendModules = new ArrayList<>();

        for (TaskDocumentParser.ModuleInfo module : modules) {
            switch (module.getType()) {
                case "database":
                    databaseModules.add(module);
                    break;
                case "config":
                    configModules.add(module);
                    break;
                case "backend":
                    backendModules.add(module);
                    break;
                case "frontend":
                    frontendModules.add(module);
                    break;
            }
        }

        int batchIndex = 0;

        // 批次1：数据库模块
        if (!databaseModules.isEmpty()) {
            List<String> moduleNames = new ArrayList<>();
            for (TaskDocumentParser.ModuleInfo m : databaseModules) {
                moduleNames.add(m.getName());
            }
            batches.add(new Batch(batchIndex++, "数据库和实体类", moduleNames));
        }

        // 批次2：配置模块
        if (!configModules.isEmpty()) {
            List<String> moduleNames = new ArrayList<>();
            for (TaskDocumentParser.ModuleInfo m : configModules) {
                moduleNames.add(m.getName());
            }
            batches.add(new Batch(batchIndex++, "项目配置", moduleNames));
        }

        // 批次3-N：后端模块（每2个一批次）
        for (int i = 0; i < backendModules.size(); i += 2) {
            List<String> moduleNames = new ArrayList<>();
            int end = Math.min(i + 2, backendModules.size());
            for (int j = i; j < end; j++) {
                moduleNames.add(backendModules.get(j).getName());
            }
            batches.add(new Batch(batchIndex++, "后端模块-" + (i/2 + 1), moduleNames));
        }

        // 批次N+1-M：前端模块（每2个一批次）
        for (int i = 0; i < frontendModules.size(); i += 2) {
            List<String> moduleNames = new ArrayList<>();
            int end = Math.min(i + 2, frontendModules.size());
            for (int j = i; j < end; j++) {
                moduleNames.add(frontendModules.get(j).getName());
            }
            batches.add(new Batch(batchIndex++, "前端模块-" + (i/2 + 1), moduleNames));
        }

        return batches;
    }

    /**
     * 保存状态到文件
     */
    private void saveStatusToFile(BatchStatus status) {
        try {
            String projectPath = "projects/" + status.getProjectId();
            Path statusDir = Paths.get(projectPath, ".status");
            Files.createDirectories(statusDir);

            Path statusFile = statusDir.resolve("generation.status");

            // 简单序列化
            StringBuilder sb = new StringBuilder();
            sb.append("projectId=").append(status.getProjectId()).append("\n");
            sb.append("completed=").append(status.isCompleted()).append("\n");
            sb.append("currentBatch=").append(status.getCurrentBatchIndex()).append("\n");
            sb.append("totalBatches=").append(status.getBatches().size()).append("\n");
            sb.append("startTime=").append(status.getStartTime()).append("\n");
            sb.append("endTime=").append(status.getEndTime()).append("\n");

            if (status.getErrorMessage() != null) {
                sb.append("error=").append(status.getErrorMessage()).append("\n");
            }

            // 保存批次信息
            for (Batch batch : status.getBatches()) {
                sb.append("batch=").append(batch.getIndex())
                  .append(",").append(batch.getName())
                  .append(",").append(batch.isCompleted())
                  .append(",").append(String.join(";", batch.getModuleNames()))
                  .append("\n");
            }

            Files.writeString(statusFile, sb.toString());
            log.debug("状态已保存到文件: {}", statusFile);

        } catch (IOException e) {
            log.error("保存状态失败, projectId: {}", status.getProjectId(), e);
        }
    }

    /**
     * 从文件加载状态
     */
    private BatchStatus loadStatusFromFile(String projectId) {
        try {
            String projectPath = "projects/" + projectId;
            Path statusFile = Paths.get(projectPath, ".status", "generation.status");

            if (!Files.exists(statusFile)) {
                return null;
            }

            String content = Files.readString(statusFile);
            BatchStatus status = new BatchStatus(projectId);

            // 简单解析
            for (String line : content.split("\n")) {
                if (line.startsWith("completed=")) {
                    status.setCompleted(Boolean.parseBoolean(line.substring(10)));
                } else if (line.startsWith("currentBatch=")) {
                    status.setCurrentBatchIndex(Integer.parseInt(line.substring(13)));
                } else if (line.startsWith("startTime=")) {
                    status.setStartTime(Long.parseLong(line.substring(10)));
                } else if (line.startsWith("endTime=")) {
                    String value = line.substring(8);
                    if (!value.isEmpty()) {
                        status.setEndTime(Long.parseLong(value));
                    }
                } else if (line.startsWith("error=")) {
                    status.setErrorMessage(line.substring(6));
                } else if (line.startsWith("batch=")) {
                    String[] parts = line.substring(6).split(",");
                    if (parts.length >= 4) {
                        int index = Integer.parseInt(parts[0]);
                        String name = parts[1];
                        boolean completed = Boolean.parseBoolean(parts[2]);
                        List<String> moduleNames = Arrays.asList(parts[3].split(";"));
                        Batch batch = new Batch(index, name, moduleNames);
                        batch.setCompleted(completed);
                        status.getBatches().add(batch);
                    }
                }
            }

            log.info("状态已从文件恢复, projectId: {}", projectId);
            return status;

        } catch (IOException e) {
            log.error("加载状态失败, projectId: {}", projectId, e);
            return null;
        }
    }
}
