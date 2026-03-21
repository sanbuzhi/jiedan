# AI异步阶段式代码生成系统 - 开发任务

## 任务总览

本任务将开发一个完整的AI异步阶段式代码生成系统，包含：

* 1个核心启动接口：POST /api/ai/code/project/start

* 4个辅助查询接口

* 数据库表设计

* 核心业务逻辑实现

***

## 阶段1：数据库与基础设施

### Task 1.1: 创建数据库表

* [x] Task 1.1.1: 创建 ai\_development\_project 表

  * 创建数据库migration脚本 V7\_\_ai\_development\_project.sql

  * 包含字段：id, project\_id, project\_name, task\_doc, status, current\_phase, progress, total\_files, error\_message, started\_at, completed\_at, created\_at, updated\_at

* [x] Task 1.1.2: 创建 ai\_development\_phase 表

  * 创建数据库migration脚本 V8\_\_ai\_development\_phase.sql

  * 包含字段：id, project\_id, phase, phase\_name, status, total\_rounds, total\_files, session\_id, summary, error\_message, started\_at, completed\_at, created\_at, updated\_at

* [x] Task 1.1.3: 创建 ai\_development\_round 表

  * 创建数据库migration脚本 V9\_\_ai\_development\_round.sql

  * 包含字段：id, phase\_id, round\_number, status, input\_prompt, output\_content, tokens\_used, files\_count, continuation, finish\_reason, error\_message, retry\_count, started\_at, completed\_at, created\_at

* [x] Task 1.1.4: 创建 ai\_development\_file 表

  * 创建数据库migration脚本 V10\_\_ai\_development\_file.sql

  * 包含字段：id, project\_id, phase, round\_number, file\_path, file\_size, file\_type, is\_complete, created\_at

***

## 阶段2：实体类与Repository

### Task 2.1: 创建实体类

* [x] Task 2.1.1: 创建 AiDevelopmentProject 实体类

  * 路径：src/main/java/com/jiedan/entity/AiDevelopmentProject.java

  * 包含所有字段对应的属性

* [x] Task 2.1.2: 创建 AiDevelopmentPhase 实体类

  * 路径：src/main/java/com/jiedan/entity/AiDevelopmentPhase.java

  * 包含所有字段对应的属性

* [x] Task 2.1.3: 创建 AiDevelopmentRound 实体类

  * 路径：src/main/java/com/jiedan/entity/AiDevelopmentRound.java

  * 包含所有字段对应的属性

* [x] Task 2.1.4: 创建 AiDevelopmentFile 实体类

  * 路径：src/main/java/com/jiedan/entity/AiDevelopmentFile.java

  * 包含所有字段对应的属性

### Task 2.2: 创建Repository

* [x] Task 2.2.1: 创建 AiDevelopmentProjectRepository

  * 路径：src/main/java/com/jiedan/repository/AiDevelopmentProjectRepository.java

  * 包含基本的CRUD方法

* [x] Task 2.2.2: 创建 AiDevelopmentPhaseRepository

  * 路径：src/main/java/com/jiedan/repository/AiDevelopmentPhaseRepository.java

  * 包含基本的CRUD方法及按项目ID查询方法

* [x] Task 2.2.3: 创建 AiDevelopmentRoundRepository

  * 路径：src/main/java/com/jiedan/repository/AiDevelopmentRoundRepository.java

  * 包含基本的CRUD方法及按阶段ID查询方法

* [x] Task 2.2.4: 创建 AiDevelopmentFileRepository

  * 路径：src/main/java/com/jiedan/repository/AiDevelopmentFileRepository.java

  * 包含基本的CRUD方法及按项目ID和阶段查询方法

***

## 阶段3：阶段配置常量

### Task 3.1: 定义阶段配置

* [x] Task 3.1.1: 创建 PhaseConfig 配置类

  * 路径：src/main/java/com/jiedan/config/AiDevelopmentConfig.java

  * 定义7个阶段的配置：名称、关键词、预估文件数

  * 使用@Value或配置类注入

***

## 阶段4：核心业务逻辑

### Task 4.1: 项目管理器 - 启动项目

* [x] Task 4.1.1: 创建 ProjectDevelopmentManager 主类

  * 路径：src/main/java/com/jiedan/service/ai/ProjectDevelopmentManager.java

  * 核心方法：startProject(projectId, projectName, taskDoc)

  * 实现：参数校验、创建目录、初始化数据库、异步启动

* [x] Task 4.1.2: 实现目录创建方法

  * createProjectDirectoryStructure(projectId)

  * 创建 projects/{projectId}/ 目录结构

* [x] Task 4.1.3: 实现数据库初始化

  * saveProjectToDatabase(project)

  * 插入 ai\_development\_project 记录

### Task 4.2: 项目管理器 - 阶段执行

* [x] Task 4.2.1: 实现 executePhase 方法

  * 参数：phase, taskDoc, previousPhaseSummary

  * 实现：创建阶段目录、初始化阶段数据库、创建新会话

* [x] Task 4.2.2: 实现轮次循环逻辑

  * while循环调用 executeRound

  * 调用 checkShouldContinue 判断是否继续

### Task 4.3: 单轮执行核心逻辑

* [x] Task 4.3.1: 实现 Prompt 构建方法

  * buildFirstRoundPrompt(taskDoc, phaseConfig)

  * buildContinueRoundPrompt(phaseConfig, currentFileList)

* [x] Task 4.3.2: 实现 AI 调用方法（带重试）

  * callAIWithRetry(sessionId, prompt, maxTokens)

  * 实现30次重试 + 指数退避逻辑

* [x] Task 4.3.3: 实现截断检测与续传

  * handleTruncation(sessionId, content)

  * 最多5次续传

### Task 4.4: 文件解析与保存

* [x] Task 4.4.1: 实现 parseAndSaveFiles 方法

  * 正则匹配 ===FILE:=== 格式

  * 安全检查：防止目录遍历

  * 保存文件到对应目录

* [x] Task 4.4.2: 实现进度文档更新

  * updateProgressDocument(projectId, phase, roundNumber, files)

  * 追加到 progress.md

* [x] Task 4.4.3: 实现会话记录保存

  * saveSessionRecord(sessionId, roundNumber, input, output)

  * 保存到 sessions/ 目录

### Task 4.5: 阶段完成判断

* [x] Task 4.5.1: 实现 checkShouldContinue 方法

  * 判断条件1：AI明确表示完成

  * 判断条件2：文件覆盖率达到80%

  * 判断条件3：连续5轮无新文件

  * 判断条件4：超过最大轮次20轮

  * 判断条件5：截断后续传完成

* [x] Task 4.5.2: 实现阶段摘要生成

  * generatePhaseSummary(phase, phaseConfig, fileList)

  * 生成 summary.md

***

## 阶段5：Controller层 - 启动接口

### Task 5.1: 核心启动接口

* [x] Task 5.1.1: 创建 ProjectDevelopmentController

  * 路径：src/main/java/com/jiedan/controller/ai/ProjectDevelopmentController.java

* [x] Task 5.1.2: 实现启动项目接口

  * POST /api/ai/code/project/start

  * 请求DTO：StartProjectRequest

  * 响应DTO：StartProjectResponse

***

## 阶段6：Controller层 - 查询接口

### Task 6.1: 查询项目状态

* [x] Task 6.1.1: 实现 GET /api/ai/code/project/{projectId}/status

  * 返回项目整体进度

  * 包含所有阶段详情

### Task 6.2: 查询阶段状态

* [x] Task 6.2.1: 实现 GET /api/ai/code/project/{projectId}/phase/{phase}/status

  * 返回指定阶段进度

  * 包含生成文件列表

### Task 6.3: 查询进度文档

* [x] Task 6.3.1: 实现 GET /api/ai/code/project/{projectId}/phase/{phase}/progress

  * 返回进度文档Markdown内容

### Task 6.4: 查询轮次详情

* [x] Task 6.4.1: 实现 GET /api/ai/code/project/{projectId}/phase/{phase}/round/{round}

  * 返回轮次详细信息

  * 包含输入输出摘要

***

## 阶段7：响应DTO设计

### Task 7.1: 启动接口DTO

* [x] Task 7.1.1: 创建 StartProjectRequest

  * 路径：src/main/java/com/jiedan/dto/ai/code/StartProjectRequest.java

  * 字段：projectId, projectName, taskDoc

* [x] Task 7.1.2: 创建 StartProjectResponse

  * 路径：src/main/java/com/jiedan/dto/ai/code/StartProjectResponse.java

  * 字段：projectId, status, message

### Task 7.2: 状态查询DTO

* [x] Task 7.2.1: 创建 ProjectStatusResponse

  * 字段：projectId, projectName, status, currentPhase, progress, phases\[], totalFiles, totalRounds等

* [x] Task 7.2.2: 创建 PhaseStatusResponse

  * 字段：phase, phaseName, status, currentRound, totalRounds, generatedFiles, pendingFiles, progress等

* [x] Task 7.2.3: 创建 PhaseProgressResponse

  * 字段：projectId, phase, content, lastUpdatedAt

* [x] Task 7.2.4: 创建 RoundDetailResponse

  * 字段：phase, round, status, inputSummary, outputSummary, tokensUsed, continuationCount, finishReason, duration等

***

## 阶段8：配置与常量

### Task 8.1: AI配置

* [x] Task 8.1.1: 创建开发配置类

  * 路径：src/main/java/com/jiedan/config/AiDevelopmentConfig.java

  * 配置项：maxRetries=30, maxTokens=32000, maxContinuations=5, maxRoundsPerPhase=20

***

## 阶段9：测试与验证

### Task 9.1: 单元测试

* [ ] Task 9.1.1: 测试文件解析方法

  * 测试各种文件路径的解析

  * 测试安全检查逻辑

* [ ] Task 9.1.2: 测试阶段配置

  * 验证7个阶段配置正确

### Task 9.2: 集成测试

* [ ] Task 9.2.1: 测试启动接口

  * 验证接口正常调用

  * 验证异步执行

* [ ] Task 9.2.2: 测试查询接口

  * 验证状态查询正常返回

***

## 任务依赖关系

```
阶段1 (数据库) → 阶段2 (实体类) → 阶段3 (配置) → 阶段4 (核心逻辑)
                                                            ↓
阶段7 (DTO) ← 阶段6 (查询接口) ← 阶段5 (启动接口)
                                                            ↓
                                                  阶段8 (配置)
                                                            ↓
                                                  阶段9 (测试)
```

***

## 预估工作量

| 阶段        | 任务数    | 预估工时     |
| --------- | ------ | -------- |
| 阶段1: 数据库  | 4      | 0.5天     |
| 阶段2: 实体类  | 8      | 0.5天     |
| 阶段3: 配置   | 1      | 0.25天    |
| 阶段4: 核心逻辑 | 12     | 3天       |
| 阶段5: 启动接口 | 2      | 0.5天     |
| 阶段6: 查询接口 | 4      | 1天       |
| 阶段7: DTO  | 4      | 0.5天     |
| 阶段8: 配置   | 1      | 0.25天    |
| 阶段9: 测试   | 3      | 1天       |
| **总计**    | **39** | **7.5天** |

