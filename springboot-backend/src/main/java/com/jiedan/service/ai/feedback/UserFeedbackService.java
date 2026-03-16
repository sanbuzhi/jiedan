package com.jiedan.service.ai.feedback;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.code.GeneratedFile;
import com.jiedan.dto.ai.feedback.UserFeedbackRequest;
import com.jiedan.dto.ai.feedback.UserFeedbackResponse;
import com.jiedan.entity.UserFeedback;
import com.jiedan.repository.UserFeedbackRepository;
import com.jiedan.service.ai.AIProviderStrategy;
import com.jiedan.service.ai.AiStrategyFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 用户反馈处理服务
 * 处理用户反馈，触发AI修复流程
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFeedbackService {

    private final UserFeedbackRepository userFeedbackRepository;
    private final AiStrategyFactory strategyFactory;
    private final ObjectMapper objectMapper;

    // 最大修复尝试次数
    private static final int MAX_REPAIR_ATTEMPTS = 3;

    /**
     * 提交用户反馈
     */
    public UserFeedbackResponse submitFeedback(UserFeedbackRequest request) {
        log.info("提交用户反馈, projectId: {}, taskId: {}", request.getProjectId(), request.getTaskId());

        try {
            // 1. 生成反馈ID
            String feedbackId = generateFeedbackId();

            // 2. 保存反馈到数据库
            UserFeedback feedback = UserFeedback.builder()
                    .feedbackId(feedbackId)
                    .projectId(request.getProjectId())
                    .taskId(request.getTaskId())
                    .feedbackType(request.getFeedbackType())
                    .description(request.getDescription())
                    .affectedFiles(toJson(request.getAffectedFiles()))
                    .expectedFix(request.getExpectedFix())
                    .severity(request.getSeverity())
                    .source(request.getSource())
                    .status("PENDING")
                    .repairAttempts(0)
                    .build();

            userFeedbackRepository.save(feedback);

            log.info("用户反馈已保存, feedbackId: {}", feedbackId);

            // 3. 立即触发修复流程（异步）
            // 实际项目中可以使用Spring的@Async或消息队列
            processFeedbackAsync(feedbackId);

            return UserFeedbackResponse.success(feedbackId, "PENDING");

        } catch (Exception e) {
            log.error("提交用户反馈失败", e);
            return UserFeedbackResponse.fail("提交反馈失败: " + e.getMessage());
        }
    }

    /**
     * 处理用户反馈（异步）
     */
    public void processFeedbackAsync(String feedbackId) {
        // 在实际项目中，这里应该使用@Async或发送到消息队列
        // 这里为了简化，直接同步处理
        new Thread(() -> processFeedback(feedbackId)).start();
    }

    /**
     * 处理用户反馈
     * 触发AI修复流程
     */
    public void processFeedback(String feedbackId) {
        log.info("开始处理用户反馈, feedbackId: {}", feedbackId);

        UserFeedback feedback = userFeedbackRepository.findById(feedbackId)
                .orElse(null);

        if (feedback == null) {
            log.error("反馈不存在: {}", feedbackId);
            return;
        }

        // 更新状态为处理中
        feedback.setStatus("PROCESSING");
        userFeedbackRepository.save(feedback);

        int attempts = 0;
        boolean success = false;

        while (attempts < MAX_REPAIR_ATTEMPTS && !success) {
            attempts++;
            feedback.setRepairAttempts(attempts);
            userFeedbackRepository.save(feedback);

            try {
                success = attemptRepair(feedback);
            } catch (Exception e) {
                log.error("修复尝试 {} 失败, feedbackId: {}", attempts, feedbackId, e);
            }
        }

        // 更新最终状态
        if (success) {
            feedback.setStatus("COMPLETED");
            feedback.setRepairResult("修复成功");
        } else {
            feedback.setStatus("FAILED");
            feedback.setRepairResult("超过最大尝试次数，修复失败");
        }

        userFeedbackRepository.save(feedback);
        log.info("用户反馈处理完成, feedbackId: {}, status: {}", feedbackId, feedback.getStatus());
    }

    /**
     * 尝试修复
     */
    private boolean attemptRepair(UserFeedback feedback) {
        log.info("尝试修复, feedbackId: {}, 第 {} 次", feedback.getFeedbackId(), feedback.getRepairAttempts());

        try {
            // 1. 读取受影响的文件
            List<String> affectedFiles = fromJson(feedback.getAffectedFiles(), new TypeReference<List<String>>() {});
            if (affectedFiles == null || affectedFiles.isEmpty()) {
                log.warn("没有指定受影响的文件, feedbackId: {}", feedback.getFeedbackId());
                return false;
            }

            // 2. 读取文件内容
            String projectPath = "projects/" + feedback.getProjectId();
            List<GeneratedFile> filesToRepair = readFiles(projectPath, affectedFiles);

            if (filesToRepair.isEmpty()) {
                log.warn("无法读取受影响的文件, feedbackId: {}", feedback.getFeedbackId());
                return false;
            }

            // 3. 构建修复Prompt
            String prompt = buildRepairPrompt(feedback, filesToRepair);

            // 4. 调用AI修复
            List<GeneratedFile> repairedFiles = callAIForRepair(prompt);

            if (repairedFiles.isEmpty()) {
                log.warn("AI未返回修复后的文件, feedbackId: {}", feedback.getFeedbackId());
                return false;
            }

            // 5. 保存修复后的文件
            saveRepairedFiles(projectPath, repairedFiles);

            // 6. 更新反馈记录
            feedback.setRepairedFiles(toJson(repairedFiles.stream().map(GeneratedFile::getPath).toList()));

            log.info("修复尝试成功, feedbackId: {}", feedback.getFeedbackId());
            return true;

        } catch (Exception e) {
            log.error("修复尝试异常, feedbackId: {}", feedback.getFeedbackId(), e);
            return false;
        }
    }

    /**
     * 构建修复Prompt
     */
    private String buildRepairPrompt(UserFeedback feedback, List<GeneratedFile> filesToRepair) {
        StringBuilder prompt = new StringBuilder();

        // 1. 角色定义
        prompt.append("【角色】\n");
        prompt.append("你是一位资深开发工程师，负责修复代码中的问题。\n\n");

        // 2. 问题描述
        prompt.append("【问题描述】\n");
        prompt.append("反馈类型: ").append(feedback.getFeedbackType()).append("\n");
        prompt.append("严重程度: ").append(feedback.getSeverity()).append("\n");
        prompt.append("问题描述: ").append(feedback.getDescription()).append("\n\n");

        // 3. 期望的修复方式
        if (feedback.getExpectedFix() != null && !feedback.getExpectedFix().isEmpty()) {
            prompt.append("【期望的修复方式】\n");
            prompt.append(feedback.getExpectedFix()).append("\n\n");
        }

        // 4. 需要修复的文件
        prompt.append("【需要修复的文件】\n");
        for (GeneratedFile file : filesToRepair) {
            prompt.append("### ").append(file.getPath()).append("\n");
            prompt.append("```").append(file.getLanguage()).append("\n");
            prompt.append(file.getContent()).append("\n");
            prompt.append("```\n\n");
        }

        // 5. 修复要求
        prompt.append("【修复要求】\n");
        prompt.append("1. 只修复问题描述中提到的问题，不要修改其他功能\n");
        prompt.append("2. 保持代码风格与原有代码一致\n");
        prompt.append("3. 确保修复后的代码能编译通过\n");
        prompt.append("4. 添加必要的注释说明修改内容\n\n");

        // 6. 输出格式
        prompt.append("【输出格式】\n");
        prompt.append("请按以下格式输出修复后的完整文件：\n\n");
        prompt.append("```\n");
        prompt.append("## 修复说明\n");
        prompt.append("{简要说明修复内容}\n\n");
        prompt.append("## 修复后的文件\n\n");
        prompt.append("### {文件路径}\n");
        prompt.append("```{语言}\n");
        prompt.append("{修复后的代码内容}\n");
        prompt.append("```\n");
        prompt.append("```\n\n");

        prompt.append("请输出完整的修复后文件。");

        return prompt.toString();
    }

    /**
     * 调用AI进行修复
     */
    private List<GeneratedFile> callAIForRepair(String prompt) {
        try {
            AIProviderStrategy strategy = strategyFactory.getStrategy(null);

            AiChatRequest chatRequest = AiChatRequest.builder()
                    .model(null)
                    .temperature(0.2)
                    .maxTokens(4000)
                    .build()
                    .addSystemMessage("你是一位资深开发工程师，负责修复代码中的问题。")
                    .addUserMessage(prompt);

            AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);

            if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
                log.error("AI修复调用失败: {}", chatResponse.getErrorMessage());
                return List.of();
            }

            // 解析修复后的文件
            return parseRepairedFiles(chatResponse.getContent());

        } catch (Exception e) {
            log.error("AI修复调用异常", e);
            return List.of();
        }
    }

    /**
     * 解析修复后的文件
     */
    private List<GeneratedFile> parseRepairedFiles(String content) {
        List<GeneratedFile> files = new ArrayList<>();

        // 按文件分割
        String[] sections = content.split("### ");

        for (int i = 1; i < sections.length; i++) {
            String section = sections[i];

            // 提取文件路径
            int pathEnd = section.indexOf("\n");
            if (pathEnd == -1) continue;

            String filePath = section.substring(0, pathEnd).trim();

            // 提取代码内容
            String codeContent = extractCodeContent(section);

            // 判断语言
            String language = detectLanguage(filePath);

            GeneratedFile file = GeneratedFile.builder()
                    .path(filePath)
                    .content(codeContent)
                    .language(language)
                    .build();

            files.add(file);
        }

        return files;
    }

    /**
     * 提取代码内容
     */
    private String extractCodeContent(String section) {
        int start = section.indexOf("```");
        if (start == -1) return "";

        start = section.indexOf("\n", start);
        if (start == -1) return "";

        int end = section.indexOf("```", start + 1);
        if (end == -1) return "";

        return section.substring(start + 1, end).trim();
    }

    /**
     * 读取文件
     */
    private List<GeneratedFile> readFiles(String projectPath, List<String> filePaths) {
        List<GeneratedFile> files = new ArrayList<>();

        for (String filePath : filePaths) {
            Path fullPath = Paths.get(projectPath, filePath);
            try {
                if (Files.exists(fullPath)) {
                    String content = Files.readString(fullPath);
                    files.add(GeneratedFile.builder()
                            .path(filePath)
                            .content(content)
                            .language(detectLanguage(filePath))
                            .build());
                }
            } catch (IOException e) {
                log.error("读取文件失败: {}", fullPath, e);
            }
        }

        return files;
    }

    /**
     * 保存修复后的文件
     */
    private void saveRepairedFiles(String projectPath, List<GeneratedFile> files) throws IOException {
        for (GeneratedFile file : files) {
            Path filePath = Paths.get(projectPath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
            log.info("保存修复后的文件: {}", filePath);
        }
    }

    /**
     * 检测编程语言
     */
    private String detectLanguage(String filePath) {
        if (filePath.endsWith(".java")) return "java";
        if (filePath.endsWith(".js")) return "javascript";
        if (filePath.endsWith(".ts")) return "typescript";
        if (filePath.endsWith(".py")) return "python";
        if (filePath.endsWith(".html")) return "html";
        if (filePath.endsWith(".css")) return "css";
        if (filePath.endsWith(".vue")) return "vue";
        return "text";
    }

    /**
     * 生成反馈ID
     */
    private String generateFeedbackId() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = UUID.randomUUID().toString().substring(0, 8);
        return "FB" + timestamp + random;
    }

    /**
     * 转换为JSON
     */
    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            log.error("JSON转换失败", e);
            return "[]";
        }
    }

    /**
     * 从JSON解析
     */
    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (Exception e) {
            log.error("JSON解析失败", e);
            return null;
        }
    }

    /**
     * 获取项目下的所有反馈
     */
    public List<UserFeedback> getProjectFeedbacks(String projectId) {
        return userFeedbackRepository.findByProjectId(projectId);
    }

    /**
     * 获取反馈详情
     */
    public UserFeedback getFeedback(String feedbackId) {
        return userFeedbackRepository.findById(feedbackId).orElse(null);
    }
}
