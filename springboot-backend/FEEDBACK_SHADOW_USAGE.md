# Feedback Shadow 监控系统 - 使用指南

## 快速开始

### 1. 系统启动

```bash
# 1. 确保数据库已初始化
mysql -u root -p < src/main/resources/db/migration/V4__Add_feedback_shadow_tables.sql

# 2. 配置AI提供商密钥
# 编辑 application.yml，设置 Volcano Engine API Key

# 3. 启动应用
mvn spring-boot:run
```

### 2. 完整开发流程示例

#### 步骤1: 初始化脚手架
```bash
curl -X POST http://localhost:8080/api/ai/code/scaffold/PROJ001 \
  -H "Content-Type: application/json" \
  -d '{
    "projectType": "springboot",
    "config": {
      "springBootVersion": "3.2.0",
      "javaVersion": "17",
      "dependencies": ["web", "data-jpa", "mysql", "lombok"]
    }
  }'
```

#### 步骤2: 执行完整开发流程
```bash
curl -X POST http://localhost:8080/api/ai/code/develop-full/PROJ001 \
  -H "Content-Type: application/json" \
  -d '{
    "projectType": "springboot",
    "scaffoldConfig": {
      "springBootVersion": "3.2.0",
      "javaVersion": "17",
      "dependencies": ["web", "data-jpa", "mysql"]
    },
    "prdSummary": "智能获客系统，包含用户管理、订单管理、客户跟进等模块...",
    "taskList": [
      {
        "id": "T1",
        "name": "用户模块-实体类",
        "type": "backend",
        "priority": "P0",
        "dependencies": []
      },
      {
        "id": "T2",
        "name": "用户模块-数据访问层",
        "type": "backend",
        "priority": "P0",
        "dependencies": ["T1"]
      },
      {
        "id": "T3",
        "name": "用户模块-服务层",
        "type": "backend",
        "priority": "P0",
        "dependencies": ["T2"]
      },
      {
        "id": "T4",
        "name": "用户模块-控制器",
        "type": "backend",
        "priority": "P0",
        "dependencies": ["T3"]
      }
    ]
  }'
```

#### 步骤3: 监控任务状态
```bash
# 获取任务列表
curl http://localhost:8080/api/ai/code/tasks/PROJ001

# 响应示例
[
  {
    "taskId": "T1",
    "taskName": "用户模块-实体类",
    "taskType": "backend",
    "state": "COMPLETED",
    "priority": "P0"
  },
  {
    "taskId": "T2",
    "taskName": "用户模块-数据访问层",
    "taskType": "backend",
    "state": "IN_PROGRESS",
    "priority": "P0"
  }
]
```

#### 步骤4: 质量门禁检查
```bash
curl -X POST http://localhost:8080/api/ai/code/quality-check/PROJ001 \
  -H "Content-Type: application/json" \
  -d '{
    "projectType": "springboot",
    "taskId": "T1"
  }'

# 响应示例
{
  "passed": true,
  "overallMessage": "所有质量检查通过",
  "compilationCheck": {
    "passed": true,
    "errors": []
  },
  "styleChecks": [],
  "securityChecks": []
}
```

### 3. 用户反馈流程

#### 提交反馈
```bash
curl -X POST http://localhost:8080/api/ai/feedback/submit \
  -H "Content-Type: application/json" \
  -d '{
    "projectId": "PROJ001",
    "taskId": "T1",
    "feedbackType": "CODE_ISSUE",
    "description": "User实体类缺少email字段的校验注解",
    "affectedFiles": ["src/main/java/com/example/entity/User.java"],
    "severity": "MEDIUM",
    "source": "USER"
  }'

# 响应示例
{
  "success": true,
  "feedbackId": "FB2024031412304512345678",
  "status": "PENDING",
  "estimatedRepairTime": 5
}
```

#### 查询反馈状态
```bash
# 获取项目所有反馈
curl http://localhost:8080/api/ai/feedback/project/PROJ001

# 获取单个反馈详情
curl http://localhost:8080/api/ai/feedback/FB2024031412304512345678
```

#### 重新处理反馈
```bash
curl -X POST http://localhost:8080/api/ai/feedback/FB2024031412304512345678/reprocess
```

## API 接口速查表

### 代码生成接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/code/scaffold/{projectId}` | POST | 初始化脚手架 |
| `/api/ai/code/develop/{projectId}` | POST | 执行增量开发 |
| `/api/ai/code/develop-full/{projectId}` | POST | 执行完整开发流程 |
| `/api/ai/code/tasks/{projectId}` | GET | 获取任务列表 |
| `/api/ai/code/quality-check/{projectId}` | POST | 质量门禁检查 |
| `/api/ai/code/repair/{projectId}` | POST | 修复任务代码 |

### 用户反馈接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/feedback/submit` | POST | 提交反馈 |
| `/api/ai/feedback/project/{projectId}` | GET | 获取项目反馈列表 |
| `/api/ai/feedback/{feedbackId}` | GET | 获取反馈详情 |
| `/api/ai/feedback/{feedbackId}/reprocess` | POST | 重新处理反馈 |

### Feedback Shadow 接口

| 接口 | 方法 | 说明 |
|------|------|------|
| `/api/ai/shadow/validate` | POST | 验证文档质量 |
| `/api/ai/shadow/repair-schedule` | POST | 调度修复任务 |

## 配置说明

### application.yml 配置

```yaml
# AI提供商配置
ai:
  provider:
    volcano:
      api-key: ${VOLCANO_API_KEY}
      endpoint: https://ark.cn-beijing.volces.com/api/v3
      model: doubao-pro-32k

# 代码生成配置
code-generation:
  max-retry: 3                    # 最大重试次数
  max-input-tokens: 8000          # 最大输入Token
  max-output-tokens: 4000         # 最大输出Token
  project-base-path: ./projects   # 项目基础路径
  temperature: 0.2                # AI温度参数

# 质量门禁配置
quality-gate:
  compilation:
    enabled: true                 # 启用编译检查
    timeout-seconds: 120          # 编译超时时间
  style-check:
    enabled: true                 # 启用代码规范检查
    max-line-length: 120          # 最大行长度
    min-comment-ratio: 0.1        # 最小注释比例
  security-check:
    enabled: true                 # 启用安全检查
    block-on-critical: true       # 严重安全问题阻断流程
```

## 常见问题

### Q1: 如何处理编译失败？
系统会自动重试最多3次。如果仍然失败：
1. 查看错误日志
2. 手动修复代码
3. 或通过反馈接口提交修复请求

### Q2: Token 超限怎么办？
系统会自动截断：
- PRD 摘要限制 2000 字符
- 编译错误最多保留 10 条
- 代码摘要只保留类名和方法签名

### Q3: 如何添加新的项目类型？
1. 在 `ScaffoldGenerator` 中添加脚手架生成逻辑
2. 在 `CodeCompiler` 中添加编译逻辑
3. 在 `CodeQualityChecker` 中添加质量检查规则

### Q4: 如何集成其他AI模型？
1. 实现 `AIProviderStrategy` 接口
2. 在 `AiStrategyFactory` 中注册新策略
3. 配置中指定新的 provider

## 监控与日志

### 日志位置
```
logs/
├── application.log          # 应用日志
├── ai-generation.log        # AI生成日志
├── feedback-shadow.log      # 质量监控日志
└── user-feedback.log        # 用户反馈日志
```

### 关键指标
- 代码生成成功率
- 平均生成时间
- 编译通过率
- 用户反馈处理时间

## 最佳实践

1. **任务拆分**：每个任务应该独立可测试
2. **依赖管理**：合理设置任务依赖关系
3. **PRD 质量**：提供清晰的需求描述
4. **及时反馈**：发现问题及时提交反馈
5. **版本控制**：定期备份生成的代码

## 技术支持

如有问题，请联系：
- 技术支持邮箱: support@jiedan.com
- 技术文档: https://docs.jiedan.com
- 问题反馈: https://github.com/jiedan/feedback-shadow/issues
