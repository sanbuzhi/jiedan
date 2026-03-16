# 修复需求验证和字段统一计划

## 问题分析

### 1. 当前问题
- `RequirementCreate` DTO 中多个字段有 `@NotBlank`/`@NotNull` 验证注解
- 前端传入 null 值时，Spring Validation 抛出 `MethodArgumentNotValidException`
- 数据库表字段和 DTO、Entity 之间存在不一致

### 2. 需要修复的内容

#### 任务1: 放宽 RequirementCreate 验证规则
**目标**: 只有需求描述字段必填，其他字段可选

**修改文件**:
- `com.jiedan.dto.RequirementCreate`

**具体修改**:
- 移除 `userType` 的 `@NotBlank`
- 移除 `projectType` 的 `@NotBlank`
- 移除 `needOnline` 的 `@NotNull`
- 移除 `urgency` 的 `@NotBlank`
- 移除 `deliveryPeriod` 的 `@NotNull`
- 移除 `uiStyle` 的 `@NotBlank`
- 添加 `requirementDescription` 字段（可选，但建议前端传入）

#### 任务2: 统一数据库字段约束
**目标**: 让数据库字段约束与 DTO 一致（都改为可选）

**修改文件**:
- 创建数据库迁移脚本 `V5__Update_requirement_constraints.sql`

**具体修改**:
- `user_type` - 保持 `nullable = false`（但DTO不强制验证，由业务逻辑处理默认值）
- `project_type` - 保持 `nullable = false`
- `need_online` - 已有默认值 `false`，保持现状
- `urgency` - 改为 nullable
- `delivery_period` - 改为 nullable
- `ui_style` - 改为 nullable

#### 任务3: 检查并统一 RequirementUpdate DTO
**目标**: 确保更新接口的字段与创建接口一致

**修改文件**:
- `com.jiedan.dto.RequirementUpdate`

**检查内容**:
- 字段列表是否与 `RequirementCreate` 一致
- 是否包含所有可更新字段

#### 任务4: 检查并统一 RequirementResponse DTO
**目标**: 确保响应字段完整

**修改文件**:
- `com.jiedan.dto.RequirementResponse`

**检查内容**:
- 是否包含所有 Entity 字段
- 字段类型是否正确

#### 任务5: 检查 AI 接口字段
**目标**: 确保 AI 接口使用正确的需求字段

**检查文件**:
- `com.jiedan.service.ai.code.IncrementalDevelopmentService`
- `com.jiedan.service.ai.code.ScaffoldGenerator`
- `com.jiedan.service.ai.shadow.FeedbackShadowService`

**检查内容**:
- AI 接口是否使用 `requirementDescription` 字段
- 字段映射是否正确

#### 任务6: 修改 Controller 默认值处理
**目标**: 为可选字段提供默认值

**修改文件**:
- `com.jiedan.controller.RequirementController`

**具体修改**:
- 在 `createRequirement` 方法中为可选字段设置默认值
- `userType` - 默认为 "individual"
- `projectType` - 默认为 "website"
- `needOnline` - 默认为 false
- `urgency` - 默认为 "normal"
- `deliveryPeriod` - 默认为 30
- `uiStyle` - 默认为 "modern"

## 实施步骤

1. 修改 `RequirementCreate` - 移除验证注解
2. 创建数据库迁移脚本 - 修改字段约束
3. 修改 `RequirementController` - 添加默认值处理
4. 检查并更新 `RequirementUpdate`
5. 检查并更新 `RequirementResponse`
6. 检查 AI 接口字段映射
7. 编译验证
8. 测试接口

## 影响范围

- 前端调用创建需求接口时，不再需要传入所有字段
- 数据库字段约束放宽，允许 null 值
- 现有数据不受影响
- AI 接口可以正常获取需求描述

## 回滚方案

如需回滚：
1. 恢复 `RequirementCreate` 的验证注解
2. 执行反向数据库迁移脚本
