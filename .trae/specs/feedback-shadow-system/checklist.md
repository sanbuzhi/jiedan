# Feedback Shadow 盯梢系统 验收清单

## 文档规范检查

- [x] AI接口输入输出详细规范文档已创建
  - [x] AI明确需求接口规范
    - [x] 输入字段定义（项目角色、项目类型、上线要求、流量预期、业务场景、功能点集合、原始需求物料、交互视觉）
    - [x] Prompt构造（System Prompt + User Prompt）
    - [x] 输出格式：Markdown PRD文档
    - [x] 文档章节规范（7大章节）
  - [x] AI拆分任务接口规范
    - [x] 输入字段定义（PRD内容、项目类型、上线要求）
    - [x] Prompt构造（任务拆分原则、任务类型定义、多端任务拆分）
    - [x] 输出格式：Markdown任务文档
    - [x] 文档章节规范（8大章节，考虑AI能力边界）
  - [x] AI开发接口规范
    - [x] 输入字段定义（任务信息、技术栈、上下文代码）
    - [x] Prompt构造
    - [x] 输出格式：代码文件列表（Markdown）
  - [x] AI功能测试接口规范
    - [x] 输入字段定义
    - [x] Prompt构造
    - [x] 输出格式：Markdown测试文档
  - [x] AI安全测试接口规范
    - [x] 输入字段定义
    - [x] Prompt构造
    - [x] 输出格式：Markdown安全报告
  - [x] Feedback Shadow接口规范
    - [x] 明确Feedback Shadow是AI接口，调用AI模型进行检测
    - [x] Prompt构造（检测维度、决策标准、决策规则）
    - [x] 输出格式：Markdown检测报告（ALLOW/REPAIR/REJECT）

- [x] 功能测试标准说明书模板已创建
  - [x] 测试范围定义
  - [x] 测试用例编写规范
  - [x] 覆盖率要求定义
  - [x] 测试执行标准
  - [x] 各项目类型测试标准
  - [x] 测试通过标准

- [x] Spec.md已同步更新
  - [x] Feedback Shadow明确为AI接口
  - [x] AI明确需求接口输入输出规范
  - [x] AI拆分任务接口输入输出规范
  - [x] AI开发接口输入输出规范（三种类型代码生成）
  - [x] AI功能测试接口输入输出规范
  - [x] AI安全测试接口输入输出规范
  - [x] 增量式开发设计
  - [x] 上下文管理机制
  - [x] 代码风格一致性
  - [x] 脚手架初始化生成
  - [x] 任务循环开发（提取→开发→自测→标记）
  - [x] Feedback Shadow干预

- [x] Tasks.md已同步更新
  - [x] 任务1标记为已完成
  - [x] 任务3明确Feedback Shadow是AI接口
  - [x] 任务3.5代码生成服务（三种类型）（新增）
  - [x] 任务7补充小程序端需求选项页面
  - [x] 任务8补充数据库存储设计

## 后端实现检查

- [ ] FeedbackShadowController.java
  - [ ] POST /v1/feedback-shadow/validate - 调用AI模型检测文档
  - [ ] POST /v1/feedback-shadow/repair - 调度修复
  - [ ] GET /v1/feedback-shadow/spec/{apiType} - 获取接口规范

- [ ] FeedbackShadowService.java
  - [ ] validateWithAI() - 调用AI模型进行质量检测
  - [ ] parseDecision() - 解析AI模型的决策（ALLOW/REPAIR/REJECT）
  - [ ] scheduleRepair() - 调度对应AI接口修复
  - [ ] checkQualityGate() - 质量门禁检查

- [ ] FeedbackShadowPromptBuilder.java
  - [ ] buildDetectionPrompt() - 构造检测Prompt
  - [ ] buildRepairPrompt() - 构造修复Prompt

- [ ] DocumentParser.java
  - [ ] parsePrdDocument() - 解析PRD文档
  - [ ] parseTaskDocument() - 解析任务文档
  - [ ] parseTestDocument() - 解析测试文档
  - [ ] parseSecurityReport() - 解析安全报告
  - [ ] parseFeedbackReport() - 解析检测报告
  - [ ] parseWithFallback() - 容错解析

### 代码生成服务（三种类型）（新增）

#### 类型1 - 脚手架生成服务

- [ ] ScaffoldGenerator.java
  - [ ] generateScaffold() - 生成项目脚手架
  - [ ] generateSpringBootScaffold() - 生成SpringBoot脚手架
  - [ ] generateWechatMiniprogramScaffold() - 生成微信小程序脚手架
  - [ ] generatePythonScaffold() - 生成Python脚手架
  - [ ] validateScaffold() - 验证脚手架完整性

#### 类型2 - 任务循环开发服务

- [ ] IncrementalDevelopmentService.java
  - [ ] developProject() - 执行完整的代码生成流程
  - [ ] generateScaffoldPhase() - 阶段1：脚手架生成
  - [ ] taskLoopDevelopmentPhase() - 阶段2：任务循环开发
  - [ ] finalFeedbackShadowPhase() - 阶段3：最终检测
  - [ ] executeTask() - 执行单个任务（提取→开发→自测→标记）
  - [ ] buildContext() - 构建代码生成上下文
  - [ ] extractCodeSummary() - 提取代码摘要
  - [ ] extractCodeStyle() - 提取代码风格
  - [ ] handleCompilationError() - 处理编译错误修复循环

- [ ] TaskScheduler.java
  - [ ] getPendingTasksOrdered() - 获取待执行任务（按依赖拓扑排序）
  - [ ] topologicalSort() - 拓扑排序算法
  - [ ] getNextExecutableTask() - 获取下一个可执行任务
  - [ ] markTaskCompleted() - 标记任务完成
  - [ ] markTaskFailed() - 标记任务失败
  - [ ] isAllTasksCompleted() - 检查是否所有任务完成

#### 类型3 - Feedback Shadow干预服务

- [ ] CodeQualityChecker.java
  - [ ] checkCodeStyle() - 检查代码风格一致性
  - [ ] checkInterfaceCompatibility() - 检查接口兼容性
  - [ ] checkSecurityIssues() - 检查安全问题
  - [ ] checkPerformanceIssues() - 检查性能问题

- [ ] FeedbackShadowService.java（更新）
  - [ ] validateCodeQuality() - 验证代码质量（针对代码生成）
  - [ ] generateRepairSuggestion() - 生成修复建议
  - [ ] scheduleCodeRepair() - 调度代码修复

#### 代码上下文管理

- [ ] CodeContextRepository.java
  - [ ] save() - 保存代码摘要
  - [ ] findByProjectId() - 查询项目所有代码摘要
  - [ ] findByProjectIdAndTaskId() - 查询指定任务代码摘要
  - [ ] saveCodeStyle() - 保存代码风格
  - [ ] findCodeStyle() - 查询代码风格

- [ ] CodeCompiler.java
  - [ ] compileScaffold() - 编译脚手架
  - [ ] compileTaskCode() - 编译任务代码
  - [ ] compileProject() - 编译整个项目
  - [ ] captureCompilationErrors() - 捕获编译错误

## 质量门禁实现检查

- [ ] 编译错误检测
  - [ ] 自动编译AI生成的代码
  - [ ] 捕获编译错误信息
  - [ ] 格式化错误报告

- [ ] 运行错误检测
  - [ ] 基础运行验证
  - [ ] 单元测试执行
  - [ ] 运行时异常捕获

- [ ] 自我修复循环
  - [ ] 接收错误信息和原代码
  - [ ] 构造修复prompt
  - [ ] 调用AI开发接口重新生成
  - [ ] 重试机制（最多3次）

## 代码结构约定检查

- [ ] ProjectStructureTemplate.java
  - [ ] SpringBoot后端结构模板
  - [ ] SpringCloud后端结构模板
  - [ ] Python后端结构模板
  - [ ] 桌面端结构模板
  - [ ] 网页前端结构模板
  - [ ] 微信小程序结构模板
  - [ ] 抖音小程序结构模板
  - [ ] Windows脚本结构模板
  - [ ] Linux脚本结构模板

- [ ] 结构验证逻辑
  - [ ] 验证必需文件存在
  - [ ] 验证目录结构正确
  - [ ] 生成结构检查报告

## 用户反馈处理检查

- [ ] UserFeedbackController.java
  - [ ] POST /v1/feedback - 提交反馈
  - [ ] GET /v1/feedback/{requirementId} - 查询反馈列表
  - [ ] PUT /v1/feedback/{id}/status - 更新反馈状态

- [ ] UserFeedbackService.java
  - [ ] 接收用户反馈（类型/描述/截图/日志）
  - [ ] 优先级自动分类（P0/P1/P2）
  - [ ] 调度对应AI接口修复
  - [ ] 反馈闭环管理

- [ ] 反馈处理工作流
  - [ ] 用户提交 → feedback_shadow接收
  - [ ] 调度修复 → AI修复
  - [ ] 再次验证 → 通知用户

## 前端/小程序端改造检查

- [ ] 小程序端改造
  - [ ] 修改 aiService.js 调用链
  - [ ] AI接口调用后自动提交feedback_shadow验证
  - [ ] 显示验证进度和结果
  - [ ] 支持用户查看验证报告
  - [ ] 补充需求选项页面（项目角色/项目类型/上线要求/流量预期/业务场景/功能点/交互视觉）

- [ ] 前端管理后台改造
  - [ ] 新增 feedback shadow 监控页面
  - [ ] 显示各AI接口输出质量统计
  - [ ] 显示自我修复记录
  - [ ] 用户反馈管理界面

## 架构文档检查

- [ ] 系统架构图
  - [ ] 整体架构设计
  - [ ] 数据流向图
  - [ ] 模块依赖关系

- [ ] 开发流程文档
  - [ ] 从需求到交付的完整流程
  - [ ] 各阶段输入输出
  - [ ] 质量门禁检查点

- [ ] 接口规范文档
  - [ ] 所有接口的详细规范
  - [ ] 错误码定义
  - [ ] 示例请求响应

## 集成测试检查

- [ ] AI明确需求接口 → Feedback Shadow 验证流程
- [ ] AI拆分任务接口 → Feedback Shadow 验证流程
- [ ] AI开发接口 → Feedback Shadow 验证流程（含编译检查）
- [ ] AI功能测试接口 → Feedback Shadow 验证流程
- [ ] AI安全测试接口 → Feedback Shadow 验证流程
- [ ] 自我修复循环测试
- [ ] 用户反馈处理流程测试

## 代码生成测试检查（三种类型）

### 类型1 - 脚手架生成测试

- [ ] 脚手架生成测试
  - [ ] SpringBoot脚手架生成
  - [ ] 微信小程序脚手架生成
  - [ ] Python脚手架生成
  - [ ] 脚手架编译验证
  - [ ] 脚手架完整性验证

### 类型2 - 任务循环开发测试

- [ ] 任务状态管理测试
  - [ ] 任务初始化（全部PENDING）
  - [ ] 任务依赖拓扑排序
  - [ ] 任务状态流转（PENDING→IN_PROGRESS→COMPLETED）
  - [ ] 获取下一个可执行任务

- [ ] 上下文传递测试
  - [ ] PRD摘要传递
  - [ ] 已生成代码摘要传递
  - [ ] 依赖任务详情传递
  - [ ] 代码风格传递

- [ ] 代码摘要提取测试
  - [ ] 类名提取
  - [ ] public方法签名提取
  - [ ] 依赖类提取
  - [ ] 代码风格提取

- [ ] 任务循环开发完整流程测试
  - [ ] 多任务顺序执行
  - [ ] 上下文累积效果
  - [ ] 代码风格一致性验证
  - [ ] 编译错误自动修复

### 类型3 - Feedback Shadow干预测试

- [ ] 代码质量检测测试
  - [ ] 代码风格一致性检测
  - [ ] 接口兼容性检测
  - [ ] 安全问题检测
  - [ ] 性能问题检测

- [ ] 干预流程测试
  - [ ] ALLOW决策流程
  - [ ] REPAIR决策流程
  - [ ] REJECT决策流程
  - [ ] 修复建议生成
  - [ ] 代码修复调度

### 完整代码生成流程测试

- [ ] 三阶段流程测试
  - [ ] 阶段1：脚手架生成
  - [ ] 阶段2：任务循环开发
  - [ ] 阶段3：最终Feedback Shadow检测

- [ ] 数据库存储测试
  - [ ] project_status 表读写
  - [ ] task_status 表状态流转
  - [ ] code_context 表摘要存储
  - [ ] code_style 表风格存储
