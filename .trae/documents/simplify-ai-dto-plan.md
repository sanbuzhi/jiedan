# AI接口简化计划：移除DTO，直接生成MD文档

## 任务目标

1. **AI明确需求(clarify-requirement)** 和 **AI拆分任务(split-tasks)** 接口简化
2. 移除复杂的DTO定义，接口只生成Markdown文档内容
3. Feedback Shadow只对文档内容进行验证，不验证DTO字段
4. 简化后的接口更易用、更灵活

## 当前问题分析

### 当前实现的问题

1. **DTO过于复杂**
   - ClarifyRequirementRequest/Response 包含多个字段
   - SplitTasksRequest/Response 包含任务列表等复杂结构
   - 需要复杂的解析逻辑将AI生成的文本转换为DTO

2. **Feedback Shadow验证过于严格**
   - 需要验证DTO定义的每个字段
   - 限制了AI生成的灵活性
   - 增加了验证失败的可能性

3. **维护成本高**
   - DTO变更需要同步修改Prompt、验证逻辑、前端代码
   - 解析逻辑容易出错

## 简化方案

### 简化后的接口设计

#### 1. AI明确需求 (clarify-requirement)

**请求简化：**
```java
// 简化前：ClarifyRequirementRequest (7个字段)
// 简化后：只需要 projectId + requirementDescription
public class ClarifyRequirementRequest {
    private String projectId;           // 项目ID
    private String requirementDescription;  // 需求描述（用户输入）
}
```

**响应简化：**
```java
// 简化前：ClarifyRequirementResponse (10+个字段)
// 简化后：只返回文档内容 + 验证结果
public class ClarifyRequirementResponse {
    private boolean success;            // 是否成功
    private String errorMessage;        // 错误信息
    private String documentContent;     // 需求文档内容（Markdown格式）
    private String validationDecision;  // 验证决策
    private List<String> validationIssues;  // 验证问题
}
```

**AI Prompt简化：**
```
你是一位专业的需求分析师。请根据用户需求，生成一份完整的需求文档。

要求：
1. 使用Markdown格式
2. 包含以下内容：
   - 需求概述
   - 功能模块清单
   - 用户角色定义
   - 核心业务流程
   - 非功能性需求建议（可选）
3. 文档要详细、完整、可实施

不需要输出JSON，直接输出Markdown文档即可。
```

#### 2. AI拆分任务 (split-tasks)

**请求简化：**
```java
// 简化前：SplitTasksRequest (5个字段)
// 简化后：只需要 projectId + requirementDoc
public class SplitTasksRequest {
    private String projectId;           // 项目ID
    private String requirementDoc;      // 需求文档内容（上一步生成的MD）
}
```

**响应简化：**
```java
// 简化前：SplitTasksResponse (包含TaskItem列表)
// 简化后：只返回文档内容 + 验证结果
public class SplitTasksResponse {
    private boolean success;            // 是否成功
    private String errorMessage;        // 错误信息
    private String documentContent;     // 任务拆分文档（Markdown格式）
    private String validationDecision;  // 验证决策
    private List<String> validationIssues;  // 验证问题
}
```

**AI Prompt简化：**
```
你是一位专业的项目经理。请根据需求文档，生成详细的任务拆分文档。

要求：
1. 使用Markdown格式
2. 包含以下内容：
   - 任务列表（每个任务包含名称、描述、预估工时、优先级）
   - 任务依赖关系
   - 关键里程碑
3. 任务要可执行、可跟踪

不需要输出JSON，直接输出Markdown文档即可。
```

### Feedback Shadow验证简化

**验证内容简化：**
- 不再验证DTO字段是否存在
- 只验证Markdown文档的完整性和合理性
- 检查文档结构是否清晰
- 检查内容是否与输入需求匹配

**简化后的验证Prompt：**
```
你是一位技术评审专家。请对以下Markdown文档进行质量检测。

检测维度：
1. 文档完整性：是否包含必要的章节
2. 内容合理性：内容是否详细、可实施
3. 格式规范性：Markdown格式是否正确

不强制要求特定字段，只要文档整体质量合格即可通过。
```

## 详细实施步骤

### 步骤1：修改DTO

#### 1.1 修改ClarifyRequirementRequest
- 保留：projectId, requirementDescription
- 移除：projectName, requirementId, projectType, userRole, model

#### 1.2 修改ClarifyRequirementResponse
- 保留：success, errorMessage, validationDecision, validationIssues
- 修改：documentContent（替代原有的多个字段）
- 移除：summary, functionalModules, userRoles, coreBusinessProcesses, nonFunctionalRequirements, fullRequirementDoc, rawResponse, model

#### 1.3 修改SplitTasksRequest
- 保留：projectId
- 修改：requirementDoc（替代原有的多个字段）
- 移除：requirementDescription, functionalModules, estimatedTotalHours, model

#### 1.4 修改SplitTasksResponse
- 保留：success, errorMessage, validationDecision, validationIssues
- 修改：documentContent（替代原有的TaskItem列表）
- 移除：tasks, totalTasks, totalEstimatedHours, rawResponse, model

### 步骤2：修改AiService

#### 2.1 修改clarifyRequirement方法
- 简化Prompt构建，直接要求生成Markdown
- 移除复杂的字段解析逻辑
- 直接返回documentContent

#### 2.2 修改splitTasks方法
- 简化Prompt构建，直接要求生成Markdown
- 移除任务列表解析逻辑
- 直接返回documentContent

#### 2.3 删除未使用的解析方法
- 删除parseTasksFromContent方法
- 删除相关的TaskItem解析逻辑

### 步骤3：修改FeedbackShadowPromptBuilder

#### 3.1 修改clarify-requirement验证Prompt
- 移除DTO字段验证要求
- 改为文档内容质量验证

#### 3.2 修改split-tasks验证Prompt
- 移除DTO字段验证要求
- 改为文档内容质量验证

### 步骤4：修改前端API（如需要）

#### 4.1 修改小程序端api.js
- 简化请求参数
- 简化响应处理

#### 4.2 修改Web端api.js
- 添加AI接口配置（如果还没有）
- 简化请求参数

### 步骤5：编译验证

1. 编译后端代码
2. 运行测试
3. 打包部署

## 文件修改清单

### 后端DTO（4个文件）
1. `ClarifyRequirementRequest.java` - 简化字段
2. `ClarifyRequirementResponse.java` - 简化字段
3. `SplitTasksRequest.java` - 简化字段
4. `SplitTasksResponse.java` - 简化字段

### 后端Service（2个文件）
5. `AiService.java` - 简化AI调用逻辑
6. `FeedbackShadowPromptBuilder.java` - 简化验证Prompt

### 前端（2个文件）
7. `miniprogram/utils/api.js` - 简化调用（如需要）
8. `frontend/src/utils/api.js` - 添加AI接口（如果还没有）

## 验收标准

1. clarify-requirement接口只接收projectId和requirementDescription
2. split-tasks接口只接收projectId和requirementDoc
3. 两个接口都只返回documentContent（Markdown文档）
4. Feedback Shadow只验证文档内容质量，不验证DTO字段
5. 移除所有复杂的字段解析逻辑
6. 所有接口编译通过
7. 测试用例通过

## 风险评估

| 风险 | 等级 | 应对措施 |
|------|------|----------|
| 前端需要适配新接口 | 中 | 同步修改前端代码，简化响应处理 |
| 文档格式不统一 | 低 | 通过Prompt规范输出格式 |
| 后续需要结构化数据 | 低 | 可以在需要时从Markdown解析 |

## 预期收益

1. **代码简化**：DTO从10+个字段简化为2-3个字段
2. **维护成本降低**：不需要维护复杂的解析逻辑
3. **灵活性提高**：AI可以更自由地生成内容
4. **验证通过率提高**：不再因为缺少特定字段而失败
