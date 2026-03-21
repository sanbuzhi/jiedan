# /v1/ai/code/project/start 接口详细流程图

## 1. 完整请求-响应流程

```
┌─────────────────────────────────────────────────────────────────────────────────┐
│                           完整请求-响应流程                                      │
├─────────────────────────────────────────────────────────────────────────────────┤
│                                                                          │
│  ┌──────────┐                              ┌────────────────────────────┐   │
│  │  前端    │                              │         后端               │   │
│  └────┬─────┘                              └────────────┬─────────────┘   │
│       │                                              │                    │
│       │  POST /v1/ai/code/project/start            │                    │
│       │  {requirementId: 1, projectId: "1"}         │                    │
│       │ ───────────────────────────────────────────► │                    │
│       │                                              │                    │
│       │                                              │  1. 参数校验      │
│       │                                              │  2. 验证Requirement│
│       │                                              │  3. 验证taskDoc   │
│       │                                              │  4. 创建异步任务  │
│       │                                              │                    │
│       │  {success: true, projectId: "1"}            │                    │
│       │ ◄────────────────────────────────────────── │                    │
│       │                                              │                    │
│       │  GET /v1/ai/code/project/1/status          │                    │
│       │ ───────────────────────────────────────────► │                    │
│       │                                              │                    │
│       │  {status: "PROCESSING", currentPhase: 1,   │                    │
│       │   progress: 14%, phases: [...]}            │                    │
│       │ ◄────────────────────────────────────────── │                    │
│       │                                              │                    │
│       │  (每5秒轮询一次)                           │                    │
│       │                                              │                    │
│       │  GET /v1/ai/code/project/1/status          │                    │
│       │ ───────────────────────────────────────────► │                    │
│       │                                              │                    │
│       │  {status: "COMPLETED", currentPhase: 7,    │                    │
│       │   progress: 100%, totalFiles: 85}          │                    │
│       │ ◄────────────────────────────────────────── │                    │
│       │                                              │                    │
└───────┴──────────────────────────────────────────────┴────────────────────┘
```

---

## 2. 接口详细流程

### 2.1 前端请求构建

```
前端代码: miniprogram/pages/index/index.js
executeAsyncProjectDevelopment(requirementId, aiNode)
    │
    ├─► 1. 获取需求信息
    │   └─► 从 allRequirements 查找 requirementId
    │
    ├─► 2. 构建请求数据
    │   └─► {
    │         requirementId: requirement.id,  // Long
    │         projectId: String(requirement.id)  // String
    │       }
    │
    ├─► 3. 调用启动接口
    │   └─► POST /v1/ai/code/project/start
    │
    ├─► 4. 处理响应
    │   └─► if (result.success) {
    │         projectId = result.data.projectId
    │         // 开始轮询
    │         pollProjectStatus(requirementId, projectId, aiNode)
    │       }
    │
    └─► 5. 轮询状态 (pollProjectStatus)
        ├─► maxPolls = 360 (最多30分钟)
        ├─► 每5秒调用 GET /v1/ai/code/project/{projectId}/status
        ├─► 检查 status === 'COMPLETED' → 结束
        ├─► 检查 status === 'FAILED' → 报错
        └─► 更新进度: wx.showLoading(`AI开发中...${progress}%`)
```

---

### 2.2 后端控制器处理

```
接口: POST /v1/ai/code/project/start
文件: ProjectDevelopmentController.java

@RequestMapping("/v1/ai/code")
public class ProjectDevelopmentController {

    @PostMapping("/project/start")
    public ApiResponse<StartProjectResponse> startProject(@RequestBody StartProjectRequest request)
    │
    ├─► Step 1: 参数校验
    │   ├─► if (request.getRequirementId() == null)
    │   │   └─► return ApiResponse.error("requirementId不能为空")
    │   │
    │   ├─► Step 2: 查询Requirement
    │   │   └─► requirement = requirementRepository.findById(requirementId)
    │   │       └─► if (requirement == null)
    │   │           └─► return ApiResponse.error("需求不存在")
    │   │
    │   ├─► Step 3: 验证taskDoc
    │   │   └─► taskDoc = requirement.getAiTaskDoc()
    │   │       └─► if (taskDoc == null || taskDoc.isEmpty())
    │   │           └─► return ApiResponse.error("该需求尚未生成任务书")
    │   │
    │   ├─► Step 4: 处理projectId
    │   │   └─► projectId = request.getProjectId() ?? String.valueOf(requirementId)
    │   │
    │   ├─► Step 5: 检查projectId唯一性
    │   │   └─► if (projectRepository.existsByProjectId(projectId))
    │   │       └─► return ApiResponse.error("项目ID已存在")
    │   │
    │   ├─► Step 6: 获取项目名称
    │   │   └─► projectName = requirement.getProjectType() ?? "项目" + projectId
    │   │
    │   └─► Step 7: 启动异步开发
    │       └─► projectManager.startProjectAsync(projectId, projectName, taskDoc)
    │           └─► @Async 注解，后台线程池执行
    │
    └─► 返回响应
        └─► ApiResponse.success(StartProjectResponse.success(projectId))
```

---

### 2.3 ProjectDevelopmentManager 异步执行

```
方法: startProjectAsync(projectId, projectName, taskDoc)
文件: ProjectDevelopmentManager.java

@Async  // 后台异步执行
public void startProjectAsync(String projectId, String projectName, String taskDoc) {
    │
    └─► 调用同步方法 startProject(...)
}

public void startProject(String projectId, String projectName, String taskDoc) {
    │
    ├─► Step 1: 参数校验
    │   ├─► if (projectId == null || isEmpty) → throw IllegalArgumentException
    │   ├─► if (projectName == null || isEmpty) → throw IllegalArgumentException
    │   └─► if (taskDoc == null || isEmpty) → throw IllegalArgumentException
    │
    ├─► Step 2: 创建项目目录结构
    │   └─► createProjectDirectoryStructure(projectId)
    │       └─► projects/{projectId}/
    │           ├─► phases/
    │           │   ├─► 1_database/
    │           │   │   ├─► sessions/
    │           │   │   └─► generated/
    │           │   ├─► 2_backend/
    │           │   ├─► 3_frontend/
    │           │   └─► ... (7个阶段目录)
    │           └─► logs/
    │
    ├─► Step 3: 保存任务书到文件
    │   └─► saveTaskDoc(projectId, taskDoc)
    │       └─► projects/{projectId}/TASKS.md
    │
    ├─► Step 4: 创建项目数据库记录
    │   └─► AiDevelopmentProject project = new AiDevelopmentProject()
    │       ├─► projectId: "1"
    │       ├─► projectName: "美业管理系统"
    │       ├─► taskDoc: "..." (任务书内容)
    │       ├─► status: "PROCESSING"
    │       ├─► currentPhase: 1
    │       ├─► progress: 0
    │       ├─► totalFiles: 0
    │       ├─► startedAt: LocalDateTime.now()
    │       └─► projectRepository.save(project)
    │
    ├─► Step 5: 解析任务书获取阶段配置
    │   └─► tasksAnalysisService.analyzeAndGetPhaseConfigs(taskDoc)
    │       │
    │       ├─► 调用AI分析任务书
    │       │   └─► AiPromptTemplate.TASKS_ANALYSIS_SYSTEM
    │       │       prompt = buildTasksAnalysisUserPrompt(taskDoc)
    │       │
    │       ├─► 解析AI返回的JSON
    │       │   └─► {phases: [{phase, phaseName, keywords, targetFiles}...]} 
    │       │
    │       ├─► 解析失败时使用默认配置
    │       │   └─► AiDevelopmentConfig.PHASE_CONFIGS (7个默认阶段)
    │       │
    │       └─► 返回 List<PhaseConfig>
    │
    ├─► Step 6: 顺序执行所有阶段
    │   └─► executeAllPhases(projectId, taskDoc, phaseConfigs)
    │       │
    │       └─► for (PhaseConfig phaseConfig : phaseConfigs)
    │           │
    │           ├─► Phase 1: 数据库与公共模块
    │           ├─► Phase 2: 用户管理与认证模块
    │           ├─► Phase 3: 库存管理模块
    │           ├─► Phase 4: 采购销售管理模块
    │           ├─► Phase 5: 流水报表模块
    │           ├─► Phase 6: 系统设置模块
    │           └─► Phase 7: 联调与测试
    │
    └─► Step 7: 更新项目状态
        └─► updateProjectStatus(projectId, "COMPLETED", null)
```

---

### 2.4 单个阶段执行详情

```
方法: executePhase(projectId, phase, taskDoc, previousPhaseSummary, phaseConfig)
文件: ProjectDevelopmentManager.java

executePhase(...) {
    │
    ├─► Step 1: 创建阶段目录
    │   └─► createPhaseDirectory(projectId, phase, phaseConfig)
    │       └─► projects/{projectId}/phases/{phaseDir}/sessions + generated/
    │
    ├─► Step 2: 创建阶段数据库记录
    │   └─► AiDevelopmentPhase phaseEntity = new AiDevelopmentPhase()
    │       ├─► projectId, phase, phaseName
    │       ├─► sessionId: "phase_X_xxxxxx"
    │       ├─► status: "PROCESSING"
    │       ├─► startedAt: now()
    │       └─► phaseRepository.save(phaseEntity)
    │
    ├─► Step 3: 多轮生成循环
    │   └─► while (roundNumber <= maxRoundsPerPhase)  // 默认20轮
    │       │
    │       ├─► executeRound(...)
    │       │   │
    │       │   ├─► Step 3.1: 构建Prompt
    │       │   │   ├─► roundNumber == 1
    │       │   │   │   └─► buildCodeGenerationFirstRoundPrompt()
    │       │   │   │       └─► 包含: 任务书 + 当前阶段 + 开发指令
    │       │   │   │
    │       │   │   └─► roundNumber > 1
    │       │   │       └─► buildCodeGenerationContinuePrompt()
    │       │   │           └─► 包含: 已开发文件列表 + 继续开发指令
    │       │   │
    │       │   ├─► Step 3.2: AI调用 (带重试)
    │       │   │   └─► callAIWithRetry(sessionId, prompt, maxTokens)
    │       │   │       ├─► 重试次数: config.getMaxRetries() (默认3次)
    │       │   │       ├─► 指数退避: baseDelayMs * 2^retryCount
    │       │   │       └─► maxTokens: 32000
    │       │   │
    │       │   ├─► Step 3.3: 解析并保存文件
    │       │   │   └─► parseAndSaveFiles(projectId, content, phase, roundNumber)
    │       │   │       ├─► 正则匹配: ===FILE:xxx=== ```语言\n代码\n```
    │       │   │       ├─► 安全检查: 防止路径遍历攻击
    │       │   │       └─► 保存到: phases/{phaseDir}/generated/{filePath}
    │       │   │
    │       │   ├─► Step 3.4: 保存会话记录
    │       │   │   └─► sessions/round_{N}_input.md + output.md
    │       │   │
    │       │   ├─► Step 3.5: 更新进度文档
    │       │   │   └─► phases/{phaseDir}/progress.md
    │       │   │
    │       │   └─► Step 3.6: 判断是否继续
    │       │       └─► shouldStopPhase(result, noNewFileCount, roundNumber)
    │       │           ├─► 连续5轮无新文件 → 停止
    │       │           ├─► 达到20轮 → 停止
    │       │           └─► AI返回stop且无文件 → 停止
    │       │
    │       └─► 返回: newFiles列表
    │
    ├─► Step 4: 更新阶段统计
    │   └─► phaseEntity.setTotalRounds(roundNumber)
    │       └─► phaseEntity.setTotalFiles(currentFileList.size())
    │           └─► phaseEntity.setStatus("COMPLETED")
    │
    └─► Step 5: 生成阶段摘要
        └─► generatePhaseSummary(phase, phaseConfig, currentFileList)
            └─► 返回: "# 阶段X 开发摘要\n\n## 生成文件统计\n..."
```

---

## 3. 数据模型

### 3.1 请求 DTO

```
StartProjectRequest
├── requirementId : Long (必填)
└── projectId     : String (可选，默认取requirementId)

StartProjectResponse
├── success       : boolean
└── projectId    : String
```

### 3.2 数据库实体

```
AiDevelopmentProject (项目总览)
├── id                 : Long (主键)
├── projectId          : String (项目ID，唯一索引)
├── projectName        : String (项目名称)
├── taskDoc            : String (任务书内容)
├── status             : String (PROCESSING/COMPLETED/FAILED)
├── currentPhase       : Integer (当前阶段 1-7)
├── progress           : Integer (进度 0-100)
├── totalFiles         : Integer (生成文件总数)
├── errorMessage       : String (失败时的错误信息)
├── startedAt          : LocalDateTime
├── completedAt        : LocalDateTime
└── createdAt, updatedAt

AiDevelopmentPhase (阶段记录)
├── id                 : Long (主键)
├── projectId          : String (关联项目)
├── phase              : Integer (阶段编号 1-7)
├── phaseName          : String (阶段名称)
├── sessionId          : String (会话ID)
├── status             : String (PROCESSING/COMPLETED/FAILED)
├── totalRounds        : Integer (总轮次数)
├── totalFiles         : Integer (生成文件数)
├── errorMessage       : String
├── startedAt          : LocalDateTime
├── completedAt        : LocalDateTime
└── createdAt, updatedAt

AiDevelopmentRound (轮次记录)
├── id                 : Long (主键)
├── phaseId            : Long (关联阶段)
├── roundNumber        : Integer (轮次编号)
├── inputPrompt        : String (输入Prompt)
├── outputContent      : String (AI输出内容)
├── tokensUsed         : Integer (消耗Token)
├── filesCount         : Integer (生成文件数)
├── continuation       : Integer (续传次数)
├── finishReason       : String (stop/length)
├── status             : String
├── startedAt          : LocalDateTime
├── completedAt        : LocalDateTime
└── createdAt, updatedAt

AiDevelopmentFile (文件记录)
├── id                 : Long (主键)
├── projectId          : String
├── phase              : Integer
├── roundNumber        : Integer
├── filePath           : String (文件路径)
├── fileSize           : Long (字节)
├── fileType           : String (java/vue/js/ts/sql...)
├── isComplete         : Boolean
└── createdAt
```

---

## 4. 文件系统结构

```
projects/{projectId}/
├── TASKS.md                           # 任务书 (复制)
├── phases/
│   ├── 1_database/
│   │   ├── sessions/
│   │   │   ├── round_1_input.md      # 第1轮输入
│   │   │   ├── round_1_output.md     # 第1轮输出
│   │   │   ├── round_2_input.md
│   │   │   └── round_2_output.md
│   │   ├── generated/
│   │   │   ├── src/main/java/...     # 生成的Java文件
│   │   │   ├── pom.xml
│   │   │   └── init.sql
│   │   └── progress.md                # 阶段进度
│   │
│   ├── 2_backend/                    # 后端业务模块
│   │   ├── sessions/
│   │   ├── generated/
│   │   │   ├── src/main/java/com/jiedan/
│   │   │   │   ├── controller/
│   │   │   │   ├── service/
│   │   │   │   ├── entity/
│   │   │   │   └── mapper/
│   │   │   └── src/main/resources/
│   │   └── progress.md
│   │
│   ├── 3_frontend/                   # 前端模块
│   │   ├── sessions/
│   │   ├── generated/
│   │   │   ├── src/
│   │   │   │   ├── views/
│   │   │   │   ├── components/
│   │   │   │   ├── api/
│   │   │   │   └── router/
│   │   │   ├── package.json
│   │   │   └── vite.config.js
│   │   └── progress.md
│   │
│   ├── 4_purchase_sale/              # 采购销售模块
│   ├── 5_report/                    # 报表模块
│   ├── 6_system/                    # 系统设置模块
│   └── 7_integration/               # 联调测试模块
│
└── logs/                            # 日志目录
```

---

## 5. 状态查询接口

### 5.1 获取项目状态

```
GET /v1/ai/code/project/{projectId}/status

Response:
{
  "success": true,
  "data": {
    "projectId": "1",
    "projectName": "美业管理系统",
    "status": "PROCESSING",  // PROCESSING / COMPLETED / FAILED
    "currentPhase": 2,
    "totalPhases": 7,
    "progress": 28,
    "totalFiles": 35,
    "startedAt": "2026-03-20T10:00:00",
    "updatedAt": "2026-03-20T10:15:00",
    "phases": [
      {
        "phase": 1,
        "name": "数据库与公共模块",
        "status": "COMPLETED",
        "totalRounds": 3,
        "totalFiles": 15,
        "startTime": "2026-03-20T10:00:00",
        "endTime": "2026-03-20T10:08:00",
        "duration": "8m",
        "recentFiles": ["pom.xml", "init.sql", "User.java"]
      },
      {
        "phase": 2,
        "name": "用户管理与认证模块",
        "status": "PROCESSING",
        "totalRounds": 2,
        "totalFiles": 20,
        "startTime": "2026-03-20T10:08:00",
        "recentFiles": ["UserController.java", "AuthService.java"]
      }
    ]
  }
}
```

---

## 6. 关键配置参数

```
配置文件: application.yml 或 AiDevelopmentConfig.java

ai-development:
  max-retries: 30                    # AI调用最大重试次数
  max-tokens: 32000                 # 单次最大token数
  max-continuations: 5               # 最大续传次数
  max-rounds-per-phase: 20           # 每个阶段最大轮次数
  no-new-file-rounds-threshold: 5    # 连续无新文件轮次阈值
  coverage-threshold: 0.8           # 覆盖率阈值
  base-delay-ms: 1000                # 重试基础延迟(毫秒)
```

---

## 7. 7个开发阶段默认配置

| 阶段 | 目录名称 | 关键词 | 预估文件数 |
|------|----------|--------|-----------|
| 1 | 1_database | 数据库,建表SQL,pom.xml,Vue,组件,API | 30 |
| 2 | 2_backend | 员工,角色,会员,认证,JWT,登录,注册 | 40 |
| 3 | 3_frontend | 商品,类别,库位,库存,盘点 | 35 |
| 4 | 4_purchase_sale | 采购,销售,订单,零售 | 45 |
| 5 | 5_report | 报表,财务,统计,图表,ECharts | 30 |
| 6 | 6_system | 系统配置,参数,日志,设置页面 | 25 |
| 7 | 7_integration | 集成,测试,前后端联调 | 20 |
