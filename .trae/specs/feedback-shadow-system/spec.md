# Feedback Shadow 盯梢系统 Spec

## Why

当前AI开发流程存在以下问题：

1. AI接口输出质量不可控，编译错误、运行错误在交付后才暴露
2. 缺乏统一的输出规范检测机制，无法自动拦截低质量输出
3. 用户反馈处理流程分散，修复迭代效率低
4. 各AI接口之间缺乏衔接标准，导致下游接口接收到的输入不规范

通过构建Feedback Shadow盯梢系统，实现AI接口输出的实时监控、自动修复、用户反馈闭环，提升AI开发交付质量。

## What Changes

### 1. 新增 Feedback Shadow 盯梢接口（AI接口）

- **feedback_shadow 是一个AI接口**，调用AI模型进行质量检测
- 每个AI接口执行完成后，输出结果必须经过feedback_shadow检测
- AI模型判断文档质量，输出决策：ALLOW（放行）/ REPAIR（修复）/ REJECT（拒绝）
- 检测不通过则调度对应AI接口自我修复

### 2. 增量式开发（核心设计）

- **AI无法一次生成完整项目代码**，必须按任务增量生成
- 任务拆分后，按依赖拓扑排序逐个执行
- 通过上下文传递（PRD摘要 + 代码摘要）保持代码一致性
- 代码风格从第一个任务提取，后续任务遵循
- 代码按任务分目录存储：`/projects/{projectId}/src/task-{taskId}/`

### 3. 代码包结构强制约定

- 在"AI开发"阶段前，根据"AI明确需求"输出的架构部分决定代码包结构
- 支持多类型项目：SpringBoot后端/SpringCloud后端/Python后端/桌面端/网页前端/微信小程序/抖音小程序/Windows脚本/Linux脚本

### 4. AI开发阶段质量门禁

- **编译报错**（最高优先级）：必须在AI开发阶段自我消化
- **运行报错**（次高优先级）：必须在AI开发阶段自我消化
- 通过feedback_shadow反复验证，直到无编译/运行错误才放行
- 放行后接受用户低优先级反馈（样式布局/性能优化/用户体验/功能欠缺）

### 5. AI功能测试规范

- 使用用户提供的功能测试标准说明书
- 新增功能测试标准文档模板（见 `functional-test-standard.md`）

### 6. AI接口输入输出规范

- 制定每个AI接口的详细输入输出规范（见 `ai-interface-specifications.md`）
- AI接口输出为Markdown格式文档
- Feedback Shadow也是AI接口，调用AI模型进行检测
- 定义文档章节规范和验证规则
- 定义增量式开发流程和上下文管理机制

## Impact

- **后端**：
  - 新增FeedbackShadowController、FeedbackShadowService、AI接口输出规范校验器
  - 新增IncrementalDevelopmentService（增量式开发服务）
  - 新增TaskScheduler（任务调度器）
  - 新增CodeContextRepository（代码上下文管理）
  - 新增数据表：project_status、task_status、code_context、code_style

- **前端/小程序**：
  - 调整AI接口调用流程，增加feedback_shadow检测环节
  - 支持查看增量开发进度

- **AI接口**：
  - 所有AI接口输出Markdown文档
  - Feedback Shadow调用AI模型检测
  - AI开发接口支持增量式生成，通过上下文保持代码一致性

## ADDED Requirements

### Requirement: Feedback Shadow 盯梢接口（AI接口）

The system SHALL provide a feedback\_shadow AI interface to monitor and validate AI interface outputs.

#### Scenario: AI接口输出检测

* **GIVEN** AI接口（明确需求/拆分任务/开发/功能测试/安全测试）执行完成，输出Markdown文档

* **WHEN** 文档提交到feedback\_shadow接口

* **THEN** feedback\_shadow调用AI模型进行质量检测

* **AND** AI模型输出检测报告，包含决策建议（ALLOW/REPAIR/REJECT）

* **AND** 根据决策执行相应操作

#### Scenario: 自我修复循环

* **GIVEN** feedback\_shadow检测到AI开发输出存在编译错误

* **WHEN** 调度AI开发接口进行修复

* **THEN** AI开发接口接收错误信息和原代码

* **AND** 生成修复后的代码

* **AND** 再次提交到feedback\_shadow检测

* **AND** 循环直到检测通过或达到最大重试次数（3次）

### Requirement: AI明确需求接口

The system SHALL provide an AI interface to clarify requirements and output PRD document.

#### Scenario: AI明确需求输入

* **GIVEN** 用户从小程序前端提交需求选项

* **THEN** 输入包含：

  * 项目角色（公司企业/个人经营者/学生/不明确/其他）文本类型

  * 项目类型（微信小程序/抖音小程序/网站系统/不明确/其他）-文本类型

  * 业务场景描述（场景、领域、目标用户，产品主要功能作用）

  * 功能点集合（标准功能勾选+自定义功能）

  * 原始需求物料（参考链接、竞品分析）

  * 交互视觉（UI参考图片、风格描述、交互要求）

#### Scenario: AI明确需求输出

* **GIVEN** AI明确需求接口执行完成

* **THEN** 输出Markdown格式PRD文档，必须包含：

  * <br />

    # 产品需求文档（文档标题）

  * <br />

    ## 1. 项目概述（背景、目标、范围、上线要求、流量预期）

  * <br />

    ## 2. 业务场景分析（核心业务流程、用户旅程地图、业务规则）

  * <br />

    ## 3. 功能需求（功能模块清单、详细说明、页面清单）

  * <br />

    ## 4. 非功能需求（性能、安全、兼容性）

  * <br />

    ## 5. 交互与视觉（风格定位、交互说明、视觉参考）

  * <br />

    ## 6. 技术架构（前端/后端/数据库/第三方服务）

  * <br />

    ## 7. 项目规划（里程碑）

* **AND** 功能需求必须包含至少一个P0优先级模块

* **AND** 技术架构必须与项目类型匹配

### Requirement: AI拆分任务接口

The system SHALL provide an AI interface to split tasks and output task document.

#### Scenario: AI拆分任务输入

* **GIVEN** PRD文档已生成

* **THEN** 输入包含：

  * PRD文档内容

  * 项目类型

  * 是否需要上线

#### Scenario: AI拆分任务输出

* **GIVEN** AI拆分任务接口执行完成

* **THEN** 输出Markdown格式任务文档，必须包含：

  * <br />

    # 任务拆分文档

  * <br />

    ## 1. 功能拆分矩阵（功能点 × 任务类型）

  * <br />

    ## 2. 任务清单（按模块分组，每个任务：ID、名称、类型、工时、优先级、依赖、交付物、验收标准）

  * <br />

    ## 3. 设计任务清单（UI/交互设计任务）

  * <br />

    ## 4. 开发任务清单（前端/后端/接口开发任务）

  * <br />

    ## 5. 测试任务清单（单元/集成/功能测试任务）

  * <br />

    ## 6. 核心开发任务（标识核心任务及理由）

  * <br />

    ## 7. 里程碑与排期（甘特图文字描述）

  * <br />

    ## 8. 依赖关系与风险

* **AND** 任务拆分原则：确保功能无遗漏、确保拆成可测试单元、确保测试可交付

* **AND** 任务类型：设计、开发、测试、文档

### Requirement: AI开发接口（三种类型代码生成）

The system SHALL provide an AI interface to generate code in three types: scaffold initialization, task loop development, and feedback shadow intervention.

#### Scenario: 类型1 - 脚手架初始化生成

- **GIVEN** 任务拆分完成，首次代码生成前
- **WHEN** 触发脚手架生成
- **THEN** 系统生成各端项目脚手架：
  - 目录结构（controller/service/repository/entity/config等）
  - 依赖配置（pom.xml/package.json/requirements.txt）
  - 基础配置文件（application.yml等）
  - 通用工具类和基础类（Result、全局异常处理等）
- **AND** 不包含业务逻辑代码
- **AND** 编译验证脚手架
- **AND** 保存到 `/projects/{projectId}/src/scaffold/`

#### Scenario: 类型2 - 任务循环开发

- **GIVEN** 脚手架已生成，存在PENDING状态任务
- **WHEN** 开始任务循环开发
- **THEN** 系统按以下流程执行：
  1. **提取任务**：按依赖拓扑排序获取下一个可执行任务
  2. **开发**：构建上下文（PRD摘要 + 代码摘要），调用AI生成代码
  3. **自测**：编译验证，捕获编译错误
  4. **标记**：编译通过标记COMPLETED，失败进入修复循环
- **AND** 循环直到所有任务完成

#### Scenario: 类型3 - Feedback Shadow干预

- **GIVEN** AI生成代码后
- **WHEN** Feedback Shadow检测代码质量
- **THEN** 系统检测以下维度：
  - 代码规范：是否符合项目代码风格
  - 接口一致性：是否与已有接口兼容
  - 安全规范：是否有明显安全问题
  - 性能规范：是否有明显性能问题
- **AND** 输出决策：ALLOW（继续）/ REPAIR（修复）/ REJECT（拒绝）
- **AND** REPAIR时生成修复建议，调度AI修复

#### Scenario: 完整的代码生成流程

- **GIVEN** 项目已拆分任务
- **WHEN** 开始代码生成
- **THEN** 系统按以下阶段执行：
  1. **阶段1 - 脚手架生成**（仅首次）：生成各端脚手架 → 编译验证 → 保存
  2. **阶段2 - 任务循环开发**：对每个任务：构建上下文 → AI生成 → Feedback Shadow检测 → 编译验证 → 保存 → 标记完成
  3. **阶段3 - Feedback Shadow最终检测**：对整个项目检测 → 生成质量报告
- **AND** 代码按任务分目录存储：`/projects/{projectId}/src/task-{taskId}/`

### Requirement: AI功能测试接口

The system SHALL provide an AI interface to generate functional tests.

#### Scenario: AI功能测试输出

* **GIVEN** AI功能测试接口执行完成

* **THEN** 输出Markdown格式测试文档，必须包含：

  * <br />

    # 功能测试文档

  * <br />

    ## 1. 测试范围

  * <br />

    ## 2. 测试用例（ID、名称、优先级、前置条件、测试步骤、预期结果）

  * <br />

    ## 3. 测试代码

  * <br />

    ## 4. 覆盖率报告

  * <br />

    ## 5. 执行结果

### Requirement: AI安全测试接口

The system SHALL provide an AI interface to generate security tests.

#### Scenario: AI安全测试输出

* **GIVEN** AI安全测试接口执行完成

* **THEN** 输出Markdown格式安全报告，必须包含：

  * <br />

    # 安全测试报告

  * <br />

    ## 1. 测试概述

  * <br />

    ## 2. 漏洞列表（ID、类型、严重程度、描述、影响、修复建议）

  * <br />

    ## 3. 风险评级

  * <br />

    ## 4. 修复建议

  * <br />

    ## 5. 合规检查

### Requirement: 增量式开发与上下文管理

The system SHALL support incremental development with context management.

#### Scenario: 任务状态管理

- **GIVEN** 项目已拆分任务
- **THEN** 系统初始化任务状态表，所有任务标记为 PENDING
- **AND** 记录任务依赖关系
- **AND** 按依赖拓扑排序确定执行顺序

#### Scenario: 代码摘要提取与存储

- **GIVEN** AI开发完成任务生成代码
- **WHEN** 代码编译通过
- **THEN** 系统提取代码摘要：
  - 类名
  - public方法签名
  - 依赖的其他类
- **AND** 保存到 code_context 表
- **AND** 供后续任务使用

#### Scenario: 代码风格一致性

- **GIVEN** 第一个开发任务完成
- **THEN** 系统提取代码风格：
  - 缩进方式（4空格/2空格/tab）
  - 命名规范（驼峰/下划线）
  - 包结构
- **AND** 保存到 code_style 表
- **AND** 后续任务遵循相同样式

### Requirement: 代码包结构强制约定

The system SHALL enforce code package structure based on architecture decisions.

#### Scenario: SpringBoot后端项目结构

* **GIVEN** 架构选型为SpringBoot后端

* **THEN** AI开发输出必须包含：

  ```
  project/
  ├── src/main/java/com/example/
  │   ├── controller/
  │   ├── service/
  │   ├── repository/
  │   ├── entity/
  │   └── config/
  ├── src/main/resources/
  │   ├── application.yml
  │   └── db/migration/
  ├── pom.xml
  └── README.md
  ```

#### Scenario: Python后端项目结构

* **GIVEN** 架构选型为Python后端

* **THEN** AI开发输出必须包含：

  ```
  project/
  ├── app/
  │   ├── __init__.py
  │   ├── models.py
  │   ├── routes.py
  │   └── services.py
  ├── tests/
  ├── requirements.txt
  ├── app.py
  └── README.md
  ```

#### Scenario: 微信小程序项目结构

* **GIVEN** 架构选型为微信小程序

* **THEN** AI开发输出必须包含：

  ```
  project/
  ├── pages/
  ├── components/
  ├── utils/
  ├── app.js
  ├── app.json
  ├── app.wxss
  └── README.md
  ```

### Requirement: AI开发质量门禁

The system SHALL enforce quality gates for AI development phase.

#### Scenario: 编译错误拦截

* **GIVEN** AI开发输出包含编译错误

* **WHEN** feedback\_shadow执行检测

* **THEN** AI模型返回REPAIR决策

* **AND** 自动调度AI开发接口修复

* **AND** 不允许进入下一阶段

#### Scenario: 运行错误拦截

* **GIVEN** AI开发输出编译通过但运行报错

* **WHEN** feedback\_shadow执行检测

* **THEN** AI模型返回REPAIR决策

* **AND** 自动调度AI开发接口修复

* **AND** 不允许进入下一阶段

#### Scenario: 质量门禁放行

* **GIVEN** AI开发输出编译通过且运行正常

* **WHEN** feedback\_shadow执行检测

* **THEN** AI模型返回ALLOW决策

* **AND** 允许进入用户验收阶段

### Requirement: 用户反馈处理

The system SHALL handle user feedback and trigger AI repair workflow.

#### Scenario: 用户提交反馈

* **GIVEN** 用户发现代码问题（样式/性能/体验/功能）

* **WHEN** 用户提交反馈

* **THEN** feedback\_shadow接收反馈

* **AND** 根据反馈类型调度对应AI接口修复

* **AND** 修复后再次经过feedback\_shadow检测

#### Scenario: 反馈优先级处理

* **GIVEN** 用户提交多个反馈

* **THEN** 按优先级处理：

  * P0（崩溃/无法运行）：立即修复

  * P1（功能缺陷）：24小时内修复

  * P2（体验优化）：排期修复

## MODIFIED Requirements

None

## REMOVED Requirements

None
