const api = require('../../../utils/api.js');

Page({
  data: {
    requirementId: null,
    currentStage: '',
    suggestion: '',
    loading: false
  },

  onLoad(options) {
    const { requirementId, stage } = options;
    this.setData({
      requirementId,
      currentStage: stage || 'clarify'
    });
  },

  // 输入建议
  onSuggestionInput(e) {
    this.setData({
      suggestion: e.detail.value
    });
  },

  // 提交建议
  handleSubmit() {
    const { requirementId, currentStage, suggestion } = this.data;
    
    if (!suggestion.trim()) {
      wx.showToast({
        title: '请输入建议',
        icon: 'none'
      });
      return;
    }

    this.setData({ loading: true });

    // 调用提交建议接口
    api.requirementApi.submitSuggestion(requirementId, {
      stage: currentStage,
      suggestion: suggestion.trim()
    }).then(() => {
      wx.showToast({
        title: '建议已提交',
        icon: 'success'
      });
      
      // 返回首页
      setTimeout(() => {
        wx.navigateBack({
          delta: 2, // 返回两层（跳过验收页面）
          success: () => {
            // 触发首页刷新
            const pages = getCurrentPages();
            const indexPage = pages[0];
            if (indexPage && indexPage.loadRequirementDetail) {
              indexPage.loadRequirementDetail(requirementId);
            }
          }
        });
      }, 1500);
    }).catch(err => {
      console.error('提交建议失败:', err);
      wx.showToast({
        title: '提交失败',
        icon: 'none'
      });
    }).finally(() => {
      this.setData({ loading: false });
    });
  },

  // 取消
  handleCancel() {
    wx.navigateBack();
  }
});
