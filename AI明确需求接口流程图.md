# AI明确需求接口详细流程图

## 1. 整体架构

```
┌─────────────────┐
│  微信小程序前端  │
│  (index.js)    │
└────────┬────────┘
         │
         │ POST /v1/ai/clarify-requirement
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
│   AiRetryService              │
│  (重试服务 + Feedback Shadow)  │
└────────┬─────────────────────┘
         │
         │
    ┌────▼────┐
    │         │
┌───▼───┐ ┌──▼───┐ ┌───────┐
│ 尝试1 │ │ 尝试2│ │ 尝试3 │
│ V1    │ │ V2   │ │ V3    │
└───┬───┘ └──┬───┘ └───┬───┘
    │         │         │
    └────┬────┘         │
         │              │
    ┌────▼──────────────▼─────┐
    │  Feedback Shadow验证      │
    │  (ALLOW / REPAIR / REJECT)│
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
executeAINodeWithRetry(requirementId, 1)
    │
    ├─► 从数据库获取需求信息
    │
    ├─► 构建请求数据
    │   {
    │     projectId: requirement.id,
    │     requirementDescription: requirement.requirementDescription
    │   }
    │
    └─► 调用后端API: aiApi.clarifyRequirement(requestData)
```

---

### 阶段2：后端控制器接收请求 (AiController.java)

```
POST /v1/ai/clarify-requirement
    │
    ├─► 验证用户认证 (@CurrentUser)
    │
    ├─► 记录日志: "收到AI明确需求请求"
    │
    └─► 调用: aiService.clarifyRequirement(request)
```

---

### 阶段3：AiService核心逻辑 (AiService.java)

```
clarifyRequirement(ClarifyRequirementRequest request)
    │
    ├─► 1. 初始化版本计数器
    │   └─► documentVersionCounter[projectId]["clarify-requirement"] = 0
    │
    ├─► 2. 使用重试服务执行
    │   └─► aiRetryService.executeWithRetry(...)
    │       │
    │       ├─► 循环最多3次尝试
    │       │   │
    │       │   ├─► 2.1 执行单次AI任务
    │       │   │   └─► executeClarifyRequirementOnce(request, previousIssues)
    │       │   │
    │       │   ├─► 2.2 保存版本到feedback目录
    │       │   │   └─► saveDocumentVersion(projectId, "clarify-requirement", content, version, false)
    │       │   │       └─► 保存到: projects/{projectId}/feedback/clarify-requirement/V{version}-{timestamp}.md
    │       │   │
    │       │   └─► 2.3 Feedback Shadow验证
    │       │       ├─► 判断: ALLOW / REPAIR / REJECT
    │       │       ├─► 如果是 ALLOW → 结束重试，返回成功
    │       │       ├─► 如果是 REPAIR → 继续重试，携带问题列表
    │       │       └─► 如果是 REJECT → 继续重试
    │       │
    │       └─► 返回最终响应
    │
    ├─► 3. 保存最终文档
    │   ├─► saveFinalDocument(projectId, "clarify-requirement", content)
    │   │   └─► 保存到: projects/{projectId}/req/REQUIREMENT.md
    │   └─► saveRequirementDocToDatabase(projectId, content)
    │       └─► 保存到: Requirement.ai_requirement_doc
    │
    └─► 4. 返回响应
        └─► ClarifyRequirementResponse {
              success: true,
              documentContent: "...",
              validationDecision: "ALLOW"
            }
```

---

### 阶段4：单次执行AI明确需求 (executeClarifyRequirementOnce)

```
executeClarifyRequirementOnce(request, previousIssues)
    │
    ├─► 1. 获取AI策略
    │   └─► strategy = strategyFactory.getStrategy(null)
    │
    ├─► 2. 构建System Prompt
    │   ├─► systemPrompt = AiPromptTemplate.CLARIFY_REQUIREMENT_SYSTEM
    │   └─► 如果有previousIssues，追加反馈问题
    │       └─► "【重要】之前生成的文档存在以下问题，请务必修正：\n..."
    │
    ├─► 3. 构建User Prompt
    │   └─► userPrompt = AiPromptTemplate.buildClarifyRequirementUserPrompt(requirementDescription)
    │
    ├─► 4. 使用续传机制生成完整内容
    │   └─► chatWithContinuation(strategy, systemPrompt, userPrompt, 8000, 5)
    │       │
    │       ├─► 循环最多6次（1次初始 + 5次续传）
    │       │   │
    │       │   ├─► 4.1 调用AI模型
    │       │   │   └─► strategy.chatCompletion(...)
    │       │   │
    │       │   ├─► 4.2 追加生成的内容
    │       │   │
    │       │   └─► 4.3 检查是否被截断
    │       │       ├─► finishReason == "length" → 继续续传
    │       │       └─► finishReason == "stop" → 结束
    │       │
    │       └─► 返回完整内容
    │
    └─► 5. 返回响应
        └─► ClarifyRequirementResponse {
              documentContent: fullContent
            }
```

---

## 3. 数据流转图

```
┌─────────────────────────────────────────────────────────────────┐
│                        数据流转路径                               │
├─────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. 请求输入                                                     │
│     └─► ClarifyRequirementRequest {                              │
│           projectId,                                            │
│           requirementDescription                                │
│         }                                                       │
│                                                                   │
│  2. AI生成过程中保存的版本                                       │
│     └─► projects/{projectId}/feedback/clarify-requirement/      │
│         ├─► V1-20260320-120000.md                              │
│         ├─► V2-20260320-120100.md                              │
│         └─► ...                                                  │
│                                                                   │
│  3. Feedback Shadow验证报告                                      │
│     └─► projects/{projectId}/feedback/                          │
│         ├─► FB-20260320-120015-xxx.md (验证V1)                │
│         ├─► FB-20260320-120115-yyy.md (验证V2)                │
│         └─► ...                                                  │
│                                                                   │
│  4. 最终交付版本                                                 │
│     ├─► 数据库: Requirement.ai_requirement_doc                  │
│     └─► 文件系统: projects/{projectId}/req/REQUIREMENT.md      │
│                                                                   │
│  5. 响应输出                                                     │
│     └─► ClarifyRequirementResponse {                             │
│           success: true,                                         │
│           documentContent: "...",                                │
│           validationDecision: "ALLOW"                            │
│         }                                                         │
│                                                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## 4. 关键组件说明

| 组件 | 文件 | 职责 |
|------|------|------|
| **前端页面** | `miniprogram/pages/index/index.js` | 发起AI明确需求请求，处理响应 |
| **AI控制器** | `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` | 接收HTTP请求，参数校验，调用服务层 |
| **AI服务** | `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` | 核心业务逻辑，协调各组件 |
| **重试服务** | `springboot-backend/src/main/java/com/jiedan/service/ai/AiRetryService.java` | 处理AI重试逻辑，集成Feedback Shadow |
| **Feedback Shadow** | `FeedbackShadowService` | 验证AI输出质量 |

---

## 5. 时间线示例

```
时间轴 (秒)
│
0s  ──► 前端发起请求
       │
1s  ──► 后端接收，开始尝试1
       │
30s ──► 尝试1完成V1
       │   Feedback Shadow验证 → REPAIR
       │   记录问题，开始尝试2
       │
60s ──► 尝试2完成V2
       │   Feedback Shadow验证 → ALLOW ✓
       │
61s ──► 保存最终文档到数据库和文件
       │
62s ──► 返回成功响应
       │
63s ──► 前端收到响应，更新流程节点到2
```

---

## 6. 与AI拆分任务接口的对比

| 特性 | AI明确需求 | AI拆分任务 |
|------|-----------|-----------|
| **执行策略** | 串行重试（最多3次） | 并行执行（3个任务同时运行） |
| **续传机制** | ✓ 使用（最多5次续传） | ✓ 使用（最多10次续传） |
| **Feedback Shadow** | ✓ 每次尝试后验证 | ✓ 每个版本后验证 |
| **早停机制** | 无 | ✓ 检测到ALLOW版本立即停止 |
| **版本选择** | 最后一次成功尝试 | 任务决策者从多个版本中选择 |
| **文件保存位置** | `req/REQUIREMENT.md` | `task/TASKS.md` |
