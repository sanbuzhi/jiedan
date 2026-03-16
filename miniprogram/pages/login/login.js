const api = require('../../utils/api.js');
const util = require('../../utils/util.js');

Page({
  data: {
    canLogin: false,
    isLoading: false,
    referrerCode: null
  },

  onLoad: function (options) {
    // 检查是否已登录
    if (util.checkLogin()) {
      wx.switchTab({
        url: '/pages/index/index'
      });
      return;
    }

    // 获取推荐码
    const referrerCode = util.getReferrerCode();
    if (referrerCode) {
      this.setData({
        referrerCode: referrerCode
      });
    }

    // 检查微信登录权限
    this.checkLoginPermission();
  },

  onShow: function () {
    // 更新推荐码
    const referrerCode = util.getReferrerCode();
    if (referrerCode !== this.data.referrerCode) {
      this.setData({
        referrerCode: referrerCode
      });
    }
  },

  // 检查登录权限
  checkLoginPermission: function () {
    wx.getSetting({
      success: (res) => {
        if (res.authSetting['scope.userInfo']) {
          // 已经授权，可以直接调用 getUserInfo 获取头像昵称
          this.setData({
            canLogin: true
          });
        } else {
          // 未授权，需要点击按钮授权
          this.setData({
            canLogin: true
          });
        }
      }
    });
  },

  // 直接登录，不再获取微信用户信息
  onGetUserProfile: function () {
    this.handleLogin();
  },

  // 处理登录
  handleLogin: function () {
    this.setData({
      isLoading: true
    });

    // 调用微信登录获取code
    wx.login({
      success: (res) => {
        if (res.code) {
          this.doLogin(res.code);
        } else {
          this.setData({
            isLoading: false
          });
          wx.showToast({
            title: '微信登录失败',
            icon: 'none'
          });
        }
      },
      fail: () => {
        this.setData({
          isLoading: false
        });
        wx.showToast({
          title: '微信登录失败',
          icon: 'none'
        });
      }
    });
  },

  // 调用后端登录接口
  doLogin: function (code) {
    const referrerCode = this.data.referrerCode;

    // 不再传递用户信息，后端使用默认头像和昵称
    api.wechatLogin.login(code, null, referrerCode)
      .then(res => {
        // 保存登录信息
        const app = getApp();
        const token = res.accessToken || res.access_token;
        app.setLoginData(token, {});

        // 清除推荐码
        if (referrerCode) {
          util.clearReferrerCode();
        }

        wx.showToast({
          title: '登录成功',
          icon: 'success'
        });

        // 获取完整用户信息并下载头像
        api.userApi.getMe()
          .then(userInfo => {
            // 确保 avatar 是完整 URL
            const userInfoWithFullAvatar = { ...userInfo };
            if (userInfoWithFullAvatar.avatar) {
              userInfoWithFullAvatar.avatar = util.getFullAvatarUrl(userInfoWithFullAvatar.avatar);
            }
            app.setLoginData(token, userInfoWithFullAvatar);
            // 下载并缓存头像
            if (userInfo.avatar) {
              util.downloadAndCacheAvatar(userInfo.avatar)
                .then(cachedPath => {
                  console.log('头像缓存成功:', cachedPath);
                })
                .catch(err => {
                  console.error('头像缓存失败:', err);
                });
            }
          })
          .catch(err => {
            console.error('获取用户信息失败:', err);
          });

        // 跳转到首页
        setTimeout(() => {
          this.setData({
            isLoading: false
          });
          wx.switchTab({
            url: '/pages/index/index'
          });
        }, 1000);
      })
      .catch(err => {
        this.setData({
          isLoading: false
        });
        console.error('登录失败:', err);
        wx.showToast({
          title: err.message || '登录失败',
          icon: 'none'
        });
      });
  },

  // 显示用户协议
  showUserAgreement: function () {
    wx.showModal({
      title: '用户协议',
      content: '这里是用户协议内容...',
      showCancel: false
    });
  },

  // 显示隐私政策
  showPrivacyPolicy: function () {
    wx.showModal({
      title: '隐私政策',
      content: '这里是隐私政策内容...',
      showCancel: false
    });
  }
});
