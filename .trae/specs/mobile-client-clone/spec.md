# 移动端客户端复刻规格说明

## Why
用户需要一个小程序端一致体验的移动端网页客户端，支持手机浏览器访问，包含完整的用户注册登录、项目管理、积分系统等功能。采用Vue技术栈开发，确保代码统一性和可维护性。

## What Changes
- 新建 `frontend/src/mobile/` 目录作为移动端专属代码区域
- 创建移动端专用路由配置
- 复刻小程序所有核心页面：
  - 引导页 (guide)
  - 首页 (index) - 我的项目 + 实时成交
  - 登录/注册页 (login) - 将微信登录改为手机号+验证码登录
  - 个人中心 (profile) - 用户信息、推荐码、推荐团队、积分记录
  - 积分兑换页 (points-exchange)
  - 积分记录页 (points-list)
  - 需求提交流程 (requirement/step, step_all, ui_gallery, approve, suggestion)
- 移动端专用布局和导航（底部TabBar）
- 响应式设计，优先适配手机屏幕

## Impact
- 新增文件：
  - `frontend/src/mobile/` - 移动端视图组件
  - `frontend/src/mobile/router/` - 移动端路由
  - `frontend/src/mobile/stores/` - 移动端状态管理
  - `frontend/src/mobile/views/` - 移动端页面
  - `frontend/src/mobile/components/` - 移动端通用组件
- 修改文件：
  - `frontend/src/router/index.js` - 添加移动端路由前缀 `/m/`
  - `frontend/index.html` - 添加移动端视口配置

## ADDED Requirements

### Requirement: 移动端路由系统
系统 SHALL 提供移动端专属路由，使用 `/m/` 前缀区分，路由配置包括：
- `/m/` - 引导页（未登录）/ 首页（已登录）
- `/m/login` - 登录注册页
- `/m/home` - 首页（我的项目 + 实时成交）
- `/m/profile` - 个人中心
- `/m/points` - 积分记录
- `/m/exchange` - 积分兑换
- `/m/requirement/step` - 分步需求提交
- `/m/requirement/step_all` - 一步需求提交
- `/m/requirement/ui_gallery` - UI选择
- `/m/requirement/approve` - 需求确认
- `/m/requirement/suggestion` - AI建议

#### Scenario: 未登录用户访问
- **WHEN** 用户打开 `/m/` 路径
- **THEN** 自动跳转至 `/m/login`

#### Scenario: 已登录用户访问登录页
- **WHEN** 已登录用户访问 `/m/login`
- **THEN** 自动跳转至 `/m/home`

### Requirement: 移动端登录注册
系统 SHALL 提供手机号+验证码的登录注册方式：
- 输入手机号获取验证码
- 验证码倒计时60秒
- 使用已有验证码123456便捷登录
- 支持推荐码输入

### Requirement: 移动端底部导航
系统 SHALL 提供移动端底部TabBar：
- 首页（项目列表）
- 我的（个人中心）
- 积分（积分记录入口）
- 退出登录

### Requirement: 移动端首页
系统 SHALL 复刻小程序首页：
- 显示"我的项目"区域
  - 无项目时显示空状态卡片，点击创建新项目
  - 有项目时显示项目卡片列表
  - 每个项目显示流程节点时间线（水平滚动）
  - 支持删除草稿状态项目
- 显示"实时成交"区域
  - 滚动显示最近成交记录

### Requirement: 移动端个人中心
系统 SHALL 复刻小程序个人中心：
- 用户信息卡片（头像、昵称、ID、积分、推荐人数）
- 编辑个人资料弹窗（头像上传、昵称修改）
- 我的推荐码区域（复制、分享）
- 推荐团队树状/列表视图
- 积分记录快捷入口
- 退出登录按钮

### Requirement: 移动端响应式布局
系统 SHALL 支持手机屏幕适配：
- 视口配置：width=device-width, initial-scale=1.0
- 最大宽度限制：建议420px以内
- 触摸友好的按钮尺寸：最小44px
- 适当的内边距和间距

## MODIFIED Requirements

### Requirement: 登录流程改造
将小程序微信一键登录改为网页端手机号+验证码登录

## REMOVED Requirements
- 微信一键登录功能（Web端不适用）
- 微信分享功能（Web端使用URL分享）
