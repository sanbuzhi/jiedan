# 推荐团队画布展示实现计划

## 目标
将个人中心页面的推荐团队列表（tree-list）替换为使用 Canvas 画布绘制的三级推荐树可视化展示。

## 当前状态
- 使用 WXML 列表展示三级推荐结构
- 数据格式：`referralTree.children` 包含一级推荐，每个节点有 `children` 属性包含下级推荐
- 接口返回数组格式，已转换为 `{children: [...]}` 对象

## 实现方案

### 1. 页面结构改造

#### 1.1 WXML 修改
```xmln
<!-- 推荐关系树 - Canvas 版本 -->
<view class="card tree-card">
  <view class="card-header">
    <text class="card-title">我的推荐团队</text>
    <text class="view-all" bindtap="toggleCanvasView">{{showCanvasView ? '列表视图' : '树状视图'}}</text>
  </view>
  
  <!-- 统计摘要 -->
  <view class="tree-summary">
    <view class="summary-item">
      <text class="summary-value level-1">{{treeStats.level1}}</text>
      <text class="summary-label">一级推荐</text>
    </view>
    <view class="summary-item">
      <text class="summary-value level-2">{{treeStats.level2}}</text>
      <text class="summary-label">二级推荐</text>
    </view>
    <view class="summary-item">
      <text class="summary-value level-3">{{treeStats.level3}}</text>
      <text class="summary-label">三级推荐</text>
    </view>
  </view>

  <!-- Canvas 树状图 -->
  <view class="canvas-container" wx:if="{{showCanvasView}}">
    <canvas 
      type="2d" 
      id="referralTreeCanvas" 
      class="referral-tree-canvas"
      bindtap="onCanvasTap"
      bindtouchstart="onCanvasTouchStart"
      bindtouchmove="onCanvasTouchMove"
      bindtouchend="onCanvasTouchEnd"
    ></canvas>
    <!-- 图例 -->
    <view class="canvas-legend">
      <view class="legend-item">
        <view class="legend-dot level-1"></view>
        <text class="legend-text">一级推荐</text>
      </view>
      <view class="legend-item">
        <view class="legend-dot level-2"></view>
        <text class="legend-text">二级推荐</text>
      </view>
      <view class="legend-item">
        <view class="legend-dot level-3"></view>
        <text class="legend-text">三级推荐</text>
      </view>
    </view>
  </view>

  <!-- 原有列表视图（保留作为备选） -->
  <view class="tree-list" wx:if="{{!showCanvasView && referralTree.children && referralTree.children.length > 0}}">
    <!-- 原有列表代码 -->
  </view>

  <!-- 空状态 -->
  <view class="empty-state" wx:if="{{!referralTree.children || referralTree.children.length === 0}}">
    <text>暂无推荐记录，快去分享您的推荐码吧！</text>
  </view>
</view>
```

#### 1.2 数据结构
```javascript
data: {
  // ... 其他数据
  showCanvasView: true,  // 默认显示画布视图
  canvasScale: 1,        // 画布缩放比例
  canvasOffsetX: 0,      // 画布偏移X
  canvasOffsetY: 0,      // 画布偏移Y
  selectedNode: null,    // 选中的节点
}
```

### 2. Canvas 绘制实现

#### 2.1 初始化画布
```javascript
// 初始化 Canvas
initCanvas: function() {
  const query = wx.createSelectorQuery();
  query.select('#referralTreeCanvas')
    .fields({ node: true, size: true })
    .exec((res) => {
      const canvas = res[0].node;
      const ctx = canvas.getContext('2d');
      
      // 设置画布尺寸（考虑设备像素比）
      const dpr = wx.getSystemInfoSync().pixelRatio;
      canvas.width = res[0].width * dpr;
      canvas.height = res[0].height * dpr;
      ctx.scale(dpr, dpr);
      
      this.canvas = canvas;
      this.ctx = ctx;
      this.canvasWidth = res[0].width;
      this.canvasHeight = res[0].height;
      
      // 绘制推荐树
      this.drawReferralTree();
    });
}
```

#### 2.2 计算树形布局
```javascript
// 计算树形节点位置
calculateTreeLayout: function(treeData) {
  const nodes = [];
  const links = [];
  
  const levelHeight = 120;  // 层级高度
  const nodeWidth = 80;     // 节点宽度
  const nodeHeight = 100;   // 节点高度
  
  // 根节点（当前用户）
  const rootX = this.canvasWidth / 2;
  const rootY = 40;
  
  nodes.push({
    id: 'root',
    x: rootX,
    y: rootY,
    level: 0,
    data: { nickname: '我', avatar: this.data.userInfo.avatar }
  });
  
  // 递归计算子节点位置
  const calculateChildren = (children, parentX, parentY, level) => {
    if (!children || children.length === 0) return;
    
    const totalWidth = children.length * nodeWidth + (children.length - 1) * 20;
    let startX = parentX - totalWidth / 2 + nodeWidth / 2;
    
    children.forEach((child, index) => {
      const x = startX + index * (nodeWidth + 20);
      const y = parentY + levelHeight;
      
      nodes.push({
        id: child.user?.id || `node-${level}-${index}`,
        x: x,
        y: y,
        level: level,
        data: child.user || child,
        parentId: level === 1 ? 'root' : parentX + '-' + parentY
      });
      
      // 添加连接线
      links.push({
        from: { x: parentX, y: parentY + nodeHeight / 2 },
        to: { x: x, y: y - nodeHeight / 2 },
        level: level
      });
      
      // 递归处理子节点
      if (child.children && child.children.length > 0) {
        calculateChildren(child.children, x, y, level + 1);
      }
    });
  };
  
  // 计算一级推荐
  if (treeData.children) {
    calculateChildren(treeData.children, rootX, rootY, 1);
  }
  
  return { nodes, links };
}
```

#### 2.3 绘制函数
```javascript
// 绘制推荐树
drawReferralTree: function() {
  if (!this.ctx || !this.data.referralTree) return;
  
  const ctx = this.ctx;
  const { nodes, links } = this.calculateTreeLayout(this.data.referralTree);
  
  // 清空画布
  ctx.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
  
  // 保存节点位置用于点击检测
  this.treeNodes = nodes;
  
  // 应用缩放和偏移
  ctx.save();
  ctx.translate(this.data.canvasOffsetX, this.data.canvasOffsetY);
  ctx.scale(this.data.canvasScale, this.data.canvasScale);
  
  // 绘制连接线
  this.drawLinks(ctx, links);
  
  // 绘制节点
  nodes.forEach(node => {
    this.drawNode(ctx, node);
  });
  
  ctx.restore();
}

// 绘制连接线
drawLinks: function(ctx, links) {
  links.forEach(link => {
    ctx.beginPath();
    ctx.moveTo(link.from.x, link.from.y);
    
    // 使用贝塞尔曲线绘制平滑连线
    const midY = (link.from.y + link.to.y) / 2;
    ctx.bezierCurveTo(
      link.from.x, midY,
      link.to.x, midY,
      link.to.x, link.to.y
    );
    
    // 根据层级设置颜色
    const colors = ['#07c160', '#2ca9e1', '#fa5151'];
    ctx.strokeStyle = colors[link.level - 1] || '#999';
    ctx.lineWidth = 2;
    ctx.stroke();
  });
}

// 绘制节点
drawNode: function(ctx, node) {
  const { x, y, level, data } = node;
  const nodeWidth = 80;
  const nodeHeight = 100;
  
  // 节点背景
  ctx.beginPath();
  ctx.roundRect(x - nodeWidth/2, y - nodeHeight/2, nodeWidth, nodeHeight, 8);
  
  // 根据层级设置颜色
  const colors = ['#07c160', '#2ca9e1', '#fa5151'];
  ctx.fillStyle = level === 0 ? '#ff9500' : (colors[level - 1] || '#999');
  ctx.fill();
  
  // 节点边框
  ctx.strokeStyle = '#fff';
  ctx.lineWidth = 2;
  ctx.stroke();
  
  // 绘制头像（圆形）
  ctx.beginPath();
  ctx.arc(x, y - 15, 25, 0, Math.PI * 2);
  ctx.fillStyle = '#f0f0f0';
  ctx.fill();
  ctx.strokeStyle = '#fff';
  ctx.lineWidth = 2;
  ctx.stroke();
  
  // 绘制昵称
  ctx.fillStyle = '#fff';
  ctx.font = '12px sans-serif';
  ctx.textAlign = 'center';
  ctx.fillText(data.nickname || '匿名', x, y + 25);
  
  // 绘制层级标签
  if (level > 0) {
    ctx.fillStyle = 'rgba(255,255,255,0.8)';
    ctx.font = '10px sans-serif';
    ctx.fillText(`${level}级推荐`, x, y + 40);
  }
}
```

### 3. 交互功能

#### 3.1 点击检测
```javascript
// Canvas 点击事件
onCanvasTap: function(e) {
  const { x, y } = e.detail;
  
  // 转换坐标（考虑缩放和偏移）
  const scale = this.data.canvasScale;
  const offsetX = this.data.canvasOffsetX;
  const offsetY = this.data.canvasOffsetY;
  
  const canvasX = (x - offsetX) / scale;
  const canvasY = (y - offsetY) / scale;
  
  // 检测点击的节点
  const clickedNode = this.treeNodes.find(node => {
    const dx = canvasX - node.x;
    const dy = canvasY - node.y;
    return Math.sqrt(dx * dx + dy * dy) < 40;
  });
  
  if (clickedNode) {
    this.setData({ selectedNode: clickedNode });
    // 显示节点详情弹窗
    this.showNodeDetail(clickedNode);
  }
}

// 显示节点详情
showNodeDetail: function(node) {
  const { data, level } = node;
  wx.showModal({
    title: `${level}级推荐详情`,
    content: `昵称: ${data.nickname || '匿名'}\n注册时间: ${this.formatDate(data.created_at)}`,
    showCancel: false
  });
}
```

#### 3.2 手势支持（缩放、拖拽）
```javascript
// 触摸开始
onCanvasTouchStart: function(e) {
  if (e.touches.length === 1) {
    // 单指拖拽
    this.touchStartX = e.touches[0].x;
    this.touchStartY = e.touches[0].y;
    this.startOffsetX = this.data.canvasOffsetX;
    this.startOffsetY = this.data.canvasOffsetY;
  } else if (e.touches.length === 2) {
    // 双指缩放
    const x = e.touches[0].x - e.touches[1].x;
    const y = e.touches[0].y - e.touches[1].y;
    this.startDistance = Math.sqrt(x * x + y * y);
    this.startScale = this.data.canvasScale;
  }
},

// 触摸移动
onCanvasTouchMove: function(e) {
  if (e.touches.length === 1) {
    // 拖拽
    const dx = e.touches[0].x - this.touchStartX;
    const dy = e.touches[0].y - this.touchStartY;
    
    this.setData({
      canvasOffsetX: this.startOffsetX + dx,
      canvasOffsetY: this.startOffsetY + dy
    }, () => {
      this.drawReferralTree();
    });
  } else if (e.touches.length === 2) {
    // 缩放
    const x = e.touches[0].x - e.touches[1].x;
    const y = e.touches[0].y - e.touches[1].y;
    const distance = Math.sqrt(x * x + y * y);
    
    const scale = (distance / this.startDistance) * this.startScale;
    this.setData({
      canvasScale: Math.max(0.5, Math.min(2, scale))
    }, () => {
      this.drawReferralTree();
    });
  }
}
```

### 4. 样式设计

#### 4.1 WXSS 样式
```css
/* Canvas 容器 */
.canvas-container {
  position: relative;
  width: 100%;
  height: 500rpx;
  background: linear-gradient(180deg, #f8f9fa 0%, #ffffff 100%);
  border-radius: 16rpx;
  overflow: hidden;
}

.referral-tree-canvas {
  width: 100%;
  height: 100%;
}

/* 图例 */
.canvas-legend {
  position: absolute;
  bottom: 20rpx;
  left: 20rpx;
  display: flex;
  gap: 20rpx;
  background: rgba(255, 255, 255, 0.9);
  padding: 16rpx 24rpx;
  border-radius: 8rpx;
  box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.1);
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8rpx;
}

.legend-dot {
  width: 16rpx;
  height: 16rpx;
  border-radius: 50%;
}

.legend-dot.level-1 { background-color: #07c160; }
.legend-dot.level-2 { background-color: #2ca9e1; }
.legend-dot.level-3 { background-color: #fa5151; }

.legend-text {
  font-size: 22rpx;
  color: #666;
}
```

### 5. 实现步骤

1. **添加 Canvas 组件** - 修改 profile.wxml，添加 canvas 元素和图例
2. **实现绘制逻辑** - 在 profile.js 中添加 initCanvas、drawReferralTree 等方法
3. **添加交互功能** - 实现点击检测、手势缩放拖拽
4. **添加样式** - 完善 canvas 容器和图例样式
5. **测试优化** - 测试不同数据量下的性能和显示效果

### 6. 备选方案

如果 Canvas 实现复杂，可以考虑：
- 使用 ECharts 小程序版本绘制树图
- 使用 D3.js 的树状布局算法
- 保持现有列表，添加缩略树状图预览

## 预期效果
- 以树状结构可视化展示三级推荐关系
- 不同层级使用不同颜色区分（一级绿、二级蓝、三级红）
- 支持手势缩放和拖拽查看
- 点击节点显示详情
- 保留列表视图切换功能
