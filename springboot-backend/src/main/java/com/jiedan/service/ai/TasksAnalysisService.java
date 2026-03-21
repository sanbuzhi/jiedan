package com.jiedan.service.ai;

import com.jiedan.config.AiDevelopmentConfig;
import com.jiedan.config.AiDevelopmentConfig.PhaseConfig;
import com.jiedan.config.HuoshanAiProperties;
import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.AiMessage;
import com.jiedan.service.ai.prompt.AiPromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class TasksAnalysisService {

    private final AIProviderStrategy aiProvider;
    private final HuoshanAiProperties aiProperties;
    private final AiDevelopmentConfig config;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 格式1: 1. **数据库阶段**：创建所有数据库表...
    // 格式2: 1. **数据库阶段**（第1-3天）
    private static final Pattern LIST_PHASE_PATTERN = Pattern.compile(
        "(?im)^\\s*(\\d+)[.、]\\s*\\*?\\*?([^*【\\n(（]+)\\*?\\*?\\s*(?:[（(][^）)]*[）)])?\\s*[:：]\\s*([^\\n]+)"
    );

    // 格式3: | 阶段1：数据库与公共模块开发 | ...
    private static final Pattern TABLE_PHASE_PATTERN = Pattern.compile(
        "(?i)\\|\\s*阶段\\s*(\\d+)[：:]\\s*([^|]+)\\|",
        Pattern.MULTILINE
    );

    // 备用：阶段数字开头
    private static final Pattern SIMPLE_PHASE_PATTERN = Pattern.compile(
        "(?i)(?:^|\\n)\\s*阶段\\s*(\\d+)\\s*[：:]\\s*([^\\n|]+)"
    );

    public List<PhaseConfig> analyzeAndGetPhaseConfigs(String tasksMdContent) {
        try {
            String fullContent = chatWithContinuation(tasksMdContent);

            log.info("AI分析任务书响应, content长度: {}, content前500字符: {}",
                fullContent.length(),
                fullContent.substring(0, Math.min(500, fullContent.length())));

            List<PhaseConfig> configs = parsePhaseConfigs(fullContent, tasksMdContent);
            log.info("AI解析任务书阶段配置结果, configs: {}", configs);
            if (configs.isEmpty()) {
                throw new RuntimeException("AI解析任务书未能提取到任何阶段配置");
            }
            return configs;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            log.error("AI分析TASKS.md失败: {}", e.getMessage());
            throw new RuntimeException("AI分析任务书异常: " + e.getMessage(), e);
        }
    }

    private String chatWithContinuation(String tasksMdContent) {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(new AiMessage("system", AiPromptTemplate.TASKS_ANALYSIS_SYSTEM));

        // 分批计算
        List<String> batches = splitContentIntoBatches(tasksMdContent, 8000);
        int totalBatches = batches.size();

        // 第一轮：发送第一批内容和任务说明
        String firstPrompt = AiPromptTemplate.buildTasksAnalysisUserPrompt(totalBatches, batches.get(0));
        messages.add(new AiMessage("user", firstPrompt));

        // 如果只有一批，且内容很短，直接发送并等待响应
        if (totalBatches == 1) {
            return sendAndGetResponse(messages);
        }

        // 多批情况：先连续发送所有批次，不等待响应
        for (int i = 1; i < totalBatches; i++) {
            String batchPrompt = "【第" + (i + 1) + "批内容（共" + totalBatches + "批）】\n\n" + batches.get(i);
            messages.add(new AiMessage("user", batchPrompt));
        }

        // 所有批次发送完毕后，发送最终指令
        messages.add(new AiMessage("user", "任务书已上传完毕！请按系统提示词的要求，从完整任务书中提取每个开发阶段的配置信息。输出JSON格式。"));

        // 一次性发送并获取响应
        return sendAndGetResponse(messages);
    }

    private String sendAndGetResponse(List<AiMessage> messages) {
        AiChatRequest chatRequest = AiChatRequest.builder()
                .temperature(0.3)
                .maxTokens(32000)
                .messages(new ArrayList<>(messages))
                .build();

        AiChatResponse chatResponse = aiProvider.chatCompletion(chatRequest);

        if (!Boolean.TRUE.equals(chatResponse.getSuccess())) {
            log.error("AI调用失败: {}", chatResponse.getErrorMessage());
            throw new RuntimeException("AI调用失败: " + chatResponse.getErrorMessage());
        }

        String content = chatResponse.getContent();
        log.info("AI响应, content长度: {}, content前200字符: {}",
                content != null ? content.length() : 0,
                content != null ? content.substring(0, Math.min(200, content.length())) : "null");

        return content != null ? content : "";
    }

    private List<String> splitContentIntoBatches(String content, int maxCharsPerBatch) {
        List<String> batches = new ArrayList<>();
        if (content.length() <= maxCharsPerBatch) {
            batches.add(content);
            return batches;
        }

        String[] lines = content.split("\n");
        StringBuilder currentBatch = new StringBuilder();

        for (String line : lines) {
            if (currentBatch.length() + line.length() + 1 > maxCharsPerBatch && currentBatch.length() > 0) {
                batches.add(currentBatch.toString());
                currentBatch = new StringBuilder();
            }
            currentBatch.append(line).append("\n");
        }

        if (currentBatch.length() > 0) {
            batches.add(currentBatch.toString());
        }

        return batches;
    }

    private boolean isJsonResponse(String content) {
        if (content == null || content.isEmpty()) {
            return false;
        }
        String trimmed = content.trim();
        return trimmed.startsWith("{") || trimmed.startsWith("[") ||
               trimmed.contains("\"phases\"") || trimmed.startsWith("```json");
    }

    private List<PhaseConfig> parsePhaseConfigs(String aiResponse, String tasksMdContent) {
        List<PhaseConfig> configs = new ArrayList<>();

        try {
            String jsonStr = extractJson(aiResponse);
            if (jsonStr != null) {
                Map<String, Object> parsed = objectMapper.readValue(jsonStr, Map.class);
                List<Map<String, Object>> phases = (List<Map<String, Object>>) parsed.get("phases");

                if (phases != null) {
                    for (Map<String, Object> p : phases) {
                        Integer phaseNum = (Integer) p.get("phase");
                        String phaseName = (String) p.get("phaseName");
                        List<String> targetFiles = (List<String>) p.get("targetFiles");
                        List<String> keywords = (List<String>) p.get("keywords");
                        String phaseTask = (String) p.get("phaseTask");

                        if (phaseNum != null && phaseName != null) {
                            PhaseConfig config = new PhaseConfig(
                                phaseNum,
                                phaseName,
                                keywords != null ? keywords : new ArrayList<>(),
                                targetFiles != null ? targetFiles : new ArrayList<>(),
                                targetFiles != null ? targetFiles.size() : 10
                            );
                            config.setPhaseTask(phaseTask);
                            configs.add(config);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("解析AI响应失败: {}", e.getMessage());
        }

        if (configs.isEmpty()) {
            configs = extractPhasesFromContent(tasksMdContent);
        }

        return configs;
    }

    private String extractJson(String content) {
        int start = content.indexOf("```json");
        if (start == -1) start = content.indexOf("```");
        int end = content.lastIndexOf("```");
        
        if (start != -1 && end != -1 && end > start) {
            String json = content.substring(start, end);
            json = json.replace("```json", "").replace("```", "").trim();
            return json;
        }
        return content;
    }

    private List<PhaseConfig> extractPhasesFromContent(String content) {
        List<PhaseConfig> configs = new ArrayList<>();
        
        // 尝试表格格式
        Matcher tableMatcher = TABLE_PHASE_PATTERN.matcher(content);
        while (tableMatcher.find()) {
            int phaseNum = Integer.parseInt(tableMatcher.group(1));
            String name = tableMatcher.group(2).trim();
            name = name.length() > 50 ? name.substring(0, 50) : name;
            
            configs.add(new PhaseConfig(
                phaseNum,
                name,
                Arrays.asList("代码文件", "模块"),
                Arrays.asList("*.java", "*.vue", "*.js"),
                10
            ));
        }
        
        // 尝试列表格式（格式1和2）
        if (configs.isEmpty()) {
            Matcher listMatcher = LIST_PHASE_PATTERN.matcher(content);
            int phaseNum = 1;
            while (listMatcher.find()) {
                String name = listMatcher.group(2).trim();
                name = name.length() > 50 ? name.substring(0, 50) : name;
                
                configs.add(new PhaseConfig(
                    phaseNum++,
                    name,
                    Arrays.asList("代码文件", "模块"),
                    Arrays.asList("*.java", "*.vue", "*.js"),
                    10
                ));
                
                if (phaseNum > 10) break;
            }
        }
        
        // 尝试简单格式
        if (configs.isEmpty()) {
            Matcher simpleMatcher = SIMPLE_PHASE_PATTERN.matcher(content);
            while (simpleMatcher.find()) {
                int phaseNum = Integer.parseInt(simpleMatcher.group(1));
                String name = simpleMatcher.group(2).trim();
                name = name.length() > 50 ? name.substring(0, 50) : name;
                
                configs.add(new PhaseConfig(
                    phaseNum,
                    name,
                    Arrays.asList("代码文件", "模块"),
                    Arrays.asList("*.java", "*.vue", "*.js"),
                    10
                ));
            }
        }
        
        if (configs.isEmpty()) {
            throw new RuntimeException("从任务书内容中未能提取到任何阶段配置");
        }

        return configs;
    }
}