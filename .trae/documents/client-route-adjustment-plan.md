# 客户前端路由调整计划

## 需求
将移动端客户端（客户视角）的路由从 `/m/` 调整为 `/client/`

## 当前状态
- 现有路由：`/m/guide`, `/m/login`, `/m/home`, `/m/profile` 等

## 调整步骤

### Step 1: 修改主路由配置
文件：`frontend/src/router/index.js`
- 将所有 `/m/` 前缀路由改为 `/client/` 前缀
- 修改路由守卫中的路径判断逻辑

### Step 2: 修改移动端页面中的路由路径
需要修改以下文件的路由跳转：
- `GuideView.vue` - `router.replace('/client/...')`
- `LoginView.vue` - `router.push('/client/home')`
- `ProfileView.vue` - `router.push('/client/...')`
- `ApproveView.vue` - `router.push('/client/home')`
- `RequirementStepView.vue` - `router.push('/client/home')`
- `RequirementStepAllView.vue` - `router.push('/client/home')`
- `MobileTabBar.vue` - `goTo('/client/...')`, `router.push('/client/login')`

### Step 3: 验证
- 确认所有路由路径已更新
- 访问 http://localhost:3000/client/ 测试

## 涉及文件清单
1. `frontend/src/router/index.js` - 主路由配置
2. `frontend/src/mobile/views/GuideView.vue`
3. `frontend/src/mobile/views/LoginView.vue`
4. `frontend/src/mobile/views/ProfileView.vue`
5. `frontend/src/mobile/views/ApproveView.vue`
6. `frontend/src/mobile/views/RequirementStepView.vue`
7. `frontend/src/mobile/views/RequirementStepAllView.vue`
8. `frontend/src/mobile/components/MobileTabBar.vue`
