# Index 页面代码优化计划

## 优化目标

优化 index.js 代码结构，去除冗余代码，提高可维护性和可读性。

***

## 一、代码结构分析

### 当前代码统计

* 总行数：约 961 行

* 方法数量：约 30+ 个

* 主要问题：代码冗余、逻辑分散、重复代码

### 主要问题清单

#### 1. 重复代码

* [ ] `initRecentDeals` 和 `updateRecentDeals` 有大量重复的数据生成逻辑

* [ ] 多处使用 `wx.getStorageSync` 和 `wx.setStorageSync`，可以封装

* [ ] `onUnload` 和 `onHide` 中定时器清理逻辑重复

#### 2. 冗余代码

* [ ] `scrollToActiveNode` 和 `scrollToNodeForProject` 方法未被使用

* [ ] `getCurrentStep` 方法未被调用

* [ ] `statusText` 对象在 data 中定义但未被使用

#### 3. 逻辑分散

* [ ] 登录检查和需求加载逻辑分散在多个方法中

* [ ] 定时器管理逻辑分散在 `onShow`、`onHide`、`onUnload`

#### 4. 代码组织问题

* [ ] 方法顺序混乱，没有按功能分组

* [ ] 配置数据（aiNodeConfig、allFlowNodes）和逻辑代码混在一起

***

## 二、优化方案

### 阶段一：移除冗余代码

#### 1. 移除未使用的方法

* `scrollToActiveNode` - 未被调用

* `scrollToNodeForProject` - 未被调用

* `getCurrentStep` - 未被调用

* `statusText` - data 中定义但未被使用

#### 2. 简化定时器管理

* 合并 `onUnload` 和 `onHide` 的定时器清理逻辑

* 使用统一的方法管理定时器

### 阶段二：提取重复逻辑

#### 1. 封装 Storage 操作

```javascript
// 提取为工具方法
const storage = {
  get: (key) => wx.getStorageSync(key),
  set: (key, value) => wx.setStorageSync(key, value),
  remove: (key) => wx.removeStorageSync(key)
};
```

#### 2. 统一错误处理

```javascript
// 提取统一的错误处理方法
handleError: function (err, message) {
  console.error(message, err);
  wx.showToast({ title: message, icon: 'none' });
}
```

#### 3. 合并数据生成逻辑

```javascript
// 合并 initRecentDeals 和 updateRecentDeals 的重复代码
generateDealData: function (count = 1) {
  // 统一的数据生成逻辑
}
```

### 阶段三：重组代码结构

#### 1. 按功能分组方法

```javascript
// 1. 生命周期方法
onLoad, onShow, onHide, onUnload

// 2. 数据加载方法
loadCurrentRequirement, loadCurrentRequirementAndCheckAutoTrigger

// 3. AI 节点执行方法
executeAINode, executeAINodeWithRetry, buildAIRequestData, callAIWithTimeout

// 4. 流程节点管理方法
calculateFlowNodes, updateFlowNodeStatus, markNodeAsFailed

// 5. 验证和产物方法
performFeedbackShadowValidation, saveAIProduct, getStageName

// 6. 用户交互方法
onFlowNodeTap, createNewProject, deleteProject, doDeleteProject

// 7. 辅助方法
handleSceneParams, parseSceneParams, checkGuideStatus, checkLoginStatus

// 8. 模拟数据方法
initRecentDeals, updateRecentDeals

// 9. 分享方法
onShareAppMessage, onShareTimeline
```

#### 2. 提取配置数据

```javascript
// 将配置数据移到文件顶部或单独提取
const CONFIG = {
  AI_NODES: { ... },
  FLOW_NODES: [ ... ],
  CLICK_HINTS: { ... },
  STATUS_MAP: { ... }
};
```

### 阶段四：优化具体逻辑

#### 1. 优化 `loadCurrentRequirement`

* 简化回调处理

* 提取数据处理逻辑为独立方法

#### 2. 优化 `executeAINodeWithRetry`

* 简化 Promise 链

* 提取错误处理逻辑

#### 3. 优化 `calculateFlowNodes`

* 简化条件判断

* 提取常量配置

#### 4. 优化 `onShow`

* 简化条件判断逻辑

* 提取刷新检查逻辑

***

## 三、具体优化步骤

### Step 1: 移除冗余代码

1. 删除 `scrollToActiveNode` 方法
2. 删除 `scrollToNodeForProject` 方法
3. 删除 `getCurrentStep` 方法
4. 从 data 中移除 `statusText`

### Step 2: 简化定时器管理

1. 创建 `clearDealsTimer` 方法
2. 统一在 `onHide` 和 `onUnload` 中调用

### Step 3: 提取重复逻辑

1. 创建 `generateDealData` 方法
2. 简化 `initRecentDeals` 和 `updateRecentDeals`

### Step 4: 重组方法顺序

按功能分组重新排列方法

### Step 5: 提取配置常量

将 aiNodeConfig、allFlowNodes 等配置提取为常量

### Step 6: 优化代码细节

1. 简化条件判断
2. 统一错误处理
3. 优化变量命名

***

## 四、预期效果

### 代码量减少

* 预计减少 100-150 行代码

* 移除 3-4 个未使用的方法

### 可读性提升

* 方法按功能分组，易于查找

* 配置数据集中管理

* 重复逻辑统一封装

### 可维护性提升

* 修改定时器逻辑只需改一处

* 添加新 AI 节点只需修改配置

* 错误处理统一，便于调试

***

## 五、风险分析

### 低风险

* 移除未使用的方法不会影响功能

* 重构代码结构不改变业务逻辑

### 注意事项

* 确保定时器清理逻辑正确

* 验证数据加载回调正常执行

* 测试 AI 节点执行流程

***

## 六、验证清单

* [ ] 页面正常加载

* [ ] 需求列表正确显示

* [ ] AI 节点自动执行正常

* [ ] 定时器正常启动和清理

* [ ] 删除项目功能正常

* [ ] 节点点击跳转正常

