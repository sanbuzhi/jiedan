# Tasks - 移动端客户端复刻

## 任务清单

- [x] Task 1: 创建移动端项目结构和基础配置
  - [x] 创建 `frontend/src/mobile/` 目录结构
  - [x] 创建移动端专用 router
  - [x] 创建移动端 stores (user store)
  - [x] 更新 index.html 添加移动端视口配置
  - [x] 更新主路由添加 `/m/` 前缀路由

- [x] Task 2: 创建移动端通用布局组件
  - [x] 创建 MobileLayout 布局组件
  - [x] 创建 MobileTabBar 底部导航组件
  - [x] 创建移动端页面容器样式

- [x] Task 3: 复刻移动端引导页 (guide)
  - [x] 创建 GuideView.vue
  - [x] 实现登录状态检测和跳转逻辑

- [x] Task 4: 复刻移动端登录/注册页 (login)
  - [x] 创建 LoginView.vue
  - [x] 实现手机号+验证码登录
  - [x] 实现验证码发送和倒计时
  - [x] 支持推荐码传入

- [x] Task 5: 复刻移动端首页 (home)
  - [x] 创建 HomeView.vue
  - [x] 实现"我的项目"区域（空状态/有项目列表）
  - [x] 实现流程节点时间线组件
  - [x] 实现"实时成交"区域
  - [x] 实现创建新项目功能

- [x] Task 6: 复刻移动端个人中心 (profile)
  - [x] 创建 ProfileView.vue
  - [x] 实现用户信息卡片
  - [x] 实现编辑个人资料弹窗
  - [x] 实现推荐码展示和复制/分享
  - [x] 实现推荐团队树状/列表视图
  - [x] 实现积分记录快捷入口
  - [x] 实现退出登录

- [x] Task 7: 复刻移动端积分页面
  - [x] 创建 PointsView.vue (积分记录)
  - [x] 创建 ExchangeView.vue (积分兑换)

- [x] Task 8: 复刻需求提交流程页面
  - [x] 创建 RequirementStepView.vue (分步提交流程)
  - [x] 创建 RequirementStepAllView.vue (一步提交)
  - [x] 创建 UiGalleryView.vue (UI选择)
  - [x] 创建 ApproveView.vue (需求确认)
  - [x] 创建 SuggestionView.vue (AI建议)

- [ ] Task 9: 验证和测试
  - [ ] 验证移动端路由跳转
  - [ ] 验证登录注册流程
  - [ ] 验证页面响应式布局
  - [ ] 验证与后端API对接

## 任务依赖
- Task 1 完成后才能开始 Task 2-8
- Task 2 是其他页面的基础
- Task 3-7 可以并行开发
- Task 8 依赖 Task 5 的项目相关逻辑
- Task 9 在所有页面完成后进行
