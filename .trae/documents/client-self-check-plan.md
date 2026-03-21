# 客户端1:1复刻自检与调整计划

## 当前实现与小程序的差异分析

### 1. 引导页 (Guide) - 严重差异
**小程序行为**:
- 显示"体验使用"和"加入开发者计划"按钮
- 用户可选择体验流程或登录

**当前实现**:
- 只有logo和自动跳转

**调整方案**: 重写GuideView.vue，添加功能按钮

### 2. 首页流程节点 - 严重差异
**小程序行为**:
- 11个流程节点定义 (FLOW_NODES)
- AI节点(1,3,4,5,6)、客户验收节点(2,7,9)
- 节点点击处理不同类型跳转
- scroll-into-view定位当前节点
- 实时更新节点状态

**当前实现**:
- 简化的流程节点
- 缺少节点点击处理逻辑
- 缺少scroll-into-view

**调整方案**: 重写HomeView.vue的流程节点逻辑

### 3. 个人中心Canvas树 - 中等差异
**小程序行为**:
- Canvas绘制推荐树
- 支持缩放和拖拽
- 头像缓存机制
- 复杂的树状图布局

**当前实现**:
- 简化的列表视图

**调整方案**: 简化Canvas树或保持列表视图

### 4. 登录页 - 已符合要求
- 手机号+验证码登录 ✓

### 5. 需求流程页面 - 需要核对
- step, step_all, ui_gallery, approve, suggestion
- 需要确保完整复刻

---

## 调整任务清单

### Task 1: 重写引导页 GuideView.vue
- 添加"体验使用"按钮 → 跳转 /client/requirement/step_all
- 添加"登录/注册"按钮 → 跳转 /client/login
- 移除自动跳转逻辑

### Task 2: 重写首页流程节点 HomeView.vue
- 实现完整的11个流程节点
- 实现节点点击处理 (onFlowNodeTap)
  - 节点0点击 → 跳转 step_all
  - 节点2,7,9(客户验收)点击 → 跳转 approve
  - AI节点(1,3,4,5,6)点击 → 显示"AI处理中"
- 实现scroll-into-view定位当前节点
- 实现节点状态计算 (active/completed/pending)
- 添加点击提示显示

### Task 3: 核对需求流程页面
- RequirementStepView.vue
- RequirementStepAllView.vue
- UiGalleryView.vue
- ApproveView.vue
- SuggestionView.vue

### Task 4: 核对个人中心
- ProfileView.vue - 确保推荐码、积分记录等功能完整

---

## 实施顺序
1. Task 1 (引导页) - 高优先级
2. Task 2 (首页流程节点) - 高优先级
3. Task 3 (需求流程) - 中优先级
4. Task 4 (个人中心) - 中优先级
