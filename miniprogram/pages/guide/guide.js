const util = require('../../utils/util.js');

Page({
  data: {
    // 页面数据
  },

  onLoad: function (options) {
    // 页面加载时的逻辑
    console.log('Guide page loaded');
  },

  onReady: function () {
    // 页面初次渲染完成
  },

  onShow: function () {
    // 如果已登录，跳转到首页
    if (util.checkLogin()) {
      wx.switchTab({ url: '/pages/index/index' });
      return;
    }
  },

  onHide: function () {
    // 页面隐藏
  },

  onUnload: function () {
    // 页面卸载
  },

  // 体验使用按钮点击事件
  onExperienceTap: function () {
    // 标记用户已看过引导页
    wx.setStorageSync('hasSeenGuide', true);

    // 跳转到 Step 1 开始需求引导流程
    wx.navigateTo({
      url: '/pages/requirement/step/step?step=1',
      success: function () {
        console.log('Navigated to step page');
      },
      fail: function (err) {
        console.error('Navigation failed:', err);
        wx.showToast({
          title: '页面跳转失败',
          icon: 'none'
        });
      }
    });
  },

  // 加入开发者计划按钮点击事件
  onJoinDeveloperTap: function () {
    wx.showToast({
      title: '功能开发中',
      icon: 'none',
      duration: 2000
    });
  },

  // 用户点击右上角分享
  onShareAppMessage: function () {
    return {
      title: 'AI自助开发系统 - 智能驱动，让开发更简单',
      path: '/pages/guide/guide',
      imageUrl: '/images/share-cover.png'
    };
  },

  // 用户点击右上角分享到朋友圈
  onShareTimeline: function () {
    return {
      title: 'AI自助开发系统 - 智能驱动，让开发更简单',
      query: '',
      imageUrl: '/images/share-cover.png'
    };
  }
});
