package com.jiedan.service.ai.feedback;

import com.jiedan.dto.ai.feedback.ValidationDecision;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 文档解析器
 * 负责解析AI输出的检测报告和各类文档
 */
@Slf4j
@Component
public class DocumentParser {

    /**
     * 解析Feedback Shadow检测报告
     */
    public FeedbackShadowReport parseFeedbackReport(String content) {
        if (content == null || content.isEmpty()) {
            log.warn("检测报告内容为空");
            return FeedbackShadowReport.builder()
                    .decision(ValidationDecision.REJECT)
                    .issues(List.of("检测报告为空"))
                    .build();
        }

        try {
            // 尝试标准解析
            return parseStandardReport(content);
        } catch (Exception e) {
            log.warn("标准解析失败，使用容错解析: {}", e.getMessage());
            // 容错解析
            return parseLenientReport(content);
        }
    }

    /**
     * 标准解析
     */
    private FeedbackShadowReport parseStandardReport(String content) {
        FeedbackShadowReport.FeedbackShadowReportBuilder builder = FeedbackShadowReport.builder();

        // 解析决策建议
        ValidationDecision decision = parseDecision(content);
        builder.decision(decision);

        // 解析问题列表
        List<String> issues = parseIssues(content);
        builder.issues(issues);

        // 解析修复建议
        String repairSuggestion = parseRepairSuggestion(content);
        builder.repairSuggestion(repairSuggestion);

        // 解析总体评价
        String summary = parseSummary(content);
        builder.summary(summary);

        return builder.build();
    }

    /**
     * 容错解析
     */
    private FeedbackShadowReport parseLenientReport(String content) {
        FeedbackShadowReport.FeedbackShadowReportBuilder builder = FeedbackShadowReport.builder();

        // 使用正则表达式提取决策
        ValidationDecision decision = extractDecisionByRegex(content);
        builder.decision(decision);

        // 提取问题（简单按行分割）
        List<String> issues = new ArrayList<>();
        String[] lines = content.split("\n");
        for (String line : lines) {
            if (line.contains("问题") || line.contains("缺陷") || line.contains("错误")) {
                issues.add(line.trim());
            }
        }
        if (issues.isEmpty()) {
            issues.add("未明确列出问题");
        }
        builder.issues(issues);

        // 提取修复建议（取后半部分内容）
        int halfLength = content.length() / 2;
        builder.repairSuggestion(content.substring(halfLength));

        builder.summary("容错解析结果");

        return builder.build();
    }

    /**
     * 解析决策
     */
    private ValidationDecision parseDecision(String content) {
        // 查找 "决策建议" 或 "建议操作" 部分
        Pattern pattern = Pattern.compile("(?:决策建议|建议操作).*?(ALLOW|REPAIR|REJECT)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String decisionStr = matcher.group(1).toUpperCase();
            try {
                return ValidationDecision.valueOf(decisionStr);
            } catch (IllegalArgumentException e) {
                log.warn("未知的决策类型: {}", decisionStr);
            }
        }

        // 备用：直接搜索关键词
        String upperContent = content.toUpperCase();
        if (upperContent.contains("ALLOW") || upperContent.contains("放行")) {
            return ValidationDecision.ALLOW;
        } else if (upperContent.contains("REJECT") || upperContent.contains("拒绝")) {
            return ValidationDecision.REJECT;
        } else {
            return ValidationDecision.REPAIR;
        }
    }

    /**
     * 使用正则提取决策
     */
    private ValidationDecision extractDecisionByRegex(String content) {
        String upperContent = content.toUpperCase();
        if (upperContent.contains("ALLOW") || upperContent.contains("通过") || upperContent.contains("放行")) {
            return ValidationDecision.ALLOW;
        } else if (upperContent.contains("REJECT") || upperContent.contains("拒绝") || upperContent.contains("失败")) {
            return ValidationDecision.REJECT;
        } else {
            return ValidationDecision.REPAIR;
        }
    }

    /**
     * 解析问题列表
     */
    private List<String> parseIssues(String content) {
        List<String> issues = new ArrayList<>();

        // 查找 "详细结果" 或 "问题列表" 部分
        Pattern pattern = Pattern.compile("(?:详细结果|问题列表|Issues).*?((?:-|[0-9]+\\.)[^\n]+\n)+", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            String issuesSection = matcher.group(0);
            // 提取列表项
            Pattern itemPattern = Pattern.compile("(?:-|[0-9]+\\.)\\s*([^\n]+)");
            Matcher itemMatcher = itemPattern.matcher(issuesSection);

            while (itemMatcher.find()) {
                String issue = itemMatcher.group(1).trim();
                if (!issue.isEmpty()) {
                    issues.add(issue);
                }
            }
        }

        if (issues.isEmpty()) {
            issues.add("未明确列出具体问题");
        }

        return issues;
    }

    /**
     * 解析修复建议
     */
    private String parseRepairSuggestion(String content) {
        // 查找 "修复建议" 部分
        Pattern pattern = Pattern.compile("(?:修复建议|Repair Suggestions).*?(?=##|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(0).trim();
        }

        return "";
    }

    /**
     * 解析总体评价
     */
    private String parseSummary(String content) {
        // 查找 "检测摘要" 部分
        Pattern pattern = Pattern.compile("(?:检测摘要|Summary).*?(?=##|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(content);

        if (matcher.find()) {
            return matcher.group(0).trim();
        }

        return "";
    }

    /**
     * 提取PRD中的功能模块列表
     */
    public List<String> extractFunctionalModules(String prdContent) {
        List<String> modules = new ArrayList<>();

        // 查找功能模块部分
        Pattern pattern = Pattern.compile("(?:功能模块|功能需求).*?(?=##|$)", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(prdContent);

        if (matcher.find()) {
            String modulesSection = matcher.group(0);
            // 提取模块名称（通常是列表项或表格第一列）
            Pattern modulePattern = Pattern.compile("(?:-|[0-9]+\\.|[|])\\s*([^|\\n]+?)(?:模块|功能)?\\s*(?:\\[P[0-9]\\]|[|])");
            Matcher moduleMatcher = modulePattern.matcher(modulesSection);

            while (moduleMatcher.find()) {
                String module = moduleMatcher.group(1).trim();
                if (!module.isEmpty() && !module.equals("模块") && !module.equals("名称")) {
                    modules.add(module);
                }
            }
        }

        return modules;
    }

    /**
     * 提取PRD中的项目类型
     */
    public String extractProjectType(String prdContent) {
        // 查找技术架构部分
        Pattern pattern = Pattern.compile("(?:技术架构|前端技术栈|后端技术栈).*?(?:SpringBoot|微信小程序|Python|Vue|React)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(prdContent);

        if (matcher.find()) {
            String matched = matcher.group(0).toLowerCase();
            if (matched.contains("springboot") || matched.contains("spring")) {
                return "springboot";
            } else if (matched.contains("微信") || matched.contains("小程序")) {
                return "wechat-miniprogram";
            } else if (matched.contains("python")) {
                return "python";
            } else if (matched.contains("vue") || matched.contains("react")) {
                return "web";
            }
        }

        return "unknown";
    }

    /**
     * 提取任务列表中的任务ID
     */
    public List<String> extractTaskIds(String taskDocumentContent) {
        List<String> taskIds = new ArrayList<>();

        Pattern pattern = Pattern.compile("TASK-\\d{3}");
        Matcher matcher = pattern.matcher(taskDocumentContent);

        while (matcher.find()) {
            taskIds.add(matcher.group());
        }

        return taskIds;
    }

    /**
     * 提取核心开发任务ID
     */
    public String extractCoreDevelopmentTaskId(String taskDocumentContent) {
        Pattern pattern = Pattern.compile("(?:核心任务|核心开发任务).*?(TASK-\\d{3})");
        Matcher matcher = pattern.matcher(taskDocumentContent);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    /**
     * 内部类：检测报告
     */
    @lombok.Data
    @lombok.Builder
    public static class FeedbackShadowReport {
        private ValidationDecision decision;
        private List<String> issues;
        private String repairSuggestion;
        private String summary;
    }
}
