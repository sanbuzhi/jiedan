# AI异步阶段式代码生成系统规格说明书

## 一、背景与目标

### 1.1 为什么需要这个系统

现有的AI代码生成接口存在以下问题：

* 接口模式为同步，无法支持中大型项目（30+页面）的代码生成

* 任务书过长时处理不规范

* 无法追踪生成进度

* 缺乏阶段式开发能力

本系统旨在提供一种**异步、按阶段、多轮会话**的AI代码生成方案，能够完整生成TASKS.md任务书规定的所有代码。

### 1.2 设计约束

基于用户测试数据：

* 每轮最佳输出：32K tokens（达成率76%）

* 超过64K后AI产出质量急剧下降

* 每轮字符数限制：10000-16000字符

* 截断处理：使用续传机制

* 重试机制：30次 + 指数退避

* 超时限制：12小时

***

## 二、系统架构

### 2.1 整体架构

```
┌─────────────────────────────────────────────────────────────────────┐
│                        项目开发管理器                                 │
│                    ProjectDevelopmentManager                          │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  startProject(taskDoc)                                             │
│       │                                                             │
│       ▼                                                             │
│  ┌─────────────────────────────────────────────────────────────┐   │
│  │ for phase = 1 to 7                                          │   │
│  │   │                                                          │   │
│  │   ▼                                                          │   │
│  │  ┌───────────────────────────────────────────────────────┐   │   │
│  │  │ startPhase(phase, taskDoc, previousPhaseSummary)    │   │   │
│  │  │   │                                                    │   │   │
│  │  │   ▼                                                    │   │   │
│  │  │  createNewSession()  ← 新会话，不带历史              │   │   │
│  │  │   │                                                    │   │   │
│  │  │   ▼                                                    │   │   │
│  │  │  ┌───────────────────────────────────────────────┐      │   │   │
│  │  │  │ while (needContinueRound)                   │      │   │   │
│  │  │  │   │                                          │      │   │   │
│  │  │  │   ▼                                          │      │   │   │
│  │  │  │  executeRound(session, roundInput)           │      │   │   │
│  │  │  │   │                                          │      │   │   │
│  │  │  │   ▼                                          │      │   │   │
│  │  │  │  parseAndSaveFiles(aiOutput)                │      │   │   │
│  │  │  │   │                                          │      │   │   │
│  │  │  │   ▼                                          │      │   │   │
│  │  │  │  updateProgressDoc()                         │      │   │   │
│  │  │  │   │                                          │      │   │   │
│  │  │  │   ▼                                          │      │   │   │
│  │  │  │  checkRoundStatus() → needContinueRound?    │      │   │   │
│  │  │  │                                              │      │   │   │
│  │  │  └───────────────────────────────────────────────┘      │   │   │
│  │  │   │                                                    │   │   │
│  │  │   ▼                                                    │   │   │
│  │  │  generatePhaseSummary()  ← 阶段完成摘要               │   │   │
│  │  └───────────────────────────────────────────────────────┘   │   │
│  │   │                                                          │   │
│  └───┴──────────────────────────────────────────────────────────┘   │
│       │                                                             │
│       ▼                                                             │
│  projectCompleted()                                                 │
└─────────────────────────────────────────────────────────────────────┘
```

***

## 三、API接口设计

### 3.1 核心接口：启动项目开发

```
POST /api/ai/code/project/start

请求：
{
    "projectId": "项目ID",
    "projectName": "项目名称",
    "taskDoc": "TASKS.md完整内容"
}

响应：
{
    "success": true,
    "data": {
        "projectId": "xxx",
        "status": "PROCESSING",
        "message": "项目开发已启动"
    }
}
```

### 3.2 辅助接口：查询项目整体进度

```
GET /api/ai/code/project/{projectId}/status
```

响应包含：

* 项目状态（PROCESSING/COMPLETED/FAILED）

* 当前阶段、进度百分比

* 所有阶段详情（每阶段状态、轮次数、文件数）

* 预估完成时间

### 3.3 辅助接口：查询指定阶段进度

```
GET /api/ai/code/project/{projectId}/phase/{phase}/status
```

响应包含：

* 阶段状态、当前轮次

* 已生成文件列表、待生成文件列表

* 阶段内进度百分比

### 3.4 辅助接口：查询进度文档

```
GET /api/ai/code/project/{projectId}/phase/{phase}/progress
```

响应返回进度文档Markdown内容

### 3.5 辅助接口：查询轮次详情

```
GET /api/ai/code/project/{projectId}/phase/{phase}/round/{round}
```

响应包含：

* 轮次输入Prompt摘要

* 输出摘要（生成文件数、字符数）

* 续传次数、结束原因

* 耗时

***

## 四、数据库设计

### 4.1 项目表 ai\_development\_project

| 字段             | 类型            | 说明    |
| -------------- | ------------- | ----- |
| id             | BIGINT PK     | 主键    |
| project\_id    | VARCHAR(50)   | 项目ID  |
| project\_name  | VARCHAR(100)  | 项目名称  |
| task\_doc      | LONGTEXT      | 任务书内容 |
| status         | VARCHAR(20)   | 状态    |
| current\_phase | INT           | 当前阶段  |
| progress       | INT           | 进度百分比 |
| total\_files   | INT           | 总文件数  |
| error\_message | VARCHAR(1000) | 错误信息  |
| started\_at    | DATETIME      | 开始时间  |
| completed\_at  | DATETIME      | 完成时间  |
| created\_at    | DATETIME      | 创建时间  |
| updated\_at    | DATETIME      | 更新时间  |

### 4.2 阶段表 ai\_development\_phase

| 字段             | 类型            | 说明     |
| -------------- | ------------- | ------ |
| id             | BIGINT PK     | 主键     |
| project\_id    | VARCHAR(50)   | 项目ID   |
| phase          | INT           | 阶段号    |
| phase\_name    | VARCHAR(100)  | 阶段名称   |
| status         | VARCHAR(20)   | 状态     |
| total\_rounds  | INT           | 总轮次数   |
| total\_files   | INT           | 生成文件数  |
| session\_id    | VARCHAR(100)  | AI会话ID |
| summary        | LONGTEXT      | 阶段摘要   |
| error\_message | VARCHAR(1000) | 错误信息   |
| started\_at    | DATETIME      | 开始时间   |
| completed\_at  | DATETIME      | 完成时间   |

### 4.3 轮次表 ai\_development\_round

| 字段              | 类型            | 说明       |
| --------------- | ------------- | -------- |
| id              | BIGINT PK     | 主键       |
| phase\_id       | BIGINT FK     | 阶段ID     |
| round\_number   | INT           | 轮次号      |
| status          | VARCHAR(20)   | 状态       |
| input\_prompt   | LONGTEXT      | 输入Prompt |
| output\_content | LONGTEXT      | 输出内容     |
| tokens\_used    | INT           | 消耗Token  |
| files\_count    | INT           | 生成文件数    |
| continuation    | INT           | 续传次数     |
| finish\_reason  | VARCHAR(20)   | 结束原因     |
| error\_message  | VARCHAR(1000) | 错误信息     |
| retry\_count    | INT           | 重试次数     |
| started\_at     | DATETIME      | 开始时间     |
| completed\_at   | DATETIME      | 完成时间     |

### 4.4 生成文件表 ai\_development\_file

| 字段            | 类型           | 说明   |
| ------------- | ------------ | ---- |
| id            | BIGINT PK    | 主键   |
| project\_id   | VARCHAR(50)  | 项目ID |
| phase         | INT          | 阶段号  |
| round\_number | INT          | 轮次号  |
| file\_path    | VARCHAR(500) | 文件路径 |
| file\_size    | BIGINT       | 文件大小 |
| file\_type    | VARCHAR(20)  | 文件类型 |
| is\_complete  | TINYINT      | 是否完整 |
| created\_at   | DATETIME     | 创建时间 |

***

## 五、阶段配置

### 5.1 七阶段配置

| 阶段 | 名称          | 关键词                    | 预估文件数 |
| -- | ----------- | ---------------------- | ----- |
| 1  | 数据库与公共模块开发  | 数据库、建表SQL、公共模块、pom.xml | 20    |
| 2  | 用户管理与认证模块开发 | 员工、角色、会员、认证、JWT        | 30    |
| 3  | 库存管理模块开发    | 商品、类别、库位、库存、盘点         | 25    |
| 4  | 采购/销售管理模块开发 | 采购、销售、订单、零售            | 35    |
| 5  | 流水报表模块开发    | 报表、财务、统计               | 20    |
| 6  | 系统设置模块开发    | 系统配置、参数、日志             | 15    |
| 7  | 联调与测试       | 集成、测试                  | 10    |

***

## 六、执行流程详细设计

### 6.1 项目启动 startProject()

```
输入参数:
  - projectId: "67"
  - projectName: "beauty-shop-manage"
  - taskDoc: "完整TASKS.md内容(约62000字符)"

处理步骤:

1. 参数校验
   ├─ projectId 不能为空
   ├─ projectName 不能为空
   └─ taskDoc 不能为空

2. 创建项目目录结构
   projects/
   └── 67/
       ├── TASKS.md              ← 保存原始任务书
       ├── README.md
       ├── phases/
       │   ├── 1_database_and_common/
       │   │   ├── sessions/     ← 会话记录
       │   │   │   ├── round_1_input.md
       │   │   │   ├── round_1_output.md
       │   │   │   ├── round_2_input.md
       │   │   │   └── round_2_output.md
       │   │   ├── generated/    ← 生成的代码
       │   │   │   ├── pom.xml
       │   │   │   └── src/
       │   │   ├── progress.md   ← 进度文档
       │   │   └── summary.md   ← 阶段摘要
       │   ├── 2_user_and_auth/
       │   └── ...
       └── logs/
           └── development.log

3. 初始化数据库记录
   插入 ai_development_project 记录

4. 异步启动阶段执行
   └─ 调用 executePhase(phase=1)

5. 返回响应
   {
     "success": true,
     "projectId": "67",
     "status": "PROCESSING"
   }
```

### 6.2 阶段执行 startPhase()

```
输入:
  - phase: 1
  - taskDoc: "完整TASKS.md"
  - previousPhaseSummary: null (阶段1为null)

处理步骤:

1. 获取阶段配置
   phaseConfig = PHASE_CONFIGS[phase]

2. 创建阶段目录

3. 初始化阶段数据库记录
   插入 ai_development_phase 记录

4. 创建新AI会话 (SessionManager)
   sessionId = "phase_1_" + UUID.randomString()
   注意: 每个阶段完全新会话，不携带历史上下文

5. 进入轮次循环
   roundNumber = 1
   while (true):
       │
       ▼
       executeRound(sessionId, roundNumber, taskDoc,
                   phaseConfig, previousPhaseSummary,
                   currentFileList)
           │
           ▼
       checkShouldContinue(phaseConfig, currentFileList)
           │
           ├─ 继续 ──→ roundNumber++, 继续循环
           └─ 结束 ──→ break

6. 阶段完成处理
   ├─ 更新阶段状态为 COMPLETED
   ├─ 生成阶段摘要 summary.md
   ├─ 生成下一阶段需要的上下文
   └─ 返回 (通知主流程进入下一阶段)
```

### 6.3 单轮执行 executeRound()

````
输入:
  - sessionId: "phase_1_abc123"
  - roundNumber: 1
  - taskDoc: "完整TASKS.md"
  - phaseConfig: 阶段配置
  - previousPhaseSummary: 上一阶段摘要
  - currentFileList: 当前已生成文件列表

输出: RoundResult {
  roundNumber: 1,
  files: [{path, content, isComplete}, ...],
  totalTokens: 35000,
  continuationCount: 1,
  finishReason: "length" | "stop" | "error",
  needsContinue: true | false
}

──────────────────────────────────────────────────────────────

第一轮 vs 后续轮次的区别:

第一轮 (roundNumber == 1):
  构建Prompt:
  
  # 任务书内容（TASKS.md完整内容，可能很长需要续传）
  【任务书】
  {taskDoc}
  
  # 当前阶段任务说明
  【当前阶段】
  阶段{phase}：{phaseConfig.name}
  请按照TASKS.md第{phase}章的规定开始提供代码文件。
  
  # 已开发文件列表（第一轮为空）
  【已开发文件】
  无（这是本阶段的第一次开发）
  
  # 开发指令
  【开发指令】
  1. 请先理解任务书内容
  2. 按照TASKS.md第{phase}章的要求生成代码
  3. 使用 ===FILE:文件路径=== 标记每个代码文件
  4. 每个文件必须完整，不要截断
  5. 确保代码可直接编译运行
  
  # 重要提醒
  - 目标产出：{phaseConfig.targetFiles}
  - 关键词：{phaseConfig.keywords}

后续轮次 (roundNumber > 1):
  构建Prompt:
  
  # 当前阶段任务说明
  【当前阶段】
  阶段{phase}：{phaseConfig.name}，继续开发
  
  # 已开发文件列表（完整列表）
  【已开发文件】
  {fileList格式化为字符串}
  例如:
  - pom.xml (15420 bytes)
  - beauty-shop-manage-common/pom.xml (3280 bytes)
  - BeautyShopManageApplication.java (4521 bytes)
  - ...
  
  # 继续开发指令
  【继续开发】
  请继续生成剩余未开发的文件，已生成的文件请勿重复。
  优先生成尚未生成的关键文件。
  
  # 输出格式
  - 使用 ===FILE:文件路径=== 标记新生成的代码文件
  - 已在【已开发文件】列表中的文件请勿重复输出

──────────────────────────────────────────────────────────────

调用AI (带重试机制):

retryCount = 0
maxRetries = 30
baseDelayMs = 1000

while retryCount < maxRetries:
    try:
        构建AI请求
        ├─ model: {configured model}
        ├─ messages: [{role: "system", content: SYSTEM_PROMPT},
        │              {role: "user", content: USER_PROMPT}]
        ├─ temperature: 0.7
        ├─ max_tokens: 32000
        └─ stream: false
        
        发送请求
        response = aiProvider.chatCompletion(request)
        
        成功
        if response.success:
            content = response.content
            finishReason = response.finish_reason
            break
            
    except TemporaryError as e:
        # 临时错误（网络超时、服务端错误等）
        retryCount++
        delay = baseDelayMs * (2 ** retryCount)  # 指数退避
        sleep(delay)
        continue
    except PermanentError as e:
        # 永久错误（认证失败、限额等）
        throw DevelopmentException("AI调用失败: " + e.message)

──────────────────────────────────────────────────────────────

截断检测与续传:

if finishReason == "length":
    截断发生，使用续传机制
    
    continuationCount = 0
    maxContinuations = 5
    fullContent = content
    
    while continuationCount < maxContinuations:
        构建续传请求
        - messages: 添加 {role: "assistant", content}
        - messages: 添加 {role: "user", content: "请继续"}
        
        response = aiProvider.chatCompletion(continuation)
        
        if response.finish_reason == "length":
            fullContent += response.content
            continuationCount++
            continue
        else:
            fullContent += response.content
            break
            
        if continuationCount >= maxContinuations:
            log.warn("达到最大续传次数")
            break
            
    content = fullContent
    needsContinue = true  # 告诉主循环继续
    
else:
    正常完成
    needsContinue = false

──────────────────────────────────────────────────────────────

文件解析与保存:

files = parseAndSaveFiles(content, projectId, phase)

parseAndSaveFiles(content, projectId, phase):
  1. 正则匹配
     pattern = "===FILE:(.+?)===\\s*```([\\w]*)\\s*(.+?)```"
     matches = pattern.findAll(content)
     
  2. 遍历处理
     for each match:
         filePath = match.group(1).trim()
         language = match.group(2)
         fileContent = match.group(3).trim()
         
  3. 安全检查
     baseDir = projects/{projectId}/phases/{phase}/generated
     fullPath = baseDir.resolve(filePath)
     if not fullPath.normalize().startsWith(baseDir):
         log.warn("非法路径，跳过: {}", filePath)
         continue
         
  4. 保存文件
     parentDir = fullPath.parent
     mkdirs(parentDir)
     writeString(fullPath, fileContent, UTF_8)
     
  5. 返回文件清单
     return [{path, content, size, isComplete: true}, ...]

──────────────────────────────────────────────────────────────

进度文档更新:

appendToProgressFile(projectId, phase, roundNumber, files)

──────────────────────────────────────────────────────────────

保存会话记录:

saveSessionRecord(sessionId, roundNumber, input, output)
└─ 保存到 projects/{id}/phases/{phase}/sessions/round_{n}_*.md

──────────────────────────────────────────────────────────────

返回结果:

return RoundResult {
  roundNumber: 1,
  files: files,
  totalTokens: calculateTokens(content),
  continuationCount: continuationCount,
  finishReason: finishReason,
  needsContinue: needsContinue
}
````

### 6.4 阶段完成判断 checkShouldContinue()

```
输入:
  - phaseConfig: 阶段配置
  - currentFileList: 当前已生成文件列表

判断逻辑:

条件1: AI明确表示阶段完成
  if lastOutput.contains("【阶段完成】") or
     lastOutput.contains("本阶段开发完成"):
      return CONTINUE_NONE  # 结束阶段

条件2: 文件覆盖率达到阈值
  # 解析TASKS.md获取当前阶段应该生成的文件清单
  expectedFiles = parseExpectedFiles(phaseConfig.keywords)
  coverage = currentFileList.size / expectedFiles.size
  
  if coverage >= 0.8:  # 80%覆盖
      return CONTINUE_NONE  # 结束阶段

条件3: 连续N轮无新文件产出
  if noNewFileRounds >= 5:
      log.warn("连续5轮无新文件，强制结束阶段")
      return CONTINUE_NONE  # 结束阶段

条件4: 超过最大轮次
  if roundNumber >= 20:  # 单阶段最多20轮
      log.warn("达到最大轮次数，强制结束阶段")
      return CONTINUE_NONE  # 结束阶段

条件5: 截断后续传完成
  if lastRound.needsContinue == false and
     lastRound.finishReason == "stop":
      return CONTINUE_NONE  # 结束阶段

否则: 继续下一轮
  return CONTINUE_NEXT_ROUND

返回值:
  - CONTINUE_NEXT_ROUND: 继续下一轮
  - CONTINUE_NONE: 结束当前阶段
  - CONTINUE_PHASE_NEXT: 进入下一阶段
```

***

## 七、文件存储结构

```
projects/
└── {projectId}/
    ├── TASKS.md                          # 原始任务书
    ├── README.md                         # 项目说明
    └── phases/
        ├── 1_database_and_common/
        │   ├── sessions/
        │   │   ├── round_1_input.md
        │   │   ├── round_1_output.md
        │   │   ├── round_2_input.md
        │   │   └── round_2_output.md
        │   ├── generated/
        │   │   ├── pom.xml
        │   │   └── src/
        │   │       └── main/
        │   │           └── java/
        │   │               └── com/
        │   │                   └── beauty/
        │   │                       └── ...
        │   ├── progress.md            # 进度文档
        │   └── summary.md            # 阶段摘要
        ├── 2_user_and_auth/
        │   └── ...
        └── 7_integration/
```

***

## 八、进度文档格式

```markdown
# 阶段{phase} - {phaseName}

## 轮次 {roundNumber} - {timestamp}

### 本轮生成 ({filesCount}个文件)
- {filePath1} ({size1} bytes)
- {filePath2} ({size2} bytes)
- ...

### 续传次数
- {continuationCount}次

### 结束原因
- {finishReason}

---
```

***

## 九、错误处理

### 9.1 重试策略

* 最大重试次数：30次

* 指数退避：1s → 2s → 4s → 8s → ... → 512s (最大)

* 可重试错误：网络超时、服务端临时错误

* 不可重试错误：认证失败、API限额耗尽

### 9.2 错误记录

* 每轮错误记录到 ai\_development\_round.error\_message

* 项目级错误记录到 ai\_development\_project.error\_message

* 错误日志保存到 projects/{id}/logs/development.log

