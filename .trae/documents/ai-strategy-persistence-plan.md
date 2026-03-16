# AI策略持久化到数据库 - 前后端改造计划

## 需求概述
将AI策略配置从localStorage迁移到数据库，实现真正的增删改查功能，支持前后端数据同步。

## 数据库设计

### 表名: ai_strategy_config

| 字段名 | 类型 | 说明 |
|--------|------|------|
| id | BIGINT | 主键，自增 |
| api_code | VARCHAR(50) | 接口代码，唯一标识 |
| api_name | VARCHAR(100) | 接口名称 |
| provider | VARCHAR(50) | AI提供商 |
| model | VARCHAR(100) | AI模型 |
| temperature | DECIMAL(3,2) | 温度参数 |
| max_tokens | INT | 最大token数 |
| enabled | TINYINT | 是否启用 |
| description | TEXT | 接口描述 |
| icon | VARCHAR(50) | 图标名称 |
| sort_order | INT | 排序顺序 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

## 实施步骤

### 步骤1: 后端数据库实体创建
**文件**: entity/AiStrategyConfig.java
- 创建JPA实体类
- 映射到ai_strategy_config表
- 包含所有配置字段

### 步骤2: 后端Repository创建
**文件**: repository/AiStrategyConfigRepository.java
- 继承JpaRepository
- 添加根据apiCode查询的方法

### 步骤3: 后端Service创建
**文件**: service/AiStrategyConfigService.java
- 增删改查业务逻辑
- 初始化默认配置
- 根据apiCode获取配置

### 步骤4: 后端Controller创建
**文件**: controller/AiStrategyConfigController.java
- GET /v1/ai/strategy/config - 获取所有配置
- GET /v1/ai/strategy/config/{apiCode} - 获取单个配置
- POST /v1/ai/strategy/config - 创建配置
- PUT /v1/ai/strategy/config/{id} - 更新配置
- DELETE /v1/ai/strategy/config/{id} - 删除配置
- POST /v1/ai/strategy/config/batch - 批量更新
- GET /v1/ai/strategy/models - 获取可用模型列表

### 步骤5: 数据库迁移脚本
**文件**: db/migration/V1__create_ai_strategy_config_table.sql
- 创建ai_strategy_config表
- 插入默认配置数据

### 步骤6: 前端API服务改造
**文件**: api/aiStrategy.js
- 更新API调用，对接后端接口
- 添加增删改查方法

### 步骤7: 前端页面改造
**文件**: views/admin/AiStrategy.vue
- 添加加载状态
- 添加增删改查功能
- 添加表格展示模式
- 支持单个/批量编辑
- 添加删除确认

### 步骤8: 前端添加新功能
- 添加新增策略按钮
- 添加编辑对话框
- 添加删除功能
- 添加表格/卡片视图切换

## 页面设计

### 列表视图
```
+----------------------------------------------------------+
|  AI策略配置                          [新增] [刷新]         |
+----------------------------------------------------------+
|  搜索: [____________]  状态: [全部▼]  提供商: [全部▼]       |
+----------------------------------------------------------+
|  | 接口名称 | 提供商 | 模型 | 温度 | Token | 状态 | 操作 | |
|  |----------|--------|------|------|-------|------|-----| |
|  | AI明确需求 | 火山引擎 | ... | 0.7 | 2000 | 启用 | 编辑删除 |
|  | AI拆分任务 | 火山引擎 | ... | 0.7 | 2000 | 启用 | 编辑删除 |
|  | ...      | ...    | ...  | ...  | ...   | ...  | ... |
+----------------------------------------------------------+
```

### 编辑对话框
```
+----------------------------------+
|  编辑AI策略配置              [X]  |
+----------------------------------+
|  接口名称: [____________]        |
|  接口代码: [____________]        |
|  AI提供商: [火山引擎▼]           |
|  AI模型:   [doubao...▼]          |
|  温度参数: [====●====] 0.7       |
|  最大Token: [2000]               |
|  启用状态: [●]                   |
|  描述:     [____________]        |
+----------------------------------+
|          [取消]  [保存]          |
+----------------------------------+
```

## 数据结构

### 后端DTO
```java
public class AiStrategyConfigDTO {
    private Long id;
    private String apiCode;
    private String apiName;
    private String provider;
    private String model;
    private Double temperature;
    private Integer maxTokens;
    private Boolean enabled;
    private String description;
    private String icon;
    private Integer sortOrder;
}
```

### 前端数据
```javascript
{
  id: 1,
  apiCode: 'clarify-requirement',
  apiName: 'AI明确需求',
  provider: 'huoshan',
  model: 'doubao-seed-2.0-code',
  temperature: 0.7,
  maxTokens: 2000,
  enabled: true,
  description: '...',
  icon: 'ChatDotRound',
  sortOrder: 1
}
```

## 依赖检查
- 后端：JPA、Lombok已存在
- 前端：Element Plus、Axios已存在

## 文件清单
### 后端
1. entity/AiStrategyConfig.java
2. repository/AiStrategyConfigRepository.java
3. service/AiStrategyConfigService.java
4. controller/AiStrategyConfigController.java
5. dto/AiStrategyConfigDTO.java
6. db/migration/V1__create_ai_strategy_config_table.sql

### 前端
1. api/aiStrategy.js（更新）
2. views/admin/AiStrategy.vue（更新）
