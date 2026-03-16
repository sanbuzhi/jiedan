# Index 页面代码审查计划

## 项目概述
- **审查目标**: miniprogram/pages/index/index.js, index.wxml, index.wxss
- **审查重点**: 流程节点逻辑、数据一致性、状态管理
- **审查日期**: 2026-03-15

---

## 一、数据模型与流程节点映射问题

### 1.1 allFlowNodes 定义审查
**位置**: index.js L28-L40

```javascript
allFlowNodes: [
  { title: '明确需求', desc: '客户提交初步需求' },      // 索引0
  { title: 'AI明确需求', desc: 'AI分析并完善需求' },    // 索引1
  { title: '客户验收', desc: '确认需求文档' },          // 索引2
  { title: 'AI拆分任务', desc: '自动拆分子任务' },      // 索引3
  { title: 'AI开发', desc: '智能编码实现' },            // 索引4
  { title: 'AI功能测试', desc: '自动化功能测试' },      // 索引5
  { title: 'AI安全测试', desc: '测试接口漏洞' },        // 索引6
  { title: '客户验收', desc: '功能验收测试' },          // 索引7
  { title: '打包交付', desc: '项目交付' },              // 索引8
  { title: '客户验收', desc: '最终验收' },              // 索引9
  { title: '项目完成', desc: '项目结束' }               // 索引10
]
```

**问题清单**:
- [ ] 节点索引 2、7、9 都是"客户验收"，标题重复，如何区分？
- [ ] 节点索引 8 的描述根据 needDeploy 动态变化，但标题固定为"打包交付"
- [ ] 后端返回的 currentFlowNode 值域是否与这个 0-10 的索引完全对应？

### 1.2 aiNodeConfig 映射审查
**位置**: index.js L144-L150

```javascript
aiNodeConfig: {
  1: { name: 'AI明确需求', api: 'clarifyRequirement', nextNode: 2, maxRetries: 3, timeout: 120000, needsApproval: true },
  3: { name: 'AI拆分任务', api: 'splitTasks', nextNode: 4, maxRetries: 3, timeout: 90000, needsApproval: true },
  4: { name: 'AI开发', api: 'generateCode', nextNode: 5, maxRetries: 3, timeout: 180000, needsApproval: true },
  5: { name: 'AI功能测试', api: 'functionalTest', nextNode: 6, maxRetries: 3, timeout: 90000, needsApproval: true },
  6: { name: 'AI安全测试', api: 'securityTest', nextNode: 7, maxRetries: 3, timeout: 90000, needsApproval: true }
}
```

**问题清单**:
- [ ] AI节点索引 1,3,4,5,6 与 allFlowNodes 索引是否一一对应？
- [ ] nextNode 值是否正确？例如索引 6 的 nextNode 是 7，但索引 7 是"客户验收"
- [ ] 所有 AI 节点的 needsApproval 都是 true，那什么情况下会走自动继续逻辑（L198-L202）？

### 1.3 getCurrentStep 映射审查
**位置**: index.js L464-L474

```javascript
getCurrentStep: function (status) {
  const stepMap = {
    'pending': 0,      // 明确需求
    'processing': 1,   // AI明确需求
    'quoted': 3,       // AI拆分任务
    'confirmed': 4,    // AI开发
    'developing': 5,   // AI功能测试
    'completed': 10    // 项目完成
  };
  return stepMap[status] || 0;
}
```

**问题清单**:
- [ ] status 值 'processing' 映射到索引 1，但 'processing' 是一个通用状态，是否准确？
- [ ] 'developing' 映射到索引 5（AI功能测试），但语义上应该是开发中
- [ ] 缺少 'quoted'、'confirmed'、'cancelled' 等状态的映射
- [ ] 这个方法是否被实际调用？搜索发现只在代码中定义，未找到调用点

---

## 二、流程节点状态计算逻辑审查

### 2.1 calculateFlowNodes 方法审查
**位置**: index.js L477-L523

**问题清单**:
- [ ] aiNodeIndexes 数组 [1, 3, 4, 5, 6] 与 aiNodeConfig 的 key 是否一致？
- [ ] clickHints 配置中，currentStep=1 时提示索引0"点击调整"，但索引0是"明确需求"不是AI节点
- [ ] currentStep=3 时提示索引2"点击验收"，但索引2是"客户验收"，索引3才是AI拆分任务
- [ ] currentStep=8 时提示索引7"点击验收"，但索引7是"客户验收"
- [ ] currentStep=10 时提示索引9"点击验收"，但索引9是"客户验收"
- [ ] clickHints 的键值对逻辑是否正确？提示应该出现在当前激活节点还是上一个节点？

### 2.2 状态流转逻辑审查
**位置**: index.js L152-L218 executeAINodeWithRetry

**问题清单**:
- [ ] 方法先执行 AI 逻辑，成功后调用 updateFlowNodeStatus 跳到 nextNode
- [ ] 但 executeAINode (L347-L363) 是先更新节点状态，再执行 AI 逻辑
- [ ] 两个方法的执行顺序不一致，是否会导致状态显示混乱？
- [ ] updateFlowNodeStatus 更新的是 nextNode，但 AI 执行的是当前 nodeIndex，逻辑是否一致？

---

## 三、API 调用与数据流审查

### 3.1 API 导入问题
**位置**: index.js L1-L2, L159, L278, L367

```javascript
// L1-L2
const util = require('../../utils/util.js');
const api = require('../../utils/api.js');

// L159
const { requirementApi, aiApi } = require('../../utils/api.js');

// L278
const { requirementApi } = require('../../utils/api.js');

// L367
const { requirementApi } = require('../../utils/api.js');
```

**问题清单**:
- [ ] 多处重复 require api.js，应该统一在顶部导入
- [ ] L405 使用 api.get()，但 api 对象结构是否支持？
- [ ] L670 使用 api.requirementApi，但 api 导出的是 requirementApi 还是 requirementApi？

### 3.2 buildAIRequestData 数据构建审查
**位置**: index.js L222-L255

**问题清单**:
- [ ] case 4 (AI开发) 的 requestData 中 taskDescription 写死为'基于需求生成项目代码'
- [ ] case 4 的 language 和 framework 写死为 'java' 和 'springboot'
- [ ] case 5 (AI功能测试) 的 code 写死为 '// 待测试代码'
- [ ] case 6 (AI安全测试) 的 code 写死为 '// 待测试代码'
- [ ] 这些硬编码值是否符合实际需求？

### 3.3 performFeedbackShadowValidation 审查
**位置**: index.js L290-L332

**问题清单**:
- [ ] apiTypeMap 中的字符串与后端 API 路径是否匹配？
- [ ] validationData.documentContent 使用 aiResult.fullRequirementDoc || aiResult.rawResponse || JSON.stringify(aiResult)
- [ ] 如果 aiResult 结构变化，是否会导致验证数据不完整？
- [ ] 验证失败时 resolve 一个模拟成功对象，这种容错是否合理？

---

## 四、UI 与交互逻辑审查

### 4.1 WXML 数据绑定审查
**位置**: index.wxml L6, L27, L35

```xml
<!-- L6 -->
<text class="section-more" bindtap="viewAllProjects" wx:if="{{projects.length > 0}}">查看全部 ></text>

<!-- L27 -->
<view wx:if="{{req.flowNodes[0] && req.flowNodes[0].status === 'active' && req.status === 'draft'}}" class="delete-btn" bindtap="deleteProject" data-id="{{req.id}}">

<!-- L35 -->
<view class="flow-node {{node.status}}" id="node-{{index}}-{{nodeIndex}}" bindtap="onFlowNodeTap" data-req-index="{{index}}" data-node-index="{{nodeIndex}}">
```

**问题清单**:
- [ ] L6 使用 projects.length，但 data 中只有 allRequirements，没有 projects
- [ ] L27 检查 req.status === 'draft'，但后端返回的状态字段名是否一致？
- [ ] L35 的 data-req-index 和 data-node-index，但在 onFlowNodeTap 中使用的是 reqIndex 和 nodeIndex

### 4.2 onFlowNodeTap 事件处理审查
**位置**: index.js L702-L731

```javascript
onFlowNodeTap: function (e) {
  const { reqIndex, nodeIndex } = e.currentTarget.dataset;
  // ...
}
```

**问题清单**:
- [ ] e.currentTarget.dataset 获取的是 data-req-index 和 data-node-index
- [ ] 但解构时使用的是 reqIndex 和 nodeIndex，命名不一致
- [ ] 节点跳转逻辑只处理了 nodeIndex 0, 2, 7，其他节点点击无响应
- [ ] 节点 9（客户验收）也是验收节点，但未被处理

### 4.3 scroll-into-view 逻辑审查
**位置**: index.js L422-L442, index.wxml L32

**问题清单**:
- [ ] scrollIntoViewId 格式为 `node-${reqIndex}-${activeNodeIndex}`
- [ ] 但 index.wxml L32 使用的是 `{{req.scrollIntoViewId}}`
- [ ] index.wxml L35 的 id 是 `node-{{index}}-{{nodeIndex}}`
- [ ] 这种绑定方式是否能正确触发滚动？

---

## 五、状态一致性问题

### 5.1 多项目状态管理
**位置**: index.js L403-L461 loadCurrentRequirement

**问题清单**:
- [ ] allRequirements 数组中每个项目都有自己的 flowNodes
- [ ] 但 currentRequirement 只保存第一个项目
- [ ] currentStep 也只保存第一个项目的步骤
- [ ] 当切换项目时，这些状态如何同步？

### 5.2 自动触发 AI 逻辑
**位置**: index.js L122-L140 checkAutoTriggerAI

**问题清单**:
- [ ] 使用 wx.getStorageSync 获取 auto_trigger_ai
- [ ] 5分钟过期检查使用 Date.now() - autoTrigger.timestamp
- [ ] 如果存储的数据结构变化，是否会报错？
- [ ] executeAINode 调用时传入 autoTrigger.requirementId 和 autoTrigger.nodeIndex
- [ ] 但这些值是否经过验证？

---

## 六、代码风格与最佳实践

### 6.1 重复代码
**问题清单**:
- [ ] L159, L278, L367 都 require 了 api.js
- [ ] L562-L577 和 L602-L634 有相似的 deals 数据结构
- [ ] 多处使用 console.log 输出调试信息

### 6.2 错误处理
**问题清单**:
- [ ] L84 catch 块只打印错误，没有用户提示
- [ ] L205-218 重试逻辑在达到最大次数后只标记失败，没有通知用户
- [ ] L451-460 catch 块设置空数据，但没有错误提示

### 6.3 硬编码值
**问题清单**:
- [ ] L144-L150 的 timeout 值硬编码
- [ ] L186 的 storage key `approval_data_${requirementId}` 硬编码
- [ ] L709, L721 的页面路径硬编码

---

## 七、审查优先级

### P0 - 严重问题（必须修复）
1. wxml L6 使用未定义的 projects 变量
2. onFlowNodeTap 中 dataset 命名与 wxml 不一致
3. executeAINode 和 executeAINodeWithRetry 执行顺序不一致

### P1 - 重要问题（建议修复）
1. allFlowNodes 中多个"客户验收"节点标题重复
2. clickHints 配置逻辑可能不正确
3. 多处重复 require api.js
4. buildAIRequestData 中大量硬编码值

### P2 - 一般问题（可选优化）
1. 错误处理不完善
2. 调试日志过多
3. 魔法数字未提取为常量

---

## 八、修复建议汇总

### 立即修复
1. 修复 wxml L6: 将 `projects.length` 改为 `allRequirements.length`
2. 修复 onFlowNodeTap: 统一 dataset 命名
3. 统一 executeAINode 和 executeAINodeWithRetry 的执行顺序

### 建议修复
1. 为重复的"客户验收"节点添加副标题或阶段标识
2. 审查 clickHints 配置逻辑
3. 将 api.js 的导入统一到文件顶部
4. 审查 buildAIRequestData 的硬编码值

### 长期优化
1. 完善错误处理和用户提示
2. 提取常量配置
3. 添加单元测试

---

## 九、审查结论

当前 index 页面的流程节点逻辑存在以下核心问题：

1. **数据模型不一致**: allFlowNodes、aiNodeConfig、getCurrentStep 之间的映射关系需要重新梳理
2. **状态流转混乱**: executeAINode 和 executeAINodeWithRetry 的执行顺序不一致
3. **UI 数据绑定错误**: wxml 中使用了未定义的变量
4. **交互逻辑缺失**: 部分节点点击无响应

建议在修复 P0 级别问题后，重新梳理整个流程节点的状态机设计。
