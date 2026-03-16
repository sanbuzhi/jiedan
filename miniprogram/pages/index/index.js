const util = require('../../utils/util.js');
const api = require('../../utils/api.js');
const { requirementApi, aiApi } = api;

// ==================== 配置常量 ====================

// 所有流程节点定义（数组索引0-10，对应后端currentFlowNode）
const FLOW_NODES = [
  { title: '明确需求', desc: '客户提交初步需求' },           // 索引0
  { title: 'AI明确需求', desc: 'AI分析并完善需求' },         // 索引1 - AI节点
  { title: '需求确认验收', desc: '确认需求文档' },           // 索引2 - 客户验收节点1
  { title: 'AI拆分任务', desc: '自动拆分子任务' },           // 索引3 - AI节点
  { title: 'AI开发', desc: '智能编码实现' },                 // 索引4 - AI节点
  { title: 'AI功能测试', desc: '自动化功能测试' },           // 索引5 - AI节点
  { title: 'AI安全测试', desc: '测试接口漏洞' },             // 索引6 - AI节点
  { title: '功能验收测试', desc: '客户功能验收' },           // 索引7 - 客户验收节点2
  { title: '打包交付', desc: '项目交付' },                   // 索引8
  { title: '最终验收', desc: '客户最终验收' },               // 索引9 - 客户验收节点3
  { title: '项目完成', desc: '项目结束' }                    // 索引10
];

// AI节点配置（使用0-based数组索引）
// needsApproval: true=执行完后等待客户验收, false=自动继续下一个AI节点
// timeout: 10分钟 = 600000毫秒
const AI_NODE_CONFIG = {
  1: { name: 'AI明确需求', api: 'clarifyRequirement', nextNode: 2, maxRetries: 3, timeout: 600000, needsApproval: true },   // → 节点2(需求确认验收)需要人工验收
  3: { name: 'AI拆分任务', api: 'splitTasks', nextNode: 4, maxRetries: 3, timeout: 600000, needsApproval: false },             // → 节点4(AI开发)自动继续
  4: { name: 'AI开发', api: 'generateCode', nextNode: 5, maxRetries: 3, timeout: 600000, needsApproval: false },              // → 节点5(AI功能测试)自动继续
  5: { name: 'AI功能测试', api: 'functionalTest', nextNode: 6, maxRetries: 3, timeout: 600000, needsApproval: false },          // → 节点6(AI安全测试)自动继续
  6: { name: 'AI安全测试', api: 'securityTest', nextNode: 7, maxRetries: 3, timeout: 600000, needsApproval: true }             // → 节点7(功能验收测试)需要人工验收
};

// 点击提示配置
const CLICK_HINTS = {
  0: { index: 0, text: '点击编辑' },
  1: { index: 1, text: 'AI处理中...' },
  2: { index: 2, text: '点击验收' },
  3: { index: 3, text: 'AI处理中...' },
  4: { index: 4, text: 'AI处理中...' },
  5: { index: 5, text: 'AI处理中...' },
  6: { index: 6, text: 'AI处理中...' },
  7: { index: 7, text: '点击验收' },
  8: { index: 8, text: '等待交付' },
  9: { index: 9, text: '点击验收' },
  10: { index: 10, text: '已完成' }
};

// AI节点索引列表
const AI_NODE_INDEXES = [1, 3, 4, 5, 6];

// API类型映射
const API_TYPE_MAP = {
  1: 'clarify-requirement',
  3: 'split-tasks',
  4: 'generate-code',
  5: 'functional-test',
  6: 'security-test'
};

// 阶段名称映射
const STAGE_MAP = {
  1: 'clarify',
  3: 'split',
  4: 'develop',
  5: 'test',
  6: 'security'
};

Page({
  data: {
    isLoggedIn: false,
    userInfo: null,
    currentRequirement: null,
    currentStep: 0,
    recentDeals: [],
    // 项目流程节点
    projectFlowNodes: [],
    // 所有项目列表
    allRequirements: [],
    // 是否显示项目（用于控制初始渲染）
    showProjects: false,
    // 流程节点配置（引用外部常量）
    allFlowNodes: FLOW_NODES
  },

  onLoad: function (options) {
    // 检查是否需要显示引导页
    this.checkGuideStatus();

    // 处理分享进入的推荐码
    if (options.referral_code) {
      wx.setStorageSync('referrerCode', options.referral_code);
      const app = getApp();
      if (app) {
        app.globalData.referrerCode = options.referral_code;
      }
    }

    // 处理扫码进入的推荐码（scene参数）
    if (options.scene) {
      this.handleSceneParams(options.scene);
    }

    // 初始化模拟成交数据
    this.initRecentDeals();
  },

  // 处理扫码场景参数
  handleSceneParams: function (scene) {
    if (!scene) {
      console.log('scene参数为空，跳过处理');
      return;
    }
    
    try {
      // scene参数是二维码生成时编码的，需要解码
      const sceneStr = decodeURIComponent(scene);
      console.log('扫码场景参数:', sceneStr);

      // 解析 scene 字符串（格式：referral_code=xxx 或 key1=value1&key2=value2）
      const params = this.parseSceneParams(sceneStr);

      // 如果有推荐码，缓存到本地
      if (params.referral_code) {
        wx.setStorageSync('referrerCode', params.referral_code);
        const app = getApp();
        if (app) {
          app.globalData.referrerCode = params.referral_code;
        }
        console.log('从扫码获取推荐码:', params.referral_code);
      }
    } catch (err) {
      console.error('解析scene参数失败:', err);
      // 扫码参数解析失败不影响主流程，静默处理
    }
  },

  // 解析scene参数字符串
  parseSceneParams: function (sceneStr) {
    const params = {};
    const pairs = sceneStr.split('&');
    pairs.forEach(pair => {
      // 使用 limit 参数确保只分割一次，避免 value 中包含 = 号被截断
      const [key, value] = pair.split('=', 2);
      if (key && value) {
        params[key] = decodeURIComponent(value);
      }
    });
    return params;
  },

  // 检查引导页状态
  checkGuideStatus: function () {
    const hasSeenGuide = wx.getStorageSync('hasSeenGuide');
    if (!hasSeenGuide) {
      // 未看过引导页，跳转到引导页
      wx.redirectTo({
        url: '/pages/guide/guide'
      });
    }
  },

  onShow: function () {
    // 检查登录状态
    this.checkLoginStatus();
    
    // 检查是否需要刷新（从step/approve页面返回时）
    const needRefresh = wx.getStorageSync('index_need_refresh');
    const autoTrigger = wx.getStorageSync('auto_trigger_ai');
    
    if (needRefresh || autoTrigger) {
      wx.removeStorageSync('index_need_refresh');
      console.log('从其他页面返回，刷新需求列表');
      
      // 先刷新需求列表，然后在回调中检查自动触发
      this.loadCurrentRequirementAndCheckAutoTrigger();
    }
    
    // 页面显示时重新启动定时器（如果数据已加载）
    if (this.data.recentDeals && this.data.recentDeals.length > 0 && !this.dealsTimer) {
      this.dealsTimer = setInterval(() => {
        if (this.data && this.data.recentDeals) {
          this.updateRecentDeals();
        }
      }, 30000);
    }
  },
  
  // 检查是否需要自动触发AI节点
  checkAutoTriggerAI: function () {
    const autoTrigger = wx.getStorageSync('auto_trigger_ai');
    if (!autoTrigger) {
      console.log('没有自动触发标记，跳过');
      return;
    }
    
    console.log('检测到自动触发标记:', autoTrigger);
    
    // 检查是否过期（5分钟内有效）
    const now = Date.now();
    if (now - autoTrigger.timestamp > 5 * 60 * 1000) {
      console.log('自动触发标记已过期，清除');
      wx.removeStorageSync('auto_trigger_ai');
      return;
    }
    
    // 清除自动触发标记
    wx.removeStorageSync('auto_trigger_ai');
    
    console.log(`准备自动执行AI节点: ${autoTrigger.nodeIndex}, 需求ID: ${autoTrigger.requirementId}`);
    
    // 等待需求列表加载完成
    setTimeout(() => {
      this.executeAINode(autoTrigger.requirementId, autoTrigger.nodeIndex);
    }, 1000);
  },
  
  // ==================== AI 节点执行方法 ====================

  // 执行AI节点业务逻辑（带重试机制）
  // 执行顺序：1.更新状态为"处理中" -> 2.执行AI逻辑 -> 3.验证结果 -> 4.更新状态到下一阶段
  executeAINodeWithRetry: function (requirementId, nodeIndex, retryCount = 0) {
    const aiNode = AI_NODE_CONFIG[nodeIndex];
    if (!aiNode) {
      console.error(`未找到索引为 ${nodeIndex} 的AI节点配置`);
      return;
    }

    console.log(`执行AI节点: ${aiNode.name}, 需求ID: ${requirementId}, 重试次数: ${retryCount}`);

    // 第一步：先将节点状态更新为"处理中"（让用户看到AI正在工作）
    // 使用一个特殊的中间状态表示AI正在处理
    const processingNodeIndex = nodeIndex; // AI执行期间保持在当前节点显示处理状态
    
    // 先获取需求详情
    requirementApi.getRequirementDetail(requirementId)
      .then(requirement => {
        // 构建请求数据
        let requestData = this.buildAIRequestData(nodeIndex, requirement);

        // 调用AI接口（带超时）
        return this.callAIWithTimeout(aiApi[aiNode.api], requestData, aiNode.timeout);
      })
      .then(res => {
        console.log(`${aiNode.name}完成:`, res);

        // 【修改】Feedback Shadow验证已内嵌到AI接口，直接检查返回的success字段
        const aiResult = res.data || res;
        if (!aiResult.success) {
          // 验证失败，抛出错误触发重试
          const errorMsg = aiResult.errorMessage || 'AI处理失败';
          throw new Error(errorMsg);
        }

        // 【新增】保存AI生成的文档内容，供后续节点使用
        this.saveAIDocumentToRequirement(requirementId, nodeIndex, aiResult);

        // 【移除】AI产物由后端保存，前端不需要保存
        // this.saveAIProduct(requirementId, nodeIndex, aiResult);

        // 第二步：AI执行成功，更新节点状态到下一阶段（客户验收或下一个AI节点）
        return this.updateFlowNodeStatus(requirementId, aiNode.nextNode);
      })
      .then(() => {
        // 如果需要客户验收，停止自动执行，等待用户点击节点
        if (aiNode.needsApproval) {
          console.log(`${aiNode.name}完成，等待客户验收，节点已更新为: ${aiNode.nextNode}`);
          // 【移除】不需要保存approval_data到本地存储
          return;
        }

        // 不需要验收，自动继续下一个AI节点
        if (AI_NODE_CONFIG[aiNode.nextNode]) {
          setTimeout(() => {
            this.executeAINodeWithRetry(requirementId, aiNode.nextNode, 0);
          }, 500);
        }
      })
      .catch(err => {
        console.error(`${aiNode.name}失败:`, err);

        // 检查是否需要重试
        if (retryCount < aiNode.maxRetries) {
          console.log(`${aiNode.name}将在3秒后重试 (${retryCount + 1}/${aiNode.maxRetries})`);
          setTimeout(() => {
            this.executeAINodeWithRetry(requirementId, nodeIndex, retryCount + 1);
          }, 3000);
        } else {
          console.error(`${aiNode.name}已达到最大重试次数，停止自动执行`);
          // 标记节点为失败状态
          this.markNodeAsFailed(requirementId, nodeIndex);
          
          // 通知用户AI处理失败
          wx.showToast({
            title: `${aiNode.name}失败，请重试`,
            icon: 'none',
            duration: 3000
          });
        }
      });
  },

  // 【已移除】AI产物由后端保存，前端不需要保存
  // saveAIProduct: function (requirementId, nodeIndex, validationResult) { ... }

  // 【新增】保存AI生成的文档内容到requirement对象，供后续节点使用
  saveAIDocumentToRequirement: function (requirementId, nodeIndex, result) {
    // 【修复】使用正确的字段名 allRequirements
    const requirement = this.data.allRequirements.find(r => r.id === requirementId);
    if (!requirement) {
      console.warn(`未找到requirement对象, requirementId: ${requirementId}`);
      return;
    }

    // 处理不同节点的返回数据
    switch (nodeIndex) {
      case 1: // AI明确需求
        if (result.documentContent) {
          requirement.requirementDoc = result.documentContent;
          console.log(`已保存需求文档到requirement对象, requirementId: ${requirementId}`);
        }
        break;
      case 3: // AI拆分任务
        if (result.documentContent) {
          requirement.taskDoc = result.documentContent;
          console.log(`已保存任务文档到requirement对象, requirementId: ${requirementId}`);
        }
        break;
      case 4: // AI生成代码
        if (result.code) {
          requirement.generatedCode = result.code;
          console.log(`已保存生成的代码到requirement对象, requirementId: ${requirementId}`);
        }
        break;
      case 5: // AI功能测试
        if (result.testCode) {
          requirement.testCode = result.testCode;
          console.log(`已保存测试代码到requirement对象, requirementId: ${requirementId}`);
        }
        break;
      case 6: // AI安全测试
        if (result.rawResponse) {
          requirement.securityReport = result.rawResponse;
          console.log(`已保存安全测试报告到requirement对象, requirementId: ${requirementId}`);
        }
        break;
    }
  },

  // 构建AI请求数据
  buildAIRequestData: function (nodeIndex, requirement) {
    let requestData = { model: null };

    switch (nodeIndex) {
      case 1: // AI明确需求
        requestData.projectId = requirement.id ? String(requirement.id) : '';
        requestData.requirementDescription = requirement.requirementDescription || '暂无描述';
        break;
      case 3: // AI拆分任务
        requestData.projectId = requirement.id ? String(requirement.id) : '';
        // 【修复】使用AI明确需求生成的文档内容
        requestData.requirementDoc = requirement.requirementDoc || '暂无需求文档';
        break;
      case 4: // AI开发
        requestData.projectId = requirement.id ? String(requirement.id) : '';
        requestData.taskDescription = '基于需求生成项目代码';
        requestData.language = 'java';
        requestData.framework = 'springboot';
        break;
      case 5: // AI功能测试
        requestData.projectId = requirement.id ? String(requirement.id) : null;
        // 【注意】需要从节点4获取生成的代码，暂时使用占位符
        requestData.code = requirement.generatedCode || '// 待测试代码，请先生成代码';
        requestData.language = 'java';
        requestData.functionDescription = requirement.requirementDescription || '暂无描述';
        break;
      case 6: // AI安全测试
        requestData.projectId = requirement.id ? String(requirement.id) : null;
        // 【注意】需要从节点4获取生成的代码，暂时使用占位符
        requestData.code = requirement.generatedCode || '// 待测试代码，请先生成代码';
        requestData.language = 'java';
        requestData.applicationType = 'web';
        break;
    }

    return requestData;
  },

  // 带超时的AI接口调用
  callAIWithTimeout: function (apiFunc, requestData, timeout) {
    return new Promise((resolve, reject) => {
      const timer = setTimeout(() => {
        reject(new Error('AI接口调用超时'));
      }, timeout);

      apiFunc(requestData)
        .then(res => {
          clearTimeout(timer);
          resolve(res);
        })
        .catch(err => {
          clearTimeout(timer);
          reject(err);
        });
    });
  },

  // 标记节点为失败状态
  markNodeAsFailed: function (requirementId, nodeIndex) {
    requirementApi.updateFlowStatus(requirementId, nodeIndex, 'failed')
      .then(() => {
        console.log(`节点${nodeIndex}已标记为失败`);
        this.loadCurrentRequirement();
      })
      .catch(err => {
        console.error('标记节点失败状态出错:', err);
      });
  },

  // 【已移除】Feedback Shadow验证已内嵌到AI接口中，前端不再单独调用
  // performFeedbackShadowValidation: function (requirementId, nodeIndex, aiResult) { ... }

  // 获取阶段名称（nodeIndex是0-based数组索引）
  getStageName: function (nodeIndex) {
    return STAGE_MAP[nodeIndex] || 'unknown';
  },

  // 执行AI节点入口（只进行参数验证，实际逻辑在 executeAINodeWithRetry 中）
  executeAINode: function (requirementId, nodeIndex) {
    const aiNode = AI_NODE_CONFIG[nodeIndex];
    if (!aiNode) {
      console.error(`未找到索引为 ${nodeIndex} 的AI节点配置`);
      return;
    }

    console.log(`准备执行AI节点: ${aiNode.name}, 需求ID: ${requirementId}, 节点索引: ${nodeIndex}`);
    
    // 显示AI开始执行的提示
    wx.showToast({
      title: `${aiNode.name}开始执行...`,
      icon: 'none',
      duration: 2000
    });

    // 直接调用带重试的执行方法，状态更新逻辑统一在 executeAINodeWithRetry 中处理
    this.executeAINodeWithRetry(requirementId, nodeIndex, 0);
  },
  
  // 更新流程节点状态（返回Promise）
  updateFlowNodeStatus: function (requirementId, nextNodeIndex) {
    return requirementApi.updateFlowStatus(requirementId, nextNodeIndex, 'active')
      .then(() => {
        console.log(`流程节点更新为: ${nextNodeIndex}`);
        // 刷新需求列表
        this.loadCurrentRequirement();
      })
      .catch(err => {
        console.error('更新流程节点失败:', err);
        throw err;
      });
  },

  onPullDownRefresh: function () {
    this.checkLoginStatus();
    wx.stopPullDownRefresh();
  },

  // 检查登录状态
  checkLoginStatus: function () {
    const isLoggedIn = util.checkLogin();
    const userInfo = wx.getStorageSync('userInfo');

    this.setData({
      isLoggedIn: isLoggedIn,
      userInfo: userInfo
    });

    // 如果已登录，获取用户当前需求状态
    if (isLoggedIn) {
      this.loadCurrentRequirement();
    }
  },

  // 加载当前进行中的需求（带回调）
  loadCurrentRequirement: function (callback) {
    console.log('开始加载需求列表...');
    requirementApi.getRequirements({ size: 10 })
      .then(res => {
        console.log('需求列表返回:', res);
        // 适配分页格式（支持 items 或 content 字段）
        const content = res.items || res.content || res;
        if (content && content.length > 0) {
          // 为每个需求计算流程节点
          const allRequirements = content.map(req => {
            // 兼容处理：后端创建项目时currentFlowNode默认为1，但新项目应该在节点0
            // 当status为draft且currentFlowNode为1时，视为节点0（明确需求阶段）
            let currentStep = req.currentFlowNode || 0;
            if (req.status === 'draft' && currentStep === 1) {
              console.log(`项目${req.id}处于draft状态但currentFlowNode为1，修正为0`);
              currentStep = 0;
            }
            const flowNodes = this.calculateFlowNodes(currentStep, req.needDeploy);
            return {
              ...req,
              currentFlowNode: currentStep, // 更新为修正后的值
              flowNodes: flowNodes
            };
          });

          // 为每个项目标记active节点，用于scroll-into-view
          allRequirements.forEach((req, reqIndex) => {
            const activeNodeIndex = req.flowNodes.findIndex(node => node.status === 'active');
            if (activeNodeIndex !== -1) {
              // 设置scrollIntoView的target id
              req.scrollIntoViewId = `node-${reqIndex}-${activeNodeIndex}`;
            }
          });

          // 先渲染但不显示，等scroll-into-view准备好后再显示
          this.setData({
            allRequirements: allRequirements,
            currentRequirement: allRequirements[0],
            currentStep: allRequirements[0].currentFlowNode || 0,
            projectFlowNodes: allRequirements[0].flowNodes,
            showProjects: false
          }, () => {
            // 延迟显示，让scroll-into-view生效
            setTimeout(() => {
              this.setData({ showProjects: true });
              // 执行回调
              if (callback && typeof callback === 'function') {
                callback();
              }
            }, 50);
          });
        } else {
          this.setData({
            allRequirements: [],
            currentRequirement: null,
            currentStep: 0,
            projectFlowNodes: []
          });
          // 执行回调
          if (callback && typeof callback === 'function') {
            callback();
          }
        }
      })
      .catch(err => {
        console.error('获取需求列表失败:', err);
        // 显示错误提示给用户
        wx.showToast({
          title: '加载项目列表失败，请下拉刷新重试',
          icon: 'none',
          duration: 2000
        });
        this.setData({
          allRequirements: [],
          currentRequirement: null,
          currentStep: 0,
          projectFlowNodes: []
        }, () => {
          // 【修复】确保回调被调用
          if (callback && typeof callback === 'function') {
            callback();
          }
        });
      });
  },

  // 加载需求列表并检查自动触发AI节点
  loadCurrentRequirementAndCheckAutoTrigger: function () {
    this.loadCurrentRequirement(() => {
      // 列表加载完成后，检查是否需要自动触发AI节点
      this.checkAutoTriggerAI();
    });
  },

  // 计算流程节点状态
  calculateFlowNodes: function (currentStep, needDeploy) {
    const hintConfig = CLICK_HINTS[currentStep];

    const nodes = FLOW_NODES.map((node, index) => {
      // currentStep 和 index 都是 0-based，直接比较
      let status = 'pending';

      if (index < currentStep) {
        status = 'completed';
      } else if (index === currentStep) {
        status = 'active';
      }

      // 根据是否需要上线调整第9步的描述（打包交付/上线部署）
      let desc = node.desc;
      if (index === 8) { // 第9个节点
        desc = needDeploy ? '上线部署' : '打包交付';
      }

      // 判断是否需要显示点击提示
      let clickHint = null;
      if (hintConfig && hintConfig.index === index) {
        clickHint = hintConfig.text;
      }

      return {
        ...node,
        desc,
        status,
        isAiNode: AI_NODE_INDEXES.includes(index), // 标记是否为AI节点
        clickHint: clickHint  // 添加点击提示
      };
    });

    return nodes;
  },

  // 生成模拟成交数据
  generateDealData: function (count = 1, isNew = false) {
    const projectTypes = ['微信小程序', '抖音小程序', '网站开发', '爬虫程序', 'H5页面'];
    const avatars = ['李', '王', '张', '刘', '陈', '杨', '赵', '黄'];
    const deals = [];

    for (let i = 0; i < count; i++) {
      const amount = Math.floor(Math.random() * 500) + 499;
      const minutesAgo = isNew ? 0 : Math.floor(Math.random() * 60);
      deals.push({
        user: avatars[Math.floor(Math.random() * avatars.length)] + '**',
        avatar: avatars[Math.floor(Math.random() * avatars.length)],
        projectType: projectTypes[Math.floor(Math.random() * projectTypes.length)],
        amount: amount.toLocaleString(),
        time: minutesAgo < 1 ? '刚刚' : `${minutesAgo}分钟前`
      });
    }

    return count === 1 ? deals[0] : deals;
  },

  // 初始化实时成交数据（模拟）
  initRecentDeals: function () {
    const deals = this.generateDealData(10);
    this.setData({ recentDeals: deals });

    // 清除可能存在的旧定时器并启动新定时器
    this.clearDealsTimer();
    this.dealsTimer = setInterval(() => {
      if (this.data && this.data.recentDeals) {
        this.updateRecentDeals();
      }
    }, 30000);
  },

  // 清除成交数据定时器
  clearDealsTimer: function() {
    if (this.dealsTimer) {
      clearInterval(this.dealsTimer);
      this.dealsTimer = null;
    }
  },

  onUnload: function() {
    // 页面卸载时清除定时器，防止内存泄漏
    this.clearDealsTimer();
  },

  onHide: function() {
    // 页面隐藏时清除定时器，避免后台运行
    this.clearDealsTimer();
  },

  // 更新成交数据
  updateRecentDeals: function () {
    const newDeal = this.generateDealData(1, true);
    const deals = this.data.recentDeals;
    
    deals.unshift(newDeal);
    if (deals.length > 10) deals.pop();

    // 更新时间显示
    deals.forEach((deal, index) => {
      if (index > 0 && deal.time !== '刚刚') {
        const minutes = parseInt(deal.time);
        if (!isNaN(minutes)) deal.time = `${minutes + 1}分钟前`;
      }
    });

    this.setData({ recentDeals: deals });
  },

  // 创建新项目
  createNewProject: function () {
    if (!this.data.isLoggedIn) {
      wx.navigateTo({
        url: '/pages/login/login'
      });
    } else {
      wx.navigateTo({
        url: '/pages/requirement/step/step?step=1'
      });
    }
  },

  // 删除项目
  deleteProject: function (e) {
    const projectId = e.currentTarget.dataset.id;
    
    wx.showModal({
      title: '确认删除',
      content: '确定要删除这个项目吗？此操作不可恢复。',
      confirmText: '删除',
      confirmColor: '#ff4d4f',
      success: (res) => {
        if (res.confirm) {
          this.doDeleteProject(projectId);
        }
      }
    });
  },

  // 执行删除项目
  doDeleteProject: function (projectId) {
    wx.showLoading({ title: '删除中...' });
    
    requirementApi.deleteRequirement(projectId)
      .then(res => {
        wx.hideLoading();
        wx.showToast({
          title: '删除成功',
          icon: 'success'
        });
        // 刷新项目列表
        this.loadCurrentRequirement();
      })
      .catch(err => {
        wx.hideLoading();
        console.error('删除失败:', err);
        // 即使报错也可能已经删除成功（后端返回204但前端认为失败）
        // 刷新列表确认
        this.loadCurrentRequirement();
        // 根据错误类型显示不同的提示
        const errorMsg = err && err.message ? '删除失败：' + err.message : '操作完成';
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 2000
        });
      });
  },

  // 查看全部项目
  viewAllProjects: function () {
    wx.showToast({
      title: '功能开发中...',
      icon: 'none'
    });
  },

  // 点击流程节点
  onFlowNodeTap: function (e) {
    const { reqIndex, nodeIndex, reqId } = e.currentTarget.dataset;
    const req = this.data.allRequirements[reqIndex];
    if (!req) {
      console.error('未找到对应的需求数据');
      return;
    }
    const node = req.flowNodes[nodeIndex];
    if (!node) {
      console.error('未找到对应的节点数据');
      return;
    }

    // 节点1（索引0）处于active状态时，点击跳转至 step_all
    if (nodeIndex === 0 && node.status === 'active') {
      wx.navigateTo({
        url: `/pages/requirement/step_all/step_all?id=${req.id}&stage=1`
      });
      return;
    }

    // 客户验收节点（索引2、7、9）处于active状态时，点击跳转至验收页面
    // 索引2=需求确认验收, 索引7=功能验收测试, 索引9=最终验收
    if ((nodeIndex === 2 || nodeIndex === 7 || nodeIndex === 9) && node.status === 'active') {
      // 【简化】直接跳转到验收页面，不需要检查本地存储
      const stageName = nodeIndex === 2 ? '需求确认验收' : nodeIndex === 7 ? '功能验收测试' : '最终验收';
      wx.navigateTo({
        url: `/pages/requirement/approve/approve?requirementId=${req.id}&projectId=${req.id}&stage=${this.getStageName(nodeIndex)}&stageName=${stageName}`
      });
      return;
    }

    // AI节点处于active状态时，提示用户AI正在处理
    if (node.isAiNode && node.status === 'active') {
      wx.showToast({
        title: `${node.title}中，请稍候...`,
        icon: 'none'
      });
      return;
    }
  },

  // 分享给朋友
  onShareAppMessage: function () {
    const referralCode = this.data.userInfo ? this.data.userInfo.referral_code : '';
    const path = referralCode ? `/pages/index/index?referral_code=${referralCode}` : '/pages/index/index'; 

    return {
      title: 'AI自助开发系统，7步完成您的项目！',
      path: path,
      imageUrl: '/images/share-cover.png'
    };
  },

  // 分享到朋友圈
  onShareTimeline: function () {
    const referralCode = this.data.userInfo ? this.data.userInfo.referral_code : '';

    return {
      title: 'AI自助开发系统 - 智能开发，透明报价',
      query: referralCode ? `referral_code=${referralCode}` : '',
      imageUrl: '/images/share-cover.png'
    };
  }
});
