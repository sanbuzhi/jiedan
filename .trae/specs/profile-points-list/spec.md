# 个人中心-积分记录列表页 产品规格文档

## Why
当前个人中心页面的"积分记录"区域仅展示最近10条记录，用户点击"查看全部"时仅显示"功能开发中..."的提示，无法查看完整的积分记录历史。为了提升用户体验，需要实现一个完整的积分记录列表页，支持分页加载、筛选和详情查看。

## What Changes

### 1. 新增积分记录列表页
- 创建新的页面 `pages/points-list/points-list`
- 展示完整的积分记录列表
- 支持分页加载（上拉加载更多）
- 支持下拉刷新

### 2. 个人中心页面跳转更新
- 更新 `profile.js` 中的 `viewAllPoints` 方法
- 点击"查看全部"跳转到积分记录列表页

### 3. 积分记录展示优化
- 显示积分变动金额（收入/支出）
- 显示积分变动描述
- 显示积分变动时间
- 显示积分变动后的余额

## Impact
- **受影响的产品能力**：个人中心-积分记录查看
- **受影响的技术资产**：
  - 前端：miniprogram/pages/profile/profile.js, miniprogram/pages/points-list/
  - 后端：已存在 `/users/points` 接口，无需修改

## ADDED Requirements

### Requirement: 积分记录列表页
The system SHALL provide a dedicated page for viewing complete point records with pagination support.

#### Scenario: 页面初始化加载
- **GIVEN** 用户从个人中心点击"查看全部"积分记录
- **WHEN** 页面完成加载
- **THEN** 显示积分记录列表（默认20条）
- **AND** 显示用户当前积分总额

#### Scenario: 分页加载更多
- **GIVEN** 用户位于积分记录列表页
- **AND** 列表数据超过一页
- **WHEN** 用户滚动至列表底部
- **THEN** 自动加载下一页数据
- **AND** 显示加载状态提示

#### Scenario: 下拉刷新
- **GIVEN** 用户位于积分记录列表页
- **WHEN** 用户执行下拉刷新操作
- **THEN** 重新加载第一页数据
- **AND** 更新积分总额显示

#### Scenario: 空状态展示
- **GIVEN** 用户无任何积分记录
- **WHEN** 页面加载完成
- **THEN** 显示空状态提示"暂无积分记录"

## MODIFIED Requirements

### Requirement: 个人中心查看全部积分跳转
**Previous**: 点击"查看全部"显示"功能开发中..."提示
**Modified**: 点击"查看全部"跳转至积分记录列表页

## REMOVED Requirements
None
