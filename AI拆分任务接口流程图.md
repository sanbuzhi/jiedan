# AI拆分任务接口详细流程图

## 1. 整体架构

```
┌─────────────────┐
│  微信小程序前端  │
│  (index.js)    │
└────────┬────────┘
         │
         │ POST /v1/ai/split-tasks
         │
┌────────▼────────┐
│  AiController   │
│  (后端控制器)   │
└────────┬────────┘
         │
         │
┌────────▼────────┐
│   AiService     │
│  (核心业务逻辑) │
└────────┬────────┘
         │
         │
┌────────▼─────────────────────┐
│   AiParallelExecutor          │
│  (并行执行器 + 多轮会话)      │
└────────┬─────────────────────┘
         │
         │
    ┌────┴────┐
    │         │
┌───▼───┐ ┌──▼───┐ ┌───────┐
│ 任务1 │ │ 任务2│ │ 任务3 │
│ V1-1  │ │ V2-1 │ │ V3-1  │
│ V1-2  │ │ V2-2 │ │ V3-2  │
│ V1-3  │ │ V2-3 │ │ V3-3  │
└───┬───┘ └──┬───┘ └───┬───┘
    │         │         │
    └────┬────┘         │
         │              │
    ┌────▼──────────────▼─────┐
    │  VersionCollector        │
    │  (版本收集器)             │
    └────┬─────────────────────┘
         │
    ┌────▼─────────────────────┐
    │ TaskDecisionService       │
    │ (任务决策者选择最佳版本)   │
    └────┬─────────────────────┘
         │
    ┌────▼─────────────────────┐
    │  保存到数据库 + 文件系统   │
    └───────────────────────────┘
```

---

## 2. 详细流程步骤

### 阶段1：前端发起请求 (miniprogram/pages/index/index.js)

```
executeAINodeWithRetry(requirementId, 3)
    │
    ├─► 从数据库获取需求文档 (requirementDoc)
    │
    ├─► 构建请求数据
    │   {
    │     projectId: requirement.id,
    │     requirementDoc: "从数据库查询"
    │   }
    │
    └─► 调用后端API: aiApi.splitTasks(requestData)
```

---

### 阶段2：后端控制器接收请求 (AiController.java)

```
POST /v1/ai/split-tasks
    │
    ├─► 验证用户认证 (@CurrentUser)
    │
    ├─► 记录日志: "收到AI拆分任务请求"
    │
    └─► 调用: aiService.splitTasks(request)
```

---

### 阶段3：AiService核心逻辑 (AiService.java)

```
splitTasks(SplitTasksRequest request)
    │
    ├─► 1. 获取需求文档
    │   └─► getRequirementDocFromDatabase(projectId)
    │       └─► 从 Requirement 表查询 ai_requirement_doc 字段
    │
    ├─► 2. 构建提示词
    │   ├─► systemPrompt = AiPromptTemplate.SPLIT_TASKS_SYSTEM
    │   └─► userPrompt = "需求文档：\n\n" + requirementDoc
    │
    ├─► 3. 实例化并行执行器
    │   └─► new AiParallelExecutor(...)
    │       ├─► taskDecisionService
    │       ├─► versionCollector
    │       ├─► feedbackShadowService
    │       ├─► aiRetryService
    │       ├─► sessionManager
    │       ├─► strategyFactory
    │       └─► 回调函数: executeSplitTasksOnceInternal
    │
    ├─► 4. 并行执行
    │   └─► parallelExecutor.executeSplitTasksParallel(request, systemPrompt, userPrompt)
    │
    ├─► 5. 保存最终文档
    │   ├─► saveFinalDocument(projectId, "split-tasks", content)
    │   │   └─► 保存到: projects/{projectId}/task/TASKS.md
    │   └─► saveTaskDocToDatabase(projectId, content)
    │       └─► 保存到: Requirement.ai_task_doc
    │
    └─► 6. 返回响应
        └─► SplitTasksResponse {
              success: true,
              documentContent: "...",
              selectedVersion: "V1-1",
              decisionReason: "..."
            }
```

---

### 阶段4：并行执行器详细流程 (AiParallelExecutor.java)

```
executeSplitTasksParallel(request, systemPrompt, userPrompt)
    │
    ├─► 1. 初始化
    │   ├─► versionCollector.initProject(projectId)
    │   ├─► allowVersionFound = false (AtomicBoolean)
    │   ├─► bestAllowVersion = null (AtomicReference)
    │   └─► completionLatch = CountDownLatch(3)
    │
    ├─► 2. 提交3个并行任务
    │   │
    │   ├─► 任务1 (taskIndex=1)
    │   │   └─► executeSingleTaskWithFeedback(...)
    │   │
    │   ├─► 任务2 (taskIndex=2)
    │   │   └─► executeSingleTaskWithFeedback(...)
    │   │
    │   └─► 任务3 (taskIndex=3)
    │       └─► executeSingleTaskWithFeedback(...)
    │
    ├─► 3. 启动监控线程
    │   └─► 每1秒检查是否有ALLOW版本生成
    │       └─► 如果有 → 取消其他任务，提前返回
    │
    ├─► 4. 等待任务完成 (最多10分钟)
    │
    ├─► 5a. 如果找到ALLOW版本 → 直接返回
    │   └─► 返回 bestAllowVersion
    │
    ├─► 5b. 如果没有ALLOW版本
    │   ├─► 从文件系统扫描所有版本文件
    │   │   └─► loadVersionsFromFileSystem(projectId)
    │   │       └─► 扫描: projects/{projectId}/feedback/split-tasks/V*.md
    │   │
    │   └─► 调用任务决策者
    │       └─► taskDecisionService.selectBestVersionWithValidation(...)
    │           └─► AI从多个版本中选择最佳的一个
    │
    └─► 6. 返回并行结果
        └─► ParallelResult {
              success: true,
              content: "...",
              selectedVersion: "V1-2",
              decisionReason: "...",
              improvements: [...]
            }
```

---

### 阶段5：单个任务执行详细流程 (executeSingleTaskWithFeedback)

```
executeSingleTaskWithFeedback(projectId, taskIndex, ...)
    │
    ├─► 1. 创建独立会话
    │   └─► sessionManager.createSession(projectId, "split-tasks-V" + taskIndex, systemPrompt)
    │
    ├─► 2. 添加初始用户消息
    │   └─► sessionManager.addUserMessage(sessionId, userPrompt)
    │
    ├─► 3. 使用重试服务执行 (包含Feedback Shadow验证)
    │   └─► aiRetryService.executeWithRetryAndSession(...)
    │       │
    │       ├─► 循环最多3次重试
    │       │   │
    │       │   ├─► 3.1 AI生成内容
    │       │   │   └─► chatWithContinuation(...)
    │       │   │       └─► 使用续传机制，避免内容被截断
    │       │   │
    │       │   ├─► 3.2 保存版本文件
    │       │   │   └─► saveDocumentVersionWithVersionId(...)
    │       │   │       └─► 保存到: projects/{projectId}/feedback/split-tasks/V{taskIndex}-{retryCount}-{timestamp}-{uuid}.md
    │       │   │
    │       │   ├─► 3.3 Feedback Shadow验证
    │       │   │   └─► feedbackShadowService.validate(...)
    │       │   │       ├─► 判断: ALLOW / REPAIR / REJECT
    │       │   │       ├─► 如果是 ALLOW → 结束重试
    │       │   │       ├─► 如果是 REPAIR → 继续重试，携带问题列表
    │       │   │       └─► 如果是 REJECT → 继续重试
    │       │   │
    │       │   └─► 3.4 保存Feedback报告
    │       │       └─► 保存到: projects/{projectId}/feedback/FB-{timestamp}-{uuid}.md
    │       │
    │       └─► 返回最终响应
    │
    └─► 4. 关闭会话
        └─► sessionManager.closeSession(sessionId)
```

---

## 3. 数据流转图

```
┌─────────────────────────────────────────────────────────────────┐
│                        数据流转路径                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. 请求输入                                                     │
│     └─► SplitTasksRequest { projectId, requirementDoc }        │
│                                                                   │
│  2. 从数据库获取                                                 │
│     └─► Requirement.ai_requirement_doc (需求文档)               │
│                                                                   │
│  3. AI生成过程中保存的版本                                       │
│     └─► projects/{projectId}/feedback/split-tasks/              │
│         ├─► V1-1-20260320-120000-abc123.md                   │
│         ├─► V1-2-20260320-120100-def456.md                   │
│         ├─► V2-1-20260320-120030-ghi789.md                   │
│         └─► V3-1-20260320-120045-jkl012.md                   │
│                                                                   │
│  4. Feedback Shadow验证报告                                      │
│     └─► projects/{projectId}/feedback/                          │
│         ├─► FB-20260320-120015-xxx.md (验证V1-1)              │
│         ├─► FB-20260320-120115-yyy.md (验证V1-2)              │
│         └─► ...                                                  │
│                                                                   │
│  5. 最终交付版本                                                 │
│     ├─► 数据库: Requirement.ai_task_doc                         │
│     └─► 文件系统: projects/{projectId}/task/TASKS.md           │
│                                                                   │
│  6. 响应输出                                                     │
│     └─► SplitTasksResponse {                                     │
│           success: true,                                         │
│           documentContent: "...",                                │
│           selectedVersion: "V1-2",                               │
│           decisionReason: "..."                                  │
│         }                                                         │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 关键组件说明

| 组件 | 文件 | 职责 |
|------|------|------|
| **前端页面** | `miniprogram/pages/index/index.js` | 发起AI拆分任务请求，处理响应 |
| **AI控制器** | `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` | 接收HTTP请求，参数校验，调用服务层 |
| **AI服务** | `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` | 核心业务逻辑，协调各组件 |
| **并行执行器** | `springboot-backend/src/main/java/com/jiedan/service/ai/AiParallelExecutor.java` | 管理3个并行任务，监控ALLOW版本 |
| **会话管理器** | `SessionManager` | 维护多轮对话历史 |
| **重试服务** | `AiRetryService` | 处理AI重试逻辑，集成Feedback Shadow |
| **版本收集器** | `VersionCollector` | 收集所有生成的版本 |
| **任务决策者** | `TaskDecisionService` | 从多个版本中选择最佳的 |
| **Feedback Shadow** | `FeedbackShadowService` | 验证AI输出质量 |

---

## 5. 时间线示例

```
时间轴 (秒)
│
0s  ──► 前端发起请求
       │
1s  ──► 后端接收，开始并行执行3个任务
       │
       ├─► 任务1开始生成V1-1
       ├─► 任务2开始生成V2-1
       └─► 任务3开始生成V3-1
       │
30s ──► 任务1完成V1-1，Feedback Shadow验证 → REPAIR
       │   任务1开始重试生成V1-2
       │
60s ──► 任务2完成V2-1，Feedback Shadow验证 → ALLOW ✓
       │   监控线程检测到ALLOW版本
       │   取消任务1和任务3
       │
61s ──► 返回V2-1作为最终结果
       │   保存到数据库和文件
       │
62s ──► 前端收到响应，更新流程节点到4
```
