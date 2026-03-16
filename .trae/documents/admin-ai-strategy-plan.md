# 后台管理AI策略配置页面开发计划

## 需求概述
在智能获客系统-后台管理页面，将第二个"订单管理"菜单项改为"AI策略"，并为每个AI接口配置不同的AI策略。

## 当前状态
- 后台管理页面有重复的"订单管理"菜单项（第31-34行）
- AI接口已实现5个：clarify-requirement、split-tasks、generate-code、functional-test、security-test
- 支持的AI模型：doubao-seed-2.0-code、kimi-k2.5 等火山引擎模型

## 实施步骤

### 步骤1: 修改AdminLayout.vue菜单
**文件**: frontend/src/views/admin/AdminLayout.vue

修改内容：
1. 将第31-34行的第二个"订单管理"改为"AI策略"
2. 修改index为 `/admin/ai-strategy`
3. 修改图标为合适的AI相关图标（如：Cpu 或 MagicStick）

### 步骤2: 添加路由配置
**文件**: frontend/src/router/index.js

添加内容：
1. 在admin子路由中添加AI策略路由
2. path: 'ai-strategy'
3. name: 'AdminAiStrategy'
4. component: 指向新建的AiStrategy.vue

### 步骤3: 创建AI策略配置页面
**文件**: frontend/src/views/admin/AiStrategy.vue

页面功能：
1. **页面标题**: AI策略配置
2. **配置列表**: 展示5个AI接口的配置项
   - AI明确需求 (clarify-requirement)
   - AI拆分任务 (split-tasks)
   - AI开发 (generate-code)
   - AI功能测试 (functional-test)
   - AI安全测试 (security-test)
3. **每个配置项包含**:
   - 接口名称
   - 接口描述
   - AI模型选择下拉框（doubao-seed-2.0-code、kimi-k2.5）
   - 温度参数滑块（0-2）
   - 最大token数输入
4. **操作按钮**:
   - 保存配置
   - 重置默认
   - 测试接口（可选）

### 步骤4: 创建API服务
**文件**: frontend/src/api/aiStrategy.js

API函数：
1. `getAiStrategyConfig()` - 获取AI策略配置
2. `saveAiStrategyConfig(config)` - 保存AI策略配置
3. `getAvailableModels()` - 获取可用模型列表

### 步骤5: 后端接口（如需持久化）
**可选**: 如果需要持久化配置，需要添加后端接口

建议接口：
1. `GET /v1/ai/strategy/config` - 获取配置
2. `POST /v1/ai/strategy/config` - 保存配置
3. `GET /v1/ai/strategy/models` - 获取可用模型列表

## 页面设计

### AI策略配置页面布局
```
+------------------------------------------+
|  AI策略配置                    [保存] [重置] |
+------------------------------------------+
|                                          |
|  AI明确需求                               |
|  +------------------------------------+  |
|  | 模型: [doubao-seed-2.0-code ▼]     |  |
|  | 温度: [====●====] 0.7              |  |
|  | 最大Token: [2000]                  |  |
|  +------------------------------------+  |
|                                          |
|  AI拆分任务                               |
|  +------------------------------------+  |
|  | 模型: [doubao-seed-2.0-code ▼]     |  |
|  | 温度: [====●====] 0.7              |  |
|  | 最大Token: [2000]                  |  |
|  +------------------------------------+  |
|                                          |
|  ... 其他接口配置 ...                      |
|                                          |
+------------------------------------------+
```

## 数据结构

### 前端配置对象
```javascript
{
  clarifyRequirement: {
    model: 'doubao-seed-2.0-code',
    temperature: 0.7,
    maxTokens: 2000
  },
  splitTasks: {
    model: 'doubao-seed-2.0-code',
    temperature: 0.7,
    maxTokens: 2000
  },
  generateCode: {
    model: 'doubao-seed-2.0-code',
    temperature: 0.3,
    maxTokens: 3000
  },
  functionalTest: {
    model: 'doubao-seed-2.0-code',
    temperature: 0.5,
    maxTokens: 2500
  },
  securityTest: {
    model: 'doubao-seed-2.0-code',
    temperature: 0.5,
    maxTokens: 2500
  }
}
```

## 依赖检查
- Element Plus UI组件库（已存在）
- Vue Router（已存在）
- Pinia/Vuex（如需要状态管理）

## 文件清单
1. frontend/src/views/admin/AdminLayout.vue - 修改菜单
2. frontend/src/router/index.js - 添加路由
3. frontend/src/views/admin/AiStrategy.vue - 新建页面
4. frontend/src/api/aiStrategy.js - 新建API服务
