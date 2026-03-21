package com.jiedan.service.ai;

import com.jiedan.config.AiDevelopmentConfig;
import com.jiedan.config.AiDevelopmentConfig.PhaseConfig;
import com.jiedan.config.HuoshanAiProperties;
import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.entity.AiDevelopmentFile;
import com.jiedan.entity.AiDevelopmentPhase;
import com.jiedan.entity.AiDevelopmentProject;
import com.jiedan.entity.AiDevelopmentRound;
import com.jiedan.repository.*;
import com.jiedan.service.ai.prompt.AiPromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProjectDevelopmentManager {

    private final AiDevelopmentProjectRepository projectRepository;
    private final AiDevelopmentPhaseRepository phaseRepository;
    private final AiDevelopmentRoundRepository roundRepository;
    private final AiDevelopmentFileRepository fileRepository;
    private final AiDevelopmentConfig config;
    private final HuoshanAiProperties aiProperties;
    private final AIProviderStrategy aiProvider;
    private final TasksAnalysisService tasksAnalysisService;

    private static final String PROJECTS_BASE_DIR = "projects";
    
    private static final Pattern[] FILE_PATTERNS = new Pattern[] {
        // ========== 规则0：匹配正确格式 ==========
        // ===FILE:src/main/java/User.java===
        // ```java
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "===FILE[：:]\\s*(.+?)===\\s*" +
            "```(\\w*)\\s*" +
            "(.+?)" +
            "```\\s*",
            Pattern.DOTALL
        ),
        // ========== 规则1：```lang\n===FILE:path===\ncode\n` ==========
        // ```sql
        // ===FILE:back/sql/tqy_stock_in_item.sql===
        // DROP TABLE IF EXISTS `tqy_stock_in_item`;
        // ```
        Pattern.compile(
            "(?s)" +
            "```(\\w*)\\s*" +
            "===FILE[：:]\\s*(.+?)\\s*===\\s*" +
            "(.+?)" +
            "```\\s*",
            Pattern.DOTALL
        ),
        // ========== 规则2：===FILE:path===```lang\ncode\n`（无空格） ==========
        // ===FILE:src/main/java/User.java===```java
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "===FILE[：:]\\s*(.+?)===\\s*" +
            "```(\\w*)\\s*" +
            "(.+?)" +
            "```",
            Pattern.DOTALL
        ),
        // ========== 规则3：===FILE:path===```\ncode\n`（无语言标识） ==========
        // ===FILE:src/main/java/User.java===
        // ```
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "===FILE[：:]\\s*(.+?)===\\s*" +
            "```\\s*" +
            "(.+?)" +
            "```",
            Pattern.DOTALL
        ),
        // ========== 规则4：===FILE:path===```lang\ncode\n```===END=== ==========
        // ===FILE:src/main/java/User.java===```java
        // public class User {}
        // ```===END===
        Pattern.compile(
            "(?s)" +
            "===FILE[：:]\\s*(.+?)===\\s*" +
            "```\\s*(.+?)```\\s*===END===",
            Pattern.DOTALL
        ),
        // ========== 规则5：===FILE:path```\ncode\n`（缺少中间===） ==========
        // ===FILE:src/main/java/User.java```
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "===FILE[：:]\\s*" +
            "([^=]+)" +        // 捕获路径（排除=号，避免捕获到===）
            "(?<!===)" +       // 负向后查找：路径末尾不能是===
            "\\s*" +
            "```\\s*" +
            "(.+?)" +
            "```",
            Pattern.DOTALL
        ),
        // ========== 规则6：===FILE:path===\n`\ncode\n` ==========
        // ===FILE:src/main/java/User.java===
        // ```
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "===FILE[：:]\\s*(.+?)\\s*" +
            "```" +
            "(.+?)" +
            "```",
            Pattern.DOTALL
        ),
        // ========== 规则7：FILE:path\n`\ncode\n`（无===包裹路径） ==========
        // FILE:src/main/java/User.java
        // ```
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "(?:===)?FILE[：:]\\s*(.+?)(?:===)?\\s*" +
            "```\\s*" +
            "(.+?)" +
            "```",
            Pattern.DOTALL
        ),
        // ========== 规则8：===任意内容===\n`\ncode\n`（宽松匹配） ==========
        // ===src/main/java/User.java===
        // ```
        // public class User {}
        // ```
        Pattern.compile(
            "(?s)" +
            "===([^=]+?)===\\s*" +
            "```\\s*" +
            "(.+?)" +
            "```",
            Pattern.DOTALL
        )
    };

    @Async
    public void startProjectAsync(String projectId, String projectName, String taskDoc) {
        try {
            startProject(projectId, projectName, taskDoc);
        } catch (Exception e) {
            log.error("项目开发执行失败, projectId: {}", projectId, e);
            updateProjectStatus(projectId, "FAILED", e.getMessage());
        }
    }

    public void startProject(String projectId, String projectName, String taskDoc) {
        log.info("开始项目开发, projectId: {}, projectName: {}", projectId, projectName);
        
        if (projectId == null || projectId.isEmpty()) {
            throw new IllegalArgumentException("projectId不能为空");
        }
        if (projectName == null || projectName.isEmpty()) {
            throw new IllegalArgumentException("projectName不能为空");
        }
        if (taskDoc == null || taskDoc.isEmpty()) {
            throw new IllegalArgumentException("taskDoc不能为空");
        }

        saveTaskDoc(projectId, taskDoc);

        AiDevelopmentProject project = new AiDevelopmentProject();
        project.setProjectId(projectId);
        project.setProjectName(projectName);
        project.setTaskDoc(taskDoc);
        project.setStatus("PROCESSING");
        project.setCurrentPhase(1);
        project.setProgress(0);
        project.setTotalFiles(0);
        project.setStartedAt(LocalDateTime.now());
        projectRepository.save(project);

        List<PhaseConfig> phaseConfigs = tasksAnalysisService.analyzeAndGetPhaseConfigs(taskDoc);

        log.info("任务书阶段配置解析完成, projectId: {}, phaseConfigs: {}", projectId, phaseConfigs);

        createProjectDirectoryStructure(projectId, phaseConfigs);

        executeAllPhases(projectId, taskDoc, phaseConfigs);

        updateProjectStatus(projectId, "COMPLETED", null);
        log.info("项目开发完成, projectId: {}", projectId);
    }

    private void executeAllPhases(String projectId, String taskDoc, List<PhaseConfig> phaseConfigs) {
        String previousPhaseSummary = null;

        for (PhaseConfig phaseConfig : phaseConfigs) {
            int phase = phaseConfig.getPhase();
            log.info("===== 开始阶段{}: {} =====", phase, phaseConfig.getName());
            
            try {
                previousPhaseSummary = executePhase(projectId, phase, taskDoc, previousPhaseSummary, phaseConfig);
                updateProjectProgress(projectId, phase, phaseConfigs.size());
            } catch (Exception e) {
                log.error("阶段{}执行失败", phase, e);
                updatePhaseStatus(projectId, phase, "FAILED", e.getMessage());
                break;
            }
            
            log.info("===== 阶段{}完成 =====", phase);
        }
    }

    private String executePhase(String projectId, int phase, String taskDoc, String previousPhaseSummary, PhaseConfig phaseConfig) {
        createPhaseDirectory(projectId, phase, phaseConfig);
        
        AiDevelopmentPhase phaseEntity = new AiDevelopmentPhase();
        phaseEntity.setProjectId(projectId);
        phaseEntity.setPhase(phase);
        phaseEntity.setPhaseName(phaseConfig.getName());
        phaseEntity.setStatus("PROCESSING");
        phaseEntity.setSessionId("phase_" + phase + "_" + UUID.randomUUID().toString().substring(0, 8));
        phaseEntity.setStartedAt(LocalDateTime.now());
        phaseRepository.save(phaseEntity);

        String sessionId = phaseEntity.getSessionId();
        List<String> currentFileList = new ArrayList<>();
        int roundNumber = 1;
        int noNewFileCount = 0;

        while (roundNumber <= config.getMaxRoundsPerPhase()) {
            log.info("阶段{} 第{}轮开始", phase, roundNumber);
            
            try {
                RoundResult result = executeRound(projectId, sessionId, roundNumber, phase, phaseConfig, 
                    taskDoc, previousPhaseSummary, currentFileList);
                
                List<String> newFiles = result.files.stream()
                    .map(f -> f.path)
                    .collect(Collectors.toList());
                
                if (newFiles.isEmpty()) {
                    noNewFileCount++;
                } else {
                    noNewFileCount = 0;
                    currentFileList.addAll(newFiles);
                }

                if (shouldStopPhase(result, noNewFileCount, roundNumber)) {
                    log.info("阶段{}结束条件满足，停止继续", phase);
                    break;
                }
                
                roundNumber++;
                
            } catch (Exception e) {
                log.error("阶段{} 第{}轮执行失败", phase, roundNumber, e);
                break;
            }
        }

        phaseEntity.setTotalRounds(roundNumber);
        phaseEntity.setTotalFiles(currentFileList.size());
        phaseEntity.setStatus("COMPLETED");
        phaseEntity.setCompletedAt(LocalDateTime.now());
        phaseRepository.save(phaseEntity);

        return generatePhaseSummary(phase, phaseConfig, currentFileList);
    }

    private RoundResult executeRound(String projectId, String sessionId, int roundNumber, int phase,
            PhaseConfig phaseConfig, String taskDoc, String previousPhaseSummary,
            List<String> currentFileList) throws Exception {
        
        String prompt = roundNumber == 1
            ? AiPromptTemplate.buildCodeGenerationFirstRoundPrompt(
                phase, phaseConfig.getName(), phaseConfig.getTargetFiles(), phaseConfig.getKeywords(), taskDoc, phaseConfig.getPhaseTask())
            : AiPromptTemplate.buildCodeGenerationContinuePrompt(
                phase, phaseConfig.getName(), phaseConfig.getTargetFiles(), phaseConfig.getKeywords(), currentFileList);

        AiDevelopmentRound roundEntity = new AiDevelopmentRound();
        roundEntity.setPhaseId(getPhaseId(phase));
        roundEntity.setRoundNumber(roundNumber);
        roundEntity.setStatus("PROCESSING");
        roundEntity.setInputPrompt(prompt);
        roundEntity.setStartedAt(LocalDateTime.now());
        roundRepository.save(roundEntity);

        String content = callAIWithRetry(sessionId, prompt, config.getMaxTokens());

        RoundResult result = handleTruncationAndSave(projectId, content, roundNumber, phaseConfig);
        
        roundEntity.setOutputContent(result.fullContent);
        roundEntity.setTokensUsed(estimateTokens(result.fullContent));
        roundEntity.setFilesCount(result.files.size());
        roundEntity.setContinuation(result.continuationCount);
        roundEntity.setFinishReason(result.finishReason);
        roundEntity.setStatus("COMPLETED");
        roundEntity.setCompletedAt(LocalDateTime.now());
        roundRepository.save(roundEntity);

        saveSessionRecord(projectId, phaseConfig, roundNumber, prompt, result.fullContent);
        
        return result;
    }

    private String callAIWithRetry(String sessionId, String prompt, int maxTokens) throws Exception {
        int retryCount = 0;
        long baseDelay = config.getBaseDelayMs();
        
        while (retryCount < config.getMaxRetries()) {
            try {
                AiChatRequest request = new AiChatRequest();
                request.setModel(aiProperties.getDefaultModel());
                request.setMaxTokens(maxTokens);
                request.setTemperature(0.7);
                request.setStream(false);
                request.setMessages(new ArrayList<>());
                request.getMessages().add(new com.jiedan.dto.ai.AiMessage("system", "你是一个专业的代码生成助手，请根据任务要求生成代码。"));
                request.getMessages().add(new com.jiedan.dto.ai.AiMessage("user", prompt));
                
                AiChatResponse response = aiProvider.chatCompletion(request);
                
                if (response != null && response.getSuccess() && response.getContent() != null) {
                    return response.getContent();
                }
                
                throw new RuntimeException("AI返回失败: " + (response != null ? response.getErrorMessage() : "未知错误"));
                
            } catch (Exception e) {
                retryCount++;
                if (retryCount >= config.getMaxRetries()) {
                    throw new RuntimeException("AI调用失败，已达最大重试次数: " + e.getMessage(), e);
                }
                
                long delay = Math.min(baseDelay * (1L << retryCount), 512000);
                log.warn("AI调用失败，第{}次重试，延迟{}ms: {}", retryCount, delay, e.getMessage());
                Thread.sleep(delay);
            }
        }
        
        throw new RuntimeException("AI调用失败，未知错误");
    }

    private RoundResult handleTruncationAndSave(String projectId, String content, int roundNumber, PhaseConfig phaseConfig) {
        RoundResult result = new RoundResult();
        result.fullContent = content;
        result.finishReason = "stop";

        if (content != null && !content.isEmpty()) {
            List<ParsedFile> files = parseAndSaveFiles(projectId, content, roundNumber, phaseConfig);
            result.files = files;
            result.continuationCount = 0;

            if (files.isEmpty()) {
                log.warn("阶段{}第{}轮解析文件为0, AI返回内容前1000字符: \n{}",
                    phaseConfig.getPhase(), roundNumber, content.substring(0, Math.min(1000, content.length())));
            }

            updateProgressDocument(projectId, phaseConfig, roundNumber, files);
        }

        return result;
    }

    private List<ParsedFile> parseAndSaveFiles(String projectId, String content, int roundNumber, PhaseConfig phaseConfig) {
        List<ParsedFile> files = new ArrayList<>();

        log.info("阶段{}第{}轮开始解析文件, 内容长度: {}, 正则匹配前100字符: {}",
            phaseConfig.getPhase(), roundNumber, content.length(),
            content.substring(0, Math.min(100, content.length())));

        Path baseDir = Paths.get(PROJECTS_BASE_DIR, projectId, "phases",
            phaseConfig.getPhaseDirName(), "generated");
        
        try {
            Files.createDirectories(baseDir);
        } catch (IOException e) {
            log.error("创建目录失败: {}", baseDir, e);
            return files;
        }
        
        int matchCount = 0;
        boolean matched = false;
        boolean debugSaved = false;

        for (int patternIndex = 0; patternIndex < FILE_PATTERNS.length; patternIndex++) {
            Pattern pattern = FILE_PATTERNS[patternIndex];
            Matcher matcher = pattern.matcher(content);

            log.info("使用第{}套正则规则尝试匹配: {}", patternIndex + 1, pattern.pattern().substring(0, Math.min(50, pattern.pattern().length())));

            while (matcher.find()) {
                matched = true;
                matchCount++;

                // 规则1：```lang\n===FILE:path===\ncode\n` 格式，group顺序不同
                // group(1)=语言, group(2)=文件路径, group(3)=代码内容
                String filePath;
                String lang;
                if (patternIndex == 1) {
                    filePath = matcher.group(2).trim();
                    lang = matcher.group(1).trim();
                } else {
                    filePath = matcher.group(1).trim();
                    lang = matcher.groupCount() > 2 ? matcher.group(2) : null;
                }
                String fileContent = matcher.group(matcher.groupCount()).trim();

                if (filePath.isEmpty() || fileContent.isEmpty()) {
                    continue;
                }

                log.info("匹配到文件: {}, 语言: {}, 内容长度: {}, 使用规则: {}",
                    filePath, lang != null ? lang : "null", fileContent.length(), patternIndex + 1);

                if (isPathTraversal(filePath, baseDir)) {
                    log.warn("非法路径，跳过: {}", filePath);
                    continue;
                }

                try {
                    Path fullPath = baseDir.resolve(filePath);
                    Files.createDirectories(fullPath.getParent());
                    Files.writeString(fullPath, fileContent);

                    long fileSize = fileContent.getBytes().length;
                    String fileType = guessFileType(filePath);
                    
                    ParsedFile parsedFile = new ParsedFile();
                    parsedFile.path = filePath;
                    parsedFile.size = fileSize;
                    parsedFile.type = fileType;
                    parsedFile.complete = true;
                    files.add(parsedFile);
                    
                    saveFileRecord(projectId, phaseConfig.getPhase(), roundNumber, filePath, fileSize, fileType, true);
                    
                    log.info("保存文件成功: {}", filePath);
                    
                } catch (IOException e) {
                    log.error("保存文件失败: {}", filePath, e);
                }
            }
            
            if (matched && matchCount > 0) {
                log.info("第{}套正则规则成功匹配到{}个文件，停止尝试其他规则", patternIndex + 1, matchCount);
                break;
            }

            // 规则0（第一套正则）匹配完成后，如果没匹配到任何文件，保存debug文件
            if (patternIndex == 0 && matchCount == 0) {
                log.warn("第1套正则规则未能匹配到任何文件，保存原始内容供调试");
                try {
                    Path debugDir = Paths.get(PROJECTS_BASE_DIR, projectId, "phases",
                        phaseConfig.getPhaseDirName(), "debug");
                    Files.createDirectories(debugDir);
                    Path debugFile = debugDir.resolve("round_" + roundNumber + "_raw.md");
                    Files.writeString(debugFile, content);
                    log.info("原始内容已保存到: {}", debugFile);
                    debugSaved = true;
                } catch (IOException e) {
                    log.error("保存调试文件失败", e);
                }
            }
        }

        if (!debugSaved && (!matched || matchCount == 0)) {
            log.warn("所有正则规则都未能匹配到文件，保存原始内容供调试");
            try {
                Path debugDir = Paths.get(PROJECTS_BASE_DIR, projectId, "phases",
                    phaseConfig.getPhaseDirName(), "debug");
                Files.createDirectories(debugDir);
                Path debugFile = debugDir.resolve("round_" + roundNumber + "_raw.md");
                Files.writeString(debugFile, content);
                log.info("原始内容已保存到: {}", debugFile);
            } catch (IOException e) {
                log.error("保存调试文件失败", e);
            }
        }
        
        log.info("阶段{}第{}轮解析完成, 总匹配次数: {}, 成功解析文件数: {}",
            phaseConfig.getPhase(), roundNumber, matchCount, files.size());
        
        return files;
    }

    private boolean isPathTraversal(String filePath, Path baseDir) {
        try {
            Path resolved = baseDir.resolve(filePath).normalize();
            return !resolved.startsWith(baseDir.normalize());
        } catch (Exception e) {
            return true;
        }
    }

    private String guessFileType(String filePath) {
        String lower = filePath.toLowerCase();
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".vue")) return "vue";
        if (lower.endsWith(".js")) return "js";
        if (lower.endsWith(".ts")) return "ts";
        if (lower.endsWith(".sql")) return "sql";
        if (lower.endsWith(".xml")) return "xml";
        if (lower.endsWith(".yml") || lower.endsWith(".yaml")) return "yaml";
        if (lower.endsWith(".properties")) return "properties";
        if (lower.endsWith(".md")) return "md";
        return "other";
    }

    private void saveFileRecord(String projectId, int phase, int roundNumber, 
            String filePath, long fileSize, String fileType, boolean isComplete) {
        AiDevelopmentFile file = new AiDevelopmentFile();
        file.setProjectId(projectId);
        file.setPhase(phase);
        file.setRoundNumber(roundNumber);
        file.setFilePath(filePath);
        file.setFileSize(fileSize);
        file.setFileType(fileType);
        file.setIsComplete(isComplete);
        fileRepository.save(file);
    }

    private void updateProgressDocument(String projectId, PhaseConfig phaseConfig, int roundNumber, List<ParsedFile> files) {
        try {
            Path progressPath = Paths.get(PROJECTS_BASE_DIR, projectId, "phases",
                phaseConfig.getPhaseDirName(), "progress.md");
            
            StringBuilder sb = new StringBuilder();
            if (Files.exists(progressPath)) {
                sb.append(Files.readString(progressPath)).append("\n\n");
            }
            
            sb.append("## 轮次 ").append(roundNumber).append(" - ")
              .append(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)).append("\n\n");
            sb.append("### 本轮生成 (").append(files.size()).append("个文件)\n");
            for (ParsedFile f : files) {
                sb.append("- ").append(f.path).append(" (").append(f.size).append(" bytes)\n");
            }
            
            Files.writeString(progressPath, sb.toString());
            
        } catch (IOException e) {
            log.error("更新进度文档失败", e);
        }
    }

    private void saveSessionRecord(String projectId, PhaseConfig phaseConfig, int roundNumber, String input, String output) {
        try {
            Path sessionDir = Paths.get(PROJECTS_BASE_DIR, projectId, "phases",
                phaseConfig.getPhaseDirName(), "sessions");
            Files.createDirectories(sessionDir);
            
            Files.writeString(sessionDir.resolve("round_" + roundNumber + "_input.md"), input);
            Files.writeString(sessionDir.resolve("round_" + roundNumber + "_output.md"), output);
            
        } catch (IOException e) {
            log.error("保存会话记录失败", e);
        }
    }

    private boolean shouldStopPhase(RoundResult result, int noNewFileCount, int roundNumber) {
        if (noNewFileCount >= config.getNoNewFileRoundsThreshold()) {
            log.info("连续{}轮无新文件，强制结束阶段", noNewFileCount);
            return true;
        }
        
        if (roundNumber >= config.getMaxRoundsPerPhase()) {
            log.info("达到最大轮次数{}，强制结束阶段", config.getMaxRoundsPerPhase());
            return true;
        }
        
        if (result.finishReason != null && result.finishReason.equals("stop") && result.files.isEmpty()) {
            return true;
        }
        
        return false;
    }

    private String generatePhaseSummary(int phase, PhaseConfig phaseConfig, List<String> fileList) {
        StringBuilder sb = new StringBuilder();
        sb.append("# 阶段").append(phase).append(" 开发摘要\n\n");
        sb.append("## 阶段信息\n");
        sb.append("- 阶段名称：").append(phaseConfig.getName()).append("\n");
        sb.append("- 关键词：").append(String.join(", ", phaseConfig.getKeywords())).append("\n\n");
        sb.append("## 生成文件统计\n");
        sb.append("- 总文件数：").append(fileList.size()).append("\n\n");
        sb.append("## 文件清单\n");
        for (String file : fileList) {
            sb.append("- ").append(file).append("\n");
        }
        return sb.toString();
    }

    private void createProjectDirectoryStructure(String projectId, List<PhaseConfig> phaseConfigs) {
        try {
            Path projectDir = Paths.get(PROJECTS_BASE_DIR, projectId);
            Files.createDirectories(projectDir);
            Files.createDirectories(projectDir.resolve("phases"));
            Files.createDirectories(projectDir.resolve("logs"));

            for (PhaseConfig pc : phaseConfigs) {
                Path phaseDir = projectDir.resolve("phases").resolve(pc.getPhaseDirName());
                Files.createDirectories(phaseDir.resolve("sessions"));
                Files.createDirectories(phaseDir.resolve("generated"));
            }

            log.info("项目目录结构创建成功: {}", projectDir);

        } catch (IOException e) {
            throw new RuntimeException("创建项目目录结构失败", e);
        }
    }

    private void createPhaseDirectory(String projectId, int phase, PhaseConfig phaseConfig) {
        try {
            Path phaseDir = Paths.get(PROJECTS_BASE_DIR, projectId, "phases", phaseConfig.getPhaseDirName());
            Files.createDirectories(phaseDir.resolve("sessions"));
            Files.createDirectories(phaseDir.resolve("generated"));
        } catch (IOException e) {
            throw new RuntimeException("创建阶段目录失败", e);
        }
    }

    private void saveTaskDoc(String projectId, String taskDoc) {
        try {
            Path taskDocPath = Paths.get(PROJECTS_BASE_DIR, projectId, "TASKS.md");
            Files.writeString(taskDocPath, taskDoc);
        } catch (IOException e) {
            throw new RuntimeException("保存任务书失败", e);
        }
    }

    private void updateProjectStatus(String projectId, String status, String errorMessage) {
        projectRepository.findByProjectId(projectId).ifPresent(project -> {
            project.setStatus(status);
            if (errorMessage != null) {
                project.setErrorMessage(errorMessage);
            }
            if ("COMPLETED".equals(status) || "FAILED".equals(status)) {
                project.setCompletedAt(LocalDateTime.now());
            }
            projectRepository.save(project);
        });
    }

    private void updatePhaseStatus(String projectId, int phase, String status, String errorMessage) {
        phaseRepository.findByProjectIdAndPhase(projectId, phase).ifPresent(phaseEntity -> {
            phaseEntity.setStatus(status);
            if (errorMessage != null) {
                phaseEntity.setErrorMessage(errorMessage);
            }
            phaseRepository.save(phaseEntity);
        });
    }

    private void updateProjectProgress(String projectId, int currentPhase, int totalPhases) {
        projectRepository.findByProjectId(projectId).ifPresent(project -> {
            project.setCurrentPhase(currentPhase);
            project.setProgress((currentPhase * 100) / totalPhases);
            projectRepository.save(project);
        });
    }

    private Long getPhaseId(int phase) {
        return (long) phase;
    }

    private int estimateTokens(String content) {
        return content != null ? content.length() / 4 : 0;
    }

    private static class RoundResult {
        String fullContent;
        String finishReason;
        List<ParsedFile> files = new ArrayList<>();
        int continuationCount;
    }

    private static class ParsedFile {
        String path;
        long size;
        String type;
        boolean complete;
    }
}
