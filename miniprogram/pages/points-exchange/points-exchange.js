const api = require('../../utils/api.js');
const util = require('../../utils/util.js');

Page({
  data: {
    userPoints: 0,
    exchangeItems: [],
    isLoading: false,
    isExchanging: false,
    showExchangeModal: false,
    selectedItem: {}
  },

  onLoad: function (options) {
    this.checkLogin();
    this.loadData();
  },

  onShow: function () {
    if (util.checkLogin()) {
      this.loadUserPoints();
    }
  },

  // 下拉刷新
  onPullDownRefresh: function () {
    this.loadData().finally(() => {
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

  // 加载数据
  loadData: function () {
    this.setData({ isLoading: true });

    return Promise.all([
      this.loadUserPoints(),
      this.loadExchangeItems()
    ]).finally(() => {
      this.setData({ isLoading: false });
    });
  },

  // 加载用户积分
  loadUserPoints: function () {
    return api.userApi.getMe()
      .then(res => {
        this.setData({
          userPoints: res.total_points || 0
        });
      })
      .catch(err => {
        console.error('获取用户积分失败:', err);
      });
  },

  // 加载可兑换物品列表
  loadExchangeItems: function () {
    return api.get('/exchange-items')
      .then(res => {
        this.setData({
          exchangeItems: res || []
        });
      })
      .catch(err => {
        console.error('获取兑换物品列表失败:', err);
        wx.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      });
  },

  // 显示兑换确认弹窗
  showExchangeModal: function (e) {
    const item = e.currentTarget.dataset.item;
    this.setData({
      showExchangeModal: true,
      selectedItem: item
    });
  },

  // 隐藏兑换确认弹窗
  hideExchangeModal: function () {
    this.setData({
      showExchangeModal: false,
      selectedItem: {}
    });
  },

  // 阻止事件冒泡
  preventBubble: function () {
    // 阻止事件冒泡，防止点击弹窗内容时关闭弹窗
  },

  // 确认兑换
  confirmExchange: function () {
    const { selectedItem, userPoints, isExchanging } = this.data;

    if (isExchanging) {
      return;
    }

    // 检查积分是否足够
    if (userPoints < selectedItem.points_required) {
      wx.showToast({
        title: '积分不足',
        icon: 'none'
      });
      return;
    }

    this.setData({ isExchanging: true });

    // 判断是纯积分兑换还是混合支付
    if (selectedItem.cash_price > 0) {
      // 积分+现金混合支付
      this.handleMixedPayment(selectedItem);
    } else {
      // 纯积分兑换
      this.handlePointsOnlyExchange(selectedItem);
    }
  },

  // 纯积分兑换流程
  handlePointsOnlyExchange: function (item) {
    const exchangeData = {
      item_id: item.id,
      points_used: item.points_required,
      cash_paid: 0
    };

    api.post('/points/exchange', exchangeData)
      .then(res => {
        this.handleExchangeSuccess(res);
      })
      .catch(err => {
        this.handleExchangeError(err);
      });
  },

  // 积分+现金混合支付处理
  handleMixedPayment: function (item) {
    const exchangeData = {
      item_id: item.id,
      points_used: item.points_required,
      cash_paid: item.cash_price
    };

    // 先调用后端接口创建兑换订单
    api.post('/points/exchange', exchangeData)
      .then(res => {
        // 检查是否需要现金支付
        if (res.need_payment && res.payment_params) {
          // 需要现金支付，调起微信支付
          this.requestWechatPay(res.payment_params, res.exchange_id);
        } else {
          // 不需要现金支付，直接成功
          this.handleExchangeSuccess(res);
        }
      })
      .catch(err => {
        this.handleExchangeError(err);
      });
  },

  // 调起微信支付
  requestWechatPay: function (paymentParams, exchangeId) {
    const that = this;

    // 校验支付参数完整性
    const requiredParams = ['timeStamp', 'nonceStr', 'package', 'paySign'];
    const missingParams = requiredParams.filter(param => !paymentParams[param]);
    
    if (missingParams.length > 0) {
      console.error('支付参数缺失:', missingParams);
      this.setData({ isExchanging: false });
      wx.showToast({
        title: '支付参数错误，请重试',
        icon: 'none'
      });
      return;
    }

    wx.requestPayment({
      timeStamp: paymentParams.timeStamp,
      nonceStr: paymentParams.nonceStr,
      package: paymentParams.package,
      signType: paymentParams.signType || 'RSA',
      paySign: paymentParams.paySign,
      success: function (res) {
        // 支付成功，通知后端确认支付
        that.confirmPayment(exchangeId);
      },
      fail: function (err) {
        console.error('支付失败:', err);
        that.setData({ isExchanging: false });

        if (err.errMsg && err.errMsg.includes('cancel')) {
          wx.showToast({
            title: '已取消支付',
            icon: 'none'
          });
        } else {
          wx.showToast({
            title: '支付失败，请重试',
            icon: 'none'
          });
        }
      }
    });
  },

  // 确认支付成功
  confirmPayment: function (exchangeId) {
    api.post(`/api/v1/points/exchange/${exchangeId}/confirm-payment`, {})
      .then(res => {
        this.handleExchangeSuccess(res);
      })
      .catch(err => {
        console.error('确认支付失败:', err);
        this.setData({ isExchanging: false });
        wx.showToast({
          title: '支付确认失败，请联系客服',
          icon: 'none',
          duration: 3000
        });
      });
  },

  // 处理兑换成功
  handleExchangeSuccess: function (res) {
    this.setData({
      isExchanging: false,
      showExchangeModal: false,
      selectedItem: {}
    });

    // 刷新用户积分
    this.loadUserPoints();

    wx.showToast({
      title: '兑换成功',
      icon: 'success',
      duration: 2000
    });

    // 可以在这里添加跳转逻辑，比如跳转到兑换记录页面
    // wx.navigateTo({
    //   url: '/pages/exchange-records/exchange-records'
    // });
  },

  // 处理兑换错误
  handleExchangeError: function (err) {
    console.error('兑换失败:', err);
    this.setData({ isExchanging: false });

    const errorMsg = err.message || '兑换失败，请重试';
    wx.showToast({
      title: errorMsg,
      icon: 'none',
      duration: 3000
    });
  }
});
