package com.jiedan.service.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiedan.config.HuoshanAiProperties;
import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.AiMessage;
import com.jiedan.dto.ai.AiUsage;
import com.jiedan.entity.enums.AiProviderType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 火山引擎AI策略实现
 * 基于火山引擎Coding Plan API的实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class HuoshanAiStrategy implements AIProviderStrategy {

    private final HuoshanAiProperties properties;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public AiChatResponse chatCompletion(AiChatRequest request) {
        log.info("开始调用火山引擎AI服务, 模型: {}", request.getModel());

        try {
            // 构建请求URL
            String url = properties.getBaseUrl() + "/chat/completions";

            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(properties.getApiKey());

            // 构建请求体
            Map<String, Object> requestBody = buildRequestBody(request);

            // 发送请求
            HttpEntity<Map<String, Object>> httpRequest = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, httpRequest, String.class);

            // 解析响应
            return parseResponse(response.getBody());

        } catch (RestClientException e) {
            log.error("火山引擎API调用失败: {}", e.getMessage(), e);
            return AiChatResponse.error("AI服务调用失败: " + e.getMessage());
        } catch (Exception e) {
            log.error("处理火山引擎响应时出错: {}", e.getMessage(), e);
            return AiChatResponse.error("处理AI响应失败: " + e.getMessage());
        }
    }

    /**
     * 构建请求体
     */
    private Map<String, Object> buildRequestBody(AiChatRequest request) {
        Map<String, Object> body = new HashMap<>();

        // 使用请求的模型或默认模型
        String model = request.getModel() != null ? request.getModel() : properties.getDefaultModel();
        body.put("model", model);

        // 转换消息格式
        List<Map<String, String>> messages = request.getMessages().stream()
                .map(this::convertMessage)
                .collect(Collectors.toList());
        body.put("messages", messages);

        // 可选参数
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        }
        if (request.getStream() != null) {
            body.put("stream", request.getStream());
        }

        return body;
    }

    /**
     * 转换消息格式
     */
    private Map<String, String> convertMessage(AiMessage message) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", message.getRole());
        msg.put("content", message.getContent());
        return msg;
    }

    /**
     * 解析响应
     */
    private AiChatResponse parseResponse(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);

        AiChatResponse.AiChatResponseBuilder builder = AiChatResponse.builder();

        // 解析choices
        if (root.has("choices") && root.get("choices").isArray() && root.get("choices").size() > 0) {
            JsonNode firstChoice = root.get("choices").get(0);

            if (firstChoice.has("message")) {
                JsonNode message = firstChoice.get("message");
                if (message.has("content")) {
                    builder.content(message.get("content").asText());
                }
                if (message.has("reasoning_content")) {
                    builder.reasoningContent(message.get("reasoning_content").asText());
                }
            }

            if (firstChoice.has("finish_reason")) {
                builder.finishReason(firstChoice.get("finish_reason").asText());
            }
        }

        // 解析usage
        if (root.has("usage")) {
            JsonNode usage = root.get("usage");
            AiUsage usageDto = AiUsage.builder()
                    .promptTokens(usage.has("prompt_tokens") ? usage.get("prompt_tokens").asInt() : 0)
                    .completionTokens(usage.has("completion_tokens") ? usage.get("completion_tokens").asInt() : 0)
                    .totalTokens(usage.has("total_tokens") ? usage.get("total_tokens").asInt() : 0)
                    .build();
            builder.usage(usageDto);
        }

        // 解析model
        if (root.has("model")) {
            builder.model(root.get("model").asText());
        }

        builder.success(true);

        log.info("火山引擎AI调用成功, 使用模型: {}, token消耗: {}",
                builder.build().getModel(),
                builder.build().getUsage() != null ? builder.build().getUsage().getTotalTokens() : 0);

        return builder.build();
    }

    @Override
    public String getProviderName() {
        return AiProviderType.HUOSHAN.getName();
    }

    @Override
    public boolean supports(String modelType) {
        if (modelType == null || modelType.isEmpty()) {
            return false;
        }
        String normalized = modelType.toLowerCase();
        return normalized.equals("huoshan") ||
                normalized.contains("doubao") ||
                normalized.contains("kimi") ||
                normalized.contains("minimax") ||
                normalized.contains("glm") ||
                normalized.contains("deepseek");
    }
}
