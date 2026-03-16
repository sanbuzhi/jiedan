const api = require('../../utils/api.js');
const util = require('../../utils/util.js');

Page({
  data: {
    userInfo: {},
    cachedAvatarPath: null,
    referralTree: {},
    pointRecords: [],
    referralCount: 0,
    isExpanded: false,
    treeStats: {
      level1: 0,
      level2: 0,
      level3: 0
    },
    showShareRulesModal: false,
    showQRCodeModal: false,
    qrCodeUrl: '',
    // Canvas 相关数据
    showCanvasView: true,
    canvasScale: 1,
    canvasOffsetX: 0,
    canvasOffsetY: 0,
    canvasWidth: 375,  // 默认画布宽度
    canvasHeight: 300, // 默认画布高度
    // 编辑资料弹窗相关
    showEditModal: false,
    editNickname: '',
    editAvatar: '',
    isSaving: false
  },

  // Canvas 相关变量
  canvas: null,
  ctx: null,
  canvasWidth: 0,
  canvasHeight: 0,
  treeNodes: [],
  touchStartX: 0,
  touchStartY: 0,
  startOffsetX: 0,
  startOffsetY: 0,
  startDistance: 0,
  startScale: 1,
  
  // 性能优化相关
  layoutCache: null,        // 布局缓存
  drawRequestId: null,      // 绘制请求ID
  isDrawing: false,         // 是否正在绘制
  lastDrawTime: 0,          // 上次绘制时间
  DEBOUNCE_DELAY: 16,       // 防抖延迟(约60fps)
  
  // 头像图片缓存
  avatarImageCache: {},      // { avatarUrl: canvasImage对象 }

  onLoad: function (options) {
    this.checkLogin();
  },

  onShow: function () {
    if (util.checkLogin()) {
      this.loadUserInfo();
      this.loadReferralTree();
      this.loadPointRecords();
    }
  },

  onPullDownRefresh: function () {
    this.loadUserInfo();
    this.loadReferralTree();
    this.loadPointRecords().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 检查登录状态
  checkLogin: function () {
    if (!util.checkLogin()) {
      wx.redirectTo({
        url: '/pages/login/login'
      });
      return false;
    }
    return true;
  },

  // 加载用户信息
  loadUserInfo: function () {
    api.userApi.getMe()
      .then(res => {
        console.log('用户信息接口返回:', res);
        // 确保 avatar 是完整 URL
        const userInfoWithFullAvatar = { ...res };
        if (userInfoWithFullAvatar.avatar) {
          userInfoWithFullAvatar.avatar = util.getFullAvatarUrl(userInfoWithFullAvatar.avatar);
        }
        this.setData({
          userInfo: userInfoWithFullAvatar
        }, () => {
          console.log('userInfo已更新:', this.data.userInfo);
          // 加载本地缓存头像
          this.loadCachedAvatar();
        });
      })
      .catch(err => {
        console.error('获取用户信息失败:', err);
      });
  },

  // 加载本地缓存头像
  loadCachedAvatar: function () {
    const serverAvatarUrl = this.data.userInfo.avatar;
    const cachedAvatarUrl = wx.getStorageSync('cachedAvatar_url');
    const cachedPath = util.getCachedAvatar();
    
    // 检查头像是否已变更（多设备同步检测）
    if (serverAvatarUrl && cachedAvatarUrl && serverAvatarUrl !== cachedAvatarUrl) {
      console.log('检测到头像已变更，重新下载:', serverAvatarUrl);
      this.downloadAndCacheAvatarFromServer();
      return;
    }
    
    if (cachedPath) {
      // 检查文件是否存在
      wx.getFileSystemManager().access({
        path: cachedPath,
        success: () => {
          console.log('使用本地缓存头像:', cachedPath);
          this.setData({
            cachedAvatarPath: cachedPath
          });
        },
        fail: () => {
          // 缓存文件不存在，从服务器下载
          this.downloadAndCacheAvatarFromServer();
        }
      });
    } else {
      // 没有缓存，从服务器下载
      this.downloadAndCacheAvatarFromServer();
    }
  },

  // 从服务器下载并缓存头像
  downloadAndCacheAvatarFromServer: function () {
    if (this.data.userInfo.avatar) {
      util.downloadAndCacheAvatar(this.data.userInfo.avatar)
        .then(cachedPath => {
          console.log('头像下载并缓存成功:', cachedPath);
          this.setData({
            cachedAvatarPath: cachedPath
          });
        })
        .catch(err => {
          console.error('头像下载失败:', err);
        });
    }
  },

  // 加载推荐关系树
  loadReferralTree: function () {
    // 如果已有缓存数据且未过期（5分钟内），直接使用缓存
    if (this._referralTreeCache && this._referralTreeCache.data) {
      const cacheAge = Date.now() - this._referralTreeCache.timestamp;
      if (cacheAge < 5 * 60 * 1000) { // 5分钟缓存
        console.log('使用缓存的推荐树数据');
        this._processTreeData(this._referralTreeCache.data);
        return Promise.resolve();
      }
    }
    
    return api.userApi.getReferralTree()
      .then(res => {
        console.log('推荐树接口返回:', res);
        
        // 缓存数据
        this._referralTreeCache = {
          data: res,
          timestamp: Date.now()
        };
        
        this._processTreeData(res);
      })
      .catch(err => {
        console.error('获取推荐树失败:', err);
      });
  },
  
  // 处理树数据
  _processTreeData: function (res) {
    // 将数组包装成对象格式
    const treeData = Array.isArray(res) ? { children: res } : res;
    
    // 确保根节点（当前用户）头像与个人中心一致
    if (treeData.user) {
      // 优先使用个人中心的头像（确保一致性）
      if (this.data.userInfo.avatar) {
        treeData.user.avatar = this.data.userInfo.avatar;
      } else if (treeData.user.avatar) {
        treeData.user.avatar = util.getFullAvatarUrl(treeData.user.avatar);
      }
    }
    
    // 初始化展开状态（一次性处理）
    if (treeData.children) {
      treeData.children.forEach(child => {
        child.expanded = false;
        // 确保头像URL完整
        if (child.user && child.user.avatar) {
          child.user.avatar = util.getFullAvatarUrl(child.user.avatar);
        }
        if (child.children) {
          child.children.forEach(grandchild => {
            grandchild.expanded = false;
            // 确保头像URL完整
            if (grandchild.user && grandchild.user.avatar) {
              grandchild.user.avatar = util.getFullAvatarUrl(grandchild.user.avatar);
            }
            if (grandchild.children) {
              grandchild.children.forEach(greatgrandchild => {
                // 确保头像URL完整
                if (greatgrandchild.user && greatgrandchild.user.avatar) {
                  greatgrandchild.user.avatar = util.getFullAvatarUrl(greatgrandchild.user.avatar);
                }
              });
            }
          });
        }
      });
    }

    // 批量更新数据
    const stats = this._calculateTreeStatsFast(treeData);
    
    this.setData({
      referralTree: treeData,
      treeStats: stats,
      referralCount: stats.level1 + stats.level2 + stats.level3
    }, () => {
      // 清除布局缓存（因为树数据已更新）
      this.layoutCache = null;
      
      // 初始化 Canvas
      if (this.data.showCanvasView && treeData.children && treeData.children.length > 0) {
        this.initCanvas();
      }
    });
  },
  
  // 快速计算树统计（不遍历多次）
  _calculateTreeStatsFast: function (tree) {
    let level1 = 0, level2 = 0, level3 = 0;

    if (tree.children) {
      level1 = tree.children.length;
      tree.children.forEach(child => {
        if (child.children) {
          level2 += child.children.length;
          child.children.forEach(grandchild => {
            if (grandchild.children) {
              level3 += grandchild.children.length;
            }
          });
        }
      });
    }

    return { level1, level2, level3 };
  },

  // ============================================
  // Canvas 画布方法
  // ============================================

  // 切换画布/列表视图
  toggleCanvasView: function () {
    const newValue = !this.data.showCanvasView;
    this.setData({
      showCanvasView: newValue,
      canvasScale: 1,
      canvasOffsetX: 0,
      canvasOffsetY: 0
    }, () => {
      if (newValue && this.data.referralTree.children && this.data.referralTree.children.length > 0) {
        setTimeout(() => {
          this.initCanvas();
        }, 300);
      }
    });
  },

  // 初始化 Canvas（带智能初始缩放）
  initCanvas: function () {
    const screenWidth = wx.getSystemInfoSync().windowWidth;
    const screenHeight = 400; // 固定高度
    
    // 1. 计算最宽层级的节点数 N
    const maxNodes = this._getMaxNodesPerLevel(this.data.referralTree);
    
    // 2. 计算所需最小宽度 = N × 节点直径 + (N-1) × 间距 + 边距
    const nodeDiameter = 64; // 32 * 2
    const minNodeSpacing = 60;
    const padding = 40;
    const requiredWidth = maxNodes * nodeDiameter + (maxNodes - 1) * minNodeSpacing + padding * 2;
    
    // 3 & 4. 根据所需宽度决定画布宽度和初始缩放
    let canvasWidth, initialScale;
    
    if (requiredWidth > screenWidth) {
      // 所需宽度 > 屏幕宽度：扩展画布，缩小显示全部
      canvasWidth = requiredWidth;
      initialScale = screenWidth / requiredWidth * 0.95; // 留 5% 边距
    } else {
      // 所需宽度 ≤ 屏幕宽度：使用屏幕宽度，保持 1:1 或适当放大
      canvasWidth = screenWidth;
      // 节点少时适当放大，但不超过 1.2 倍
      initialScale = Math.min(1.2, Math.max(1, 5 / maxNodes));
    }
    
    // 确保缩放比例在合理范围内
    initialScale = Math.max(0.3, Math.min(1.2, initialScale));
    
    this.setData({
      canvasWidth: canvasWidth,
      canvasHeight: screenHeight,
      canvasScale: initialScale,
      canvasOffsetX: 0,
      canvasOffsetY: 0
    }, () => {
      // 延迟执行以确保 DOM 更新
      setTimeout(() => {
        const query = wx.createSelectorQuery();
        query.select('#referralTreeCanvas')
          .fields({ node: true, size: true })
          .exec((res) => {
            if (!res[0]) {
              console.warn('Canvas 元素未找到');
              return;
            }
            
            const canvas = res[0].node;
            const ctx = canvas.getContext('2d');
            
            // 设置画布尺寸（考虑设备像素比）
            const dpr = wx.getSystemInfoSync().pixelRatio;
            canvas.width = canvasWidth * dpr;
            canvas.height = screenHeight * dpr;
            ctx.scale(dpr, dpr);
            
            this.canvas = canvas;
            this.ctx = ctx;
            this.canvasWidth = canvasWidth;
            this.canvasHeight = screenHeight;
            
            // 绘制推荐树
            this.drawReferralTree();
          });
      }, 100);
    });
  },
  
  // 获取最宽层级的节点数（利用已有的 treeStats 数据，避免重复计算）
  _getMaxNodesPerLevel: function (treeData) {
    // 优先使用已有的统计信息
    const stats = this.data.treeStats;
    if (stats && (stats.level1 > 0 || stats.level2 > 0 || stats.level3 > 0)) {
      // level0=1(根), level1, level2, level3
      return Math.max(1, stats.level1, stats.level2, stats.level3);
    }
    
    // 如果没有统计信息，快速计算
    let maxNodes = 1; // 至少根节点
    
    if (treeData && treeData.children) {
      // 一级节点数
      maxNodes = Math.max(maxNodes, treeData.children.length);
      
      let level2Count = 0;
      let level3Count = 0;
      
      treeData.children.forEach(level1 => {
        if (level1.children) {
          level2Count += level1.children.length;
          
          level1.children.forEach(level2 => {
            if (level2.children) {
              level3Count += level2.children.length;
            }
          });
        }
      });
      
      maxNodes = Math.max(maxNodes, level2Count, level3Count);
    }
    
    return maxNodes;
  },

  // 计算树形节点位置 - 采用自底向上的 Reingold-Tilford 算法
  calculateTreeLayout: function (treeData) {
    const nodes = [];
    const links = [];
    
    const levelHeight = 120;  // 层级高度
    const nodeRadius = 32;    // 节点半径
    const minNodeSpacing = 60; // 最小间距（增加以避免交叉）
    
    // 第一步：自底向上计算每个子树的宽度
    const calculateSubtreeWidth = (node) => {
      if (!node.children || node.children.length === 0) {
        // 叶子节点
        node._subtreeWidth = nodeRadius * 2;
        node._leafCount = 1;
        return;
      }
      
      // 递归计算子节点
      let childrenWidth = 0;
      let leafCount = 0;
      node.children.forEach((child, index) => {
        calculateSubtreeWidth(child);
        childrenWidth += child._subtreeWidth;
        leafCount += child._leafCount;
        if (index < node.children.length - 1) {
          childrenWidth += minNodeSpacing; // 子树之间的间距
        }
      });
      
      // 当前节点的子树宽度 = max(节点自身宽度, 子节点总宽度)
      node._subtreeWidth = Math.max(nodeRadius * 2, childrenWidth);
      node._leafCount = leafCount;
    };
    
    // 第二步：自顶向下计算每个节点的位置
    const calculateNodePosition = (node, x, y, parentX, parentY) => {
      // 设置当前节点位置
      node._x = x;
      node._y = y;
      
      // 添加到节点数组
      nodes.push({
        id: node.user?.id || node.user?.userId || `node-${y}`,
        x: x,
        y: y,
        level: node.level || 0,
        data: node.user || node,
        subtreeWidth: node._subtreeWidth
      });
       
      // 添加连接线
      if (parentX !== null && parentY !== null) {
        links.push({
          from: { x: parentX, y: parentY + nodeRadius },
          to: { x: x, y: y - nodeRadius },
          level: node.level || 1
        });
      }
      
      // 计算子节点位置
      if (node.children && node.children.length > 0) {
        let currentX = x - node._subtreeWidth / 2;
        
        node.children.forEach((child, index) => {
          // 子节点在其子树的中心位置
          const childX = currentX + child._subtreeWidth / 2;
          const childY = y + levelHeight;
          
          calculateNodePosition(child, childX, childY, x, y);
          
          // 移动到下一个子树的位置
          currentX += child._subtreeWidth + minNodeSpacing;
        });
      }
    };
    
    // 执行布局计算
    if (treeData) {
      // 先计算子树宽度（自底向上）
      calculateSubtreeWidth(treeData);
      
      // 再计算节点位置（自顶向下）
      const rootX = this.canvasWidth / 2;
      const rootY = 50;
      calculateNodePosition(treeData, rootX, rootY, null, null);
    }
    
    return { nodes, links };
  },

  // 绘制推荐树（带防抖和缓存）
  drawReferralTree: function () {
    if (!this.ctx || !this.data.referralTree) return;
    
    // 取消之前的绘制请求
    if (this.drawRequestId) {
      clearTimeout(this.drawRequestId);
    }
    
    // 使用 setTimeout 模拟 requestAnimationFrame（微信小程序不支持 RAF）
    this.drawRequestId = setTimeout(() => {
      this._doDrawTree();
    }, 0);
  },
  
  // 实际绘制方法
  _doDrawTree: function () {
    if (this.isDrawing) return; // 防止重复绘制
    this.isDrawing = true;
    
    const startTime = Date.now();
    const ctx = this.ctx;
    
    // 使用缓存的布局数据（如果树数据未变化）
    let layoutData;
    if (this.layoutCache && this.layoutCache.treeData === this.data.referralTree) {
      layoutData = this.layoutCache;
    } else {
      layoutData = this.calculateTreeLayout(this.data.referralTree);
      // 缓存布局数据
      this.layoutCache = {
        treeData: this.data.referralTree,
        nodes: layoutData.nodes,
        links: layoutData.links
      };
    }
    
    const { nodes, links } = layoutData;
    
    // 清空画布
    ctx.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
    
    // 保存节点位置用于点击检测
    this.treeNodes = nodes;
    
    // 应用缩放和偏移
    ctx.save();
    ctx.translate(this.data.canvasOffsetX, this.data.canvasOffsetY);
    ctx.scale(this.data.canvasScale, this.data.canvasScale);
    
    // 批量绘制连接线
    this._batchDrawLinks(ctx, links);
    
    // 批量绘制节点
    this._batchDrawNodes(ctx, nodes);
    
    ctx.restore();
    
    this.isDrawing = false;
    this.lastDrawTime = Date.now() - startTime;
    
    // 性能日志（开发环境可开启）
    // console.log(`绘制耗时: ${this.lastDrawTime}ms, 节点数: ${nodes.length}`);
  },
  
  // 批量绘制连接线（减少状态切换）
  _batchDrawLinks: function (ctx, links) {
    // 按层级分组绘制，减少样式切换
    const linksByLevel = [[], [], []]; // level 1, 2, 3
    links.forEach(link => {
      const level = link.level - 1;
      if (linksByLevel[level]) {
        linksByLevel[level].push(link);
      }
    });
    
    const colors = ['#07c160', '#2ca9e1', '#fa5151'];
    
    linksByLevel.forEach((levelLinks, index) => {
      if (levelLinks.length === 0) return;
      
      ctx.beginPath();
      ctx.strokeStyle = colors[index] || '#999';
      ctx.lineWidth = 2;
      
      levelLinks.forEach(link => {
        ctx.moveTo(link.from.x, link.from.y);
        const midY = (link.from.y + link.to.y) / 2;
        ctx.bezierCurveTo(
          link.from.x, midY,
          link.to.x, midY,
          link.to.x, link.to.y
        );
      });
      
      ctx.stroke();
    });
  },
  
  // 批量绘制节点（减少状态切换）
  _batchDrawNodes: function (ctx, nodes) {
    // 按层级分组
    const nodesByLevel = [[], [], [], []]; // level 0, 1, 2, 3
    nodes.forEach(node => {
      const level = node.level;
      if (nodesByLevel[level]) {
        nodesByLevel[level].push(node);
      }
    });
    
    // 从底层到顶层绘制
    for (let level = 3; level >= 0; level--) {
      nodesByLevel[level].forEach(node => {
        this._drawNodeOptimized(ctx, node);
      });
    }
  },
  
  // 优化的节点绘制方法
  _drawNodeOptimized: function (ctx, node) {
    const { x, y, level, data } = node;
    const avatarRadius = 28;
    const colors = ['#07c160', '#2ca9e1', '#fa5151'];
    const nodeColor = level === 0 ? '#ff9500' : (colors[level - 1] || '#999');
    const nickname = (data.nickname || '匿名');
    
    // 绘制外圈光环
    ctx.beginPath();
    ctx.arc(x, y, avatarRadius + 4, 0, Math.PI * 2);
    ctx.fillStyle = nodeColor;
    ctx.fill();
    
    // 绘制头像背景
    ctx.beginPath();
    ctx.arc(x, y, avatarRadius, 0, Math.PI * 2);
    ctx.fillStyle = '#f5f5f5';
    ctx.fill();
    
    // 绘制头像边框
    ctx.beginPath();
    ctx.arc(x, y, avatarRadius, 0, Math.PI * 2);
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 3;
    ctx.stroke();
    
    // 尝试绘制头像
    // 根节点（当前用户）优先使用本地缓存头像
    let avatarUrl = data.avatar;
    if (level === 0 && this.data.cachedAvatarPath) {
      // 根节点使用本地缓存路径（如果可用）
      avatarUrl = this.data.cachedAvatarPath;
    }
    
    let avatarDrawn = false;
    
    if (avatarUrl) {
      const cachedImage = this.avatarImageCache[avatarUrl];
      if (cachedImage && cachedImage.loaded && cachedImage.image) {
        // 头像已加载，裁剪圆形并绘制
        ctx.save();
        ctx.beginPath();
        ctx.arc(x, y, avatarRadius - 2, 0, Math.PI * 2);
        ctx.clip();
        ctx.drawImage(cachedImage.image, x - avatarRadius + 2, y - avatarRadius + 2, (avatarRadius - 2) * 2, (avatarRadius - 2) * 2);
        ctx.restore();
        avatarDrawn = true;
      } else if (!cachedImage) {
        // 头像未加载，开始加载
        this._loadAvatarImage(avatarUrl);
      }
    }
    
    // 如果没绘制头像，显示首字母
    if (!avatarDrawn) {
      ctx.fillStyle = nodeColor;
      ctx.font = 'bold 16px sans-serif';
      ctx.textAlign = 'center';
      ctx.textBaseline = 'middle';
      const initial = nickname.charAt(0).toUpperCase();
      ctx.fillText(initial, x, y);
    }
    
    // 绘制昵称
    ctx.fillStyle = '#333';
    ctx.font = '11px sans-serif';
    ctx.textBaseline = 'alphabetic';
    const displayName = nickname.length > 4 ? nickname.substring(0, 4) + '...' : nickname;
    ctx.fillText(displayName, x, y + avatarRadius + 18);
    
    // 绘制层级标签
    const badgeX = x + avatarRadius - 5;
    const badgeY = y + avatarRadius - 5;
    
    ctx.beginPath();
    ctx.arc(badgeX, badgeY, 10, 0, Math.PI * 2);
    ctx.fillStyle = level === 0 ? '#ff9500' : nodeColor;
    ctx.fill();
    
    ctx.fillStyle = '#fff';
    ctx.font = 'bold 9px sans-serif';
    ctx.textBaseline = 'middle';
    ctx.fillText(level === 0 ? '我' : `${level}`, badgeX, badgeY);
  },
  
  // 加载头像图片
  _loadAvatarImage: function (avatarUrl) {
    if (this.avatarImageCache[avatarUrl] || !this.canvas) {
      return;
    }
    
    // 标记开始加载
    this.avatarImageCache[avatarUrl] = { loaded: false };
    
    try {
      const img = this.canvas.createImage();
      img.onload = () => {
        this.avatarImageCache[avatarUrl] = { loaded: true, image: img };
        // 重新绘制树
        this.drawReferralTree();
      };
      img.onerror = () => {
        console.error('头像加载失败:', avatarUrl);
        this.avatarImageCache[avatarUrl] = { loaded: false, error: true };
      };
      
      // 处理本地文件路径 (wxfile://)
      if (avatarUrl.startsWith('wxfile://')) {
        // 本地文件直接使用
        img.src = avatarUrl;
      } else {
        // 网络图片直接使用
        img.src = avatarUrl;
      }
    } catch (e) {
      console.error('创建头像图片对象失败:', e);
      this.avatarImageCache[avatarUrl] = { loaded: false, error: true };
    }
  },

  // 绘制连接线
  drawLinks: function (ctx, links) {
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
  },

  // 绘制圆角矩形（兼容小程序）
  drawRoundRect: function (ctx, x, y, width, height, radius) {
    ctx.beginPath();
    ctx.moveTo(x + radius, y);
    ctx.lineTo(x + width - radius, y);
    ctx.quadraticCurveTo(x + width, y, x + width, y + radius);
    ctx.lineTo(x + width, y + height - radius);
    ctx.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
    ctx.lineTo(x + radius, y + height);
    ctx.quadraticCurveTo(x, y + height, x, y + height - radius);
    ctx.lineTo(x, y + radius);
    ctx.quadraticCurveTo(x, y, x + radius, y);
    ctx.closePath();
  },

  // 绘制节点
  drawNode: function (ctx, node) {
    const { x, y, level, data } = node;
    const avatarRadius = 28; // 头像半径
    
    // 根据层级设置颜色
    const colors = ['#07c160', '#2ca9e1', '#fa5151'];
    const nodeColor = level === 0 ? '#ff9500' : (colors[level - 1] || '#999');
    
    // 绘制外圈光环
    ctx.beginPath();
    ctx.arc(x, y, avatarRadius + 4, 0, Math.PI * 2);
    ctx.fillStyle = nodeColor;
    ctx.fill();
    
    // 绘制头像背景（圆形）
    ctx.beginPath();
    ctx.arc(x, y, avatarRadius, 0, Math.PI * 2);
    ctx.fillStyle = '#f5f5f5';
    ctx.fill();
    
    // 绘制头像边框
    ctx.beginPath();
    ctx.arc(x, y, avatarRadius, 0, Math.PI * 2);
    ctx.strokeStyle = '#fff';
    ctx.lineWidth = 3;
    ctx.stroke();
    
    // 绘制首字母或昵称（在头像内）
    ctx.fillStyle = nodeColor;
    ctx.font = 'bold 16px sans-serif';
    ctx.textAlign = 'center';
    ctx.textBaseline = 'middle';
    const nickname = (data.nickname || '匿名');
    const initial = nickname.charAt(0).toUpperCase();
    ctx.fillText(initial, x, y);
    
    // 绘制昵称（头像下方）
    ctx.fillStyle = '#333';
    ctx.font = '11px sans-serif';
    ctx.textBaseline = 'alphabetic';
    const displayName = nickname.length > 4 ? nickname.substring(0, 4) + '...' : nickname;
    ctx.fillText(displayName, x, y + avatarRadius + 18);
    
    // 绘制层级标签（小圆点）
    if (level > 0) {
      // 在头像右下角绘制层级标识
      const badgeX = x + avatarRadius - 5;
      const badgeY = y + avatarRadius - 5;
      
      ctx.beginPath();
      ctx.arc(badgeX, badgeY, 10, 0, Math.PI * 2);
      ctx.fillStyle = nodeColor;
      ctx.fill();
      
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 9px sans-serif';
      ctx.textBaseline = 'middle';
      ctx.fillText(`${level}`, badgeX, badgeY);
    }
    
    // 如果是根节点，添加"我"标识
    if (level === 0) {
      const badgeX = x + avatarRadius - 5;
      const badgeY = y + avatarRadius - 5;
      
      ctx.beginPath();
      ctx.arc(badgeX, badgeY, 10, 0, Math.PI * 2);
      ctx.fillStyle = '#ff9500';
      ctx.fill();
      
      ctx.fillStyle = '#fff';
      ctx.font = 'bold 8px sans-serif';
      ctx.textBaseline = 'middle';
      ctx.fillText('我', badgeX, badgeY);
    }
  },

  // ============================================
  // Canvas 交互方法（高性能平滑版）
  // ============================================

  // 触摸开始
  onCanvasTouchStart: function (e) {
    // 停止正在进行的惯性滑动
    if (this._inertiaAnimationId) {
      cancelAnimationFrame(this._inertiaAnimationId);
      this._inertiaAnimationId = null;
    }
    
    if (e.touches.length === 1) {
      // 单指拖拽
      this.touchStartX = e.touches[0].x;
      this.touchStartY = e.touches[0].y;
      this.startOffsetX = this.data.canvasOffsetX;
      this.startOffsetY = this.data.canvasOffsetY;
      
      // 记录最后几个移动点用于计算惯性
      this._moveHistory = [];
      this._lastMoveTime = Date.now();
    } else if (e.touches.length === 2) {
      // 双指缩放
      const x = e.touches[0].x - e.touches[1].x;
      const y = e.touches[0].y - e.touches[1].y;
      this.startDistance = Math.sqrt(x * x + y * y);
      this.startScale = this.data.canvasScale;
      
      // 记录双指中心点和偏移量
      this.startCenterX = (e.touches[0].x + e.touches[1].x) / 2;
      this.startCenterY = (e.touches[0].y + e.touches[1].y) / 2;
      this.startOffsetX = this.data.canvasOffsetX;
      this.startOffsetY = this.data.canvasOffsetY;
    }
  },

  // 触摸移动（高性能版）
  onCanvasTouchMove: function (e) {
    const now = Date.now();
    
    // 节流：每 16ms 最多执行一次（约 60fps）
    if (this._lastTouchMoveTime && now - this._lastTouchMoveTime < 16) {
      return;
    }
    this._lastTouchMoveTime = now;
    
    if (e.touches.length === 1) {
      // 拖拽
      const dx = e.touches[0].x - this.touchStartX;
      const dy = e.touches[0].y - this.touchStartY;
      
      const newOffsetX = this.startOffsetX + dx;
      const newOffsetY = this.startOffsetY + dy;
      
      // 记录移动历史用于惯性计算
      this._moveHistory.push({
        x: newOffsetX,
        y: newOffsetY,
        time: now
      });
      if (this._moveHistory.length > 3) {
        this._moveHistory.shift();
      }
      
      // 直接更新数据并绘制，不经过 setData
      this.data.canvasOffsetX = newOffsetX;
      this.data.canvasOffsetY = newOffsetY;
      this._drawImmediate();
    } else if (e.touches.length === 2) {
      // 缩放 - 以双指中心为缩放中心
      const x = e.touches[0].x - e.touches[1].x;
      const y = e.touches[0].y - e.touches[1].y;
      const distance = Math.sqrt(x * x + y * y);
      
      const newScale = Math.max(0.3, Math.min(2, 
        (distance / this.startDistance) * this.startScale
      ));
      
      // 计算当前双指中心
      const centerX = (e.touches[0].x + e.touches[1].x) / 2;
      const centerY = (e.touches[0].y + e.touches[1].y) / 2;
      
      // 计算以双指中心为缩放中心的偏移量
      // 公式：newOffset = center - (center - oldOffset) * (newScale / oldScale)
      const scaleRatio = newScale / this.startScale;
      this.data.canvasOffsetX = centerX - (this.startCenterX - this.startOffsetX) * scaleRatio;
      this.data.canvasOffsetY = centerY - (this.startCenterY - this.startOffsetY) * scaleRatio;
      this.data.canvasScale = newScale;
      
      this._drawImmediate();
    }
  },

  // 立即绘制（高性能，不经过缓存检查）
  _drawImmediate: function () {
    if (!this.ctx || !this.layoutCache) return;
    
    const ctx = this.ctx;
    const { nodes, links } = this.layoutCache;
    
    // 清空画布
    ctx.clearRect(0, 0, this.canvasWidth, this.canvasHeight);
    
    // 应用变换
    ctx.save();
    ctx.translate(this.data.canvasOffsetX, this.data.canvasOffsetY);
    ctx.scale(this.data.canvasScale, this.data.canvasScale);
    
    // 批量绘制
    this._batchDrawLinks(ctx, links);
    this._batchDrawNodes(ctx, nodes);
    
    ctx.restore();
  },

  // 触摸结束
  onCanvasTouchEnd: function (e) {
    // 同步数据到视图（一次性）
    this.setData({
      canvasOffsetX: this.data.canvasOffsetX,
      canvasOffsetY: this.data.canvasOffsetY,
      canvasScale: this.data.canvasScale
    });
    
    // 计算惯性
    if (this._moveHistory && this._moveHistory.length >= 2) {
      const lastPoint = this._moveHistory[this._moveHistory.length - 1];
      const firstPoint = this._moveHistory[0];
      const timeDiff = lastPoint.time - firstPoint.time;
      
      if (timeDiff > 0 && timeDiff < 150) {
        const velocityX = (lastPoint.x - firstPoint.x) / timeDiff * 16;
        const velocityY = (lastPoint.y - firstPoint.y) / timeDiff * 16;
        const speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
        
        if (speed > 3) {
          this._startInertiaSlide(velocityX, velocityY);
        }
      }
    }
    
    this._moveHistory = null;
    this._lastTouchMoveTime = null;
  },
  
  // 惯性滑动
  _startInertiaSlide: function (velocityX, velocityY) {
    const friction = 0.92;
    const minVelocity = 0.3;
    
    const slide = () => {
      velocityX *= friction;
      velocityY *= friction;
      
      this.data.canvasOffsetX += velocityX;
      this.data.canvasOffsetY += velocityY;
      this._drawImmediate();
      
      const speed = Math.sqrt(velocityX * velocityX + velocityY * velocityY);
      if (speed > minVelocity) {
        this._inertiaAnimationId = requestAnimationFrame(slide);
      } else {
        // 惯性结束，同步数据
        this.setData({
          canvasOffsetX: this.data.canvasOffsetX,
          canvasOffsetY: this.data.canvasOffsetY
        });
      }
    };
    
    this._inertiaAnimationId = requestAnimationFrame(slide);
  },

  // 加载积分记录
  loadPointRecords: function () {
    return api.userApi.getPoints(0, 10)
      .then(res => {
        console.log('积分记录接口返回:', res);
        // 处理分页数据，提取 content 数组
        const records = res.content || res;
        this.setData({
          pointRecords: records
        }, () => {
          console.log('pointRecords已更新:', this.data.pointRecords);
        });
      })
      .catch(err => {
        console.error('获取积分记录失败:', err);
      });
  },

  // 计算推荐统计
  calculateTreeStats: function (tree) {
    let level1 = 0, level2 = 0, level3 = 0;

    if (tree.children) {
      level1 = tree.children.length;
      tree.children.forEach(child => {
        if (child.children) {
          level2 += child.children.length;
          child.children.forEach(grandchild => {
            if (grandchild.children) {
              level3 += grandchild.children.length;
            }
          });
        }
      });
    }

    this.setData({
      treeStats: { level1, level2, level3 },
      referralCount: level1 + level2 + level3
    });
  },

  // 切换展开状态
  toggleExpand: function (e) {
    const index = e.currentTarget.dataset.index;
    const key = `referralTree.children[${index}].expanded`;
    const currentValue = this.data.referralTree.children[index].expanded;

    this.setData({
      [key]: !currentValue
    });
  },

  // 切换子节点展开状态
  toggleChildExpand: function (e) {
    const parentIndex = e.currentTarget.dataset.parentIndex;
    const childIndex = e.currentTarget.dataset.childIndex;
    const key = `referralTree.children[${parentIndex}].children[${childIndex}].expanded`;
    const currentValue = this.data.referralTree.children[parentIndex].children[childIndex].expanded;

    this.setData({
      [key]: !currentValue
    });
  },

  // 展开/收起全部
  expandAll: function () {
    const isExpanded = !this.data.isExpanded;
    const tree = this.data.referralTree;

    if (tree.children) {
      tree.children.forEach(child => {
        child.expanded = isExpanded;
        if (child.children) {
          child.children.forEach(grandchild => {
            grandchild.expanded = isExpanded;
          });
        }
      });
    }

    this.setData({
      referralTree: tree,
      isExpanded: isExpanded
    });
  },

  // 复制推荐码
  copyReferralCode: function () {
    const code = this.data.userInfo.referralCode;
    if (code) {
      util.copyToClipboard(code, '推荐码已复制');
    }
  },

  // 分享
  onShare: function () {
    this.showShareRules();
  },

  // 显示分享规则弹窗
  showShareRules: function () {
    this.setData({
      showShareRulesModal: true
    });
  },

  // 隐藏分享规则弹窗
  hideShareRules: function () {
    this.setData({
      showShareRulesModal: false
    });
  },

  // 复制分享链接
  copyShareLink: function () {
    const referralCode = this.data.userInfo.referralCode;
    const sharePath = `https://your-domain.com/pages/index/index?referral_code=${referralCode}`;
    util.copyToClipboard(sharePath, '分享链接已复制');
  },

  // 生成二维码
  generateQRCode: function () {
    this.hideShareRules();
    this.setData({
      showQRCodeModal: true,
      qrCodeUrl: ''
    });

    const referralCode = this.data.userInfo.referralCode;
    const scene = `referral_code=${referralCode}`;

    api.userApi.getQRCode(scene)
      .then(res => {
        this.setData({
          qrCodeUrl: res.qr_code_url
        });
      })
      .catch(err => {
        console.error('生成二维码失败:', err);
        wx.showToast({
          title: '生成二维码失败',
          icon: 'none'
        });
      });
  },

  // 显示二维码预览
  showQRCode: function () {
    this.setData({
      showQRCodeModal: true
    });
  },

  // 隐藏二维码预览
  hideQRCode: function () {
    this.setData({
      showQRCodeModal: false
    });
  },

  // 保存二维码到相册
  saveQRCode: function () {
    const qrCodeUrl = this.data.qrCodeUrl;
    if (!qrCodeUrl) {
      wx.showToast({
        title: '二维码未生成',
        icon: 'none'
      });
      return;
    }

    wx.downloadFile({
      url: qrCodeUrl,
      success: (res) => {
        if (res.statusCode === 200) {
          wx.saveImageToPhotosAlbum({
            filePath: res.tempFilePath,
            success: () => {
              wx.showToast({
                title: '已保存到相册',
                icon: 'success'
              });
            },
            fail: (err) => {
              console.error('保存图片失败:', err);
              if (err.errMsg.includes('auth deny')) {
                wx.showModal({
                  title: '提示',
                  content: '需要授权保存到相册权限',
                  success: (modalRes) => {
                    if (modalRes.confirm) {
                      wx.openSetting();
                    }
                  }
                });
              } else {
                wx.showToast({
                  title: '保存失败',
                  icon: 'none'
                });
              }
            }
          });
        }
      },
      fail: (err) => {
        console.error('下载图片失败:', err);
        wx.showToast({
          title: '下载图片失败',
          icon: 'none'
        });
      }
    });
  },

  // 阻止事件冒泡
  preventBubble: function () {
    // 阻止事件冒泡，防止点击弹窗内容时关闭弹窗
  },

  // 分享给朋友
  onShareAppMessage: function () {
    const referralCode = this.data.userInfo.referralCode;
    return {
      title: '推荐你使用接单助手，智能获客轻松接单！',
      path: `/pages/index/index?referral_code=${referralCode}`,
      imageUrl: '/images/share-cover.png'
    };
  },

  // 分享到朋友圈
  onShareTimeline: function () {
    const referralCode = this.data.userInfo.referralCode;
    return {
      title: '接单助手 - 智能获客，轻松接单',
      query: `referral_code=${referralCode}`,
      imageUrl: '/images/share-cover.png'
    };
  },

  // 查看全部积分
  viewAllPoints: function () {
    wx.navigateTo({
      url: '/pages/points-list/points-list'
    });
  },

  // 跳转到积分兑换页
  navigateToExchange: function () {
    wx.navigateTo({
      url: '/pages/points-exchange/points-exchange'
    });
  },

  // 退出登录
  logout: function () {
    util.showConfirm('确认退出', '退出后需要重新登录').then(confirm => {
      if (confirm) {
        const app = getApp();
        app.clearLoginData();

        wx.showToast({
          title: '已退出登录',
          icon: 'success'
        });

        setTimeout(() => {
          wx.redirectTo({
            url: '/pages/login/login'
          });
        }, 1000);
      }
    });
  },

  // 格式化日期
  formatDate: function (date) {
    return util.formatDate(date, 'yyyy-MM-dd');
  },

  // ============================================
  // 编辑个人资料功能
  // ============================================

  // 点击头像
  onAvatarTap: function () {
    this.showEditModal();
  },

  // 显示编辑弹窗
  showEditModal: function () {
    this.setData({
      showEditModal: true,
      editNickname: this.data.userInfo.nickname || '',
      editAvatar: ''
    });
  },

  // 隐藏编辑弹窗
  hideEditModal: function () {
    this.setData({
      showEditModal: false,
      editNickname: '',
      editAvatar: ''
    });
  },

  // 选择头像
  chooseAvatar: function () {
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const tempFilePath = res.tempFiles[0].tempFilePath;
        this.setData({
          editAvatar: tempFilePath
        });
      },
      fail: (err) => {
        console.error('选择图片失败:', err);
      }
    });
  },

  // 昵称输入
  onNicknameInput: function (e) {
    this.setData({
      editNickname: e.detail.value
    });
  },

  // 保存个人资料
  saveProfile: function () {
    const { editNickname, editAvatar } = this.data;

    // 验证昵称
    if (!editNickname || editNickname.trim().length < 2) {
      wx.showToast({
        title: '昵称至少需要2个字符',
        icon: 'none'
      });
      return;
    }

    this.setData({ isSaving: true });

    // 如果有新头像，先上传头像
    if (editAvatar) {
      this.uploadAvatarAndSave(editAvatar, editNickname);
    } else {
      // 只更新昵称
      this.updateNicknameOnly(editNickname);
    }
  },

  // 上传头像并保存
  uploadAvatarAndSave: function (avatarPath, nickname) {
    api.userApi.uploadAvatar(avatarPath)
      .then(res => {
        // 头像上传成功，先更新本地缓存
        return util.updateCachedAvatar(avatarPath)
          .then(cachedPath => {
            console.log('本地缓存头像已更新:', cachedPath);
            this.setData({
              cachedAvatarPath: cachedPath
            });
            
            // 使用后端返回的完整用户信息更新
            if (res.user) {
              // 确保 avatar 是完整 URL
              const userInfoWithFullAvatar = { ...res.user };
              if (userInfoWithFullAvatar.avatar) {
                userInfoWithFullAvatar.avatar = util.getFullAvatarUrl(userInfoWithFullAvatar.avatar);
              }
              const app = getApp();
              app.setLoginData(app.globalData.token, userInfoWithFullAvatar);
              this.setData({
                userInfo: userInfoWithFullAvatar
              });
            }
            
            // 再更新昵称（如果有修改）
            if (nickname && nickname !== this.data.userInfo.nickname) {
              return this.updateNickname(nickname);
            }
            return Promise.resolve();
          });
      })
      .then(() => {
        this.setData({ isSaving: false });
        this.hideEditModal();
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        });
      })
      .catch(err => {
        this.setData({ isSaving: false });
        console.error('保存失败:', err);
        wx.showToast({
          title: err.message || '保存失败',
          icon: 'none'
        });
      });
  },

  // 只更新昵称
  updateNicknameOnly: function (nickname) {
    this.updateNickname(nickname)
      .then(res => {
        // 更新本地用户信息
        const updatedUserInfo = { ...this.data.userInfo, nickname: nickname };
        const app = getApp();
        app.setLoginData(app.globalData.token, updatedUserInfo);
        this.setData({
          userInfo: updatedUserInfo,
          isSaving: false
        });
        this.hideEditModal();
        wx.showToast({
          title: '保存成功',
          icon: 'success'
        });
      })
      .catch(err => {
        this.setData({ isSaving: false });
        console.error('保存失败:', err);
        wx.showToast({
          title: err.message || '保存失败',
          icon: 'none'
        });
      });
  },

  // 更新昵称
  updateNickname: function (nickname) {
    return api.userApi.updateNickname(nickname);
  }
});
