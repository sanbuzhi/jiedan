# Feedback Shadow 监控系统 - 架构文档

## 1. 系统概述

### 1.1 系统名称
**Feedback Shadow 监控系统** - 智能获客系统的AI代码生成质量监控与修复平台

### 1.2 系统定位
本系统是"智能获客系统"的核心AI开发组件，提供：
- AI接口输出的质量监控（Feedback Shadow）
- 自动化代码生成（三种类型：脚手架/增量开发/质量干预）
- 编译错误自修复机制
- 用户反馈驱动的代码修复流程

### 1.3 核心设计理念

#### 1.3.1 增量式开发
AI不能一次生成完整项目代码，必须采用**任务-by-任务**的增量开发模式：
- 每个任务独立生成、编译、测试
- 通过代码摘要传递上下文，确保多轮调用一致性
- Token 控制：输入截断、输出限制

#### 1.3.2 质量门禁
- **P0（编译错误）**：必须在AI开发阶段自解决
- **P1（运行时错误）**：必须在AI开发阶段自解决
- **P2（代码规范）**：记录并提示，不阻断流程
- **P3（安全检查）**：严重问题必须修复

#### 1.3.3 Feedback Shadow
每个AI接口执行后，由Feedback Shadow进行检测：
- **ALLOW**：质量通过，继续下一步
- **REPAIR**：发现问题，自动调度修复
- **REJECT**：严重问题，终止流程

---

## 2. 系统架构

### 2.1 整体架构图

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端/小程序端                                    │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐ │
│  │ 需求录入    │  │ 任务监控    │  │ 代码查看    │  │ 反馈提交            │ │
│  └─────────────┘  └─────────────┘  └─────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              REST API 层                                    │
│  ┌────────────────────┐  ┌────────────────────┐  ┌────────────────────────┐ │
│  │ CodeGeneration     │  │ UserFeedback       │  │ FeedbackShadow         │ │
│  │ Controller         │  │ Controller         │  │ Controller             │ │
│  │ /api/ai/code/*     │  │ /api/ai/feedback/* │  │ /api/ai/shadow/*       │ │
│  └────────────────────┘  └────────────────────┘  └────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              服务层 (Service)                                │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    CodeGenerationOrchestrator                       │   │
│  │                         (代码生成编排器)                             │   │
│  │  ┌───────────────┐  ┌─────────────────────┐  ┌───────────────────┐ │   │
│  │  │  Type 1       │  │  Type 2             │  │  Type 3           │ │   │
│  │  │  Scaffold     │  │  Incremental        │  │  Quality Gate     │ │   │
│  │  │  Generator    │  │  Development        │  │  (Shadow)         │ │   │
│  │  └───────────────┘  └─────────────────────┘  └───────────────────┘ │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌────────────────────────┐  ┌─────────────────────────────────────────┐   │
│  │ UserFeedbackService    │  │ FeedbackShadowService                   │   │
│  │ (用户反馈处理)          │  │ (质量监控与检测)                         │   │
│  └────────────────────────┘  └─────────────────────────────────────────┘   │
│                                                                             │
│  ┌────────────────────────┐  ┌─────────────────────────────────────────┐   │
│  │ TaskScheduler          │  │ CodeCompiler                            │   │
│  │ (任务调度器)            │  │ (代码编译器)                             │   │
│  └────────────────────────┘  └─────────────────────────────────────────┘   │
│                                                                             │
│  ┌────────────────────────┐  ┌─────────────────────────────────────────┐   │
│  │ CodeQualityChecker     │  │ DocumentParser                          │   │
│  │ (代码质量检查)          │  │ (文档解析器)                             │   │
│  └────────────────────────┘  └─────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              数据访问层 (Repository)                          │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐  ┌──────────────┐ │
│  │ ProjectStatus │  │ TaskStatus    │  │ CodeContext   │  │ UserFeedback │ │
│  │ Repository    │  │ Repository    │  │ Repository    │  │ Repository   │ │
│  └───────────────┘  └───────────────┘  └───────────────┘  └──────────────┘ │
└─────────────────────────────────────────────────────────────────────────────┘
                                       │
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              外部服务                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                    AI Provider (Volcano Engine / Doubao)            │   │
│  │  - clarify-requirement  │  - split-tasks  │  - generate-code        │   │
│  │  - functional-test      │  - security-test │  - feedback-shadow     │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 2.2 模块说明

#### 2.2.1 Controller 层
| 控制器 | 路径 | 职责 |
|--------|------|------|
| CodeGenerationController | /api/ai/code/* | 代码生成主入口 |
| UserFeedbackController | /api/ai/feedback/* | 用户反馈处理 |
| FeedbackShadowController | /api/ai/shadow/* | 质量监控接口 |

#### 2.2.2 Service 层
| 服务 | 职责 |
|------|------|
| CodeGenerationOrchestrator | 编排三种类型的代码生成流程 |
| ScaffoldGenerator | Type 1: 脚手架生成 |
| IncrementalDevelopmentService | Type 2: 增量开发核心服务 |
| CodeQualityChecker | Type 3: 代码质量检查 |
| UserFeedbackService | 用户反馈处理与AI修复 |
| FeedbackShadowService | 质量监控与决策 |
| TaskScheduler | 任务拓扑排序与状态管理 |
| CodeCompiler | 代码编译验证 |

#### 2.2.3 Repository 层
| 实体 | 存储内容 |
|------|----------|
| ProjectStatus | 项目状态、当前阶段 |
| TaskStatus | 任务状态、依赖关系、重试次数 |
| CodeContext | 代码摘要（类名、方法签名） |
| UserFeedback | 用户反馈、修复状态 |

---

## 3. 核心流程

### 3.1 完整AI开发流程

```
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────┐
│  开始   │ -> │  脚手架生成  │ -> │  任务初始化  │ -> │  增量开发   │ -> │ 质量门禁 │
└─────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └────┬────┘
                                                                              │
                                                                              ▼
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────┐
│  完成   │ <- │  结束       │ <- │  修复失败?  │ <- │  发现问题?  │ <- │  检查   │
└─────────┘    └─────────────┘    └─────────────┘    └─────────────┘    └─────────┘
```

### 3.2 增量开发循环（Type 2）

```
                    ┌─────────────────────────────────────┐
                    │                                     │
                    ▼                                     │
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌──┴────────┐
│ 开始    │ -> │ 提取下一任务 │ -> │ AI生成代码  │ -> │ 编译检查  │
└─────────┘    └─────────────┘    └─────────────┘    └─────┬─────┘
                                                           │
                              ┌────────────────────────────┘
                              │ 失败
                              ▼
                    ┌─────────────────┐
                    │  保存错误信息   │
                    │  重试计数+1     │
                    └────────┬────────┘
                             │ 超过3次?
              ┌──────────────┴──────────────┐
              │ 是                          │ 否
              ▼                             ▼
    ┌─────────────────┐           ┌─────────────────┐
    │   标记失败      │           │   重新生成代码  │
    │   结束          │           │   (带错误信息)  │
    └─────────────────┘           └─────────────────┘
                              │
                              │ 成功
                              ▼
                    ┌─────────────────┐
                    │   保存代码      │
                    │   提取摘要      │
                    │   保存上下文    │
                    │   标记完成      │
                    └────────┬────────┘
                             │
                             ▼
                    ┌─────────────────┐
                    │   还有任务?     │
                    └────────┬────────┘
              ┌──────────────┴──────────────┐
              │ 是                          │ 否
              ▼                             ▼
    ┌─────────────────┐           ┌─────────────────┐
    │   继续循环      │           │   全部完成      │
    └─────────────────┘           └─────────────────┘
```

### 3.3 用户反馈处理流程

```
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 提交反馈 │ -> │ 保存到DB    │ -> │ 异步触发    │ -> │ 读取文件    │
└─────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                        │
                                                        ▼
┌─────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│ 完成    │ <- │ 更新状态    │ <- │ 保存修复    │ <- │ AI修复      │
└─────────┘    └─────────────┘    └─────────────┘    └─────────────┘
                                                        │
                              ┌────────────────────────┘
                              │ 失败
                              ▼
                    ┌─────────────────┐
                    │  重试计数+1     │
                    │  最多3次        │
                    └─────────────────┘
```

---

## 4. API 接口清单

### 4.1 代码生成接口

#### 4.1.1 初始化脚手架
```
POST /api/ai/code/scaffold/{projectId}
Request:
{
  "projectType": "springboot",
  "config": {
    "springBootVersion": "3.2.0",
    "javaVersion": "17",
    "dependencies": ["web", "data-jpa", "mysql"]
  }
}
Response:
{
  "success": true,
  "files": [...],
  "usage": {...},
  "responseTimeMs": 15000
}
```

#### 4.1.2 执行增量开发
```
POST /api/ai/code/develop/{projectId}
Request:
{
  "projectType": "springboot",
  "prdSummary": "PRD摘要内容..."
}
Response:
{
  "success": true,
  "message": "增量开发已启动",
  "projectId": "PROJ001"
}
```

#### 4.1.3 执行完整开发流程
```
POST /api/ai/code/develop-full/{projectId}
Request:
{
  "projectType": "springboot",
  "scaffoldConfig": {...},
  "prdSummary": "PRD摘要...",
  "taskList": [
    {"id": "T1", "name": "用户模块", "type": "backend", "priority": "P0", "dependencies": []},
    {"id": "T2", "name": "订单模块", "type": "backend", "priority": "P0", "dependencies": ["T1"]}
  ]
}
```

#### 4.1.4 获取任务列表
```
GET /api/ai/code/tasks/{projectId}
Response:
[
  {
    "taskId": "T1",
    "taskName": "用户模块",
    "taskType": "backend",
    "state": "COMPLETED",
    "priority": "P0"
  }
]
```

#### 4.1.5 质量门禁检查
```
POST /api/ai/code/quality-check/{projectId}
Request:
{
  "projectType": "springboot",
  "taskId": "T1"
}
Response:
{
  "passed": true,
  "overallMessage": "所有质量检查通过",
  "compilationCheck": {...},
  "styleChecks": [...],
  "securityChecks": [...]
}
```

### 4.2 用户反馈接口

#### 4.2.1 提交反馈
```
POST /api/ai/feedback/submit
Request:
{
  "projectId": "PROJ001",
  "taskId": "T1",
  "feedbackType": "CODE_ISSUE",
  "description": "用户登录接口返回格式不正确",
  "affectedFiles": ["src/main/java/com/example/UserController.java"],
  "severity": "HIGH",
  "source": "USER"
}
Response:
{
  "success": true,
  "feedbackId": "FB2024031412304512345678",
  "status": "PENDING",
  "estimatedRepairTime": 5
}
```

---

## 5. 数据模型

### 5.1 项目状态 (ProjectStatus)
```java
{
  "projectId": "String",          // 项目ID
  "state": "String",              // 状态: SCAFFOLDING/DEVELOPING/COMPLETED/FAILED
  "currentPhase": "String",       // 当前阶段描述
  "currentTaskId": "String",      // 当前执行任务ID
  "completedTasks": "JSON",       // 已完成任务列表
  "pendingTasks": "JSON"          // 待执行任务列表
}
```

### 5.2 任务状态 (TaskStatus)
```java
{
  "id": "Long",                   // 自增ID
  "projectId": "String",          // 项目ID
  "taskId": "String",             // 任务ID
  "taskName": "String",           // 任务名称
  "taskType": "String",           // 类型: design/frontend/backend/api/test/doc
  "state": "String",              // 状态: PENDING/IN_PROGRESS/COMPLETED/FAILED
  "priority": "String",           // 优先级: P0/P1/P2
  "dependencies": "JSON",         // 依赖任务ID列表
  "generatedCodePath": "String",  // 生成代码路径
  "retryCount": "Integer",        // 重试次数
  "errorMessage": "String"        // 错误信息
}
```

### 5.3 代码上下文 (CodeContext)
```java
{
  "id": "Long",                   // 自增ID
  "projectId": "String",          // 项目ID
  "taskId": "String",             // 任务ID
  "taskName": "String",           // 任务名称
  "className": "String",          // 类名
  "publicMethods": "JSON",        // public方法签名列表
  "dependencies": "JSON"          // 依赖的其他类
}
```

### 5.4 用户反馈 (UserFeedback)
```java
{
  "feedbackId": "String",         // 反馈ID
  "projectId": "String",          // 项目ID
  "taskId": "String",             // 任务ID
  "feedbackType": "String",       // 类型: CODE_ISSUE/DESIGN_ISSUE/FUNCTION_MISSING/OTHER
  "description": "String",        // 问题描述
  "affectedFiles": "JSON",        // 受影响文件列表
  "expectedFix": "String",        // 期望修复方式
  "severity": "String",           // 严重程度: CRITICAL/HIGH/MEDIUM/LOW
  "source": "String",             // 来源: USER/TEST/AUTO
  "status": "String",             // 状态: PENDING/PROCESSING/COMPLETED/FAILED
  "repairResult": "String",       // 修复结果
  "repairedFiles": "JSON",        // 修复后的文件
  "repairAttempts": "Integer"     // 修复尝试次数
}
```

---

## 6. 关键设计决策

### 6.1 增量开发策略

**问题**：AI无法一次生成完整项目代码

**解决方案**：
1. 任务拆分：将项目拆分为独立的任务单元
2. 拓扑排序：按依赖关系确定执行顺序
3. 上下文传递：通过代码摘要传递已完成的代码信息
4. 循环开发：提取→开发→自测→标记的循环流程

### 6.2 Token 控制策略

**输入控制**：
- PRD 摘要截断至 2000 字符
- 代码摘要只保留类名和方法签名
- 编译错误最多保留 10 条

**输出控制**：
- 最大输出 Token：4000
- 温度参数：0.2（降低随机性）

### 6.3 质量门禁策略

**编译错误（P0）**：
- 必须自修复，最多重试 3 次
- 修复失败则标记任务失败

**代码规范（P1）**：
- 命名规范、行长度、导入组织等
- 记录但不阻断流程

**安全检查（P2）**：
- SQL注入、XSS、硬编码密钥等
- 严重问题必须修复

### 6.4 错误处理策略

**重试机制**：
- 最大重试次数：3 次
- 每次重试将错误信息反馈给 AI
- 超过次数标记失败，人工介入

**降级策略**：
- 单个任务失败不影响其他任务
- 记录失败原因，供人工查看

---

## 7. 部署说明

### 7.1 依赖要求
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Python 3.8+（用于Python项目编译检查）

### 7.2 配置文件
```yaml
# application.yml
ai:
  provider:
    volcano:
      api-key: ${VOLCANO_API_KEY}
      endpoint: https://ark.cn-beijing.volces.com/api/v3
  code-generation:
    max-retry: 3
    max-input-tokens: 8000
    max-output-tokens: 4000
    project-base-path: ./projects
```

### 7.3 数据库初始化
```sql
-- 执行 src/main/resources/db/migration/V1__init_feedback_shadow.sql
```

---

## 8. 扩展规划

### 8.1 短期规划
- [ ] 支持更多项目类型（Vue、React、Node.js）
- [ ] 集成更多AI模型（GPT-4、Claude）
- [ ] 代码版本管理（Git集成）

### 8.2 中期规划
- [ ] 智能任务拆分（AI自动拆分任务）
- [ ] 代码相似度检测（避免重复代码）
- [ ] 性能测试集成

### 8.3 长期规划
- [ ] 多模态支持（图片、语音需求输入）
- [ ] 自适应Prompt优化
- [ ] AI代码审查员

---

## 9. 附录

### 9.1 术语表
| 术语 | 说明 |
|------|------|
| Feedback Shadow | 质量监控系统，负责AI输出的质量检测 |
| PRD | Product Requirement Document，产品需求文档 |
| Token | AI模型的输入/输出计量单位 |
| 脚手架 | 项目基础结构和配置 |
| 增量开发 | 任务-by-任务的开发模式 |
| 代码摘要 | 代码的简化表示（类名、方法签名） |

### 9.2 参考文档
- [Volcano Engine API文档](https://www.volcengine.com/docs/82379)
- [Spring Boot官方文档](https://spring.io/projects/spring-boot)
- [AI代码生成最佳实践](https://...)

---

**文档版本**: 1.0  
**最后更新**: 2024-03-14  
**作者**: AI开发团队
