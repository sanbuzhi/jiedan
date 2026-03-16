const api = require('../../../utils/api.js');

Page({
  data: {
    requirementId: null,
    projectId: null,
    currentStage: '',
    stageName: '',
    loading: false,
    actionLoading: false
  },

  onLoad(options) {
    const { requirementId, projectId, stage, stageName } = options;
    this.setData({
      requirementId,
      projectId,
      currentStage: stage || 'clarify',
      stageName: stageName || '需求明确'
    });
  },

  // 继续操作 - 客户验收通过
  handleContinue() {
    const { requirementId, projectId, currentStage } = this.data;

    this.setData({ actionLoading: true });

    // 调用客户放行接口
    api.requirementApi.approveStage(requirementId, {
      stage: currentStage,
      projectId: projectId,
      action: 'approve'
    }).then(() => {
      wx.showToast({
        title: '验收通过',
        icon: 'success'
      });

      // 根据当前阶段判断下一个AI节点
      const nextAINodeMap = {
        'clarify': 3,    // 需求明确验收后 → AI拆分任务(索引3)
        'split': 4,      // 任务拆分验收后 → AI开发(索引4)
        'develop': 5,    // 开发验收后 → AI功能测试(索引5)
        'test': 6,       // 功能测试验收后 → AI安全测试(索引6)
        'security': 7    // 安全测试验收后 → 打包交付(索引7，非AI节点)
      };
      const nextNodeIndex = nextAINodeMap[currentStage];

      // 如果是AI节点，设置自动触发标记
      if (nextNodeIndex && nextNodeIndex <= 6) {
        const triggerData = {
          requirementId: requirementId,
          nodeIndex: nextNodeIndex,
          timestamp: Date.now()
        };
        wx.setStorageSync('auto_trigger_ai', triggerData);
        console.log(`【调试】设置自动触发AI节点: ${nextNodeIndex}`, triggerData);
        
        // 立即验证是否设置成功
        const verify = wx.getStorageSync('auto_trigger_ai');
        console.log('【调试】验证auto_trigger_ai设置结果:', verify);
      }

      // 设置标记通知index页面需要刷新
      wx.setStorageSync('index_need_refresh', true);
      
      // 返回首页
      setTimeout(() => {
        wx.switchTab({
          url: '/pages/index/index'
        });
      }, 1500);
    }).catch(err => {
      console.error('验收失败:', err);
      wx.showToast({
        title: '验收失败',
        icon: 'none'
      });
    }).finally(() => {
      this.setData({ actionLoading: false });
    });
  },

  // 重试操作 - 客户有建议
  handleRetry() {
    const { requirementId, currentStage } = this.data;
    
    wx.navigateTo({
      url: `/pages/requirement/suggestion/suggestion?requirementId=${requirementId}&stage=${currentStage}`
    });
  },

  // 取消操作
  handleCancel() {
    wx.navigateBack();
  }
});
