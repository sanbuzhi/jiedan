const api = require('../../utils/api.js');
const util = require('../../utils/util.js');

const PAGE_SIZE = 20;

Page({
  data: {
    totalPoints: 0,
    pointRecords: [],
    isLoading: false,
    hasMore: true,
    skip: 0
  },

  onLoad: function (options) {
    this.checkLogin();
    this.loadData();
  },

  onShow: function () {
    if (util.checkLogin()) {
      this.refreshData();
    }
  },

  // 下拉刷新
  onPullDownRefresh: function () {
    this.refreshData().finally(() => {
      wx.stopPullDownRefresh();
    });
  },

  // 上拉加载更多
  onReachBottom: function () {
    if (this.data.hasMore && !this.data.isLoading) {
      this.loadMore();
    }
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

  // 刷新数据
  refreshData: function () {
    this.setData({
      skip: 0,
      hasMore: true,
      pointRecords: []
    });
    return this.loadData();
  },

  // 加载数据
  loadData: function () {
    this.setData({ isLoading: true });

    return Promise.all([
      this.loadUserInfo(),
      this.loadPointRecords()
    ]).finally(() => {
      this.setData({ isLoading: false });
    });
  },

  // 加载用户信息（获取总积分）
  loadUserInfo: function () {
    return api.userApi.getMe()
      .then(res => {
        this.setData({
          totalPoints: res.total_points || 0
        });
      })
      .catch(err => {
        console.error('获取用户信息失败:', err);
      });
  },

  // 加载积分记录
  loadPointRecords: function () {
    const { skip } = this.data;

    return api.userApi.getPoints(skip, PAGE_SIZE)
      .then(res => {
        const records = res || [];
        const currentRecords = this.data.pointRecords;

        this.setData({
          pointRecords: skip === 0 ? records : currentRecords.concat(records),
          hasMore: records.length === PAGE_SIZE
        });
      })
      .catch(err => {
        console.error('获取积分记录失败:', err);
        wx.showToast({
          title: '加载失败，请重试',
          icon: 'none'
        });
      });
  },

  // 加载更多
  loadMore: function () {
    const { skip, pointRecords } = this.data;

    this.setData({
      skip: skip + PAGE_SIZE,
      isLoading: true
    });

    this.loadPointRecords().finally(() => {
      this.setData({ isLoading: false });
    });
  },

  // 格式化日期
  formatDate: function (date) {
    return util.formatDate(date, 'yyyy-MM-dd hh:mm');
  }
});
