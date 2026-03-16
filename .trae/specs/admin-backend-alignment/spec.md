# 管理后台前后端接口对齐 Spec

## Why
前端管理后台界面存在多个接口调用与后端实际实现不匹配的问题，导致功能无法正常使用。需要系统性地检查并对齐所有接口，确保管理后台功能完整可用。

## What Changes
- 对齐所有管理后台前端接口调用与后端实现
- 实现缺失的后端接口
- 调整前端代码以适应后端实际返回的数据结构
- **搁置**：后端已实现但前端未使用的接口（如素材管理、发布任务等）

## Impact
- 影响前端：frontend/src/views/admin/*.vue, frontend/src/utils/api.js
- 影响后端：springboot-backend/src/main/java/com/jiedan/controller/*.java

## ADDED Requirements

### Requirement: 用户管理接口
后端 SHALL 提供管理员用户管理接口

#### Scenario: 获取用户列表
- **WHEN** 前端调用 GET /admin/users
- **THEN** 返回分页用户列表，包含 id, phone, nickname, referral_code, total_points, is_active, created_at

#### Scenario: 切换用户状态
- **WHEN** 前端调用 PUT /admin/users/{id}/toggle-active
- **THEN** 切换用户启用/禁用状态

#### Scenario: 更新用户信息
- **WHEN** 前端调用 PUT /admin/users/{id}
- **THEN** 更新用户昵称、积分等信息

### Requirement: 订单管理接口
后端 SHALL 提供管理员订单管理接口

#### Scenario: 获取订单列表
- **WHEN** 前端调用 GET /orders（带分页和筛选参数）
- **THEN** 返回分页订单列表，包含 id, amount, status, payment_type, created_at 及关联用户信息

#### Scenario: 确认收款
- **WHEN** 前端调用 PUT /orders/{id}/pay
- **THEN** 将订单状态更新为已确认

#### Scenario: 退款
- **WHEN** 前端调用 PUT /orders/{id}/refund
- **THEN** 处理订单退款

#### Scenario: 获取订单详情
- **WHEN** 前端调用 GET /orders/{id}
- **THEN** 返回订单完整详情

### Requirement: 需求管理接口
后端 SHALL 提供管理员需求管理接口

#### Scenario: 获取需求列表
- **WHEN** 前端调用 GET /api/v1/requirements（带分页和筛选参数）
- **THEN** 返回分页需求列表

#### Scenario: 获取需求详情
- **WHEN** 前端调用 GET /api/v1/requirements/{id}
- **THEN** 返回需求完整详情

#### Scenario: 更新需求状态
- **WHEN** 前端调用 PUT /api/v1/requirements/{id}
- **THEN** 更新需求状态和备注

### Requirement: 仪表盘统计接口
后端 SHALL 提供仪表盘统计数据

#### Scenario: 获取仪表盘数据
- **WHEN** 前端调用 GET /analytics/dashboard?days={days}
- **THEN** 返回统计数据：总浏览量、点击量、注册数、订单数、收入、最近需求、最近订单、平台分布等

### Requirement: A/B测试接口
后端 SHALL 提供A/B测试管理接口

#### Scenario: 获取实验列表
- **WHEN** 前端调用 GET /analytics/experiments
- **THEN** 返回实验列表

#### Scenario: 创建实验
- **WHEN** 前端调用 POST /analytics/experiments
- **THEN** 创建新实验

#### Scenario: 更新实验
- **WHEN** 前端调用 PUT /analytics/experiments/{id}
- **THEN** 更新实验信息

#### Scenario: 获取实验结果
- **WHEN** 前端调用 GET /analytics/experiments/{id}/results
- **THEN** 返回实验统计数据

### Requirement: 优化规则接口
后端 SHALL 提供优化规则管理接口

#### Scenario: 获取规则列表
- **WHEN** 前端调用 GET /analytics/rules
- **THEN** 返回优化规则列表

#### Scenario: 创建规则
- **WHEN** 前端调用 POST /analytics/rules
- **THEN** 创建新规则

#### Scenario: 更新规则
- **WHEN** 前端调用 PUT /analytics/rules/{id}
- **THEN** 更新规则

#### Scenario: 删除规则
- **WHEN** 前端调用 DELETE /analytics/rules/{id}
- **THEN** 删除规则

#### Scenario: 运行优化
- **WHEN** 前端调用 POST /analytics/optimize
- **THEN** 执行优化算法

#### Scenario: 获取优化建议
- **WHEN** 前端调用 GET /analytics/optimize/suggestions
- **THEN** 返回优化建议列表

### Requirement: 系统配置接口
后端 SHALL 提供系统配置管理接口

#### Scenario: 获取系统配置
- **WHEN** 前端调用 GET /admin/system-config
- **THEN** 返回系统配置项

#### Scenario: 更新系统配置
- **WHEN** 前端调用 PUT /admin/system-config
- **THEN** 更新系统配置

## MODIFIED Requirements

### Requirement: 前端API路径调整
前端 SHALL 调整部分API路径以匹配后端实际接口

#### Scenario: 需求管理路径
- **CHANGED FROM**: /requirements
- **CHANGED TO**: /api/v1/requirements

## REMOVED Requirements
无
