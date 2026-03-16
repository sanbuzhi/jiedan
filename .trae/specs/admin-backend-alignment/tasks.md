# 管理后台前后端接口对齐 Tasks

## 第一阶段：核心管理接口实现

### Task 1: 实现 AdminController 用户管理接口
**描述**: 创建 AdminController 提供管理员用户管理功能
**子任务**:
- [x] 创建 AdminController.java，添加 GET /admin/users 接口
- [x] 添加 PUT /admin/users/{id}/toggle-active 接口
- [x] 添加 PUT /admin/users/{id} 接口
- [x] 在 UserRepository 添加分页查询方法

### Task 2: 完善 OrderController 订单管理接口
**描述**: 补充 OrderController 缺失的管理员订单管理功能
**子任务**:
- [x] 修改 GET /orders 接口支持分页和筛选参数
- [x] 添加 GET /orders/{id} 获取订单详情接口
- [x] 添加 PUT /orders/{id}/pay 确认收款接口
- [x] 添加 PUT /orders/{id}/refund 退款接口
- [x] 确保返回数据包含 user_phone 等前端需要的字段

### Task 3: 完善 RequirementController 需求管理接口
**描述**: 确保 RequirementController 支持管理员需求管理
**子任务**:
- [x] 修改 GET /api/v1/requirements 支持分页和筛选参数
- [x] 添加 GET /api/v1/requirements/{id} 获取详情接口
- [x] 添加 PUT /api/v1/requirements/{id} 更新状态接口

## 第二阶段：分析优化接口实现

### Task 4: 实现 A/B测试管理接口
**描述**: 创建 ABTestController 提供 A/B 测试管理功能
**子任务**:
- [x] 创建 ABTestController.java
- [x] 添加 GET /analytics/experiments 接口
- [x] 添加 POST /analytics/experiments 接口
- [x] 添加 PUT /analytics/experiments/{id} 接口
- [x] 添加 GET /analytics/experiments/{id}/results 接口
- [~] 创建 Experiment 实体和 Repository（使用内存数据简化实现）

### Task 5: 实现优化规则管理接口
**描述**: 创建 OptimizationController 提供优化规则管理
**子任务**:
- [x] 创建 OptimizationController.java
- [x] 添加 GET /analytics/rules 接口
- [x] 添加 POST /analytics/rules 接口
- [x] 添加 PUT /analytics/rules/{id} 接口
- [x] 添加 DELETE /analytics/rules/{id} 接口
- [x] 添加 POST /analytics/optimize 接口
- [x] 添加 GET /analytics/optimize/suggestions 接口
- [~] 创建 OptimizationRule 实体和 Repository（使用内存数据简化实现）

### Task 6: 实现系统配置管理接口
**描述**: 创建 SystemConfigController 提供系统配置管理
**子任务**:
- [x] 创建 SystemConfigController.java
- [x] 添加 GET /admin/system-config 接口
- [x] 添加 PUT /admin/system-config 接口
- [~] 创建 SystemConfig 实体和 Repository（使用内存数据简化实现）

## 第三阶段：前端代码调整

### Task 7: 调整前端 API 调用
**描述**: 修改前端 api.js 和 admin 页面以适应后端接口
**子任务**:
- [x] 修改 api.js 中需求管理路径为 /api/v1/requirements
- [x] 调整 Dashboard.vue 数据字段映射（如 top_platforms 结构）
- [x] 调整 OrderManagement.vue 期望的数据结构
- [x] 调整 RequirementManagement.vue 期望的数据结构
- [x] 调整 UserManagement.vue 期望的数据结构

## Task Dependencies
- Task 2 依赖 Task 1（共用 User 相关查询）
- Task 4、5、6 可以并行执行
- Task 7 依赖所有后端接口 Task 完成
