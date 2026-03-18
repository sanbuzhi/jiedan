# 真正的多轮会话实现计划

## 目标
- AI拆分任务使用独立的多轮会话，维持对话历史
- Feedback Shadow验证使用独立的会话，不共享对话历史
- AI生成需求文档也使用独立的多轮会话

## 当前问题分析

### 现有实现
1. **AiService.chatWithContinuation** - 已支持多轮对话（messages列表），但重试时未使用
2. **AiRetryService** - 只是简单地在userPrompt中附加上下文，不是真正的多轮会话
3. **FeedbackShadowService** - 每次调用都是独立的全新会话

### 问题
- 重试时AI需要重新理解整个需求，上下文会丢失
- 每次请求都要重复发送需求文档，浪费Token

---

## 实现步骤

### 步骤1：创建会话管理器（SessionManager）
**文件**: `src/main/java/com/jiedan/service/ai/SessionManager.java`（新建）

**功能**:
- 为每个任务/会话维护独立的messages列表
- 支持添加用户消息、助手消息
- 支持上下文压缩（当消息过多时）

**核心方法**:
```java
public class SessionManager {
    // 每个会话ID维护独立的messages列表
    private final Map<String, List<AiMessage>> sessions = new ConcurrentHashMap<>();
    
    // 创建新会话
    public String createSession(String projectId, String apiType, String systemPrompt);
    
    // 添加用户消息
    public void addUserMessage(String sessionId, String content);
    
    // 添加助手消息
    public void addAssistantMessage(String sessionId, String content);
    
    // 获取当前会话的所有消息
    public List<AiMessage> getMessages(String sessionId);
    
    // 压缩上下文（保留系统提示和关键消息）
    public void compressContext(String sessionId);
    
    // 清理会话
    public void closeSession(String sessionId);
}
```

### 步骤2：修改AiRetryService，支持真正的多轮会话
**文件**: `src/main/java/com/jiedan/service/ai/AiRetryService.java`

**修改内容**:
- 新增 `executeWithRetryAndContextWithSession` 方法
- 接收SessionManager和sessionId参数
- 每次重试时，通过session.addUserMessage()和session.addAssistantMessage()累积对话历史

**核心逻辑**:
```java
public <T extends AiResult> T executeWithRetryWithSession(
        SessionManager sessionManager,
        String sessionId,
        BiFunction<List<AiMessage>, List<String>, T> taskFunction,
        String projectId, String apiType) {
    // 每次重试：
    // 1. 添加用户消息（包含需求或上一轮内容+问题）
    // 2. 调用AI，获取响应
    // 3. 添加助手消息（AI响应）
    // 4. Feedback Shadow验证
    // 5. 如果失败，继续循环；如果成功，返回
}
```

### 步骤3：修改AiService，使用SessionManager
**文件**: `src/main/java/com/jiedan/service/ai/AiService.java`

**修改内容**:
1. 注入SessionManager
2. clarifyRequirement方法 - 创建独立会话
3. splitTasks方法 - 每个并行任务创建独立会话

**核心逻辑**:
```java
// AI明确需求 - 独立多轮会话
public ClarifyRequirementResponse clarifyRequirement(ClarifyRequirementRequest request) {
    String sessionId = sessionManager.createSession(
        request.getProjectId(), 
        "clarify-requirement", 
        AiPromptTemplate.CLARIFY_REQUIREMENT_SYSTEM
    );
    // 使用sessionManager执行重试逻辑
    // ...
}

// AI拆分任务 - 每个并行任务独立会话
public SplitTasksResponse splitTasks(SplitTasksRequest request) {
    // 每个并行任务创建独立sessionId
    // 任务1: session-V1, 任务2: session-V2, 任务3: session-V3
    // ...
}
```

### 步骤4：确保FeedbackShadow使用独立会话
**文件**: `src/main/java/com/jiedan/service/ai/feedback/FeedbackShadowService.java`

**修改内容**:
- validateWithAI方法保持不变（每次都是全新会话）
- 不需要SessionManager，因为验证不需要维持对话历史

**验证逻辑**:
```java
// Feedback Shadow保持独立会话
public FeedbackShadowValidateResponse validateWithAI(FeedbackShadowValidateRequest request) {
    // 每次调用都是全新会话，不共享历史
    // 构建prompt -> 调用AI -> 解析结果
}
```

### 步骤5：修改AiParallelExecutor，支持多轮会话
**文件**: `src/main/java/com/jiedan/service/ai/AiParallelExecutor.java`

**修改内容**:
- 每个并行任务使用独立的sessionId
- 重试时在同一session中继续对话

**核心逻辑**:
```java
// 每个并行任务
for (int taskIndex = 1; taskIndex <= PARALLEL_COUNT; taskIndex++) {
    String sessionId = sessionManager.createSession(
        projectId, 
        "split-tasks-V" + taskIndex,
        systemPrompt
    );
    
    // 使用session执行重试
    SplitTasksResponse response = aiRetryService.executeWithRetryWithSession(
        sessionManager,
        sessionId,
        (messages, previousIssues) -> {
            // 调用executeSplitTasksOnceInternal
            // ...
        },
        projectId, "split-tasks"
    );
}
```

---

## 文件修改清单

| 文件 | 修改类型 | 说明 |
|------|---------|------|
| `SessionManager.java` | 新建 | 会话管理器 |
| `AiRetryService.java` | 修改 | 支持多轮会话的重试方法 |
| `AiService.java` | 修改 | 使用SessionManager |
| `AiParallelExecutor.java` | 修改 | 每个任务独立会话 |
| `FeedbackShadowService.java` | 无需修改 | 保持独立会话 |

---

## 实现顺序

1. **第一步**: 创建SessionManager类
2. **第二步**: 修改AiRetryService，添加多轮会话支持
3. **第三步**: 修改AiService，在clarifyRequirement中使用SessionManager
4. **第四步**: 修改AiParallelExecutor，在splitTasks中使用SessionManager
5. **第五步**: 编译验证

---

## 预期效果

### 真正的多轮会话流程
```
会话1 (AI明确需求):
  [user] 需求描述
  [assistant] 需求文档v1
  [user] Feedback: 缺少功能模块
  [assistant] 需求文档v2
  [user] Feedback: 格式问题
  [assistant] 需求文档v3（验证通过）

会话2 (AI拆分任务-任务1):
  [user] 需求文档
  [assistant] 任务书v1-1
  [user] Feedback: 缺少第5章
  [assistant] 任务书v1-2
  [user] Feedback: 项目类型不匹配
  [assistant] 任务书v1-3（验证通过）

会话3 (Feedback Shadow验证):
  [system] 检测标准
  [user] 待检测文档
  [assistant] 检测报告（全新会话）

会话4 (AI拆分任务-任务2): 独立会话...
会话5 (AI拆分任务-任务3): 独立会话...
```

### 优势
- AI可以精准记住上一轮的修改内容
- 不需要重复发送需求文档
- Feedback Shadow验证不干扰主流程
- 每个并行任务有独立的会话历史
