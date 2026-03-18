package com.jiedan.service.ai;

import com.jiedan.dto.ai.AiMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 会话管理器
 * 为每个AI任务维护独立的多轮对话历史
 * 支持上下文压缩，防止Token超限
 */
@Slf4j
@Component
public class SessionManager {

    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    /**
     * 会话上下文
     */
    private static class SessionContext {
        final String projectId;
        final String apiType;
        final String systemPrompt;
        final List<AiMessage> messages;
        long createdAt;
        long lastActiveAt;

        SessionContext(String projectId, String apiType, String systemPrompt) {
            this.projectId = projectId;
            this.apiType = apiType;
            this.systemPrompt = systemPrompt;
            this.messages = new ArrayList<>();
            this.createdAt = System.currentTimeMillis();
            this.lastActiveAt = System.currentTimeMillis();
        }

        void updateActiveTime() {
            this.lastActiveAt = System.currentTimeMillis();
        }
    }

    /**
     * 创建新会话
     * @param projectId 项目ID
     * @param apiType API类型（如 clarify-requirement, split-tasks）
     * @param systemPrompt 系统提示词
     * @return 会话ID
     */
    public String createSession(String projectId, String apiType, String systemPrompt) {
        String sessionId = UUID.randomUUID().toString();
        SessionContext context = new SessionContext(projectId, apiType, systemPrompt);
        context.messages.add(AiMessage.system(systemPrompt));
        sessions.put(sessionId, context);

        log.info("创建新会话: sessionId={}, projectId={}, apiType={}", sessionId, projectId, apiType);
        return sessionId;
    }

    /**
     * 添加用户消息
     * @param sessionId 会话ID
     * @param content 用户消息内容
     */
    public void addUserMessage(String sessionId, String content) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.messages.add(AiMessage.user(content));
            context.updateActiveTime();
            log.debug("添加用户消息: sessionId={}, 当前消息数={}", sessionId, context.messages.size());
        } else {
            log.warn("会话不存在: {}", sessionId);
        }
    }

    /**
     * 添加助手消息（AI响应）
     * @param sessionId 会话ID
     * @param content AI响应内容
     */
    public void addAssistantMessage(String sessionId, String content) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.messages.add(AiMessage.assistant(content));
            context.updateActiveTime();
            log.debug("添加助手消息: sessionId={}, 当前消息数={}", sessionId, context.messages.size());
        } else {
            log.warn("会话不存在: {}", sessionId);
        }
    }

    /**
     * 获取当前会话的所有消息（用于AI调用）
     * @param sessionId 会话ID
     * @return 消息列表
     */
    public List<AiMessage> getMessages(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            return new ArrayList<>(context.messages);
        }
        log.warn("会话不存在: {}", sessionId);
        return List.of();
    }

    /**
     * 压缩上下文
     * 当消息过多时，保留系统提示、原始用户请求和最近的对话
     * @param sessionId 会话ID
     */
    public void compressContext(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context == null) {
            return;
        }

        int currentSize = context.messages.size();
        if (currentSize <= 10) {
            return;
        }

        log.info("开始压缩上下文: sessionId={}, 当前消息数={}", sessionId, currentSize);

        List<AiMessage> compressedMessages = new ArrayList<>();
        compressedMessages.add(context.messages.get(0));

        if (currentSize > 2) {
            compressedMessages.add(context.messages.get(1));
        }

        int recentCount = Math.min(6, currentSize - 2);
        int startIdx = currentSize - recentCount;
        for (int i = startIdx; i < currentSize; i++) {
            compressedMessages.add(context.messages.get(i));
        }

        context.messages.clear();
        context.messages.addAll(compressedMessages);

        log.info("上下文压缩完成: sessionId={}, 压缩后消息数={}", sessionId, context.messages.size());
    }

    /**
     * 清理会话
     * @param sessionId 会话ID
     */
    public void closeSession(String sessionId) {
        SessionContext context = sessions.remove(sessionId);
        if (context != null) {
            log.info("关闭会话: sessionId={}, projectId={}, apiType={}, 总消息数={}",
                    sessionId, context.projectId, context.apiType, context.messages.size());
        }
    }

    /**
     * 获取会话信息（用于调试）
     * @param sessionId 会话ID
     * @return 会话信息字符串
     */
    public String getSessionInfo(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            return String.format("Session[id=%s, projectId=%s, apiType=%s, messages=%d, createdAt=%d]",
                    sessionId, context.projectId, context.apiType, context.messages.size(), context.createdAt);
        }
        return "Session not found: " + sessionId;
    }

    /**
     * 清理过期会话（超过30分钟无活动）
     */
    public void cleanupExpiredSessions() {
        long now = System.currentTimeMillis();
        long expireTime = 30 * 60 * 1000L;

        sessions.entrySet().removeIf(entry -> {
            SessionContext context = entry.getValue();
            boolean expired = (now - context.lastActiveAt) > expireTime;
            if (expired) {
                log.info("清理过期会话: sessionId={}, projectId={}, idleTime={}ms",
                        entry.getKey(), context.projectId, now - context.lastActiveAt);
            }
            return expired;
        });
    }
}
