# AI服务Controller策略模式 任务清单

## 任务依赖拓扑
```
任务1 ──► 任务2 ──► 任务3 ──► 任务4 ──► 任务5 ──┬──► 任务7
                                              │
                                              └──► 任务6
```

## 任务详情

### 任务1: 火山引擎API Demo测试
**任务描述**: 先进行小Demo测试，验证火山引擎模型API调用方式可行
- [x] 1.1 创建临时测试类 HuoshanApiDemoTest
  - 使用API Key: e492b8fd-34cf-4016-a7bb-f3bb055135bc
  - 测试Base URL: https://ark.cn-beijing.volces.com/api/coding/v3
  - 测试模型: doubao-seed-2.0-code
- [x] 1.2 测试OpenAI兼容接口调用
  - 使用Spring RestTemplate或WebClient
  - 测试/chat/completions端点
  - 验证请求/响应格式
- [x] 1.3 确认API调用方式
  - 确认认证方式（Bearer Token）
  - 确认请求体格式（OpenAI兼容格式）
  - 确认响应解析方式

### 任务2: 项目依赖和配置
**任务描述**: 添加必要的依赖和配置文件
- [x] 2.1 更新 application.yml
  - 添加 ai.huoshan 配置节点
  - 配置 api-key, base-url, default-model, timeout
- [x] 2.2 创建配置属性类
  - HuoshanAiProperties.java - 火山引擎配置属性类
- [x] 2.3 创建RestTemplate配置
  - RestTemplateConfig.java - 配置HTTP客户端

### 任务3: AI策略接口和工厂
**任务描述**: 定义策略接口和工厂类
- [x] 3.1 创建 AIProviderStrategy.java 接口
  - 方法: chatCompletion(AiChatRequest request)
  - 方法: getProviderName()
  - 方法: supports(String modelType)
- [x] 3.2 创建 AiStrategyFactory.java 工厂类
  - 管理所有策略实现
  - 方法: getStrategy(String modelType)
  - 使用Spring的List注入所有策略
- [x] 3.3 创建枚举 AiProviderType.java
  - HUOSHAN("huoshan", "火山引擎")
  - 预留: OPENAI("openai", "OpenAI")
  - 预留: WENXIN("wenxin", "文心一言")

### 任务4: 火山引擎策略实现
**任务描述**: 实现火山引擎AI策略
- [x] 4.1 创建 HuoshanAiStrategy.java
  - 实现 AIProviderStrategy 接口
  - 注入 HuoshanAiProperties
  - 使用 RestTemplate 调用API
- [x] 4.2 实现 chatCompletion 方法
  - 构建OpenAI兼容格式的请求体
  - 添加Authorization头（Bearer Token）
  - 发送POST请求到 /chat/completions
  - 解析响应并返回标准格式
- [x] 4.3 实现错误处理
  - API调用异常处理
  - 超时处理
  - 限流处理

### 任务5: DTO数据传输对象
**任务描述**: 创建AI服务相关的DTO
- [x] 5.1 创建 AiChatRequest.java
  - 字段: model（模型名称）
  - 字段: messages（消息列表）
  - 字段: temperature（可选）
  - 字段: maxTokens（可选）
  - 字段: stream（是否流式）
- [x] 5.2 创建 AiChatResponse.java
  - 字段: content（AI生成的内容）
  - 字段: usage（token使用量）
  - 字段: model（实际使用的模型）
  - 字段: finishReason（结束原因）
- [x] 5.3 创建 AiMessage.java
  - 字段: role（system/user/assistant）
  - 字段: content（消息内容）
- [x] 5.4 创建各业务场景的Request/Response DTO
  - ClarifyRequirementRequest/Response
  - SplitTasksRequest/Response
  - GenerateCodeRequest/Response
  - FunctionalTestRequest/Response
  - SecurityTestRequest/Response

### 任务6: AI服务Controller
**任务描述**: 创建统一的AI服务Controller
- [x] 6.1 创建 AiController.java
  - 注解: @RestController, @RequestMapping("/v1/ai")
  - 注入 AiStrategyFactory
- [x] 6.2 实现 AI明确需求 接口
  - POST /v1/ai/clarify-requirement
  - 接收需求描述，返回完善后的需求
- [x] 6.3 实现 AI拆分任务 接口
  - POST /v1/ai/split-tasks
  - 接收需求，返回子任务列表
- [x] 6.4 实现 AI开发 接口
  - POST /v1/ai/generate-code
  - 接收任务描述，返回生成的代码
- [x] 6.5 实现 AI功能测试 接口
  - POST /v1/ai/functional-test
  - 接收代码，返回测试用例和结果
- [x] 6.6 实现 AI安全测试 接口
  - POST /v1/ai/security-test
  - 接收代码，返回安全检测报告

### 任务7: 接口测试
**任务描述**: 构造测试样例，确保接口功能正常
- [x] 7.1 创建 AiServiceManualTest.java 测试类
  - 手动创建依赖，不使用SpringBootTest
  - 使用真实API调用进行测试
- [x] 7.2 测试 AI明确需求 接口
  - 构造测试样例：酒店管理系统需求
  - 验证响应格式正确
  - 验证返回内容不为空
- [x] 7.3 测试 AI拆分任务 接口
  - 构造测试样例：酒店管理系统任务拆分
  - 验证返回任务列表
- [x] 7.4 测试 AI开发 接口
  - 构造测试样例：生成HotelRoom Java类
  - 验证返回代码格式正确
- [x] 7.5 测试 AI功能测试 接口
  - 构造测试样例：Calculator类测试
  - 验证返回测试用例
- [x] 7.6 测试 AI安全测试 接口
  - 构造测试样例：包含SQL注入漏洞的代码
  - 验证返回安全检测结果
- [x] 7.7 测试策略切换
  - 验证通过model参数可以切换策略（doubao-seed-2.0-code、kimi-k2.5）

## Task Dependencies
- 任务2 依赖 任务1
- 任务3 依赖 任务2
- 任务4 依赖 任务3
- 任务5 依赖 任务3
- 任务6 依赖 任务4 和 任务5
- 任务7 依赖 任务6
