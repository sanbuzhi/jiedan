# Feedback Shadow 盯梢系统 任务清单

## 任务依赖拓扑
```
任务1 ──► 任务2 ──► 任务3 ──► 任务4 ──► 任务5 ──► 任务6 ──► 任务7 ──► 任务8
```

## 任务详情

### 任务1: 设计AI接口输入输出规范文档 ✅ 已完成
**任务描述**: 作为需求设计师/系统架构师，制定每个AI接口的详细输入输出规范
- [x] 1.1 AI明确需求接口规范
  - 输入字段定义（项目角色、项目类型、上线要求、流量预期、业务场景、功能点集合、原始需求物料、交互视觉）
  - Prompt构造（System Prompt + User Prompt）
  - 输出格式：Markdown PRD文档
  - 文档章节规范（7大章节）
- [x] 1.2 AI拆分任务接口规范
  - 输入字段定义（PRD内容、项目类型、上线要求）
  - Prompt构造（任务拆分原则、任务类型定义、多端任务拆分）
  - 输出格式：Markdown任务文档
  - 文档章节规范（8大章节，考虑AI能力边界）
- [x] 1.3 AI开发接口规范
  - 输入字段定义（任务信息、技术栈、上下文代码）
  - Prompt构造
  - 输出格式：代码文件列表（Markdown）
- [x] 1.4 AI功能测试接口规范
  - 输入字段定义
  - Prompt构造
  - 输出格式：Markdown测试文档
- [x] 1.5 AI安全测试接口规范
  - 输入字段定义
  - Prompt构造
  - 输出格式：Markdown安全报告
- [x] 1.6 Feedback Shadow接口规范
  - 明确Feedback Shadow是AI接口，调用AI模型进行检测
  - Prompt构造（检测维度、决策标准、决策规则）
  - 输出格式：Markdown检测报告（ALLOW/REPAIR/REJECT）

**交付物**: `.trae/specs/feedback-shadow-system/ai-interface-specifications.md`

### 任务2: 创建功能测试标准说明书模板 ✅ 已完成
**任务描述**: 创建AI功能测试使用的标准文档模板
- [x] 2.1 创建 functional-test-standard.md 模板
  - 测试范围定义
  - 测试用例编写规范
  - 测试覆盖率要求
  - 测试通过标准
- [x] 2.2 定义测试优先级（P0/P1/P2）
- [x] 2.3 定义测试类型（单元测试/集成测试/E2E测试）

**交付物**: `.trae/specs/feedback-shadow-system/functional-test-standard.md`

### 任务3: 后端Feedback Shadow接口开发
**任务描述**: 开发feedback_shadow盯梢接口（AI接口）
- [ ] 3.1 创建 FeedbackShadowController.java
  - POST /v1/feedback-shadow/validate - 调用AI模型检测文档
  - POST /v1/feedback-shadow/repair - 调度修复
  - GET /v1/feedback-shadow/spec/{apiType} - 获取接口规范
- [ ] 3.2 创建 FeedbackShadowService.java
  - validateWithAI() - 调用AI模型进行质量检测
  - parseDecision() - 解析AI模型的决策（ALLOW/REPAIR/REJECT）
  - scheduleRepair() - 调度对应AI接口修复
  - checkQualityGate() - 质量门禁检查
- [ ] 3.3 创建 FeedbackShadowPromptBuilder.java
  - buildDetectionPrompt() - 构造检测Prompt
  - buildRepairPrompt() - 构造修复Prompt
- [ ] 3.4 创建 DocumentParser.java
  - parsePrdDocument() - 解析PRD文档
  - parseTaskDocument() - 解析任务文档
  - parseTestDocument() - 解析测试文档
  - parseSecurityReport() - 解析安全报告
  - parseFeedbackReport() - 解析检测报告
  - parseWithFallback() - 容错解析

### 任务3.5: 代码生成服务（三种类型）（新增）
**任务描述**: 实现三种类型的代码生成：脚手架初始化、任务循环开发、Feedback Shadow干预

#### 3.5.1 类型1 - 脚手架生成服务
- [ ] 创建 ScaffoldGenerator.java
  - generateScaffold() - 生成项目脚手架
  - generateSpringBootScaffold() - 生成SpringBoot脚手架
  - generateWechatMiniprogramScaffold() - 生成微信小程序脚手架
  - generatePythonScaffold() - 生成Python脚手架
  - validateScaffold() - 验证脚手架完整性

#### 3.5.2 类型2 - 任务循环开发服务
- [ ] 创建 IncrementalDevelopmentService.java
  - developProject() - 执行完整的代码生成流程
  - generateScaffoldPhase() - 阶段1：脚手架生成
  - taskLoopDevelopmentPhase() - 阶段2：任务循环开发
  - finalFeedbackShadowPhase() - 阶段3：最终检测
  - executeTask() - 执行单个任务（提取→开发→自测→标记）
  - buildContext() - 构建代码生成上下文
  - extractCodeSummary() - 提取代码摘要
  - extractCodeStyle() - 提取代码风格
- [ ] 创建 TaskScheduler.java
  - getPendingTasksOrdered() - 获取待执行任务（按依赖拓扑排序）
  - topologicalSort() - 拓扑排序算法
  - getNextExecutableTask() - 获取下一个可执行任务
  - markTaskCompleted() - 标记任务完成
  - markTaskFailed() - 标记任务失败
  - isAllTasksCompleted() - 检查是否所有任务完成

#### 3.5.3 类型3 - Feedback Shadow干预服务
- [ ] 创建 CodeQualityChecker.java
  - checkCodeStyle() - 检查代码风格一致性
  - checkInterfaceCompatibility() - 检查接口兼容性
  - checkSecurityIssues() - 检查安全问题
  - checkPerformanceIssues() - 检查性能问题
- [ ] 更新 FeedbackShadowService.java
  - validateCodeQuality() - 验证代码质量（针对代码生成）
  - generateRepairSuggestion() - 生成修复建议
  - scheduleCodeRepair() - 调度代码修复

#### 3.5.4 代码上下文管理
- [ ] 创建 CodeContextRepository.java
  - save() - 保存代码摘要
  - findByProjectId() - 查询项目所有代码摘要
  - findByProjectIdAndTaskId() - 查询指定任务代码摘要
  - saveCodeStyle() - 保存代码风格
  - findCodeStyle() - 查询代码风格
- [ ] 创建 CodeCompiler.java
  - compileScaffold() - 编译脚手架
  - compileTaskCode() - 编译任务代码
  - compileProject() - 编译整个项目
  - captureCompilationErrors() - 捕获编译错误

### 任务4: AI开发质量门禁实现
**任务描述**: 实现AI开发阶段的质量门禁机制
- [ ] 4.1 编译错误检测
  - 自动编译AI生成的代码
  - 捕获编译错误信息
  - 格式化错误报告
- [ ] 4.2 运行错误检测
  - 基础运行验证（main方法执行）
  - 单元测试执行
  - 捕获运行时异常
- [ ] 4.3 自我修复循环
  - 接收错误信息和原代码
  - 构造修复prompt
  - 调用AI开发接口重新生成
  - 再次验证直到通过或达到最大重试次数（默认3次）

### 任务5: 代码包结构强制约定实现
**任务描述**: 根据架构选型强制约定代码包结构
- [ ] 5.1 创建 ProjectStructureTemplate.java
  - SpringBoot后端结构模板
  - SpringCloud后端结构模板
  - Python后端结构模板
  - 桌面端结构模板
  - 网页前端结构模板
  - 微信小程序结构模板
  - 抖音小程序结构模板
  - Windows脚本结构模板
  - Linux脚本结构模板
- [ ] 5.2 结构验证逻辑
  - 验证必需文件存在
  - 验证目录结构正确
  - 生成结构检查报告

### 任务6: 用户反馈处理流程实现
**任务描述**: 实现用户反馈接收和处理流程
- [ ] 6.1 创建 UserFeedbackController.java
  - POST /v1/feedback - 提交反馈
  - GET /v1/feedback/{requirementId} - 查询反馈列表
  - PUT /v1/feedback/{id}/status - 更新反馈状态
- [ ] 6.2 创建 UserFeedbackService.java
  - 接收用户反馈（类型/描述/截图/日志）
  - 优先级自动分类（P0/P1/P2）
  - 调度对应AI接口修复
  - 反馈闭环管理
- [ ] 6.3 创建反馈处理工作流
  - 用户提交 → feedback_shadow接收 → 调度修复 → AI修复 → 再次验证 → 通知用户

### 任务7: 前端/小程序端对接改造
**任务描述**: 调整前端调用流程，增加feedback_shadow检测环节
- [ ] 7.1 小程序端改造
  - 修改 aiService.js 调用链
  - AI接口调用后自动提交feedback_shadow验证
  - 显示验证进度和结果
  - 支持用户查看验证报告
  - 补充需求选项页面（项目角色/项目类型/上线要求/流量预期/业务场景/功能点/交互视觉）
- [ ] 7.2 前端管理后台改造
  - 新增 feedback shadow 监控页面
  - 显示各AI接口输出质量统计
  - 显示自我修复记录
  - 用户反馈管理界面

### 任务8: 完整架构文档编写
**任务描述**: 编写开发流程全流程的完整架构文档
- [ ] 8.1 系统架构图
  - 整体架构设计
  - 数据流向图
  - 模块依赖关系
  - 增量式开发流程图
- [ ] 8.2 开发流程文档
  - 从需求到交付的完整流程
  - 各阶段输入输出
  - 质量门禁检查点
  - 增量式开发详细流程
  - 上下文传递机制
- [ ] 8.3 接口规范文档
  - 所有接口的详细规范
  - 错误码定义
  - 示例请求响应
  - 数据库存储设计
- [ ] 8.4 数据库存储设计文档
  - project_status 表设计
  - task_status 表设计
  - generated_files 表设计
  - code_context 表设计（代码摘要）
  - code_style 表设计（代码风格）

## Task Dependencies
- 任务2 依赖 任务1 ✅
- 任务3 依赖 任务1
- 任务4 依赖 任务3
- 任务5 依赖 任务1
- 任务6 依赖 任务3
- 任务7 依赖 任务3、4、5、6
- 任务8 依赖 所有前置任务
