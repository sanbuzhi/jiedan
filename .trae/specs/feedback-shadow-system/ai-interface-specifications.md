# AI接口输入输出详细规范（基于Markdown文档输出）

## 概述

本文档定义了5个AI接口的输入输出规范。与常规API不同，这些AI接口的输出是**Markdown格式文档**，由AI模型生成内容，系统负责创建和管理文档文件。

**核心设计原则**：

- AI接口输出为Markdown文档，便于人类阅读和后续处理
- 文档结构有明确规范（段落、章节），但内容自由发挥
- Feedback Shadow也是AI模型，负责检测文档质量
- 文档存储在系统中，有版本管理
- **增量式开发**：AI开发按任务拆分，逐步生成代码

**⚠️ AI能力边界说明**：

1. AI输出可能不完全符合格式要求，需要系统容错处理
2. AI可能遗漏某些章节，需要Feedback Shadow检测补充
3. AI的工时估算可能不准确，仅供参考
4. AI生成的代码可能有编译错误，需要自我修复循环
5. **AI无法一次生成完整项目代码**，必须按任务增量生成

***

## 通用约束

### Token限制

| 接口              | 输入Token | 输出Token | 说明        |
| --------------- | ------- | ------- | --------- |
| 明确需求            | 4000    | 3000    | 需求描述+约束说明 |
| 拆分任务            | 6000    | 4000    | 需求文档+上下文  |
| 代码生成            | 6000    | 6000    | 任务+上下文+规范 |
| 功能测试            | 4000    | 4000    | 代码+测试标准   |
| 安全测试            | 3000    | 3000    | 代码+安全规范   |
| Feedback Shadow | 6000    | 1500    | 文档内容+检测指令 |

**⚠️ 注意**：实际token限制可能因模型而异，建议预留20%余量。

### 响应时间预期

| 接口              | 预期时间    | 超时时间 | 说明          |
| --------------- | ------- | ---- | ----------- |
| 明确需求            | 10-30秒  | 60秒  | 需要生成完整PRD   |
| 拆分任务            | 15-45秒  | 90秒  | 需要详细任务拆分    |
| 代码生成            | 30-120秒 | 180秒 | 需要生成完整代码    |
| 功能测试            | 20-60秒  | 120秒 | 需要生成测试用例和代码 |
| 安全测试            | 15-45秒  | 90秒  | 需要分析代码漏洞    |
| Feedback Shadow | 10-30秒  | 60秒  | 需要检测文档质量    |

### 文档存储规范

```
/projects/{projectId}/
├── requirements/
│   └── PRD-v1.md              # AI明确需求输出
├── tasks/
│   └── TASK-v1.md             # AI拆分任务输出
├── src/                       # AI开发输出（代码文件）
│   ├── task-{taskId}/         # 按任务分目录存储
│   └── context.json           # 代码摘要上下文
├── tests/
│   └── TEST-v1.md             # AI功能测试输出
├── security/
│   └── SEC-v1.md              # AI安全测试输出
└── feedback/
    └── FB-{timestamp}.md      # Feedback Shadow报告
```

***

## 1. AI明确需求接口 (clarify-requirement)

### 1.1 输入（从小程序前端需求选项出发）

**系统层DTO**:

```java
public class ClarifyRequirementRequest {
    // 1. 项目角色
    private String projectRole;           // 
    
    // 2. 项目类型（从小程序前端选项）
    private String projectType;           // 项目类型：微信小程序/抖音小程序/网页/H5/APP
    
    // 3. 上线要求
    private Boolean needOnline;           // 是否需要上线

    
    // 4. 业务场景描述
    private String businessScenario;      // 业务场景详细描述（限制500字以内）
    private String businessDomain;        // 业务领域：电商/教育/金融/医疗/餐饮/旅游/其他
    private String targetUsers;           // 目标用户群体（限制200字以内）
    
    // 6. 功能点集合（用户勾选+补充）
    private List<String> selectedFeatures;     // 用户勾选的标准功能点（最多10个）
    private String customFeatures;             // 用户自定义功能描述（限制500字以内）
    
    // 7. 原始需求物料（可选）
    private List<String> referenceMaterials;   // 参考链接（最多3个）
    private String additionalRequirements;     // 补充需求说明（限制300字以内）
    
    // 8. 交互视觉（可选）
    private List<String> uiReferenceImages;    // UI参考图片URL列表（最多3张）
    private String uiStyleDescription;         // UI风格描述：简约/商务/活泼/科技感
    private String interactionRequirements;    // 交互要求（限制200字以内）
}
```

<br />

### 1.2 Prompt构造

**System Prompt**:

```
你是一位资深的需求分析师和系统架构师。请根据用户提供的项目信息，输出一份产品需求文档（PRD）。

【角色定位】
根据用户的项目角色调整分析角度：
- 产品经理：侧重功能完整性和用户体验
- 开发者：侧重技术可行性和实现方案
- 设计师：侧重交互和视觉设计
- 创业者：侧重商业价值和MVP范围

【文档要求】
1. 使用Markdown格式
2. 必须包含以下章节（按顺序）：
   - # 产品需求文档（文档标题，包含项目名称）
   - ## 1. 项目概述（背景、目标、范围、上线要求、流量预期）
   - ## 2. 功能需求（功能模块清单、详细说明）
   - ## 3. 技术架构（前端/后端/数据库/第三方服务）
   - ## 4. 项目规划（里程碑）
3. 功能需求必须包含至少一个P0优先级模块（核心功能）
4. 技术架构必须与项目类型匹配
5. 语言：中文

```

**User Prompt**:

```
请根据以下项目信息生成PRD文档：

【项目角色】${projectRole}
【项目类型】${projectType}
【上线要求】${needOnline ? '需要上线，平台：' + onlinePlatform : '不需要上线'}
【流量预期】${trafficExpectation}
【业务场景】${businessScenario}
【业务领域】${businessDomain}
【目标用户】${targetUsers}
【功能需求】${selectedFeatures}${customFeatures ? '\n自定义功能：' + customFeatures : ''}
${referenceMaterials ? '【参考物料】' + referenceMaterials : ''}
${uiStyleDescription ? '【UI风格】' + uiStyleDescription : ''}

请直接输出Markdown格式的PRD文档内容。
```

<br />

### 1.3 输出

**AI输出**: Markdown格式PRD文档

**系统处理**:

1. 创建文件 `/projects/{projectId}/requirements/PRD-v1.md`
2. 解析文档提取关键信息（容错处理）
3. 存储元数据到数据库

**输出DTO**:

```java
public class ClarifyRequirementResponse {
    private boolean success;
    private String errorMessage;
    
    private String documentPath;
    private String documentContent;
    
    // 解析后的关键信息（可能为空，需要容错）
    private String projectType;         // 解析出的项目类型
    private List<String> functionalModules;  // 功能模块列表
    private String architecture;        // 技术架构摘要
    private Integer estimatedHours;     // 预估工时（可能不准确）
    private Boolean needOnline;
    private String trafficExpectation;
    
    // 元数据
    private AiUsage usage;
    private Long responseTimeMs;
    private String rawResponse;         // 原始AI输出（用于调试）
}
```

**⚠️ 解析容错**：

- 章节可能缺失，使用默认值
- 工时估算可能异常，设置合理范围
- 功能模块可能格式不标准，需要正则提取

<br />

***

## 2. AI拆分任务接口 (split-tasks)

### 2.1 输入

```java
public class SplitTasksRequest {
    private String projectId;
    private String prdDocumentPath;
    private String prdSummary;          // PRD摘要（不是完整内容，控制token）
    private String projectType;
    private Boolean needOnline;
    private Integer estimatedHours;     // 总工时参考
}
```

<br />

### 2.2 Prompt构造

**System Prompt**:

```
你是一位资深的项目管理专家。请根据PRD摘要，输出任务拆分文档。

【任务拆分原则】
1. 每个功能模块拆分为：设计 → 开发 → 测试
2. 开发任务按端拆分（前端/后端/接口）
3. 确保任务可交付、可测试

【任务类型】
- design：UI设计、交互设计
- frontend：前端开发
- backend：后端开发
- api：接口开发
- test：测试
- doc：文档

【文档要求】
1. 使用Markdown格式
2. 必须包含：
   - # 任务拆分文档
   - ## 1. 任务清单（每个任务：ID、名称、类型、工时、优先级、依赖、交付物）
   - ## 2. 核心开发任务（标识核心任务及理由）
   - ## 3. 里程碑规划
3. 任务ID格式：TASK-001
4. 优先级：P0/P1/P2
5. 语言：中文

【输出约束】
- 任务数量控制在15个以内
- 单个任务工时不超过16小时
- 总工时与PRD估算接近（±30%）
```

**User Prompt**:

```
请根据以下PRD摘要拆分任务：

【项目类型】${projectType}
【上线要求】${needOnline}
【总工时参考】${estimatedHours}小时

【PRD摘要】
${prdSummary}

请输出Markdown格式的任务拆分文档。
```

**⚠️ Prompt优化**：

- 使用PRD摘要而非完整内容
- 明确任务数量限制（15个以内）
- 设置工时范围约束

### 2.3 输出

**AI输出**: Markdown格式任务文档

**系统处理**:

1. 创建文件 `/projects/{projectId}/tasks/TASK-v1.md`
2. 解析任务列表（容错处理）
3. 提取核心开发任务
4. **初始化任务状态表**，标记所有任务为PENDING

**输出DTO**:

```java
public class SplitTasksResponse {
    private boolean success;
    private String errorMessage;
    
    private String documentPath;
    private String documentContent;
    
    // 解析后的任务信息（可能不完整）
    private List<TaskInfo> tasks;
    private String coreDevelopmentTaskId;
    
    private AiUsage usage;
    private Long responseTimeMs;
}
```

##

***

## 3. AI开发接口 (generate-code)

### 3.1 代码生成的三种类型

#### 类型1：初始化代码生成（脚手架项目）

**触发时机**：任务拆分完成后，首次代码生成前

**生成内容**：
- 各端项目脚手架（目录结构、配置文件、依赖管理）
- 不包含业务逻辑代码

**示例**：
```
SpringBoot后端脚手架：
- pom.xml（依赖配置）
- application.yml（基础配置）
- 包目录结构（controller/service/repository/entity/config）
- 启动类
- 统一返回结果类（Result）
- 全局异常处理类

微信小程序脚手架：
- app.js/app.json/app.wxss
- pages/目录结构
- utils/工具目录
- config/配置目录
```

**Prompt设计**：
```
你是一位资深架构师。请生成{项目类型}的项目脚手架。

【项目信息】
项目类型：${projectType}
技术栈：${frontendFramework}/${backendFramework}

【脚手架要求】
1. 标准的目录结构
2. 基础依赖配置（pom.xml/package.json/requirements.txt）
3. 基础配置文件
4. 通用的工具类和基础类
5. 不包含业务逻辑代码

【输出】
只输出脚手架文件，每个文件完整可运行。
```

#### 类型2：任务循环开发（核心流程）

**流程**：提取任务 → 开发 → 自测 → 标记

```
while (还有PENDING任务) {
    // 1. 提取任务
    Task task = taskScheduler.getNextTask();
    
    // 2. 开发
    CodeResponse response = aiGenerateCode(task);
    
    // 3. 自测（编译验证）
    if (compile(response)) {
        // 4. 标记完成
        markTaskCompleted(task);
        saveCodeSummary(task, response);
    } else {
        // 修复循环
        repairLoop(task, compileErrors);
    }
}
```

**详细步骤**：

**步骤1：提取任务**
- 按依赖拓扑排序获取下一个可执行任务
- 检查依赖任务是否已完成
- 加载任务详情和上下文

**步骤2：开发（AI生成代码）**
- 构建Prompt（任务描述 + 上下文）
- 调用AI生成代码
- 解析输出，提取代码文件

**步骤3：自测（编译验证）**
- 将代码写入临时目录
- 执行编译命令
- 捕获编译错误
- 生成测试报告

**步骤4：标记**
- 编译通过 → 标记COMPLETED
- 编译失败 → 进入修复循环

#### 类型3：Feedback Shadow干预

**干预时机**：
1. 代码生成后，编译通过前（文档质量检测）
2. 用户反馈后（问题修复）
3. 代码风格不一致时（风格修正）

**干预流程**：
```
AI生成代码
    │
    ▼
Feedback Shadow检测（文档质量）
    │
    ├─ ALLOW ──► 继续编译验证
    │
    ├─ REPAIR ──► 生成修复建议 ──► AI修复 ──► 再次检测
    │
    └─ REJECT ──► 终止当前任务，记录错误

编译验证
    │
    ├─ 通过 ──► 保存代码
    │
    └─ 失败 ──► AI修复（不经过Feedback Shadow）──► 再次编译
```

**Feedback Shadow检测维度**：
1. **代码规范**：是否符合项目代码风格
2. **接口一致性**：是否与已有接口兼容
3. **安全规范**：是否有明显的安全问题
4. **性能规范**：是否有明显的性能问题

### 3.2 完整的代码生成流程

```
开始代码生成
    │
    ▼
【阶段1：脚手架生成】（仅首次）
    │
    ├─ 生成各端脚手架
    ├─ 编译验证脚手架
    └─ 保存脚手架代码
    │
    ▼
【阶段2：任务循环开发】
    │
    ├─ 获取待执行任务（拓扑排序）
    │
    ├─ 对每个任务：
    │   │
    │   ├─ 构建上下文（PRD摘要 + 代码摘要）
    │   │
    │   ├─ AI生成代码
    │   │
    │   ├─ Feedback Shadow检测（文档质量）
    │   │   ├─ REPAIR ──► 修复循环
    │   │   └─ ALLOW ──► 继续
    │   │
    │   ├─ 编译验证
    │   │   ├─ 失败 ──► 修复循环（最多3次）
    │   │   └─ 通过 ──► 继续
    │   │
    │   ├─ 保存代码
    │   ├─ 提取代码摘要
    │   └─ 标记任务完成
    │
    ▼
【阶段3：Feedback Shadow最终检测】
    │
    ├─ 对整个项目代码进行检测
    ├─ 生成质量报告
    └─ 输出ALLOW/REPAIR/REJECT
    │
    ▼
代码生成完成
```

### 3.3 输入

```java
/**
 * 代码生成请求（统一入口）
 */
public class GenerateCodeRequest {
    // 生成类型
    private GenerateType generateType;  // SCAFFOLD/TASK/FEEDBACK_REPAIR
    
    // 项目信息
    private String projectId;
    private String projectType;
    private String frontendFramework;
    private String backendFramework;
    
    // 类型1：脚手架生成
    private ScaffoldConfig scaffoldConfig;
    
    // 类型2：任务开发
    private String taskId;
    private String taskName;
    private String taskDescription;
    private String taskType;
    private List<String> taskDependencies;
    
    // 上下文（类型2和3）
    private String prdSummary;
    private List<CodeSummary> contextSummaries;
    private List<CodeSummary> dependencySummaries;
    private CodeStyle codeStyle;
    
    // 类型3：Feedback修复
    private String previousCode;
    private List<CompilationError> compilationErrors;
    private String feedbackShadowReport;
    private Integer fixAttempt;
}

/**
 * 生成类型枚举
 */
public enum GenerateType {
    SCAFFOLD,       // 脚手架生成
    TASK,           // 任务开发
    FEEDBACK_REPAIR // Feedback Shadow修复
}

/**
 * 脚手架配置
 */
public class ScaffoldConfig {
    private List<String> modules;       // 需要生成的模块（前端/后端/小程序）
    private String javaVersion;         // Java版本
    private String springBootVersion;   // SpringBoot版本
    private List<String> dependencies;  // 基础依赖
}
```

### 3.2 输入

```java
public class GenerateCodeRequest {
    private String projectId;
    private String taskId;
    private String taskName;
    private String taskDescription;     // 任务描述（限制300字以内）
    private String taskType;            // frontend/backend/api
    
    // 上下文（精简）
    private String prdSummary;          // PRD摘要（不是完整内容）
    private List<CodeSummary> contextSummaries;  // 已生成代码摘要列表
    private List<CodeSummary> dependencySummaries; // 依赖任务的详细摘要
    
    // 技术栈
    private String projectType;
    private String frontendFramework;
    private String backendFramework;
    
    // 代码风格（第一个任务后记录）
    private CodeStyle codeStyle;
    
    // 修复模式
    private List<CompilationError> previousErrors;  // 上次错误（最多5条）
    private Integer fixAttempt;           // 修复尝试次数（0-3）
}

/**
 * 代码摘要（用于上下文传递）
 */
public class CodeSummary {
    private String taskId;
    private String taskName;
    private List<FileSummary> files;
}

public class FileSummary {
    private String filePath;
    private String className;
    private List<String> publicMethods;  // public方法签名
    private List<String> dependencies;   // 依赖的其他类
}

/**
 * 代码风格（保持项目一致性）
 */
public class CodeStyle {
    private String indentation;          // 缩进方式（4空格/2空格/tab）
    private String namingConvention;     // 命名规范（驼峰/下划线）
    private String packageStructure;     // 包结构
}
```

**⚠️ 输入约束**：

- 任务描述限制300字
- PRD使用摘要而非完整内容
- 上下文代码使用摘要（类名、方法签名）
- 错误信息最多5条

### 3.3 Prompt构造

**System Prompt**:

```
你是一位资深的开发工程师。请根据任务描述生成可编译的代码。

【代码要求】
1. 代码完整、可编译
2. 符合技术栈规范
3. 包含必要注释
4. 无硬编码敏感信息
5. 遵循项目代码风格（如有）

【输出格式】
```

## 文件列表

### {文件路径}

```{语言}
{代码内容}
```

**说明**: {文件说明}

```

【约束】
- 文件数量不超过5个
- 单个文件代码不超过200行
- 只输出关键文件，omit非核心代码
```

**User Prompt（正常生成）**:

```
生成代码：

【任务】${taskName}
【描述】${taskDescription}
【类型】${taskType}
【技术栈】${projectType} ${frontendFramework} ${backendFramework}

${codeStyle ? "【代码风格】\n" + codeStyle : ""}

${prdSummary ? "【需求摘要】\n" + prdSummary : ""}

${contextSummaries ? "【已生成代码摘要】\n" + contextSummaries : ""}

${dependencySummaries ? "【依赖任务详情】\n" + dependencySummaries : ""}

请输出代码文件列表。
```

**User Prompt（修复模式）**:

```
修复代码：

【任务】${taskName}
【错误】${previousErrors}
【原代码】${originalCodeSummary}

请修复错误并输出完整代码。
```

**⚠️ Prompt优化**：

- 限制文件数量（最多5个）
- 限制代码行数（单个文件200行）
- 修复模式只传递错误信息和关键代码

### 3.4 输出

**AI输出**: 代码文件列表（Markdown格式）

**系统处理**:

1. 解析AI输出，提取代码文件
2. 创建文件到 `/projects/{projectId}/src/task-{taskId}/` 目录
3. **立即编译验证**，捕获编译错误
4. **提取代码摘要**，保存到上下文

**输出DTO**:

```java
public class GenerateCodeResponse {
    private boolean success;
    private String errorMessage;
    
    private List<GeneratedFile> files;
    private String readmeContent;
    
    // 编译验证结果
    private Boolean compilationPassed;
    private List<CompilationError> compilationErrors;
    
    // 提取的代码摘要（用于后续任务）
    private CodeSummary codeSummary;
    
    private AiUsage usage;
    private Long responseTimeMs;
}
```

**⚠️ 关键改进**：

- AI生成代码后立即编译验证
- 编译错误反馈给AI修复
- 不依赖AI自报的compilationStatus
- 提取代码摘要供后续任务使用

### 3.5 增量开发上下文管理

```java
@Service
public class IncrementalDevelopmentService {
    
    @Autowired
    private ProjectStatusRepository projectStatusRepository;
    
    @Autowired
    private CodeContextRepository codeContextRepository;
    
    /**
     * 执行增量开发
     */
    public void developProject(String projectId) {
        // 1. 获取项目状态
        ProjectStatus status = projectStatusRepository.findByProjectId(projectId);
        
        // 2. 获取待执行任务（按依赖排序）
        List<TaskInfo> pendingTasks = getPendingTasksOrdered(projectId);
        
        // 3. 逐个执行任务
        for (TaskInfo task : pendingTasks) {
            executeTask(projectId, task);
        }
    }
    
    private void executeTask(String projectId, TaskInfo task) {
        // 3.1 构建上下文
        GenerateCodeRequest request = buildContext(projectId, task);
        
        // 3.2 调用AI生成代码
        GenerateCodeResponse response = aiService.generateCode(request);
        
        // 3.3 编译验证
        CompilationResult compileResult = compilerService.compile(response.getFiles());
        
        if (compileResult.isSuccess()) {
            // 3.4 保存代码
            fileService.saveTaskFiles(projectId, task.getId(), response.getFiles());
            
            // 3.5 更新任务状态
            taskService.markCompleted(projectId, task.getId());
            
            // 3.6 提取并保存代码摘要（关键！）
            CodeSummary summary = extractCodeSummary(projectId, task.getId(), response.getFiles());
            codeContextRepository.save(projectId, task.getId(), summary);
            
            // 3.7 记录代码风格（第一个任务）
            if (isFirstTask(projectId, task.getId())) {
                CodeStyle style = extractCodeStyle(response.getFiles().get(0));
                codeContextRepository.saveCodeStyle(projectId, style);
            }
            
        } else {
            // 3.8 编译失败，进入修复循环
            handleCompilationError(projectId, task, compileResult.getErrors());
        }
    }
    
    /**
     * 构建代码生成请求的上下文
     */
    private GenerateCodeRequest buildContext(String projectId, TaskInfo task) {
        GenerateCodeRequest request = new GenerateCodeRequest();
        
        // 当前任务信息
        request.setTaskId(task.getId());
        request.setTaskName(task.getName());
        request.setTaskDescription(task.getDescription());
        request.setTaskType(task.getType());
        
        // PRD摘要
        String prdSummary = getPrdSummary(projectId);
        request.setPrdSummary(prdSummary);
        
        // 已生成代码摘要（所有已完成任务）
        List<CodeSummary> contextSummaries = codeContextRepository.findByProjectId(projectId);
        request.setContextSummaries(contextSummaries);
        
        // 依赖任务的详细摘要
        List<CodeSummary> dependencySummaries = new ArrayList<>();
        for (String depTaskId : task.getDependencies()) {
            CodeSummary depSummary = codeContextRepository.findByProjectIdAndTaskId(projectId, depTaskId);
            if (depSummary != null) {
                dependencySummaries.add(depSummary);
            }
        }
        request.setDependencySummaries(dependencySummaries);
        
        // 代码风格
        CodeStyle codeStyle = codeContextRepository.findCodeStyle(projectId);
        request.setCodeStyle(codeStyle);
        
        return request;
    }
    
    /**
     * 提取代码摘要
     */
    private CodeSummary extractCodeSummary(String projectId, String taskId, List<GeneratedFile> files) {
        CodeSummary summary = new CodeSummary();
        summary.setTaskId(taskId);
        summary.setTaskName(taskService.getTask(taskId).getName());
        
        List<FileSummary> fileSummaries = new ArrayList<>();
        for (GeneratedFile file : files) {
            FileSummary fileSummary = new FileSummary();
            fileSummary.setFilePath(file.getPath());
            fileSummary.setClassName(extractClassName(file.getContent()));
            fileSummary.setPublicMethods(extractPublicMethods(file.getContent()));
            fileSummary.setDependencies(extractDependencies(file.getContent()));
            fileSummaries.add(fileSummary);
        }
        summary.setFiles(fileSummaries);
        
        return summary;
    }
}
```

***

## 4. AI功能测试接口 (functional-test)

### 4.1 输入

```java
public class FunctionalTestRequest {
    private String projectId;
    private String codeSummary;         // 代码摘要（类名、方法签名）
    private String functionDescription; // 功能描述
    private String projectType;
}
```

**⚠️ 输入优化**：

- 传递代码摘要而非完整代码
- 摘要包含：类名、public方法签名

### 4.2 Prompt构造

**System Prompt**:

```
你是一位测试工程师。请为代码生成功能测试文档。

【文档要求】
1. 使用Markdown格式
2. 包含：
   - # 功能测试文档
   - ## 1. 测试用例（ID、名称、步骤、预期结果）
   - ## 2. 测试代码
3. 用例ID：TC-001
4. 优先级：P0/P1

【约束】
- 用例数量不超过10个
- 只生成核心功能测试
```

**User Prompt**:

```
生成测试：

【功能】${functionDescription}
【代码摘要】${codeSummary}
【项目类型】${projectType}

请输出测试文档。
```

### 4.3 输出

**AI输出**: Markdown格式测试文档

**系统处理**:

1. 创建文件 `/projects/{projectId}/tests/TEST-v1.md`
2. 提取测试代码并执行

***

## 5. AI安全测试接口 (security-test)

### 5.1 输入

```java
public class SecurityTestRequest {
    private String projectId;
    private String codeSummary;         // 代码摘要
    private String applicationType;     // web/api/mobile
    private String projectType;
}
```

### 5.2 Prompt构造

**System Prompt**:

```
你是一位安全工程师。请对代码进行安全测试。

【检查项】
1. SQL注入
2. XSS攻击
3. 敏感信息泄露
4. 硬编码凭证

【输出格式】
- # 安全测试报告
- ## 1. 漏洞列表（如有）
- ## 2. 风险评级
- ## 3. 修复建议

【约束】
- 只报告high/critical级别漏洞
- 如无漏洞，明确说明"未发现严重漏洞"
```

### 5.3 输出

**AI输出**: Markdown格式安全报告

**系统处理**:

1. 创建文件 `/projects/{projectId}/security/SEC-v1.md`
2. 解析漏洞列表

***

## 6. Feedback Shadow 验证接口

### 6.1 设计说明

**Feedback Shadow是一个AI接口**，调用AI模型进行质量检测。

**核心设计**：

- 调用AI模型判断文档质量
- 决策：ALLOW / REPAIR / REJECT
- 不是硬编码规则

**⚠️ 关键改进**：

- Feedback Shadow只做文档质量检测
- 代码编译验证由系统在AI开发接口后立即执行
- 编译错误直接反馈给AI开发接口修复，不经过Feedback Shadow

### 6.2 输入

```java
public class FeedbackShadowValidateRequest {
    private String projectId;
    private String apiType;             // clarify-requirement/split-tasks/generate-code/functional-test/security-test
    private String documentPath;
    private String documentContent;     // 文档内容（限制3000字以内）
    private String previousFeedback;    // 上次反馈（修复时）
}
```

**⚠️ 输入优化**：

- 文档内容限制3000字，超长则截断
- 代码生成接口的验证主要依赖编译结果，Feedback Shadow只做辅助检查

### 6.3 AI Prompt构造

**System Prompt**:

```
你是一位技术评审专家。请对AI生成的文档进行质量检测。

【检测维度】
1. 完整性：是否包含必需章节
2. 准确性：内容是否合理
3. 规范性：格式是否正确

【决策标准】
- ALLOW：文档质量良好
- REPAIR：有小问题需要修复
- REJECT：有严重问题无法使用

【输出要求】
- # 质量检测报告
- ## 1. 检测摘要
- ## 2. 问题列表
- ## 3. 决策建议（ALLOW/REPAIR/REJECT）

【约束】
- 检测时间控制在10秒内
- 只检测明显问题
```

**User Prompt**:

```
检测文档：

【类型】${apiType}
【内容】${documentContent}

请输出检测报告。
```

### 6.4 输出

**AI模型输出**: Markdown格式检测报告

**系统处理**:

1. 创建文件 `/projects/{projectId}/feedback/FB-{timestamp}.md`
2. 解析决策（ALLOW/REPAIR/REJECT）

**⚠️ 决策逻辑**：

- 对于代码生成接口，主要依据编译结果
- Feedback Shadow的决策作为辅助参考
- 编译失败 → 强制REPAIR
- 编译通过 + Feedback Shadow ALLOW → ALLOW
- 编译通过 + Feedback Shadow REPAIR → REPAIR

***

## 7. 数据库存储设计

### 7.1 项目状态表

```sql
CREATE TABLE project_status (
    project_id VARCHAR(64) PRIMARY KEY,
    current_phase VARCHAR(50),        -- clarify-requirement/split-tasks/developing/testing/completed
    current_task_id VARCHAR(64),      -- 当前执行的任务ID
    completed_tasks JSON,             -- 已完成任务ID列表
    pending_tasks JSON,               -- 待执行任务ID列表
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);
```

### 7.2 任务状态表

```sql
CREATE TABLE task_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(64),
    task_id VARCHAR(64),
    task_name VARCHAR(200),
    task_type VARCHAR(50),            -- design/frontend/backend/api/test/doc
    state VARCHAR(50),                -- PENDING/IN_PROGRESS/COMPLETED/FAILED
    priority VARCHAR(10),             -- P0/P1/P2
    dependencies JSON,                -- 依赖任务ID列表
    generated_code_path VARCHAR(500), -- 生成代码的存储路径
    retry_count INT DEFAULT 0,
    error_message TEXT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id)
);
```

### 7.3 代码文件表

```sql
CREATE TABLE generated_files (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(64),
    task_id VARCHAR(64),
    file_path VARCHAR(500),
    file_content TEXT,
    language VARCHAR(50),
    created_at TIMESTAMP,
    INDEX idx_project_task (project_id, task_id)
);
```

### 7.4 代码摘要表（用于上下文传递）

```sql
CREATE TABLE code_context (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(64),
    task_id VARCHAR(64),
    task_name VARCHAR(200),
    class_name VARCHAR(200),
    public_methods JSON,              -- ["register(String phone)", "login(String phone, String password)"]
    dependencies JSON,                -- 依赖的其他类
    created_at TIMESTAMP,
    INDEX idx_project_id (project_id),
    INDEX idx_task_id (task_id)
);
```

### 7.5 代码风格表

```sql
CREATE TABLE code_style (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    project_id VARCHAR(64) UNIQUE,
    indentation VARCHAR(20),          -- 4_spaces/2_spaces/tab
    naming_convention VARCHAR(50),    -- camelCase/snake_case
    package_structure VARCHAR(200),
    created_at TIMESTAMP
);
```

***

## 8. 关键流程改进

### 8.1 AI开发接口流程（关键改进）

```
AI开发接口生成代码
    │
    ▼
系统立即编译验证（不是Feedback Shadow）
    │
    ├─ 编译通过 ──► Feedback Shadow文档检测 ──► ALLOW/REPAIR
    │
    └─ 编译失败 ──► 直接调度AI修复（不经过Feedback Shadow）
                      │
                      ▼
                  AI修复生成
                      │
                      ▼
                  再次编译验证（循环最多3次）
```

**改进原因**：

- 编译错误是客观事实，不需要AI判断
- 减少Feedback Shadow调用次数，节省token
- 加快修复循环速度

### 8.2 增量开发完整流程

```
1. 初始化项目
   │
   ▼
2. AI明确需求 ──► 生成PRD ──► Feedback Shadow检测
   │
   ▼
3. AI拆分任务 ──► 生成任务清单 ──► 初始化任务状态表（全部PENDING）
   │
   ▼
4. 增量开发阶段
   │
   ├─ 获取PENDING任务（按依赖排序）
   │
   ├─ 对每个任务：
   │   ├─ 构建上下文（PRD摘要 + 已生成代码摘要）
   │   ├─ 调用AI生成代码
   │   ├─ 编译验证
   │   ├─ 保存代码
   │   ├─ 提取代码摘要
   │   ├─ 保存到上下文表
   │   └─ 标记任务COMPLETED
   │
   ▼
5. 所有任务完成
   │
   ▼
6. AI功能测试 / AI安全测试
```

***

## 9. 容错与降级策略

### 9.1 AI输出解析容错

```java
public class DocumentParser {
    
    public ParsedDocument parseWithFallback(String content) {
        try {
            // 尝试标准解析
            return parseStandard(content);
        } catch (ParseException e) {
            // 容错解析
            return parseLenient(content);
        }
    }
    
    private ParsedDocument parseLenient(String content) {
        // 使用正则表达式提取关键信息
        // 不严格要求格式
        // 缺失字段使用默认值
    }
}
```

### 9.2 超时降级

| 接口              | 超时处理          |
| --------------- | ------------- |
| 明确需求            | 返回简化版PRD      |
| 拆分任务            | 返回基础任务列表      |
| 代码生成            | 返回核心代码文件      |
| Feedback Shadow | 默认ALLOW（人工复核） |

### 9.3 重试策略

```java
public class AiCallStrategy {
    
    public AiResponse callWithRetry(AiRequest request) {
        int maxRetries = 3;
        for (int i = 0; i < maxRetries; i++) {
            try {
                return aiClient.call(request);
            } catch (TimeoutException e) {
                if (i == maxRetries - 1) {
                    return fallbackResponse(request);
                }
                Thread.sleep(1000 * (i + 1)); // 指数退避
            }
        }
    }
}
```

***

## 10. 质量门禁

| 阶段   | 检查项             | 优先级 | 失败处理       |
| ---- | --------------- | --- | ---------- |
| AI开发 | 编译通过            | P0  | 自动修复（最多3次） |
| AI开发 | Feedback Shadow | P1  | 修复后重试      |
| 用户验收 | P0功能正常          | P0  | 紧急修复       |
| 用户验收 | P1功能缺陷          | P1  | 24小时内修复    |

