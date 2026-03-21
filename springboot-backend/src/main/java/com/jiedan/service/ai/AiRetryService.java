package com.jiedan.service.ai;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.AiMessage;
import com.jiedan.dto.ai.SplitTasksResponse;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateRequest;
import com.jiedan.dto.ai.feedback.FeedbackShadowValidateResponse;
import com.jiedan.dto.ai.feedback.ValidationDecision;
import com.jiedan.service.ai.feedback.FeedbackShadowService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * AI重试服务
 * 封装AI调用的重试逻辑，集成Feedback Shadow验证
 * 【核心功能】重试时携带Feedback Shadow的反馈建议，让AI针对性修正
 * 【多轮对话】支持使用SessionManager维持真正的多轮对话历史
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AiRetryService {

    private final FeedbackShadowService feedbackShadowService;
    private static final int MAX_RETRIES = 3;
    private static final long INITIAL_RETRY_DELAY_MS = 1000;
    private static final int MAX_CONTINUATION = 10;

    /**
     * 【核心方法】执行AI任务并自动重试（真正的多轮会话模式）
     * 使用SessionManager维持对话历史，Feedback Shadow使用独立会话
     *
     * @param sessionManager 会话管理器
     * @param sessionId 会话ID
     * @param strategyFactory AI策略工厂
     * @param projectId 项目ID
     * @param apiType API类型
     * @param taskIndex 任务索引（1-3）
     * @param maxTokens 最大token数
     * @param temperature 温度参数
     * @param onContentGenerated 内容生成回调（每次AI生成内容后调用，用于保存版本文件）
     * @return SplitTasksResponse
     */
    public SplitTasksResponse executeWithRetryAndSession(
            SessionManager sessionManager,
            String sessionId,
            AiStrategyFactory strategyFactory,
            String projectId, String apiType,
            Integer taskIndex,
            int maxTokens, double temperature,
            Consumer<SplitTasksResponse> onContentGenerated) {

        int attempts = 0;
        Exception lastException = null;
        SplitTasksResponse lastResult = null;
        List<String> previousIssues = new ArrayList<>();

        while (attempts < MAX_RETRIES) {
            attempts++;
            log.info("执行AI任务（真正多轮会话模式）: sessionId={}, apiType={}, projectId={}, 尝试次数: {}/{}",
                    sessionId, apiType, projectId, attempts, MAX_RETRIES);

            try {
                // 获取当前会话的所有消息
                List<AiMessage> currentMessages = sessionManager.getMessages(sessionId);
                if (currentMessages.isEmpty()) {
                    throw new IllegalStateException("会话消息为空: " + sessionId);
                }

                // 【关键】使用续传机制生成完整内容
                AIProviderStrategy strategy = strategyFactory.getStrategy(null);
                String content = chatWithContinuation(strategy, currentMessages, maxTokens, temperature);
                log.info("AI生成完整内容长度: {}, content前100字符: {}", content.length(), content.substring(0, Math.min(100, content.length())));

                // 将AI响应添加到会话历史
                sessionManager.addAssistantMessage(sessionId, content);

                // 直接构建结果对象
                SplitTasksResponse result = SplitTasksResponse.builder()
                        .documentContent(content)
                        .retryCount(attempts)
                        .validationDecision("PENDING")
                        .build();
                lastResult = result;
                
                log.info("结果对象内容设置后, documentContent长度: {}, retryCount: {}", content.length(), attempts);

                // 【关键】Feedback Shadow验证使用独立会话，不共享对话历史
                FeedbackShadowValidateRequest validateRequest = FeedbackShadowValidateRequest.builder()
                        .projectId(projectId)
                        .apiType(apiType)
                        .taskIndex(taskIndex)
                        .retryCount(attempts)
                        .documentContent(content)
                        .build();

                FeedbackShadowValidateResponse validationResult = feedbackShadowService.validateWithAI(validateRequest);
                log.info("Feedback Shadow验证结果: projectId={}, decision={}, issues={}",
                        projectId, validationResult.getDecision(), validationResult.getIssues());

                // 根据验证结果处理
                switch (validationResult.getDecision()) {
                    case ALLOW:
                        result.setSuccess(true);
                        result.setValidationDecision(ValidationDecision.ALLOW.name());
                        result.setValidationIssues(validationResult.getIssues());
                        log.info("AI任务验证通过: sessionId={}, apiType={}, 尝试次数: {}/{}",
                                sessionId, apiType, attempts, MAX_RETRIES);
                        
                        // 回调：保存版本文件
                        if (onContentGenerated != null) {
                            onContentGenerated.accept(result);
                        }
                        return result;

                    case REPAIR:
                    case REJECT:
                        // 回调：保存版本文件（即使验证失败也保存）
                        if (onContentGenerated != null) {
                            onContentGenerated.accept(result);
                        }
                        // 验证失败，记录问题，用于下次重试
                        if (validationResult.getIssues() != null && !validationResult.getIssues().isEmpty()) {
                            previousIssues = validationResult.getIssues();
                            log.warn("AI任务验证失败，记录问题用于重试: sessionId={}, apiType={}, issues={}",
                                    sessionId, apiType, previousIssues);
                        }

                        // 【关键】将Feedback问题作为用户消息添加到会话历史
                        String feedbackMessage = "【Feedback Shadow验证发现的问题】\n请根据以下问题，针对性修改上述内容中的对应部分：\n";
                        for (int i = 0; i < previousIssues.size(); i++) {
                            feedbackMessage += (i + 1) + ". " + previousIssues.get(i) + "\n";
                        }
                        feedbackMessage += "\n【要求】\n";
                        feedbackMessage += "- 只修改有问题的部分，不要重新生成整个文档\n";
                        feedbackMessage += "- 确保修改后的内容仍然包含完整的章节\n";
                        feedbackMessage += "- 输出完整的修改后文档，不要只输出修改的部分\n";

                        sessionManager.addUserMessage(sessionId, feedbackMessage);

                        // 上下文压缩，防止Token超限
                        sessionManager.compressContext(sessionId);

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
                log.error("AI任务执行异常: sessionId={}, apiType={}, projectId={}, 尝试次数: {}/{}, 错误: {}",
                        sessionId, apiType, projectId, attempts, MAX_RETRIES, e.getMessage(), e);
            }

            // 如果不是最后一次尝试，等待后重试
            if (attempts < MAX_RETRIES) {
                long delay = INITIAL_RETRY_DELAY_MS * attempts;
                log.info("AI任务将在 {}ms 后重试，问题数: {}", delay, previousIssues.size());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        log.error("AI任务达到最大重试次数: sessionId={}, apiType={}, projectId={}, 最大重试次数: {}, 最终问题: {}",
                sessionId, apiType, projectId, MAX_RETRIES, previousIssues);

        // 清理会话
        sessionManager.closeSession(sessionId);

        if (lastResult != null) {
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
     * 执行AI任务并自动重试（带上一轮内容的多轮对话模式 - 兼容旧版本）
     * 【核心】重试时携带上一轮生成的内容，让AI在原有基础上针对性修改
     *
     * @param contextFunction 上下文函数，接收(上一轮内容, 问题列表)，返回AI结果
     * @param projectId 项目ID
     * @param apiType API类型
     * @param <T> 返回类型
     * @return AI任务结果
     */
    public <T extends AiResult> T executeWithRetryAndContext(
            java.util.function.BiFunction<String, List<String>, T> contextFunction,
            String projectId, String apiType) {
        int attempts = 0;
        Exception lastException = null;
        T lastResult = null;
        List<String> previousIssues = new ArrayList<>(); // 记录之前的问题
        String previousContent = ""; // 记录上一轮生成的内容

        while (attempts < MAX_RETRIES) {
            attempts++;
            log.info("执行AI任务（多轮对话模式）: apiType={}, projectId={}, 尝试次数: {}/{}, 上一轮内容长度: {}",
                    apiType, projectId, attempts, MAX_RETRIES, previousContent.length());

            try {
                // 【关键】执行AI任务，传入上一轮内容和问题列表
                T result = contextFunction.apply(previousContent, previousIssues);
                lastResult = result;

                // 获取AI生成的内容
                String content = extractContent(result);
                log.info("AI生成内容长度: {}", content.length());

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
                        result.setSuccess(true);
                        result.setValidationDecision(ValidationDecision.ALLOW.name());
                        result.setValidationIssues(validationResult.getIssues());
                        log.info("AI任务验证通过: apiType={}, projectId={}, 尝试次数: {}/{}",
                                apiType, projectId, attempts, MAX_RETRIES);
                        return result;

                    case REPAIR:
                    case REJECT:
                        // 验证失败，记录问题，并保存上一轮内容用于下次重试
                        if (validationResult.getIssues() != null && !validationResult.getIssues().isEmpty()) {
                            previousIssues = validationResult.getIssues();
                            log.warn("AI任务验证失败，记录问题用于重试: apiType={}, projectId={}, issues={}",
                                    apiType, projectId, previousIssues);
                        }
                        // 【关键】保存上一轮内容，用于下次重试时针对性修改
                        previousContent = content;
                        log.info("已保存上一轮内容，长度: {}，用于下次重试时针对性修改", previousContent.length());

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
                long delay = INITIAL_RETRY_DELAY_MS * attempts;
                log.info("AI任务将在 {}ms 后重试，携带上一轮内容长度: {}，问题数: {}",
                        delay, previousContent.length(), previousIssues.size());
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }

        log.error("AI任务达到最大重试次数: apiType={}, projectId={}, 最大重试次数: {}, 最终问题: {}",
                apiType, projectId, MAX_RETRIES, previousIssues);

        if (lastResult != null) {
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
     * 执行AI任务并自动重试（兼容旧版本）
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
     * 支持续传的AI调用方法
     * 当内容被截断时，自动继续生成剩余内容
     * @param strategy AI策略
     * @param messages 消息列表
     * @param maxTokens 每次调用的最大token数
     * @param temperature 温度参数
     * @return 完整的AI响应内容
     */
    private String chatWithContinuation(AIProviderStrategy strategy, List<AiMessage> messages, 
                                       int maxTokens, double temperature) {
        StringBuilder fullContent = new StringBuilder();
        List<AiMessage> currentMessages = new ArrayList<>(messages);
        
        int continuationCount = 0;
        boolean isComplete = false;
        String lastContent = "";
        
        while (!isComplete && continuationCount <= MAX_CONTINUATION) {
            AiChatRequest chatRequest = AiChatRequest.builder()
                    .temperature(temperature)
                    .maxTokens(maxTokens)
                    .messages(new ArrayList<>(currentMessages))
                    .build();
            
            AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);
            
            if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
                log.error("AI续传调用失败: {}", chatResponse.getErrorMessage());
                throw new RuntimeException("AI调用失败: " + chatResponse.getErrorMessage());
            }
            
            String content = chatResponse.getContent();
            if (content != null && !content.isEmpty()) {
                if (content.equals(lastContent)) {
                    log.warn("检测到重复内容，停止续传");
                    isComplete = true;
                    break;
                }
                fullContent.append(content);
                lastContent = content;
            }
            
            String finishReason = chatResponse.getFinishReason();
            log.info("AI调用完成, finishReason: {}, 当前内容长度: {}, 续传次数: {}/{}", 
                    finishReason, fullContent.length(), continuationCount, MAX_CONTINUATION);
            
            if ("length".equals(finishReason)) {
                continuationCount++;
                log.info("内容被截断，开始第{}次续传", continuationCount);
                
                currentMessages.add(AiMessage.assistant(content));
                currentMessages.add(AiMessage.user("请继续生成剩余内容，从上次中断的地方开始，不要重复已生成的内容。当前已生成" + fullContent.length() + "字符。"));
            } else {
                isComplete = true;
            }
        }
        
        if (continuationCount > MAX_CONTINUATION) {
            log.warn("达到最大续传次数{}，内容可能不完整", MAX_CONTINUATION);
        }
        
        return fullContent.toString();
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
