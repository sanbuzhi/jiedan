const api = require('./utils/api.js');

App({
  globalData: {
    userInfo: null,
    token: null,
    isLoggedIn: false,
    referrerCode: null
  },

  onLaunch: function (options) {
    console.log('App Launch', options);
    
    // 检查是否有分享进入的推荐码
    if (options.query && options.query.referral_code) {
      this.globalData.referrerCode = options.query.referral_code;
      wx.setStorageSync('referrerCode', options.query.referral_code);
    }
    
    // 从本地存储恢复登录状态
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    
    if (token) {
      this.globalData.token = token;
      this.globalData.userInfo = userInfo;
      this.globalData.isLoggedIn = true;
      
      // 验证token有效性
      this.validateToken();
    }
  },

  onShow: function (options) {
    console.log('App Show', options);
    
    // 处理从分享卡片进入的场景
    if (options.scene === 1007 || options.scene === 1008 || options.scene === 1044) {
      if (options.query && options.query.referral_code) {
        this.globalData.referrerCode = options.query.referral_code;
        wx.setStorageSync('referrerCode', options.query.referral_code);
      }
    }
  },

  onHide: function () {
    console.log('App Hide');
  },

  onError: function (msg) {
    console.error('App Error:', msg);
  },

  // 验证token是否有效
  validateToken: function () {
    api.get('/users/me')
      .then(res => {
        this.globalData.userInfo = res;
        wx.setStorageSync('userInfo', res);
      })
      .catch(err => {
        console.error('Token validation failed:', err);
        // token失效，清除登录状态
        this.clearLoginData();
      });
  },

  // 设置登录数据
  setLoginData: function (token, userInfo) {
    this.globalData.token = token;
    this.globalData.userInfo = userInfo;
    this.globalData.isLoggedIn = true;
    
    wx.setStorageSync('token', token);
    wx.setStorageSync('userInfo', userInfo);
  },

  // 清除登录数据
  clearLoginData: function () {
    this.globalData.token = null;
    this.globalData.userInfo = null;
    this.globalData.isLoggedIn = false;
    
    wx.removeStorageSync('token');
    wx.removeStorageSync('userInfo');
  },

  // 获取全局数据
  getGlobalData: function () {
    return this.globalData;
  }
});
