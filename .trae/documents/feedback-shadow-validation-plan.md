# Feedback Shadow验证逻辑修改计划

## 任务目标

1. 修改Feedback Shadow验证逻辑，限制只验证DTO定义的字段
2. 所有AI接口（clarify-requirement、split-tasks、generate-code、functional-test、security-test）都遵循此规则
3. 拆分任务（split-tasks）去掉"预估工时"要求

## 当前问题分析

### Feedback Shadow当前验证方式
- 使用通用Prompt进行质量检测
- 验证维度：完整性、准确性、规范性、一致性、可行性
- 会检测AI生成文档中的所有内容，不限于DTO定义的字段

### 需要修改的内容
1. **FeedbackShadowPromptBuilder.java** - 修改各API类型的特定检测重点
2. **SplitTasksRequest.java** - 移除estimatedTotalHours字段（如需要）
3. **SplitTasksResponse.java** - 调整返回字段

## 详细实施步骤

### 步骤1：修改FeedbackShadowPromptBuilder

文件：`springboot-backend/src/main/java/com/jiedan/service/ai/feedback/FeedbackShadowPromptBuilder.java`

修改 `getApiTypeSpecificPrompt` 方法，为每个API类型定义明确的验证范围：

#### 1.1 clarify-requirement（AI明确需求）
验证范围（只验证DTO定义的字段）：
- summary（需求概述）
- functionalModules（功能模块清单）
- userRoles（用户角色定义）
- coreBusinessProcesses（核心业务流程）
- nonFunctionalRequirements（非功能性需求建议）

不验证：P0优先级模块、技术架构、工时估算等超出规范的内容

#### 1.2 split-tasks（AI拆分任务）
验证范围：
- tasks（任务列表）- 包含taskName、description、estimatedHours、priority、dependencies
- totalTasks（任务总数）

**修改点**：去掉对"预估工时合理性"的强制要求

#### 1.3 generate-code（AI生成代码）
验证范围：
- code（生成的代码）
- explanation（代码说明）

#### 1.4 functional-test（AI功能测试）
验证范围：
- testCases（测试用例列表）
- testCode（测试代码）

#### 1.5 security-test（AI安全测试）
验证范围：
- vulnerabilities（漏洞列表）
- summary（测试摘要）

### 步骤2：修改通用检测Prompt

在 `buildDetectionPrompt` 方法中添加明确说明：
```
【验证范围限制】
只验证DTO规范中定义的字段，不要求补充超出规范的内容。
如果AI生成的文档包含额外内容，只要核心字段完整即可通过验证。
```

### 步骤3：修改拆分任务相关代码（可选）

如果业务上确实需要移除"预估工时"：

#### 3.1 修改SplitTasksRequest.java
- 移除estimatedTotalHours字段
- 修改相关验证逻辑

#### 3.2 修改AiService.splitTasks方法
- 移除对estimatedTotalHours的处理
- 修改Prompt构建逻辑

#### 3.3 修改SplitTasksResponse.java
- 调整返回字段结构

### 步骤4：编译验证

1. 编译后端代码
2. 运行测试
3. 打包部署

## 修改后的验证Prompt示例

### clarify-requirement验证Prompt
```
【clarify-requirement验证范围】
只验证以下DTO字段是否存在且合理：
1. summary（需求概述）- 必须存在，描述项目整体目标
2. functionalModules（功能模块清单）- 必须存在，列出核心功能模块
3. userRoles（用户角色定义）- 必须存在，定义系统用户角色
4. coreBusinessProcesses（核心业务流程）- 必须存在，描述主要业务流程
5. nonFunctionalRequirements（非功能性需求建议）- 可选，性能/安全等建议

【不验证内容】
- 不验证P0优先级模块（超出DTO规范）
- 不验证技术架构细节（超出DTO规范）
- 不验证工时估算（超出DTO规范）
- 不强制要求补充DTO未定义的字段

【通过标准】
只要上述核心字段存在且内容合理，即可通过验证（ALLOW）。
```

### split-tasks验证Prompt
```
【split-tasks验证范围】
只验证以下DTO字段是否存在且合理：
1. tasks（任务列表）- 必须存在，每个任务包含：
   - taskName（任务名称）
   - description（任务描述）
   - estimatedHours（预估工时）- 存在即可，不验证合理性
   - priority（优先级）
   - dependencies（依赖任务）
2. totalTasks（任务总数）- 必须存在

【不验证内容】
- 不验证预估工时的准确性（只要存在即可）
- 不验证任务拆分的粒度是否合理
- 不强制要求补充DTO未定义的字段
```

## 验收标准

1. Feedback Shadow只验证DTO定义的字段
2. AI生成额外内容不会导致验证失败
3. 拆分任务不再强制要求预估工时合理性
4. 所有AI接口编译通过
5. 测试用例通过

## 风险评估

| 风险 | 等级 | 应对措施 |
|------|------|----------|
| 验证放宽导致质量下降 | 中 | 保留核心字段验证，只是不强制额外内容 |
| 业务逻辑变更影响 | 低 | 预估工时字段保留，只是不强制验证合理性 |
| 测试用例失败 | 低 | 同步更新测试用例 |
