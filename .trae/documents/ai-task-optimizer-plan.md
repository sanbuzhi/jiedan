# AI任务优化改造计划

## 目标
优化AI拆分任务流程，移除前端重试机制，改为后端并行执行多个任务，并引入"任务决策者"选择最佳结果。

## 改造内容

### 1. 移除前端重试机制

**文件**: `miniprogram/pages/index/index.js`

**修改内容**:
- 将AI节点配置中的 `maxRetries` 从 3 改为 0
- 移除 `executeAINodeWithRetry` 方法中的重试逻辑
- 失败时直接提示用户，不再自动重试

### 2. 后端并行执行改造

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java`

**修改内容**:
- 新增 `splitTasksParallel` 方法，并行触发3个拆分任务
- 使用 `CompletableFuture` 实现并行执行
- 监听每个任务的结果，一旦有任务生成FINAL文件，立即返回
- 如果3个任务都未完成，则进入决策流程

### 3. 新增任务决策者服务

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/TaskDecisionService.java`

**功能**:
- 接收系统提示词、用户提示词和所有临时任务文件
- 调用AI模型进行决策分析
- 返回最佳版本的任务书

**决策维度**:
- 用户需求匹配度（任务书内容是否符合用户原始需求）
- 文档完整性（7个章节是否齐全）
- 内容详细程度
- 技术可行性

### 4. 新增并行任务执行器

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/AiParallelExecutor.java`

**功能**:
- 管理并行任务的生命周期
- 监控任务执行状态
- 收集任务结果
- 触发决策流程

### 5. 新增决策Prompt模板

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/prompt/AiPromptTemplate.java`

**新增内容**:
- `TASK_DECISION_SYSTEM`: 任务决策者系统提示词
- `buildTaskDecisionUserPrompt`: 构建决策用户提示词方法

## 执行步骤

### 阶段1: 移除前端重试
1. 修改 `miniprogram/pages/index/index.js`
   - 修改AI_NODE_CONFIG配置
   - 简化executeAINodeWithRetry方法

### 阶段2: 创建任务决策者服务
1. 创建 `TaskDecisionService.java`
2. 实现决策逻辑
3. 添加AI模型调用

### 阶段3: 创建并行执行器
1. 创建 `AiParallelExecutor.java`
2. 实现并行任务管理
3. 集成任务决策者

### 阶段4: 改造AiService
1. 新增 `splitTasksParallel` 方法
2. 修改原有 `splitTasks` 方法调用链
3. 集成并行执行器

### 阶段5: 添加决策Prompt
1. 修改 `AiPromptTemplate.java`
2. 添加决策相关Prompt模板

### 阶段6: 测试验证
1. 单元测试
2. 集成测试
3. 端到端测试

## 技术细节

### 并行执行流程
```
splitTasksParallel 调用
  ↓
创建3个 CompletableFuture 任务
  ↓
并行执行3个AI拆分任务
  ↓
┌─────────────────────────────────────┐
│ 监听器检查是否有FINAL文件生成          │
│                                     │
│ YES → 立即返回该结果                 │
│                                     │
│ NO  → 等待所有任务完成               │
│     → 调用任务决策者                 │
│     → 返回决策结果                   │
└─────────────────────────────────────┘
```

### 任务决策者输入
```
系统提示词: AiPromptTemplate.SPLIT_TASKS_SYSTEM
用户提示词: 需求文档内容
候选版本: [V1内容, V2内容, V3内容]
```

### 任务决策者输出
```
{
  "selectedVersion": "V2",
  "reason": "用户需求匹配度高，文档结构完整",
  "improvements": ["建议补充数据库设计细节"]
}
```

## 风险控制

1. **超时控制**: 每个并行任务设置10分钟超时
2. **资源限制**: 使用线程池限制并发数
3. **降级策略**: 决策失败时返回最新版本
4. **日志记录**: 详细记录决策过程和原因

## 预期效果

1. 减少前端复杂度
2. 提高任务成功率
3. 通过并行执行减少总体等待时间
4. 通过AI决策提高结果质量
