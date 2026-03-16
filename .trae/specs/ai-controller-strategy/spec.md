# AI服务Controller策略模式 Spec

## Why
小程序端的流程节点中，"AI明确需求"/"AI拆分任务"/"AI开发"/"AI功能测试"/"AI安全测试"等AI操作步骤都需要后端接口支持。为了支持灵活切换不同的AI模型提供商（火山引擎、OpenAI、文心一言等），需要将AI接口部分做成一个统一的Controller，并使用策略模式实现不同AI模型的切换。目前先实现火山引擎API策略。

## What Changes
- 新增 **AI服务Controller**：统一处理所有AI相关请求
- 新增 **AI策略接口**：定义AI服务标准契约
- 新增 **火山引擎策略实现**：基于火山引擎Coding Plan API的实现
- 新增 **策略工厂**：根据模型类型动态选择策略实现
- 新增 **AI服务DTO**：请求/响应数据传输对象
- **支持的操作类型**：
  - AI明确需求：分析并完善用户需求描述
  - AI拆分任务：将需求拆分为可执行的子任务
  - AI开发：基于需求生成代码
  - AI功能测试：生成功能测试用例并执行
  - AI安全测试：执行安全漏洞扫描和检测

## Impact
- 新增代码：controller/AiController.java, service/ai/*, dto/ai/*
- 配置文件：application.yml 新增火山引擎API配置
- 依赖变更：pom.xml 可能需要添加HTTP客户端依赖

## ADDED Requirements

### Requirement: AI服务统一入口
The system SHALL provide a unified AI service controller to handle all AI-related operations.

#### Scenario: AI明确需求
- **WHEN** 用户提交需求描述
- **THEN** 调用AI服务分析并返回完善后的需求文档

#### Scenario: AI拆分任务
- **WHEN** 用户提交已确认的需求
- **THEN** 调用AI服务将需求拆分为可执行的子任务列表

#### Scenario: AI开发
- **WHEN** 用户提交开发任务
- **THEN** 调用AI服务生成对应的代码实现

#### Scenario: AI功能测试
- **WHEN** 用户提交功能代码
- **THEN** 调用AI服务生成功能测试用例并返回测试结果

#### Scenario: AI安全测试
- **WHEN** 用户提交代码进行安全检测
- **THEN** 调用AI服务扫描安全漏洞并返回检测报告

### Requirement: AI策略模式
The system SHALL use Strategy Pattern to support switching between different AI model providers.

#### Scenario: 策略选择
- **GIVEN** 请求中包含模型类型参数
- **WHEN** 调用AI服务
- **THEN** 策略工厂根据模型类型选择对应的策略实现

#### Scenario: 火山引擎策略
- **GIVEN** 模型类型为 "huoshan"
- **WHEN** 调用AI服务
- **THEN** 使用火山引擎Coding Plan API进行调用
- **AND** 支持模型：doubao-seed-2.0-code, doubao-seed-2.0-pro, kimi-k2.5 等

### Requirement: 火山引擎API集成
The system SHALL integrate with Volcano Engine Coding Plan API.

#### Scenario: API调用
- **GIVEN** 有效的API Key: e492b8fd-34cf-4016-a7bb-f3bb055135bc
- **AND** Base URL: https://ark.cn-beijing.volces.com/api/coding/v3
- **WHEN** 发送聊天补全请求
- **THEN** 返回AI生成的内容

#### Scenario: 流式响应支持
- **WHEN** 请求包含 stream=true 参数
- **THEN** 返回SSE流式响应

## Configuration

### 火山引擎配置
```yaml
ai:
  huoshan:
    api-key: e492b8fd-34cf-4016-a7bb-f3bb055135bc
    base-url: https://ark.cn-beijing.volces.com/api/coding/v3
    default-model: doubao-seed-2.0-code
    timeout: 30000
```

## API Endpoints

### POST /v1/ai/clarify-requirement
AI明确需求 - 分析并完善用户需求

### POST /v1/ai/split-tasks
AI拆分任务 - 将需求拆分为子任务

### POST /v1/ai/generate-code
AI开发 - 基于需求生成代码

### POST /v1/ai/functional-test
AI功能测试 - 生成功能测试用例

### POST /v1/ai/security-test
AI安全测试 - 执行安全漏洞扫描

## MODIFIED Requirements
None

## REMOVED Requirements
None
