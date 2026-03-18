package com.jiedan.service.ai;

import com.jiedan.dto.ai.AiChatRequest;
import com.jiedan.dto.ai.AiChatResponse;
import com.jiedan.dto.ai.AiMessage;
import com.jiedan.service.ai.VersionCollector.VersionInfo;
import com.jiedan.service.ai.prompt.AiPromptTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * 任务决策者服务
 * 负责在多个AI生成的任务书版本中选择最佳版本
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskDecisionService {

    private final AiStrategyFactory strategyFactory;

    /**
     * 决策结果
     */
    public record DecisionResult(
        String selectedVersion,
        String reason,
        List<String> improvements,
        String selectedContent
    ) {}

    /**
     * 提取JSON内容
     */
    private String extractJson(String content) {
        int start = content.indexOf("{");
        int end = content.lastIndexOf("}");
        if (start >= 0 && end > start) {
            return content.substring(start, end + 1);
        }
        return null;
    }

    /**
     * 提取字段值
     */
    private String extractField(String json, String field) {
        String pattern = "\"" + field + "\"\\s*:\\s*\"([^\"]*)\"";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    /**
     * 提取改进建议
     */
    private List<String> extractImprovements(String json) {
        List<String> improvements = new java.util.ArrayList<>();
        String pattern = "\"improvements\"\\s*:\\s*\\[([^\\]]*)\\]";
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            String arrayContent = matcher.group(1);
            // 提取字符串
            java.util.regex.Matcher itemMatcher = java.util.regex.Pattern.compile("\"([^\"]*)\"").matcher(arrayContent);
            while (itemMatcher.find()) {
                improvements.add(itemMatcher.group(1));
            }
        }
        return improvements;
    }

    /**
     * 【新增】从带验证信息的版本中选择最佳任务书
     * 优先选择ALLOW状态的版本，其次REPAIR，最后REJECT
     *
     * @param systemPrompt 系统提示词
     * @param userPrompt 用户提示词（需求文档）
     * @param versions 带验证信息的版本列表
     * @return 决策结果
     */
    public DecisionResult selectBestVersionWithValidation(String systemPrompt, String userPrompt,
                                                           List<VersionInfo> versions) {
        log.info("开始任务决策(带验证信息)，候选版本数: {}", versions.size());

        if (versions.isEmpty()) {
            throw new IllegalArgumentException("候选版本不能为空");
        }

        if (versions.size() == 1) {
            VersionInfo version = versions.get(0);
            log.info("只有一个候选版本，直接返回: {}", version.getVersionId());
            return new DecisionResult(
                    version.getVersionId(),
                    "只有一个候选版本，验证状态: " + version.getValidationDecision(),
                    List.of(),
                    version.getContent()
            );
        }

        // 按验证状态分组
        List<VersionInfo> allowVersions = versions.stream()
                .filter(v -> "ALLOW".equals(v.getValidationDecision()))
                .toList();
        List<VersionInfo> repairVersions = versions.stream()
                .filter(v -> "REPAIR".equals(v.getValidationDecision()))
                .toList();
        List<VersionInfo> rejectVersions = versions.stream()
                .filter(v -> "REJECT".equals(v.getValidationDecision()))
                .toList();

        log.info("版本分布: ALLOW={}, REPAIR={}, REJECT={}",
                allowVersions.size(), repairVersions.size(), rejectVersions.size());

        // 第一优先级：ALLOW版本
        if (!allowVersions.isEmpty()) {
            log.info("存在ALLOW版本，从中选择最佳版本");
            return selectBestFromVersions(userPrompt, allowVersions,
                    "ALLOW版本中选择最佳");
        }

        // 第二优先级：REPAIR版本
        if (!repairVersions.isEmpty()) {
            log.info("不存在ALLOW版本，从REPAIR版本中选择最佳");
            return selectBestFromVersions(userPrompt, repairVersions,
                    "REPAIR版本中选择最佳（建议人工复核）");
        }

        // 第三优先级：REJECT版本
        log.warn("不存在ALLOW/REPAIR版本，只能从REJECT版本中选择");
        return selectBestFromVersions(userPrompt, rejectVersions,
                "REJECT版本中选择相对最佳（质量不佳）");
    }

    /**
     * 【新增】从指定版本列表中选择最佳
     */
    private DecisionResult selectBestFromVersions(String userPrompt,
                                                   List<VersionInfo> versions, String selectionContext) {
        // 如果只有一个版本，直接返回
        if (versions.size() == 1) {
            VersionInfo version = versions.get(0);
            return new DecisionResult(
                    version.getVersionId(),
                    selectionContext + ": " + version.getVersionId(),
                    List.of(),
                    version.getContent()
            );
        }

        // 构建带验证信息的决策Prompt
        String decisionPrompt = buildDecisionPromptWithValidation(userPrompt, versions);

        // 调用AI进行决策
        DecisionResult result = callAiForDecision(userPrompt, versions, selectionContext);
        if (result != null) {
            return result;
        }

        // AI调用失败，降级策略：返回最新版本
        VersionInfo latestVersion = versions.stream()
                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                .orElse(versions.get(0));
        return new DecisionResult(
                latestVersion.getVersionId(),
                selectionContext + "（决策失败，返回最新版本）",
                List.of(),
                latestVersion.getContent()
        );
    }

    /**
     * 【新增】调用AI进行决策，支持分段处理
     * 如果版本数量较多，采用锦标赛机制分批决策
     */
    private DecisionResult callAiForDecision(String userPrompt,
                                              List<VersionInfo> versions, String selectionContext) {
        // 如果版本数量超过3个，采用锦标赛机制
        if (versions.size() > 3) {
            log.info("版本数量超过3个，采用锦标赛机制分批决策");
            return tournamentDecision(userPrompt, versions, selectionContext);
        }

        // 直接决策
        return singleBatchDecision(userPrompt, versions, selectionContext);
    }

    /**
     * 【新增】单批次决策 - 使用上行续传方式
     * 通过多轮对话逐个提交版本内容，避免单次token超限
     */
    private DecisionResult singleBatchDecision(String userPrompt,
                                                List<VersionInfo> versions, String selectionContext) {
        log.info("使用上行续传方式进行决策，版本数: {}", versions.size());

        AIProviderStrategy strategy = strategyFactory.getStrategy(null);
        List<AiMessage> messages = new ArrayList<>();

        // 第1轮：发送系统提示词和需求文档
        String systemPrompt = buildDecisionSystemPrompt();
        messages.add(AiMessage.system(systemPrompt));

        String introPrompt = buildDecisionIntroPrompt(userPrompt, versions);
        messages.add(AiMessage.user(introPrompt));

        String aiResponse = sendMessageAndGetResponse(strategy, messages, 8000);
        if (aiResponse == null) {
            return null;
        }
        messages.add(AiMessage.assistant(aiResponse));
        log.info("第1轮：已发送系统提示和需求文档，AI响应: {}", aiResponse.substring(0, Math.min(100, aiResponse.length())));

        // 第2-N轮：逐个发送版本内容
        for (int i = 0; i < versions.size(); i++) {
            VersionInfo version = versions.get(i);
            String versionPrompt = buildVersionPrompt(version, i + 1, versions.size());
            messages.add(AiMessage.user(versionPrompt));

            aiResponse = sendMessageAndGetResponse(strategy, messages, 8000);
            if (aiResponse == null) {
                return null;
            }
            messages.add(AiMessage.assistant(aiResponse));
            log.info("第{}轮：已发送版本{}内容，AI响应: {}", i + 2, version.getVersionId(),
                    aiResponse.substring(0, Math.min(100, aiResponse.length())));
        }

        // 最后一轮：发送决策请求
        String decisionPrompt = buildFinalDecisionPrompt(versions);
        messages.add(AiMessage.user(decisionPrompt));

        aiResponse = sendMessageAndGetResponse(strategy, messages, 16000);
        if (aiResponse == null) {
            return null;
        }
        messages.add(AiMessage.assistant(aiResponse));
        log.info("最终轮：已发送决策请求，AI响应: {}", aiResponse.substring(0, Math.min(200, aiResponse.length())));

        // 解析决策结果
        return parseDecisionResultWithValidation(aiResponse, versions, selectionContext);
    }

    /**
     * 构建决策系统提示词
     */
    private String buildDecisionSystemPrompt() {
        return """
            【角色定义】
            你是资深的技术评审专家，擅长评估技术文档的质量和适用性。

            【核心任务】
            从多个AI生成的任务书候选版本中，选择出一个最符合用户需求的最佳版本。

            【决策维度】
            1. 用户需求匹配度（最重要）
               - 项目类型是否一致（比如美妆管理系统、宠物管理系统、图书馆管理系统）
               - 任务书内容是否准确反映用户的原始需求
               - 功能模块是否与需求文档一致
               - 是否存在偏离需求的内容（如生成错误类型的项目）

            2. 文档完整性
               - 是否包含全部7个必要章节
               - 每个章节的内容是否完整
               - 是否存在截断或缺失

            3. 内容详细程度
               - 技术规格是否具体（含版本号）
               - 接口定义是否完整（URL、参数、响应）
               - 数据库设计是否详细（字段、类型、约束）

            4. 技术可行性
               - 技术栈选择是否合理
               - 架构设计是否可行
               - 实现难度评估

            【重要规则】
            - 我会逐个给你发送候选版本的内容，请先仔细分析每个版本
            - 等我发送完所有版本后，再给出最终决策
            - 输出格式必须是JSON，包含以下字段：
              {
                "selectedVersion": "V1-1",
                "reason": "选择理由",
                "improvements": ["改进建议"]
              }
            """;
    }

    /**
     * 构建初始提示词（需求文档概述）
     */
    private String buildDecisionIntroPrompt(String userPrompt, List<VersionInfo> versions) {
        String versionList = versions.stream()
                .map(VersionInfo::getVersionId)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
        
        return "【用户需求文档】\n" + userPrompt + "\n\n" +
                "【候选版本信息】\n" +
                "共有 " + versions.size() + " 个候选版本需要分析：\n" +
                "版本列表：" + versionList + "\n\n" +
                "【任务】\n" +
                "我将逐个发送每个版本的完整内容，请仔细分析。等全部发送完后，请告诉我最终选择哪个版本及理由。\n";
    }

    /**
     * 构建版本内容提示词
     */
    private String buildVersionPrompt(VersionInfo version, int currentNum, int totalNum) {
        return "=== 候选版本 " + currentNum + "/" + totalNum + " ===\n" +
                "版本ID: " + version.getVersionId() + "\n" +
                "验证状态: " + version.getValidationDecision() + "\n\n" +
                "【版本内容开始】\n" +
                version.getContent() + "\n" +
                "【版本内容结束】\n\n" +
                "请分析这个版本的特点和优缺点。\n";
    }

    /**
     * 构建最终决策提示词
     */
    private String buildFinalDecisionPrompt(List<VersionInfo> versions) {
        return "【任务完成】\n" +
                "以上是全部 " + versions.size() + " 个候选版本的内容分析。\n\n" +
                "【输出要求】\n" +
                "请从以上版本中选择最佳版本，输出JSON格式：\n" +
                "{\n" +
                "  \"selectedVersion\": \"选中的版本号\",\n" +
                "  \"reason\": \"详细的选择理由，包括与其他版本的对比\",\n" +
                "  \"improvements\": [\"改进建议1\", \"改进建议2\"]\n" +
                "}\n\n" +
                "注意：\n" +
                "- 项目类型必须与需求文档一致（禁止选择宠物社区、校园二手书等与需求无关的版本）\n" +
                "- 必须包含全部7个必要章节\n" +
                "- 优先选择验证状态为ALLOW的版本\n";
    }

    /**
     * 发送消息并获取响应
     */
    private String sendMessageAndGetResponse(AIProviderStrategy strategy, List<AiMessage> messages, int maxTokens) {
        // 上下文压缩：如果消息太多，只保留关键信息
        if (messages.size() > 20) {
            log.info("上下文消息过多({})，进行压缩", messages.size());
            List<AiMessage> compressedMessages = new ArrayList<>();
            // 保留系统提示
            compressedMessages.add(messages.get(0));
            // 保留用户需求概述（第2条）
            compressedMessages.add(messages.get(1));
            // 保留最近6轮对话（12条消息）
            int startIdx = Math.max(2, messages.size() - 12);
            for (int i = startIdx; i < messages.size(); i++) {
                compressedMessages.add(messages.get(i));
            }
            messages = compressedMessages;
            log.info("上下文压缩完成，当前消息数: {}", messages.size());
        }

        AiChatRequest chatRequest = AiChatRequest.builder()
                .model(null)
                .temperature(0.3)
                .maxTokens(maxTokens)
                .messages(new ArrayList<>(messages))
                .build();

        AiChatResponse response = strategy.chatCompletion(chatRequest);

        if (!Boolean.TRUE.equals(response.getSuccess())) {
            log.error("AI调用失败: {}", response.getErrorMessage());
            return null;
        }

        return response.getContent();
    }

    /**
     * 【新增】锦标赛决策机制 - 支持第一轮并行执行
     * 将版本分批比较，最终选出最佳版本
     */
    private DecisionResult tournamentDecision(String userPrompt,
                                               List<VersionInfo> versions, String selectionContext) {
        log.info("开始锦标赛决策，总版本数: {}", versions.size());

        List<VersionInfo> currentRound = new java.util.ArrayList<>(versions);
        int round = 1;

        while (currentRound.size() > 3) {
            log.info("锦标赛第{}轮，候选版本数: {}", round, currentRound.size());
            List<VersionInfo> winners;

            // 第一轮：并行执行决策
            if (round == 1) {
                log.info("第1轮：并行执行决策，共 {} 组", (currentRound.size() + 2) / 3);
                winners = parallelBatchDecision(userPrompt, currentRound, round, selectionContext);
            } else {
                // 后续轮次：串行执行
                winners = serialBatchDecision(userPrompt, currentRound, round, selectionContext);
            }

            currentRound = winners;
            round++;
        }

        // 最终轮：从剩余的3个或更少版本中选出最佳
        log.info("锦标赛最终轮，候选版本数: {}", currentRound.size());
        DecisionResult finalResult = singleBatchDecision(userPrompt, currentRound,
                selectionContext + " - 决赛轮");

        if (finalResult != null) {
            return finalResult;
        }

        // 最终决策失败，返回最新版本
        VersionInfo latestVersion = currentRound.stream()
                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                .orElse(currentRound.get(0));
        return new DecisionResult(
                latestVersion.getVersionId(),
                selectionContext + "（锦标赛决策失败，返回最新版本）",
                List.of(),
                latestVersion.getContent()
        );
    }

    /**
     * 【新增】并行批量决策 - 第一轮使用
     * 将版本分成多组，每组并行执行决策
     */
    private List<VersionInfo> parallelBatchDecision(String userPrompt,
                                                    List<VersionInfo> currentRound,
                                                    int round,
                                                    String selectionContext) {
        // 将版本分成每3个一组
        List<List<VersionInfo>> batches = new java.util.ArrayList<>();
        for (int i = 0; i < currentRound.size(); i += 3) {
            int end = Math.min(i + 3, currentRound.size());
            batches.add(currentRound.subList(i, end));
        }

        // 使用CompletableFuture并行执行每组的决策
        List<java.util.concurrent.CompletableFuture<VersionInfo>> futures = batches.stream()
                .map(batch -> java.util.concurrent.CompletableFuture.supplyAsync(() -> {
                    if (batch.size() == 1) {
                        return batch.get(0);
                    }
                    DecisionResult result = singleBatchDecision(userPrompt, batch,
                            selectionContext + " - 第" + round + "轮");
                    if (result != null) {
                        return batch.stream()
                                .filter(v -> v.getVersionId().equals(result.selectedVersion()))
                                .findFirst()
                                .orElse(batch.get(0));
                    } else {
                        return batch.stream()
                                .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                                .orElse(batch.get(0));
                    }
                }))
                .toList();

        // 等待所有并行任务完成
        List<VersionInfo> winners = futures.stream()
                .map(java.util.concurrent.CompletableFuture::join)
                .toList();

        log.info("第{}轮并行决策完成，晋级版本数: {}", round, winners.size());
        return winners;
    }

    /**
     * 【新增】串行批量决策 - 后续轮次使用
     */
    private List<VersionInfo> serialBatchDecision(String userPrompt,
                                                 List<VersionInfo> currentRound,
                                                 int round,
                                                 String selectionContext) {
        List<VersionInfo> winners = new java.util.ArrayList<>();

        for (int i = 0; i < currentRound.size(); i += 3) {
            int end = Math.min(i + 3, currentRound.size());
            List<VersionInfo> batch = currentRound.subList(i, end);

            if (batch.size() == 1) {
                winners.add(batch.get(0));
            } else {
                DecisionResult result = singleBatchDecision(userPrompt, batch,
                        selectionContext + " - 第" + round + "轮");
                if (result != null) {
                    VersionInfo winner = batch.stream()
                            .filter(v -> v.getVersionId().equals(result.selectedVersion()))
                            .findFirst()
                            .orElse(batch.get(0));
                    winners.add(winner);
                } else {
                    VersionInfo latest = batch.stream()
                            .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                            .orElse(batch.get(0));
                    winners.add(latest);
                }
            }
        }

        log.info("第{}轮串行决策完成，晋级版本数: {}", round, winners.size());
        return winners;
    }

    /**
     * 【新增】构建带验证信息的决策Prompt
     */
    private String buildDecisionPromptWithValidation(String userPrompt,
                                                      List<VersionInfo> versions) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("【对任务书的严格要求】\n");
        String taskMdMustNeed = """
        1. 格式：Markdown
        2. 语言：中文
        3. 结构：必须包含以下7个章节，缺一不可：
           ## 1. 项目技术规格
           - 技术栈（同步需求文档的技术栈，如果需求文档没有则需要自行分析。含具体版本号，如Spring Boot 2.7.18）
           - 项目结构规范（各端目录结构（视技术栈决定））
           - 代码生成规则（依赖版本、校验规则、JWT配置等）
           ## 2. 前端页面开发清单
           - 按模块列出所有页面（页面路径、核心功能、必用组件、接口调用、跳转关系）
           ## 3. 后端接口开发清单
           - 按模块列出所有接口（URL、请求方式、请求参数、响应格式、业务逻辑、关联数据库表）
           ## 4. 数据库表结构设计
           - 所有业务表的字段定义（字段名、数据类型、约束、默认值、注释）
           - 主键、外键、索引
           - 表间关联关系
           ## 5. 业务逻辑规则
           - 核心业务流程
           - 数据校验规则
           - 状态流转规则
           - 异常处理规则
           ## 6. 开发执行顺序
           - 阶段划分（数据库→后端→前端→联调）
           - 依赖关系
           - 执行优先级
           ## 7. 代码生成规范
           - 命名规范（类名、方法名、变量名、表名、字段名）
           - 代码结构（Controller/Service/Mapper分层）
           - 注释要求
           - 错误码定义
        【质量标准】
        - 所有指令可直接映射为代码逻辑
        - 无冗余描述，每句话都指向代码实现
        - 文档结构完整，可被TaskDocumentParser解析
        - 项目类型与需求文档完全一致
        """; //任务严格要求
        prompt.append(taskMdMustNeed).append("\n\n");

        prompt.append("【用户需求文档】\n");
        prompt.append(userPrompt).append("\n\n");

        prompt.append("【候选版本】\n");
        prompt.append("共有 ").append(versions.size()).append(" 个候选版本:\n\n");

        for (VersionInfo version : versions) {
            prompt.append("=== ").append(version.getVersionId()).append(" ===\n");
            prompt.append("验证状态: ").append(version.getValidationDecision()).append("\n");
            prompt.append("---\n");
            // 使用完整内容，不截断
            prompt.append(version.getContent()).append("\n\n");
        }

        prompt.append("【比较方法 - 请按以下步骤执行】\n");
        prompt.append("第一步：快速检查每个版本的验证状态（ALLOW > REPAIR > REJECT）\n");
        prompt.append("第二步：逐一检查每个版本是否包含全部 7 个章节\n");
        prompt.append("第三步：对比各版本的技术规格是否与需求文档一致（特别是项目类型）\n");
        prompt.append("第四步：评估数据库设计的完整性（字段、类型、约束是否详细）\n");
        prompt.append("第五步：检查前后端开发清单是否详细（页面路径、接口 URL 是否具体）\n");
        prompt.append("第六步：综合以上因素选择最佳版本\n\n");

        prompt.append("【重点关注 - 一票否决项】\n");
        prompt.append("❌ 项目类型与需求文档不一致（如需求是美妆店管理系统，任务书却是宠物社区/校园二手书等）\n");
        prompt.append("❌ 缺少 7 个必要章节中的任何一个\n");
        prompt.append("❌ 技术栈与需求文档冲突\n");
        prompt.append("✅ 优先选择：项目类型完全匹配 + 7 章节完整 + 技术规格详细 + 验证状态为 ALLOW\n\n");

        prompt.append("【输出要求】\n");
        prompt.append("请严格按照以下 JSON 格式输出（不要包含 Markdown 代码块标记）：\n");
        prompt.append("{\n");
        prompt.append("  \"selectedVersion\": \"V1-1\",\n");
        prompt.append("  \"reason\": \"详细说明为什么选择这个版本，包括与其他版本的对比分析\",\n");
        prompt.append("  \"improvements\": [\"改进建议 1\", \"改进建议 2\"]\n");
        prompt.append("}\n\n");

        prompt.append("【要求】\n");
        prompt.append("请根据决策维度分析以上候选版本，选择最佳版本。\n");
        prompt.append("注意：所有候选版本都已经过 Feedback Shadow 验证，请优先考虑用户需求匹配度和文档完整性。\n");
        prompt.append("输出格式必须是 JSON，包含以下字段:\n");
        prompt.append("- selectedVersion: 选中的版本号（如 V1-1）\n");
        prompt.append("- reason: 选择理由\n");
        prompt.append("- improvements: 改进建议列表\n");

        return prompt.toString();
    }

    /**
     * 【新增】解析带验证信息的决策结果
     */
    private DecisionResult parseDecisionResultWithValidation(String content, List<VersionInfo> versions,
                                                               String selectionContext) {
        try {
            // 尝试从JSON格式解析
            String json = extractJson(content);
            if (json != null) {
                String selectedVersion = extractField(json, "selectedVersion");
                String reason = extractField(json, "reason");

                // 查找选中的版本
                VersionInfo selected = versions.stream()
                        .filter(v -> v.getVersionId().equals(selectedVersion))
                        .findFirst()
                        .orElse(null);

                if (selected != null) {
                    log.info("决策完成，选中版本: {}, 理由: {}", selectedVersion, reason);
                    return new DecisionResult(
                            selectedVersion,
                            selectionContext + ": " + (reason != null ? reason : "AI决策选择"),
                            extractImprovements(json),
                            selected.getContent()
                    );
                }
            }

            // 解析失败，返回最新版本
            log.warn("决策结果解析失败，返回最新版本");
            VersionInfo latestVersion = versions.stream()
                    .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                    .orElse(versions.get(0));
            return new DecisionResult(
                    latestVersion.getVersionId(),
                    selectionContext + "（决策解析失败，返回最新版本）",
                    List.of(),
                    latestVersion.getContent()
            );

        } catch (Exception e) {
            log.error("解析决策结果异常", e);
            VersionInfo latestVersion = versions.stream()
                    .max(Comparator.comparingLong(VersionInfo::getTimestamp))
                    .orElse(versions.get(0));
            return new DecisionResult(
                    latestVersion.getVersionId(),
                    selectionContext + "（决策解析异常: " + e.getMessage() + "）",
                    List.of(),
                    latestVersion.getContent()
            );
        }
    }
}
