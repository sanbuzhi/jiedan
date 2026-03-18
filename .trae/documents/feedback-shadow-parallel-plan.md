# Feedback Shadow + 并行执行改造计划

## 目标
保留Feedback Shadow验证机制，同时实现并行执行。每个并行任务内部仍然进行Feedback Shadow验证和重试，确保文档完整性和质量。

## 核心设计

### 版本文件生成规则
- 总版本数 = Feedback Shadow验证次数 × 并行任务数
- 示例：如果每个并行任务重试3次，3个并行任务，则最多生成 3×3 = 9 个版本文件
- 版本命名：V{并行任务索引}-{验证次数}，如 V1-1, V1-2, V1-3, V2-1, V2-2...

### 禁止截断要求
- 每个版本文件必须完整保存，不允许任何截断
- 使用续传机制确保内容完整性
- 验证失败时保存完整文档，便于后续分析和决策

## 改造内容

### 1. 改造AiParallelExecutor

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/AiParallelExecutor.java`

**修改内容**:
- 每个并行任务内部调用 `AiRetryService.executeWithRetry`
- 保留Feedback Shadow验证逻辑
- 版本文件命名改为 `V{taskIndex}-{retryCount}` 格式
- 确保每个版本完整保存，不截断

### 2. 改造AiService.executeSplitTasksOnceInternal

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/AiService.java`

**修改内容**:
- 该方法改为支持Feedback Shadow验证
- 接收 `taskIndex` 和 `retryCount` 参数
- 版本保存使用 `V{taskIndex}-{retryCount}` 命名

### 3. 改造TaskDecisionService

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/TaskDecisionService.java`

**修改内容**:
- 支持读取多个版本的任务书（最多9个）
- 决策时考虑Feedback Shadow的验证结果
- 优先选择通过验证（ALLOW）的版本

### 4. 新增版本收集器

**文件**: `springboot-backend/src/main/java/com/jiedan/service/ai/VersionCollector.java`

**功能**:
- 收集所有并行任务生成的版本
- 记录每个版本的验证结果
- 为任务决策者提供完整信息

## 执行步骤

### 阶段1: 创建版本收集器
1. 创建 `VersionCollector.java`
2. 定义版本信息结构（版本号、内容、验证结果、验证问题）
3. 实现版本收集和查询方法

### 阶段2: 改造AiParallelExecutor
1. 修改并行任务执行逻辑
2. 每个任务内部使用 `AiRetryService.executeWithRetry`
3. 版本命名改为 `V{taskIndex}-{retryCount}`
4. 集成版本收集器

### 阶段3: 改造AiService
1. 修改 `executeSplitTasksOnceInternal` 方法
2. 支持接收 retryCount 参数
3. 使用新的版本命名规则

### 阶段4: 改造TaskDecisionService
1. 支持读取多个版本（最多9个）
2. 决策时考虑验证结果
3. 优先选择ALLOW状态的版本

### 阶段5: 测试验证
1. 单元测试
2. 集成测试
3. 验证版本文件完整性

## 技术细节

### 并行执行流程（含Feedback Shadow）
```
splitTasksParallel 调用
  ↓
创建3个 CompletableFuture 任务
  ↓
并行执行3个AI拆分任务（每个内部有Feedback Shadow验证）
  ↓
任务1: V1-1(验证REPAIR) → V1-2(验证REPAIR) → V1-3(验证ALLOW) ✅
任务2: V2-1(验证REJECT) → V2-2(验证ALLOW) ✅
任务3: V3-1(验证ALLOW) ✅
  ↓
收集所有版本和验证结果
  ↓
调用任务决策者
  ↓
优先选择ALLOW版本（V1-3, V2-2, V3-1）
  ↓
在ALLOW版本中选择最佳
  ↓
返回决策结果
```

### 版本信息结构
```java
public class VersionInfo {
    private String versionId;           // V1-1, V1-2, V2-1...
    private String content;             // 完整文档内容
    private String validationDecision;  // ALLOW/REPAIR/REJECT
    private List<String> validationIssues;  // 验证问题
    private int taskIndex;              // 并行任务索引
    private int retryCount;             // 重试次数
}
```

### 决策优先级
1. **第一优先级**: 验证状态为ALLOW的版本
2. **第二优先级**: 验证状态为REPAIR的版本
3. **第三优先级**: 验证状态为REJECT的版本（如果都没有ALLOW/REPAIR）

在同级优先级内，按决策维度（用户需求匹配度、文档完整性等）选择最佳。

## 风险控制

1. **文件数量控制**: 最多9个版本文件，不会无限增长
2. **存储空间**: 每个版本完整保存，需要更多存储空间
3. **决策复杂度**: 版本增多会增加决策时间，需要优化决策算法
4. **超时控制**: 每个并行任务独立设置超时，避免整体超时

## 预期效果

1. **保留质量验证**: Feedback Shadow继续发挥作用
2. **提高成功率**: 并行执行增加成功概率
3. **完整记录**: 所有尝试版本都完整保存，便于分析
4. **智能决策**: 基于验证结果和文档质量双重选择
