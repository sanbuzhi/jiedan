# Canvas 树形图布局优化计划

## 问题分析

当前 Canvas 树形图绘制存在以下问题：
1. 节点和连线出现交叉
2. 布局算法没有考虑所有层级的节点数量
3. 画布宽度固定，无法适应大量节点

## 优化方案

### 1. 前端布局算法重构

**核心思路**：采用"自底向上"的布局算法

1. **计算每级最大节点数**
   - 遍历整棵树，统计每一层的节点数量
   - 找出节点数最多的层级

2. **根据最大节点数计算画布宽度**
   - 计算所需最小宽度 = 最大节点数 × (节点直径 + 最小间距)
   - 如果超过屏幕宽度，启用横向滚动或缩放

3. **采用 Reingold-Tilford 树布局算法**
   - 自底向上计算每个子树的宽度
   - 父节点位于子节点的中间位置
   - 兄弟子树之间保持最小间距

### 2. 后端接口优化

**新增接口**：`GET /users/referrals/tree-stats`

返回树形结构的统计信息：
```json
{
  "maxLevel": 3,
  "maxNodesPerLevel": [1, 5, 10, 8],
  "totalNodes": 24,
  "levelStats": {
    "level1": 5,
    "level2": 10,
    "level3": 8
  }
}
```

**优势**：
- 前端无需遍历整棵树即可获取布局所需信息
- 减少前端计算量，提高性能
- 便于提前计算画布尺寸

### 3. 实现步骤

#### 步骤 1：后端接口开发
1. 在 `UserController` 添加 `/referrals/tree-stats` 接口
2. 在 `UserService` 实现树结构统计方法
3. 创建 `ReferralTreeStats` DTO

#### 步骤 2：前端布局算法重构
1. 修改 `calculateTreeLayout` 方法
2. 实现自底向上的布局计算
3. 根据最大节点数动态调整画布宽度
4. 添加横向滚动支持

#### 步骤 3：性能优化
1. 使用虚拟滚动（节点过多时）
2. 优化绘制性能（分层渲染）
3. 添加防抖处理

### 4. 技术细节

#### 布局算法伪代码
```
function calculateLayout(node, level):
    if node is leaf:
        node.width = NODE_WIDTH
        return
    
    for child in node.children:
        calculateLayout(child, level + 1)
    
    // 计算子树总宽度
    totalChildrenWidth = sum(child.width for child in node.children)
    spacing = (len(node.children) - 1) * MIN_SPACING
    node.width = max(NODE_WIDTH, totalChildrenWidth + spacing)
    
    // 计算子节点位置
    currentX = node.x - node.width / 2
    for child in node.children:
        child.x = currentX + child.width / 2
        currentX += child.width + MIN_SPACING
```

#### 画布宽度计算
```javascript
const NODE_WIDTH = 64;  // 节点直径
const MIN_SPACING = 40; // 最小间距
const PADDING = 40;     // 边距

// 计算所需画布宽度
const maxNodesInLevel = Math.max(...nodesPerLevel);
const requiredWidth = maxNodesInLevel * NODE_WIDTH + 
                      (maxNodesInLevel - 1) * MIN_SPACING + 
                      PADDING * 2;

// 如果超过屏幕宽度，启用滚动
if (requiredWidth > screenWidth) {
    canvasWidth = requiredWidth;
    enableHorizontalScroll = true;
} else {
    canvasWidth = screenWidth;
    enableHorizontalScroll = false;
}
```

### 5. 文件修改清单

#### 后端
- [ ] `UserController.java` - 添加 tree-stats 接口
- [ ] `UserService.java` - 添加树统计方法
- [ ] `ReferralTreeStats.java` - 新建 DTO

#### 前端
- [ ] `profile.js` - 重构 calculateTreeLayout
- [ ] `profile.wxml` - 添加滚动容器
- [ ] `profile.wxss` - 添加滚动样式

### 6. 预期效果

1. **无交叉**：所有节点和连线清晰分离
2. **自适应**：根据节点数量自动调整布局
3. **高性能**：优化后的算法时间复杂度 O(n)
4. **可扩展**：支持大量节点（100+）
