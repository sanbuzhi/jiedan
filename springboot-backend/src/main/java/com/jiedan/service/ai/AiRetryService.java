package com.jiedan.service.ai;

import com.jiedan.dto.ai.feedback.FeedbackShadowValidateRequest;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateResponse;
import com.jiedan.dto.ai.feedback.ValidationDecision;
import com.jiedan.service.ai.feedback.FeedbackShadowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * AI重试服务
 * 封装AI调用的重试逻辑，集成Feedback Shadow验证
 * 【核心功能】重试时携带Feedback Shadow的反馈建议，让AI针对性修正
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRetryService {

    private final FeedbackShadowService feedbackShadowService;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;

    /**
     * 执行AI任务并自动重试
     * 【重试机制】每次重试都会携带之前验证失败的问题列表，让AI针对性修正
     *
     * @param taskFunction AI任务函数，接收之前的问题列表，返回AI结果
     * @param projectId 项目ID
     * @param apiType API类型
     * @param <T> 返回类型
     * @return AI任务结果
     */
    public <T extends AiResult> T executeWithRetry(Function<List<String>, T> taskFunction,
                                                    String projectId, String apiType) {
        int attempts = 0;
        Exception lastException = null;
        T lastResult = null;
        List<String> previousIssues = new ArrayList<>(); // 记录之前的问题

        while (attempts < MAX_RETRIES) {
            attempts++;
            log.info("执行AI任务: apiType={}, projectId={}, 尝试次数: {}/{}",
                    apiType, projectId, attempts, MAX_RETRIES);

            try {
                // 【关键】执行AI任务，传入之前的问题列表（首次为空）
                T result = taskFunction.apply(previousIssues);
                lastResult = result;

                // 获取AI生成的内容
                String content = extractContent(result);

                // Feedback Shadow验证
                FeedbackShadowValidateRequest validateRequest = FeedbackShadowValidateRequest.builder()
                        .projectId(projectId)
                        .apiType(apiType)
                        .documentContent(content)
                        .build();

                FeedbackShadowValidateResponse validationResult = feedbackShadowService.validateWithAI(validateRequest);
                log.info("Feedback Shadow验证结果: projectId={}, decision={}, issues={}",
                        projectId, validationResult.getDecision(), validationResult.getIssues());

                // 根据验证结果处理
                switch (validationResult.getDecision()) {
                    case ALLOW:
                        // 验证通过，设置成功标志并返回
                        result.setSuccess(true);
                        result.setValidationDecision(ValidationDecision.ALLOW.name());
                        result.setValidationIssues(validationResult.getIssues());
                        log.info("AI任务验证通过: apiType={}, projectId={}, 尝试次数: {}/{}",
                                apiType, projectId, attempts, MAX_RETRIES);
                        return result;

                    case REPAIR:
                    case REJECT:
                        // 验证失败，记录问题用于下次重试
                        if (validationResult.getIssues() != null && !validationResult.getIssues().isEmpty()) {
                            previousIssues = validationResult.getIssues();
                            log.warn("AI任务验证失败，记录问题用于重试: apiType={}, projectId={}, issues={}",
                                    apiType, projectId, previousIssues);
                        }
                        result.setSuccess(false);
                        result.setErrorMessage(validationResult.getIssues() != null ?
                                String.join(", ", validationResult.getIssues()) : "验证失败");
                        result.setValidationDecision(validationResult.getDecision().name());
                        result.setValidationIssues(validationResult.getIssues());
                        lastResult = result;
                        break;
                }

            } catch (Exception e) {
                lastException = e;
                log.error("AI任务执行异常: apiType={}, projectId={}, 尝试次数: {}/{}, 错误: {}",
                        apiType, projectId, attempts, MAX_RETRIES, e.getMessage(), e);
            }

            // 如果不是最后一次尝试，等待后重试
            if (attempts < MAX_RETRIES) {
                long delay = INITIAL_RETRY_DELAY_MS * attempts; // 指数退避
                log.info("AI任务将在 {}ms 后重试，携带 {} 个问题...", delay, previousIssues.size());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        // 达到最大重试次数
        log.error("AI任务达到最大重试次数: apiType={}, projectId={}, 最大重试次数: {}, 最终问题: {}",
                apiType, projectId, MAX_RETRIES, previousIssues);

        if (lastResult != null) {
            // 返回最后一次的结果（标记为失败）
            lastResult.setSuccess(false);
            if (lastResult.getErrorMessage() == null) {
                lastResult.setErrorMessage("达到最大重试次数，AI任务执行失败。问题：" +
                        (previousIssues.isEmpty() ? "未知" : String.join(", ", previousIssues)));
            }
            return lastResult;
        }

        throw new RuntimeException("AI任务执行失败，已达到最大重试次数: " + MAX_RETRIES, lastException);
    }

    /**
     * 从结果中提取内容
     */
    private <T> String extractContent(T result) {
        if (result == null) {
            return "";
        }
        // 使用反射获取rawResponse或content字段
        try {
            java.lang.reflect.Method getRawResponse = result.getClass().getMethod("getRawResponse");
            Object rawResponse = getRawResponse.invoke(result);
            if (rawResponse != null) {
                return rawResponse.toString();
            }
        } catch (Exception ignored) {
        }
        return result.toString();
    }

    /**
     * AI结果接口
     */
    public interface AiResult {
        void setSuccess(boolean success);
        boolean isSuccess();
        void setErrorMessage(String errorMessage);
        String getErrorMessage();
        void setValidationDecision(String validationDecision);
        void setValidationIssues(java.util.List<String> validationIssues);
    }
}
