# AI异步阶段式代码生成系统 - 验收检查清单

## 数据库层

- [x] ai_development_project 表创建成功，包含所有必需字段
- [x] ai_development_phase 表创建成功，包含所有必需字段
- [x] ai_development_round 表创建成功，包含所有必需字段
- [x] ai_development_file 表创建成功，包含所有必需字段
- [x] 表之间的外键关系正确

## 实体类

- [x] AiDevelopmentProject 实体类字段完整
- [x] AiDevelopmentPhase 实体类字段完整
- [x] AiDevelopmentRound 实体类字段完整
- [x] AiDevelopmentFile 实体类字段完整
- [x] 实体类使用正确的JPA注解

## Repository层

- [x] AiDevelopmentProjectRepository 基本CRUD方法可用
- [x] AiDevelopmentPhaseRepository 基本CRUD方法可用
- [x] AiDevelopmentRoundRepository 基本CRUD方法可用
- [x] AiDevelopmentFileRepository 基本CRUD方法可用

## 阶段配置

- [x] PhaseConfig 包含7个阶段的配置
- [x] 每个阶段配置包含：名称、关键词、预估文件数
- [x] 配置可通过配置文件注入

## 核心业务逻辑

- [x] ProjectDevelopmentManager.startProject() 正确实现参数校验
- [x] 目录创建方法正确创建项目目录结构
- [x] 数据库初始化正确插入项目记录
- [x] executePhase() 方法正确实现阶段执行逻辑
- [x] executeRound() 方法正确实现单轮执行
- [x] Prompt构建正确区分第一轮和后续轮次
- [x] AI调用带30次重试 + 指数退避
- [x] 截断检测与续传正确实现（最多5次）
- [x] parseAndSaveFiles() 正确解析和保存文件
- [x] 安全检查防止目录遍历攻击
- [x] 进度文档正确更新
- [x] 会话记录正确保存
- [x] checkShouldContinue() 正确判断5种结束条件
- [x] 阶段摘要正确生成

## Controller层 - 启动接口

- [x] POST /api/ai/code/project/start 接口正确实现
- [x] 请求参数校验通过
- [x] 异步执行正确触发
- [x] 正确返回 projectId 和 status

## Controller层 - 查询接口

- [x] GET /api/ai/code/project/{projectId}/status 正确实现
- [x] 返回完整的项目状态信息
- [x] GET /api/ai/code/project/{projectId}/phase/{phase}/status 正确实现
- [x] 返回完整的阶段状态信息
- [x] GET /api/ai/code/project/{projectId}/phase/{phase}/progress 正确实现
- [x] 正确返回进度文档内容
- [x] GET /api/ai/code/project/{projectId}/phase/{phase}/round/{round} 正确实现
- [x] 正确返回轮次详情

## DTO

- [x] StartProjectRequest 字段完整
- [x] StartProjectResponse 字段完整
- [x] ProjectStatusResponse 字段完整
- [x] PhaseStatusResponse 字段完整
- [x] PhaseProgressResponse 字段完整
- [x] RoundDetailResponse 字段完整

## 配置

- [x] AiDevelopmentConfig 配置正确
- [x] maxRetries = 30
- [x] maxTokens = 32000
- [x] maxContinuations = 5
- [x] maxRoundsPerPhase = 20

## 文件存储

- [x] 项目目录正确创建
- [x] phases/ 目录结构正确
- [x] sessions/ 目录正确
- [x] generated/ 目录正确
- [x] progress.md 正确生成
- [x] summary.md 正确生成

## 错误处理

- [x] 网络超时正确重试
- [x] 指数退避正确计算
- [x] 不可重试错误正确抛出
- [x] 错误信息正确记录到数据库
