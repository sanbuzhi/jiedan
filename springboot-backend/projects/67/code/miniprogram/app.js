App({
  globalData: {
    token: '',
    userInfo: null
  },

  onLaunch() {
    // 检查本地是否有token
    const token = wx.getStorageSync('token');
    const userInfo = wx.getStorageSync('userInfo');
    if (token) {
      this.globalData.token = token;
    }
    if (userInfo) {
      this.globalData.userInfo = userInfo;
    }
  }
})