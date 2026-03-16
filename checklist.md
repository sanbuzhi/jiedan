# AI自助开发系统 - 重构检查清单

## 11个节点业务流程逐行审查

### 节点0：明确需求（客户输入）

**前端代码位置：**
- `miniprogram/pages/requirement/step_all/step_all.js` - doSubmitRequirement方法

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/RequirementController.java` - createRequirement/updateRequirement

**审查结果：**
- [ ] 用户完成step_all后提交需求
- [ ] 后端保存需求数据（status=draft, currentFlowNode=0）
- [ ] 前端设置auto_trigger_ai标记
- [ ] 跳转到index页面

**问题记录：**
1. 标记机制依赖本地存储，可能丢失
2. 没有处理提交失败的情况

**修复建议：**
- [ ] 后端创建需求后自动触发AI流程（异步消息队列）
- [ ] 或者保持当前机制但增加可靠性

---

### 节点1：AI明确需求（AI节点 - 需要验收）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - executeAINodeWithRetry方法

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` - clarifyRequirement
- `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` - clarifyRequirement

**审查结果：**
- [ ] 前端调用 `/v1/ai/clarify-requirement`
- [ ] 后端调用AI模型
- [ ] 【需要修改】后端增加Feedback Shadow验证
- [ ] 【需要修改】后端返回success字段
- [ ] 前端根据success判断成功/失败

**问题记录：**
1. 当前前端调用Feedback Shadow验证（需要移除）
2. AI响应DTO缺少success字段

**修复清单：**
- [ ] 修改 `ClarifyRequirementResponse` DTO，添加success字段
- [ ] 修改 `AiService.clarifyRequirement` 方法，集成Feedback Shadow验证
- [ ] 移除前端 `performFeedbackShadowValidation` 调用

---

### 节点2：需求确认验收（客户验收）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - onFlowNodeTap方法
- `miniprogram/pages/requirement/approve/approve.js`

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/RequirementController.java` - approveStage

**简化后流程：**
- [ ] 用户点击节点2（active状态）
- [ ] 【移除】前端不需要检查本地存储approval_data_${req.id}
- [ ] 跳转到approve页面
- [ ] 用户点击"继续"，调用 `/v1/requirements/{id}/approve`
- [ ] 后端更新currentFlowNode到3
- [ ] 前端设置auto_trigger_ai标记（节点3）

**说明：**
- 前端不需要管理approval_data本地存储
- 不需要处理问题和建议数据
- 用户点击验收节点直接跳转到approve页面即可

---

### 节点3：AI拆分任务（AI节点 - 自动继续）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - executeAINodeWithRetry方法

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` - splitTasks
- `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` - splitTasks

**审查结果：**
- [ ] 前端自动触发（auto_trigger_ai标记）
- [ ] 前端调用 `/v1/ai/split-tasks`
- [ ] 【需要修改】后端增加Feedback Shadow验证
- [ ] 【需要修改】后端返回success字段
- [ ] 前端根据success判断，成功后自动继续节点4

**修复清单：**
- [ ] 修改 `SplitTasksResponse` DTO，添加success字段
- [ ] 修改 `AiService.splitTasks` 方法，集成Feedback Shadow验证

---

### 节点4：AI开发（AI节点 - 自动继续）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - executeAINodeWithRetry方法

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` - generateCode
- `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` - generateCode

**审查结果：**
- [ ] 前端自动触发（节点3完成后）
- [ ] 前端调用 `/v1/ai/generate-code`
- [ ] 【需要修改】后端增加Feedback Shadow验证
- [ ] 【需要修改】后端返回success字段
- [ ] 前端根据success判断，成功后自动继续节点5

**修复清单：**
- [ ] 修改 `GenerateCodeResponse` DTO，添加success字段
- [ ] 修改 `AiService.generateCode` 方法，集成Feedback Shadow验证

---

### 节点5：AI功能测试（AI节点 - 自动继续）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - executeAINodeWithRetry方法

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` - functionalTest
- `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` - functionalTest

**审查结果：**
- [ ] 前端自动触发（节点4完成后）
- [ ] 前端调用 `/v1/ai/functional-test`
- [ ] 【需要修改】后端增加Feedback Shadow验证
- [ ] 【需要修改】后端返回success字段
- [ ] 前端根据success判断，成功后自动继续节点6

**修复清单：**
- [ ] 修改 `FunctionalTestResponse` DTO，添加success字段
- [ ] 修改 `AiService.functionalTest` 方法，集成Feedback Shadow验证

---

### 节点6：AI安全测试（AI节点 - 需要验收）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - executeAINodeWithRetry方法

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/AiController.java` - securityTest
- `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java` - securityTest

**审查结果：**
- [ ] 前端自动触发（节点5完成后）
- [ ] 前端调用 `/v1/ai/security-test`
- [ ] 【需要修改】后端增加Feedback Shadow验证
- [ ] 【需要修改】后端返回success字段
- [ ] 成功后更新到节点7（客户验收）

**修复清单：**
- [ ] 修改 `SecurityTestResponse` DTO，添加success字段
- [ ] 修改 `AiService.securityTest` 方法，集成Feedback Shadow验证

---

### 节点7：功能验收测试（客户验收）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - onFlowNodeTap方法
- `miniprogram/pages/requirement/approve/approve.js`

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/RequirementController.java` - approveStage

**审查结果：**
- [ ] 用户点击节点7（active状态）
- [ ] 跳转到approve页面
- [ ] 用户点击"继续"，调用 `/v1/requirements/{id}/approve`
- [ ] 后端更新currentFlowNode到8

---

### 节点8：打包交付（系统处理）

**审查结果：**
- [ ] 系统自动处理
- [ ] 更新currentFlowNode到9

---

### 节点9：最终验收（客户验收）

**前端代码位置：**
- `miniprogram/pages/index/index.js` - onFlowNodeTap方法
- `miniprogram/pages/requirement/approve/approve.js`

**后端代码位置：**
- `springboot-backend/src/main/java/com/jiedan/controller/RequirementController.java` - approveStage

**审查结果：**
- [ ] 用户点击节点9（active状态）
- [ ] 跳转到approve页面
- [ ] 用户点击"继续"，调用 `/v1/requirements/{id}/approve`
- [ ] 后端更新currentFlowNode到10

---

### 节点10：项目完成

**审查结果：**
- [ ] 项目状态变为completed
- [ ] 流程结束

---

## 代码修改检查清单

### 后端修改

#### 1. DTO修改
- [ ] `ClarifyRequirementResponse` - 添加success、errorMessage、validationDecision、validationIssues字段
- [ ] `SplitTasksResponse` - 添加success、errorMessage、validationDecision、validationIssues字段
- [ ] `GenerateCodeResponse` - 添加success、errorMessage、validationDecision、validationIssues字段
- [ ] `FunctionalTestResponse` - 添加success、errorMessage、validationDecision、validationIssues字段
- [ ] `SecurityTestResponse` - 添加success、errorMessage、validationDecision、validationIssues字段

#### 2. Service修改
- [ ] `AiService.clarifyRequirement` - 集成Feedback Shadow验证
- [ ] `AiService.splitTasks` - 集成Feedback Shadow验证
- [ ] `AiService.generateCode` - 集成Feedback Shadow验证
- [ ] `AiService.functionalTest` - 集成Feedback Shadow验证
- [ ] `AiService.securityTest` - 集成Feedback Shadow验证
- [ ] 【新增】`AiRetryService` - 重试机制

#### 3. Controller删除
- [ ] 删除 `FeedbackShadowController.java`

### 前端修改

#### 1. API修改
- [ ] `miniprogram/utils/api.js` - 删除feedbackShadow相关接口

#### 2. 页面修改
- [ ] `miniprogram/pages/index/index.js` - 删除performFeedbackShadowValidation方法
- [ ] `miniprogram/pages/index/index.js` - 删除saveAIProduct方法
- [ ] `miniprogram/pages/index/index.js` - 修改executeAINodeWithRetry方法（移除AI产物保存和approval_data保存）
- [ ] `miniprogram/pages/index/index.js` - 简化onFlowNodeTap方法（移除本地存储检查）

---

## 测试检查清单

### 单元测试
- [ ] AiService.clarifyRequirement测试
- [ ] AiService.splitTasks测试
- [ ] AiService.generateCode测试
- [ ] AiService.functionalTest测试
- [ ] AiService.securityTest测试
- [ ] FeedbackShadowService.validateWithAI测试

### 接口测试
- [ ] POST /v1/ai/clarify-requirement
- [ ] POST /v1/ai/split-tasks
- [ ] POST /v1/ai/generate-code
- [ ] POST /v1/ai/functional-test
- [ ] POST /v1/ai/security-test

### 端到端测试
- [ ] 创建新项目流程
- [ ] step页面5步流程
- [ ] step_all页面3阶段流程
- [ ] AI节点1自动触发
- [ ] AI节点链式执行（1→3→4→5→6）
- [ ] 客户验收节点2
- [ ] 客户验收节点7
- [ ] 客户验收节点9

### 异常测试
- [ ] AI接口超时
- [ ] Feedback Shadow验证失败（REJECT）
- [ ] Feedback Shadow验证需要修复（REPAIR）
- [ ] 重试机制（3次）
- [ ] 超过最大重试次数

---

## 验收标准检查清单

### 功能验收
- [ ] FeedbackShadowController已删除
- [ ] AiService集成Feedback Shadow验证
- [ ] AI接口返回包含success字段
- [ ] 前端不再调用Feedback Shadow接口
- [ ] AI流程正常运行（从节点1到节点6）
- [ ] 客户验收流程正常

### 性能验收
- [ ] AI接口响应时间 < 3分钟
- [ ] Feedback Shadow验证不阻塞主流程
- [ ] 重试机制正常工作

### 兼容性验收
- [ ] 现有需求数据不受影响
- [ ] 前端页面正常显示
- [ ] 后端API响应格式兼容

---

## 风险检查清单

### 高风险
- [ ] Feedback Shadow验证失败导致AI流程卡住
  - 应对措施：增加最大重试次数，超过后标记为失败

### 中风险
- [ ] AI接口响应时间增加
  - 应对措施：增加超时时间，或者异步执行验证

### 低风险
- [ ] 前端兼容性问题
  - 应对措施：保持API响应格式兼容

---

## 文档检查清单

- [x] spec.md - 需求规格说明书
- [x] tasks.md - 任务分解
- [x] checklist.md - 检查清单
- [ ] API文档更新
- [ ] 部署文档更新

---

## 最终确认

**重构前确认：**
- [ ] 已备份代码
- [ ] 已创建feature分支
- [ ] 已通知相关团队成员

**重构后确认：**
- [ ] 所有检查项已完成
- [ ] 所有测试通过
- [ ] 代码已review
- [ ] 已合并到主分支
