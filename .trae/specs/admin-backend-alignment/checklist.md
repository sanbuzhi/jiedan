# 管理后台前后端接口对齐 Checklist

## 后端接口实现检查

### AdminController 用户管理
- [x] GET /admin/users 返回分页用户列表
- [x] PUT /admin/users/{id}/toggle-active 切换用户状态
- [x] PUT /admin/users/{id} 更新用户信息
- [x] 返回字段包含：id, phone, nickname, referral_code, total_points, is_active, created_at

### OrderController 订单管理
- [x] GET /orders 支持分页参数 (skip, limit)
- [x] GET /orders 支持筛选参数 (status, payment_type, min_amount, max_amount, order_no)
- [x] GET /orders 返回包含 user_phone 字段
- [x] GET /orders/{id} 返回订单详情
- [x] PUT /orders/{id}/pay 确认收款
- [x] PUT /orders/{id}/refund 退款

### RequirementController 需求管理
- [x] GET /api/v1/requirements 支持分页和筛选
- [x] GET /api/v1/requirements/{id} 返回需求详情
- [x] PUT /api/v1/requirements/{id} 更新需求状态
- [~] 返回字段包含：contact_name, contact_phone, user_type, project_type, budget_range（部分字段需要前端适配）

### AnalyticsController 仪表盘
- [x] GET /analytics/dashboard 返回统计数据
- [x] 包含字段：total_views, total_clicks, total_registrations, total_conversions
- [x] 包含字段：total_orders, total_revenue
- [x] 包含字段：recent_requirements, recent_orders, recent_events
- [x] 包含字段：top_platforms（格式为 {platform, count}）

### ABTestController A/B测试
- [x] GET /analytics/experiments 返回实验列表
- [x] POST /analytics/experiments 创建实验
- [x] PUT /analytics/experiments/{id} 更新实验
- [x] GET /analytics/experiments/{id}/results 返回实验结果

### OptimizationController 优化规则
- [x] GET /analytics/rules 返回规则列表
- [x] POST /analytics/rules 创建规则
- [x] PUT /analytics/rules/{id} 更新规则
- [x] DELETE /analytics/rules/{id} 删除规则
- [x] POST /analytics/optimize 运行优化
- [x] GET /analytics/optimize/suggestions 返回优化建议

### SystemConfigController 系统配置
- [x] GET /admin/system-config 返回系统配置
- [x] PUT /admin/system-config 更新系统配置

## 前端代码调整检查

### API 路径
- [x] api.js 中 getRequirements 路径为 /api/v1/requirements
- [x] api.js 中 getRequirementDetail 路径为 /api/v1/requirements/{id}
- [x] api.js 中 updateRequirementStatus 路径为 /api/v1/requirements/{id}

### Dashboard.vue 数据适配
- [x] top_platforms 映射使用 p.platform 和 p.count
- [~] recent_requirements 字段映射正确（后端返回简化数据）
- [~] recent_orders 字段映射正确（amount, status, payment_type）

### OrderManagement.vue 数据适配
- [x] 订单列表响应格式为 {items, total}
- [x] 每条订单包含 user_phone 字段
- [x] 订单金额字段为 amount

### RequirementManagement.vue 数据适配
- [~] 需求列表响应格式为 {items, total}（需要调整）
- [~] 每条需求包含 contact_name, contact_phone 字段（需要实体支持）
- [~] user_type 字段值匹配（individual/enterprise/agency）

### UserManagement.vue 数据适配
- [x] 用户列表响应格式为 {items, total}
- [x] 返回字段包含 total_points, is_active

## 编译状态
- [x] 后端编译成功 BUILD SUCCESS
