package com.jiedan.service.ai.feedback;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.AiMessage;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateRequest;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateResponse;
import com.jiedan.dto.ai.feedback.ValidationDecision;
import com.jiedan.entity.AiValidationRecord;
import com.jiedan.repository.AiValidationRecordRepository;
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
import java.util.List;
import java.util.UUID;

/**
 * Feedback Shadow服务
 * 负责调用AI模型进行质量检测
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackShadowService {

    private final AiStrategyFactory strategyFactory;
    private final FeedbackShadowPromptBuilder promptBuilder;
    private final DocumentParser documentParser;
    private final AiValidationRecordRepository validationRecordRepository;

    /**
     * 验证文档质量（调用AI模型）
     */
    public FeedbackShadowValidateResponse validateWithAI(FeedbackShadowValidateRequest request) {
        long startTime = System.currentTimeMillis();
        String requestId = request.getRequestId() != null ? 
                request.getRequestId() : UUID.randomUUID().toString();

        log.info("开始Feedback Shadow验证, requestId: {}, apiType: {}, projectId: {}", 
                requestId, request.getApiType(), request.getProjectId());

        try {
            // 1. 获取文档内容
            String documentContent = getDocumentContent(request);
            if (documentContent == null || documentContent.isEmpty()) {
                return buildErrorResponse(requestId, "文档内容为空");
            }

            // 2. 构建检测Prompt
            String prompt = promptBuilder.buildDetectionPrompt(
                    request.getApiType(), 
                    documentContent
            );

            // 3. 调用AI模型
            AIProviderStrategy strategy = strategyFactory.getStrategy(null);
            AiChatRequest chatRequest = AiChatRequest.builder()
                    .model(null) // 使用默认模型
                    .temperature(0.3) // 检测任务使用较低temperature
                    .maxTokens(1500)
                    .build()
                    .addSystemMessage(prompt);

            AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);

            if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
                log.error("Feedback Shadow AI调用失败: {}", chatResponse.getErrorMessage());
                return buildErrorResponse(requestId, "AI检测失败: " + chatResponse.getErrorMessage());
            }

            // 4. 解析检测报告
            String reportContent = chatResponse.getContent();
            DocumentParser.FeedbackShadowReport report = documentParser.parseFeedbackReport(reportContent);

            // 5. 保存检测报告
            String reportPath = saveReport(request.getProjectId(), requestId, reportContent);

            // 6. 构建响应
            long responseTime = System.currentTimeMillis() - startTime;

            // 7. 保存验证记录到数据库
            saveValidationRecord(request, report, chatResponse, responseTime);

            return FeedbackShadowValidateResponse.builder()
                    .success(true)
                    .requestId(requestId)
                    .reportPath(reportPath)
                    .reportContent(reportContent)
                    .decision(report.getDecision())
                    .issues(report.getIssues())
                    .repairSuggestion(report.getRepairSuggestion())
                    .usage(chatResponse.getUsage())
                    .responseTimeMs(responseTime)
                    .build();

        } catch (Exception e) {
            log.error("Feedback Shadow验证异常, requestId: {}", requestId, e);
            return buildErrorResponse(requestId, "验证异常: " + e.getMessage());
        }
    }

    /**
     * 调度修复
     */
    public FeedbackShadowValidateResponse scheduleRepair(
            String projectId,
            String apiType,
            String originalDocument,
            String feedbackReport) {
        
        log.info("调度修复, projectId: {}, apiType: {}", projectId, apiType);

        // 构建修复Prompt
        String repairPrompt = promptBuilder.buildRepairPrompt(
                apiType,
                originalDocument,
                feedbackReport
        );

        // 调用AI修复
        AIProviderStrategy strategy = strategyFactory.getStrategy(null);
        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(null)
                .temperature(0.3)
                .maxTokens(3000)
                .build()
                .addSystemMessage(repairPrompt);

        AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);

        if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
            log.error("修复调度失败: {}", chatResponse.getErrorMessage());
            return buildErrorResponse(null, "修复失败: " + chatResponse.getErrorMessage());
        }

        // 修复后的文档需要再次验证
        FeedbackShadowValidateRequest validateRequest = FeedbackShadowValidateRequest.builder()
                .projectId(projectId)
                .apiType(apiType)
                .documentContent(chatResponse.getContent())
                .previousFeedback(feedbackReport)
                .build();

        return validateWithAI(validateRequest);
    }

    /**
     * 检查质量门禁
     * 【放宽】允许REPAIR和ALLOW都通过，只有REJECT才失败
     */
    public boolean checkQualityGate(ValidationDecision decision, String apiType) {
        // 放宽质量门禁：ALLOW和REPAIR都视为通过，只有REJECT才失败
        return decision == ValidationDecision.ALLOW || decision == ValidationDecision.REPAIR;
    }

    /**
     * 获取文档内容
     */
    private String getDocumentContent(FeedbackShadowValidateRequest request) {
        // 如果直接传了内容，直接使用
        if (request.getDocumentContent() != null && !request.getDocumentContent().isEmpty()) {
            return request.getDocumentContent();
        }

        // 否则从文件读取
        if (request.getDocumentPath() != null && !request.getDocumentPath().isEmpty()) {
            try {
                Path path = Paths.get(request.getDocumentPath());
                return Files.readString(path);
            } catch (IOException e) {
                log.error("读取文档失败: {}", request.getDocumentPath(), e);
                return null;
            }
        }

        return null;
    }

    /**
     * 保存检测报告
     */
    private String saveReport(String projectId, String requestId, String reportContent) {
        try {
            // 创建目录
            String dirPath = String.format("projects/%s/feedback", projectId);
            Path dir = Paths.get(dirPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            // 生成文件名
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
            String fileName = String.format("FB-%s-%s.md", timestamp, requestId.substring(0, 8));
            Path filePath = dir.resolve(fileName);

            // 写入文件
            Files.writeString(filePath, reportContent);

            log.info("检测报告已保存: {}", filePath);
            return filePath.toString();

        } catch (IOException e) {
            log.error("保存检测报告失败", e);
            return null;
        }
    }

    /**
     * 构建错误响应
     */
    private FeedbackShadowValidateResponse buildErrorResponse(String requestId, String errorMessage) {
        return FeedbackShadowValidateResponse.builder()
                .success(false)
                .requestId(requestId)
                .errorMessage(errorMessage)
                .decision(ValidationDecision.REJECT)
                .issues(List.of(errorMessage))
                .build();
    }

    /**
     * 保存验证记录到数据库
     */
    private void saveValidationRecord(FeedbackShadowValidateRequest request,
                                       DocumentParser.FeedbackShadowReport report,
                                       AiChatResponse chatResponse,
                                       long responseTime) {
        try {
            AiValidationRecord record = AiValidationRecord.builder()
                    .projectId(request.getProjectId())
                    .taskId(request.getTaskId())
                    .validationType("DOCUMENT")
                    .documentType(request.getApiType())
                    .decision(report.getDecision() != null ? report.getDecision().name() : "UNKNOWN")
                    .issues(report.getIssues() != null ? String.join(",", report.getIssues()) : null)
                    .suggestions(report.getRepairSuggestion())
                    .tokenUsage(chatResponse.getUsage() != null ? chatResponse.getUsage().getTotalTokens() : null)
                    .responseTimeMs(responseTime)
                    .build();

            validationRecordRepository.save(record);
            log.info("验证记录已保存, projectId: {}, decision: {}",
                    request.getProjectId(), record.getDecision());

        } catch (Exception e) {
            log.error("保存验证记录失败", e);
        }
    }
}
