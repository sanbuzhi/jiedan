# AI自助开发系统 - 重构规格说明书

## 1. 项目概述

### 1.1 业务背景
本项目是一个AI自助开发小程序系统，通过11个流程节点完成从需求收集到项目交付的全流程自动化开发。

### 1.2 核心流程（11个节点）

| 索引 | 节点名称 | 类型 | 说明 |
|------|----------|------|------|
| 0 | 明确需求 | 客户输入 | 客户提交初步需求 |
| 1 | AI明确需求 | AI节点 | AI分析并完善需求 → 需要客户验收 |
| 2 | 需求确认验收 | 客户验收 | 确认需求文档 |
| 3 | AI拆分任务 | AI节点 | 自动拆分子任务 → 自动继续 |
| 4 | AI开发 | AI节点 | 智能编码实现 → 自动继续 |
| 5 | AI功能测试 | AI节点 | 自动化功能测试 → 自动继续 |
| 6 | AI安全测试 | AI节点 | 测试接口漏洞 → 需要客户验收 |
| 7 | 功能验收测试 | 客户验收 | 客户功能验收 |
| 8 | 打包交付 | 系统处理 | 项目交付 |
| 9 | 最终验收 | 客户验收 | 客户最终验收 |
| 10 | 项目完成 | 完成 | 项目结束 |

### 1.3 AI节点配置

```javascript
const AI_NODE_CONFIG = {
  1: { name: 'AI明确需求', api: 'clarifyRequirement', nextNode: 2, maxRetries: 3, timeout: 600000, needsApproval: true },
  3: { name: 'AI拆分任务', api: 'splitTasks', nextNode: 4, maxRetries: 3, timeout: 600000, needsApproval: false },
  4: { name: 'AI开发', api: 'generateCode', nextNode: 5, maxRetries: 3, timeout: 600000, needsApproval: false },
  5: { name: 'AI功能测试', api: 'functionalTest', nextNode: 6, maxRetries: 3, timeout: 600000, needsApproval: false },
  6: { name: 'AI安全测试', api: 'securityTest', nextNode: 7, maxRetries: 3, timeout: 600000, needsApproval: true }
};
```

---

## 2. 当前架构问题分析

### 2.1 Feedback Shadow 设计问题

**当前流程（问题）：**
```
前端调用AI接口 → AI返回结果 → 前端调用Feedback Shadow验证 → 根据验证结果决定放行/重试
```

**问题点：**
1. 前端需要处理复杂的验证逻辑
2. 网络请求次数过多（AI调用 + Feedback Shadow验证）
3. 前端需要维护重试状态
4. 验证失败时的用户体验差

**目标流程（优化后）：**
```
前端调用AI接口 → 后端本地执行AI + Feedback Shadow验证 → 后端决定放行/重试 → 返回true/false给前端
```

### 2.2 代码位置

| 文件 | 路径 | 说明 |
|------|------|------|
| FeedbackShadowController | `springboot-backend/src/main/java/com/jiedan/controller/FeedbackShadowController.java` | 需要移除的Controller |
| FeedbackShadowService | `springboot-backend/src/main/java/com/jiedan/service/ai/feedback/FeedbackShadowService.java` | 保留业务层 |
| AiService | `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` | 需要集成Feedback Shadow |
| 前端API调用 | `miniprogram/utils/api.js` | 移除feedback-shadow相关接口 |
| 前端index.js | `miniprogram/pages/index/index.js` | 移除performFeedbackShadowValidation调用 |

---

## 3. 重构需求规格

### 3.1 需求1：移除FeedbackShadowController

**要求：**
- 删除 `FeedbackShadowController.java` 文件
- 保留 `FeedbackShadowService` 作为业务层服务
- 相关DTO保留（FeedbackShadowValidateRequest/Response等）

**影响范围：**
- 前端不再直接调用 `/v1/feedback-shadow/validate`
- 前端不再直接调用 `/v1/feedback-shadow/repair`
- 统计和记录查询功能暂时移除（后续可迁移到AdminController）

### 3.2 需求2：Feedback Shadow内嵌到AI接口

**当前AI接口：**
- `POST /v1/ai/clarify-requirement`
- `POST /v1/ai/split-tasks`
- `POST /v1/ai/generate-code`
- `POST /v1/ai/functional-test`
- `POST /v1/ai/security-test`

**修改方案：**

在 `AiService` 的每个方法中，在AI调用完成后，增加Feedback Shadow验证步骤：

```java
public ClarifyRequirementResponse clarifyRequirement(ClarifyRequirementRequest request) {
    // 1. 调用AI获取结果
    AiChatResponse chatResponse = strategy.chatCompletion(chatRequest);
    
    // 2. 【新增】Feedback Shadow验证
    FeedbackShadowValidateRequest validateRequest = FeedbackShadowValidateRequest.builder()
        .projectId(request.getProjectId())
        .apiType("clarify-requirement")
        .documentContent(chatResponse.getContent())
        .build();
    
    FeedbackShadowValidateResponse validationResult = feedbackShadowService.validateWithAI(validateRequest);
    
    // 3. 【新增】根据验证结果处理
    if (validationResult.getDecision() == ValidationDecision.REJECT) {
        // 重试逻辑（最多3次）
        // ...
    }
    
    // 4. 返回结果
    return ClarifyRequirementResponse.builder()
        .success(validationResult.isSuccess() && validationResult.getDecision() != ValidationDecision.REJECT)
        .fullRequirementDoc(chatResponse.getContent())
        .build();
}
```

### 3.3 需求3：前端接口简化

**当前前端调用（index.js）：**
```javascript
executeAINodeWithRetry(requirementId, nodeIndex, retryCount = 0) {
    // 1. 调用AI接口
    this.callAIWithTimeout(aiApi[aiNode.api], requestData, aiNode.timeout)
      .then(res => {
        // 2. 调用Feedback Shadow验证
        return this.performFeedbackShadowValidation(requirementId, nodeIndex, res);
      })
      .then(validationResult => {
        // 3. 根据验证结果处理
        // ...
      });
}
```

**优化后前端调用：**
```javascript
executeAINodeWithRetry(requirementId, nodeIndex, retryCount = 0) {
    // 1. 调用AI接口（内部已完成Feedback Shadow验证）
    this.callAIWithTimeout(aiApi[aiNode.api], requestData, aiNode.timeout)
      .then(res => {
        // 2. 直接根据AI接口返回的success判断
        if (res.success) {
          // 验证通过，继续下一步
        } else {
          // 验证失败，需要重试
        }
      });
}
```

**需要移除的前端代码：**
1. `performFeedbackShadowValidation` 方法（index.js 第449-483行）
2. `saveAIProduct` 方法（index.js 第307-376行）← AI产物由后端保存，前端不需要保存
3. `api.js` 中的 feedbackShadow 相关接口（第284-299行）

### 3.4 需求4：AI接口响应格式调整

**当前响应格式：**
```json
{
  "code": 0,
  "data": {
    "fullRequirementDoc": "...",
    "rawResponse": "...",
    "model": "..."
  }
}
```

**调整后响应格式：**
```json
{
  "code": 0,
  "data": {
    "success": true,
    "fullRequirementDoc": "...",
    "rawResponse": "...",
    "model": "...",
    "validationDecision": "ALLOW",
    "validationIssues": []
  }
}
```

当验证失败时：
```json
{
  "code": 0,
  "data": {
    "success": false,
    "errorMessage": "需求文档缺少功能模块清单",
    "validationDecision": "REJECT",
    "validationIssues": ["缺少功能模块清单", "用户角色定义不完整"]
  }
}
```

---

## 4. 详细业务流程审查

### 4.1 节点0 → 节点1 流程

**触发条件：** 用户在step_all页面完成需求提交

**当前流程：**
1. 用户完成step_all页面3个阶段
2. 调用 `doSubmitRequirement` 提交需求
3. 设置 `auto_trigger_ai` 标记
4. 跳转到index页面
5. index页面检测到标记，自动执行AI节点1

**问题：**
- 标记机制依赖本地存储，可能丢失
- 没有处理提交失败的情况

**建议：**
- 后端创建需求后自动触发AI流程（异步消息队列）
- 或者保持当前机制但增加可靠性

### 4.2 AI节点执行流程（节点1/3/4/5/6）

**当前流程：**
```
executeAINodeWithRetry
├── 1. 获取需求详情
├── 2. 构建请求数据
├── 3. 调用AI接口（带超时）
├── 4. Feedback Shadow验证 ← 【需要移除】
├── 5. 保存AI产物到本地存储
├── 6. 更新节点状态到下一阶段
└── 7. 如果需要验收，保存数据并停止
    如果不需要验收，自动继续下一个AI节点
```

**优化后流程：**
```
executeAINodeWithRetry
├── 1. 获取需求详情
├── 2. 构建请求数据
├── 3. 调用AI接口（内部已完成Feedback Shadow验证）
├── 4. 检查AI接口返回的success字段
│   ├── 成功：继续下一步
│   └── 失败：进入重试逻辑
├── 5. 【移除】保存AI产物到本地存储 ← AI产物由后端保存
├── 6. 更新节点状态到下一阶段
└── 7. 如果需要验收，停止自动执行
    如果不需要验收，自动继续下一个AI节点
```

### 4.3 客户验收流程（节点2/7/9）

**简化后流程：**
1. 用户点击流程节点（索引2/7/9）
2. 跳转到approve页面
3. 用户选择"继续"或"重试+建议"

**说明：**
- 前端不需要检查 `approval_data_${req.id}` 本地存储
- 不需要管理问题和建议数据
- 用户点击验收节点直接跳转到approve页面即可

### 4.4 建议提交流程

**当前流程：**
1. 用户在approve页面点击"重试+建议"
2. 跳转到suggestion页面
3. 用户输入建议并提交
4. 调用 `submitSuggestion` 接口
5. 返回首页

**问题：**
- 建议提交后没有触发AI重试机制
- 建议数据存储在stepData中，没有专门的表

**建议：**
- 建议提交后自动触发对应AI节点的重试
- 或者增加建议审核流程

---

## 5. 数据流分析

### 5.1 前端数据流

```
step页面（5步） → 创建需求 → step_all页面（3阶段） → 提交需求 → index页面
                                                        ↓
                                              自动触发AI节点1
                                                        ↓
                                              AI节点链式执行（1→3→4→5→6）
                                                        ↓
                                              客户验收节点（2/7/9）
```

### 5.2 后端数据流

```
RequirementController.createRequirement
    ↓
Requirement（status=draft, currentFlowNode=0）
    ↓
RequirementController.updateRequirement（step_all提交）
    ↓
Requirement（更新详细需求字段）
    ↓
AiController.clarifyRequirement（节点1）
    ↓
AiService.clarifyRequirement + FeedbackShadow验证 ← 【重构点】
    ↓
RequirementController.updateFlowStatus（currentFlowNode=2）
    ↓
...（后续AI节点类似）
```

---

## 6. 接口清单

### 6.1 需要修改的接口

| 接口 | 方法 | 修改内容 |
|------|------|----------|
| /v1/ai/clarify-requirement | POST | 增加Feedback Shadow验证 |
| /v1/ai/split-tasks | POST | 增加Feedback Shadow验证 |
| /v1/ai/generate-code | POST | 增加Feedback Shadow验证 |
| /v1/ai/functional-test | POST | 增加Feedback Shadow验证 |
| /v1/ai/security-test | POST | 增加Feedback Shadow验证 |

### 6.2 需要移除的接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /v1/feedback-shadow/validate | POST | 移除Controller，保留Service |
| /v1/feedback-shadow/repair | POST | 移除Controller，保留Service |
| /v1/feedback-shadow/spec/{apiType} | GET | 移除 |
| /v1/feedback-shadow/validate-batch | POST | 移除 |
| /v1/feedback-shadow/stats | GET | 移除 |
| /v1/feedback-shadow/records | GET | 移除 |
| /v1/feedback-shadow/repair-records | GET | 移除 |

### 6.3 保持不变的接口

| 接口 | 方法 | 说明 |
|------|------|------|
| /v1/requirements | POST/GET/PUT/DELETE | 需求CRUD |
| /v1/requirements/{id}/flow-status | GET/PUT | 流程状态管理 |
| /v1/requirements/{id}/approve | POST | 客户验收 |
| /v1/requirements/{id}/suggestion | POST | 提交建议 |
| /v1/requirements/{id}/ai-quotation | POST/GET | AI估价 |

---

## 7. 代码审查发现的问题

### 7.1 前端问题

1. **index.js 第559-563行：** 节点状态修正逻辑
   ```javascript
   if (req.status === 'draft' && currentStep === 1) {
     currentStep = 0;
   }
   ```
   - 问题：这种修正逻辑表明后端数据不一致
   - 建议：后端创建需求时currentFlowNode应该默认为0

2. **index.js 第449-483行：** performFeedbackShadowValidation
   - 问题：验证失败时也resolve，导致流程继续
   - 建议：验证失败应该reject，触发重试逻辑

3. **api.js 第287-299行：** Feedback Shadow接口
   - 问题：重复定义了feedbackShadowValidate和validateWithFeedbackShadow
   - 建议：移除这两个接口

### 7.2 后端问题

1. **AiService.java：**
   - 问题：没有集成Feedback Shadow验证
   - 建议：在每个AI方法中增加验证步骤

2. **RequirementController.java 第86行：**
   - 问题：创建需求时currentFlowNode默认为0，但前端期望draft状态对应节点0
   - 建议：保持现状或统一前后端逻辑

3. **FeedbackShadowController.java：**
   - 问题：Controller层过于复杂，应该简化
   - 建议：移除Controller，只保留Service

---

## 8. 重构后的架构

### 8.1 后端架构

```
Controller层
├── AiController（修改：集成Feedback Shadow）
├── AiQuotationController（不变）
├── AiStrategyConfigController（不变）
├── RequirementController（不变）
└── FeedbackShadowController（删除） ← 【重构点】

Service层
├── AiService（修改：集成Feedback Shadow验证）
├── FeedbackShadowService（保留）
├── AiQuotationService（不变）
└── ...
```

### 8.2 前端架构

```
pages
├── index/index.js（修改：移除Feedback Shadow调用）
├── requirement/step/step.js（不变）
├── requirement/step_all/step_all.js（不变）
├── requirement/approve/approve.js（不变）
└── requirement/suggestion/suggestion.js（不变）

utils
├── api.js（修改：移除Feedback Shadow接口）
└── util.js（不变）
```

---

## 9. 风险评估

### 9.1 重构风险

| 风险 | 等级 | 应对措施 |
|------|------|----------|
| Feedback Shadow验证失败导致AI流程卡住 | 高 | 增加最大重试次数，超过后标记为失败 |
| AI接口响应时间增加 | 中 | 增加超时时间，或者异步执行验证 |
| 前端兼容性问题 | 低 | 保持API响应格式兼容 |

### 9.2 回滚方案

如果重构出现问题，需要：
1. 恢复FeedbackShadowController
2. 恢复前端Feedback Shadow调用
3. 恢复AiService原逻辑

---

## 10. 验收标准

### 10.1 功能验收

- [ ] FeedbackShadowController已删除
- [ ] AiService集成Feedback Shadow验证
- [ ] AI接口返回包含success字段
- [ ] 前端不再调用Feedback Shadow接口
- [ ] AI流程正常运行（从节点1到节点6）
- [ ] 客户验收流程正常

### 10.2 性能验收

- [ ] AI接口响应时间在可接受范围内（< 3分钟）
- [ ] Feedback Shadow验证不阻塞主流程

### 10.3 兼容性验收

- [ ] 现有需求数据不受影响
- [ ] 前端页面正常显示
