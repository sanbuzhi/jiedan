# 客户端1:1复刻 - 详细实施计划

## 1. 引导页 (GuideView.vue) 实施

### 目标
完全复刻小程序的引导页，包括功能介绍、流程说明、用户须知、按钮操作。

### 实施内容
```vue
<!-- 参考 miniprogram/pages/guide/guide.wxml 和 guide.wxss -->
- 顶部装饰区域（渐变背景 + 圆形装饰）
- Logo + 标题区域
- 可滚动内容区域（功能介绍、流程说明、用户须知、免责声明）
- 底部按钮区域
  - "体验使用" → 跳转 /client/requirement/step_all
  - "登录/注册" → 跳转 /client/login
```

---

## 2. 首页 (HomeView.vue) 实施

### 目标
完全复刻小程序的首页，包括项目列表和流程节点。

### 实施内容

#### 2.1 项目状态映射
```javascript
const STATUS_MAP = {
  draft: '草稿',
  pending: '待处理',
  processing: '进行中',
  developing: '开发中',
  completed: '已完成'
}
```

#### 2.2 完整流程节点定义 (11个节点)
```javascript
const FLOW_NODES = [
  { title: '明确需求', desc: '客户提交初步需求' },
  { title: 'AI明确需求', desc: 'AI分析并完善需求' },
  { title: '需求确认验收', desc: '确认需求文档' },
  { title: 'AI拆分任务', desc: '自动拆分子任务' },
  { title: 'AI开发', desc: '智能编码实现' },
  { title: 'AI功能测试', desc: '自动化功能测试' },
  { title: 'AI安全测试', desc: '测试接口漏洞' },
  { title: '功能验收测试', desc: '客户功能验收' },
  { title: '打包交付', desc: '项目交付' },
  { title: '最终验收', desc: '客户最终验收' },
  { title: '项目完成', desc: '项目结束' }
]

const AI_NODE_INDEXES = [1, 3, 4, 5, 6]
const APPROVE_NODE_INDEXES = [2, 7, 9] // 客户验收节点
```

#### 2.3 节点点击处理
```javascript
onFlowNodeTap(reqIndex, nodeIndex, reqId) {
  // 节点0(明确需求) → 跳转 step_all
  if (nodeIndex === 0 && node.status === 'active') {
    router.push(`/client/requirement/step_all?id=${reqId}&stage=1`)
  }
  // 客户验收节点(2,7,9) → 跳转 approve
  if ([2, 7, 9].includes(nodeIndex) && node.status === 'active') {
    const stageName = nodeIndex === 2 ? '需求确认验收' : nodeIndex === 7 ? '功能验收测试' : '最终验收'
    const stage = ['clarify', 'security', 'final'][nodeIndex === 2 ? 0 : nodeIndex === 7 ? 1 : 2]
    router.push(`/client/requirement/approve?requirementId=${reqId}&stage=${stage}&stageName=${stageName}`)
  }
  // AI节点(1,3,4,5,6) → 显示"AI处理中"
  if (AI_NODE_INDEXES.includes(nodeIndex) && node.status === 'active') {
    showToast(`${node.title}中，请稍候...`)
  }
}
```

#### 2.4 scroll-into-view 定位
```javascript
// 为每个项目设置 scrollIntoViewId
allRequirements.forEach((req, reqIndex) => {
  const activeNodeIndex = req.flowNodes.findIndex(node => node.status === 'active')
  if (activeNodeIndex !== -1) {
    req.scrollIntoViewId = `node-${reqIndex}-${activeNodeIndex}`
  }
})
```

#### 2.5 节点状态计算
```javascript
calculateFlowNodes(currentStep) {
  return FLOW_NODES.map((node, index) => {
    let status = 'pending'
    if (index < currentStep) status = 'completed'
    else if (index === currentStep) status = 'active'

    return {
      ...node,
      status,
      isAiNode: AI_NODE_INDEXES.includes(index),
      clickHint: this.getClickHint(index, status)
    }
  })
}

getClickHint(index, status) {
  if (status !== 'active') return null
  if (index === 0) return '点击编辑'
  if ([2, 7, 9].includes(index)) return '点击验收'
  if ([1, 3, 4, 5, 6].includes(index)) return 'AI处理中...'
  if (index === 8) return '等待交付'
  if (index === 10) return '已完成'
  return null
}
```

---

## 3. 验收标准

### 引导页
- [ ] 显示完整的功能介绍、流程说明、用户须知、免责声明
- [ ] "体验使用"按钮点击后跳转 step_all
- [ ] "登录/注册"按钮点击后跳转 login

### 首页
- [ ] 显示11个完整流程节点
- [ ] 项目卡片包含状态标签、删除按钮（草稿状态时）
- [ ] 流程节点水平滚动，scroll-into-view 定位当前节点
- [ ] AI节点 active 状态显示 🤖 动画图标
- [ ] 节点点击有正确的跳转逻辑
- [ ] 无项目时显示空状态卡片
- [ ] 实时成交区域正常显示

---

## 4. 实施顺序

1. **重写 GuideView.vue** - 完全复刻引导页
2. **重写 HomeView.vue** - 完全复刻首页流程节点逻辑
3. **验证功能** - 确保跳转和交互正确
