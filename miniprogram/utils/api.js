// API基础配置
// 统一使用 util.getApiBaseUrl() 获取基础URL，确保全局一致
const util = require('./util.js');

// 请求超时时间（毫秒）
const REQUEST_TIMEOUT = 30000;

// 请求拦截器
const request = (options) => {
  return new Promise((resolve, reject) => {
    const token = wx.getStorageSync('token');

    // 合并默认配置
    const requestOptions = {
      url: `${util.getApiBaseUrl()}${options.url}`,
      method: options.method || 'GET',
      data: options.data || {},
      timeout: options.timeout || REQUEST_TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : ''
      },
      success: (res) => {
        if (res.statusCode === 200 || res.statusCode === 201) {
          resolve(res.data);
        } else if (res.statusCode === 401) {
          // Token过期，清除登录状态并跳转到登录页
          const app = getApp();
          if (app) {
            app.clearLoginData();
          }
          wx.removeStorageSync('token');
          wx.removeStorageSync('userInfo');

          wx.showToast({
            title: '登录已过期，请重新登录',
            icon: 'none'
          });

          setTimeout(() => {
            wx.navigateTo({
              url: '/pages/login/login'
            });
          }, 1500);

          reject(new Error('Unauthorized'));
        } else {
          const errorMsg = res.data && res.data.detail ? res.data.detail : '请求失败';
          wx.showToast({
            title: errorMsg,
            icon: 'none'
          });
          reject(new Error(errorMsg));
        }
      },
      fail: (err) => {
        console.error('请求失败:', err);
        let errorMsg = '网络请求失败';
        if (err.errMsg && err.errMsg.includes('timeout')) {
          errorMsg = '请求超时，请检查网络连接';
        } else if (err.errMsg && err.errMsg.includes('fail')) {
          errorMsg = '网络连接失败，请检查服务器地址';
        }
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
        reject(err);
      }
    };

    wx.request(requestOptions);
  });
};

// GET请求
const get = (url, params = {}) => {
  // 构建查询字符串
  let queryString = '';
  if (Object.keys(params).length > 0) {
    queryString = '?' + Object.keys(params)
      .map(key => `${encodeURIComponent(key)}=${encodeURIComponent(params[key])}`)
      .join('&');
  }
  
  return request({
    url: url + queryString,
    method: 'GET'
  });
};

// POST请求
const post = (url, data = {}, options = {}) => {
  return request({
    url: url,
    method: 'POST',
    data: data,
    timeout: options.timeout
  });
};

// PUT请求
const put = (url, data = {}) => {
  return request({
    url: url,
    method: 'PUT',
    data: data
  });
};

// DELETE请求
const del = (url, data = {}) => {
  return request({
    url: url,
    method: 'DELETE',
    data: data
  });
};

// 微信登录相关API
const wechatLogin = {
  // 微信授权登录
  login: (code, userInfo, referralCode = null) => {
    return post('/auth/wechat-login', {
      code: code,
      nickname: userInfo ? userInfo.nickName : null,
      avatar: userInfo ? userInfo.avatarUrl : null,
      referral_code: referralCode
    });
  }
};

// 用户相关API
const userApi = {
  // 获取当前用户信息
  getMe: () => get('/users/me'),

  // 更新用户信息
  updateMe: (data) => put('/users/me', data),

  // 上传头像
  uploadAvatar: (filePath) => {
    return new Promise((resolve, reject) => {
      const token = wx.getStorageSync('token');
      wx.uploadFile({
        url: `${util.getApiBaseUrl()}/users/me/avatar`,
        filePath: filePath,
        name: 'file',
        header: {
          'Authorization': token ? `Bearer ${token}` : ''
        },
        success: (res) => {
          if (res.statusCode === 200) {
            const data = JSON.parse(res.data);
            resolve(data);
          } else {
            const errorMsg = res.data ? JSON.parse(res.data).detail || '上传失败' : '上传失败';
            reject(new Error(errorMsg));
          }
        },
        fail: (err) => {
          reject(err);
        }
      });
    });
  },

  // 更新昵称
  updateNickname: (nickname) => put('/users/me/nickname', { nickname }),

  // 获取推荐关系列表
  getReferrals: (level) => get('/users/referrals', level ? { level } : {}),

  // 获取推荐关系树
  getReferralTree: () => get('/users/referrals/tree'),

  // 获取积分记录
  getPoints: (skip = 0, limit = 20) => get('/users/points', { skip, limit }),

  // 获取小程序二维码（带推荐码）
  getQRCode: (params = {}) => {
    // 支持两种格式：
    // 1. 普通二维码：getQRCode({ referral_code: 'xxx' })
    // 2. 带页面路径：getQRCode({ page: 'pages/index/index', scene: 'referral_code=xxx' })
    return post('/users/qrcode', params);
  },

  // 获取兑换物品列表
  getRedeemItems: (params = {}) => get('/users/redeem-items', params),

  // 积分兑换
  redeemPoints: (itemId, quantity = 1) => post('/users/redeem', { item_id: itemId, quantity }),

  // 获取兑换记录
  getRedeemRecords: (skip = 0, limit = 20) => get('/users/redeem-records', { skip, limit })
};

// 需求相关API
const requirementApi = {
  // 创建需求
  createRequirement: (data) => post('/requirements', data),

  // 获取需求列表
  getRequirements: (params = {}) => get('/requirements', params),

  // 获取需求详情
  getRequirementDetail: (id) => get(`/requirements/${id}`),

  // 更新需求
  updateRequirement: (id, data) => put(`/requirements/${id}`, data),

  // 删除需求
  deleteRequirement: (id) => del(`/requirements/${id}`),

  // 计算预算
  calculateBudget: (id) => get(`/requirements/${id}/budget`),

  // ========== 步骤数据管理 API ==========

  // 保存步骤数据
  saveStepData: (id, step, data) => post(`/requirements/${id}/step-data`, { step, data }),

  // 获取步骤数据
  getStepData: (id) => get(`/requirements/${id}/step-data`),

  // ========== 流程节点状态管理 API ==========

  // 获取流程节点状态
  getFlowStatus: (id) => get(`/requirements/${id}/flow-status`),

  // 更新流程节点状态
  updateFlowStatus: (id, nodeIndex, status) => put(`/requirements/${id}/flow-status`, { nodeIndex: nodeIndex, status: status }),

  // ========== 客户验收相关 API ==========

  // 阶段验收通过
  approveStage: (id, data) => post(`/requirements/${id}/approve`, data),

  // 提交建议
  submitSuggestion: (id, data) => post(`/requirements/${id}/suggestion`, data)
};

// 订单相关API
const orderApi = {
  // 创建订单
  createOrder: (data) => post('/orders', data),
  
  // 获取订单列表
  getOrders: (params = {}) => get('/orders', params),
  
  // 获取订单详情
  getOrderDetail: (id) => get(`/orders/${id}`),
  
  // 支付订单
  payOrder: (id, data) => put(`/orders/${id}/pay`, data),
  
  // 退款订单
  refundOrder: (id, data) => put(`/orders/${id}/refund`, data),
  
  // 取消订单
  cancelOrder: (id) => put(`/orders/${id}/cancel`)
};

// AI相关API（Feedback Shadow System）
const aiApi = {
  // ========== AI核心接口 ==========
  
  // AI明确需求 - 节点1（超时40分钟，匹配后端配置）
  clarifyRequirement: (data) => post('/ai/clarify-requirement', data, { timeout: 2400000 }),

  // AI拆分任务 - 节点3（超时40分钟，匹配后端配置）
  splitTasks: (data) => post('/ai/split-tasks', data, { timeout: 2400000 }),

  // AI生成代码 - 节点4（超时40分钟，匹配后端配置）
  generateCode: (data) => post('/ai/generate-code', data, { timeout: 2400000 }),

  // AI功能测试 - 节点5（超时40分钟，匹配后端配置）
  functionalTest: (data) => post('/ai/functional-test', data, { timeout: 2400000 }),

  // AI安全测试 - 节点6（超时40分钟，匹配后端配置）
  securityTest: (data) => post('/ai/security-test', data, { timeout: 2400000 }),

  // ========== 【步骤6】模块化生成接口 ==========
  
  // 解析任务书，获取模块列表
  parseTaskDocument: (projectId, taskDoc) => post('/ai/parse-task', { projectId, taskDoc }, { timeout: 30000 }),
  
  // 生成完整项目（按模块顺序）
  generateProject: (data) => post('/ai/generate-project', data, { timeout: 1800000 }), // 30分钟
  
  // 生成单个模块
  generateModule: (data) => post('/ai/generate-module', data, { timeout: 660000 }),

  // ========== 【步骤10】大项目分批次生成接口 ==========
  
  // 启动大项目分批次生成
  startLargeProject: (projectId, data) => post(`/ai/large-project/start/${projectId}`, data, { timeout: 660000 }),
  
  // 执行下一批次生成
  executeNextBatch: (projectId, data) => post(`/ai/large-project/next/${projectId}`, data, { timeout: 660000 }),
  
  // 获取批次生成状态
  getBatchStatus: (projectId) => get(`/ai/large-project/status/${projectId}`),

  // ========== 【步骤11】依赖分析和自动更新接口 ==========
  
  // 分析项目依赖关系
  analyzeDependencies: (projectId, projectPath) => post(`/ai/analyze-dependencies/${projectId}`, { projectPath }, { timeout: 60000 }),
  
  // 分析变更影响
  analyzeImpact: (projectId, changedModule, changedClasses) => post(`/ai/analyze-impact/${projectId}`, { changedModule, changedClasses }, { timeout: 30000 }),
  
  // 创建自动更新任务
  createAutoUpdateTask: (projectId, moduleName, projectPath) => post(`/ai/auto-update/create/${projectId}`, { moduleName, projectPath }, { timeout: 30000 }),
  
  // 执行自动更新任务
  executeAutoUpdateTask: (taskId) => post(`/ai/auto-update/execute/${taskId}`, {}, { timeout: 1800000 }), // 30分钟
  
  // 获取更新任务状态
  getUpdateTaskStatus: (taskId) => get(`/ai/auto-update/status/${taskId}`)
};

// 数据统计API
const analyticsApi = {
  // 获取仪表盘数据
  getDashboardStats: (days = 30) => get('/analytics/dashboard', { days }),
  
  // 埋点上报
  trackEvent: (data) => post('/analytics/track', data)
};

module.exports = {
  request,
  get,
  post,
  put,
  del,
  wechatLogin,
  userApi,
  requirementApi,
  orderApi,
  aiApi,
  analyticsApi
};
