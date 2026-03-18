const app = getApp();
const { request } = require('../../utils/request');
const { setStorage } = require('../../utils/storage');

Page({
  data: {
    userInfo: null,
    taskList: [],
    currentTab: 0
  },

  onLoad() {
    // 检查是否已登录
    if (app.globalData.userInfo) {
      this.setData({ userInfo: app.globalData.userInfo });
      this.loadTasks();
    }
  },

  onShow() {
    // 页面显示时刷新任务列表
    if (this.data.userInfo) {
      this.loadTasks();
    }
  },

  onTabItemTap(e) {
    this.setData({ currentTab: e.index });
    this.loadTasks();
  },

  // 微信登录
  async handleLogin() {
    const that = this;
    wx.getUserProfile({
      desc: '用于完善用户资料',
      success: async (profileRes) => {
        wx.login({
          success: async (loginRes) => {
            try {
              const res = await request({
                url: '/api/user/login',
                method: 'POST',
                data: {
                  code: loginRes.code,
                  nickname: profileRes.userInfo.nickName,
                  avatarUrl: profileRes.userInfo.avatarUrl
                }
              });
              // 保存登录信息
              app.globalData.token = res.data.token;
              app.globalData.userInfo = {
                nickname: res.data.nickname,
                avatarUrl: res.data.avatarUrl
              };
              setStorage('token', res.data.token);
              setStorage('userInfo', app.globalData.userInfo);
              that.setData({ userInfo: app.globalData.userInfo });
              that.loadTasks();
            } catch (error) {
              console.error('登录失败', error);
            }
          }
        });
      }
    });
  },

  // 加载任务列表
  async loadTasks() {
    try {
      let status = null;
      if (this.data.currentTab === 1) status = 0;
      if (this.data.currentTab === 2) status = 1;
      const res = await request({
        url: '/api/task/list',
        method: 'GET',
        data: { status }
      });
      this.setData({ taskList: res.data });
    } catch (error) {
      console.error('加载任务失败', error);
    }
  },

  // 切换任务状态
  async toggleStatus(e) {
    const { id, status } = e.currentTarget.dataset;
    try {
      await request({
        url: `/api/task/status/${id}/${status === 1 ? 0 : 1}`,
        method: 'PUT'
      });
      this.loadTasks();
    } catch (error) {
      console.error('更新状态失败', error);
    }
  },

  // 编辑任务
  editTask(e) {
    const { id } = e.currentTarget.dataset;
    const task = this.data.taskList.find(t => t.id === id);
    wx.navigateTo({
      url: `/pages/add-task/add-task?task=${encodeURIComponent(JSON.stringify(task))}`
    });
  },

  // 删除任务
  deleteTask(e) {
    const { id } = e.currentTarget.dataset;
    const that = this;
    wx.showModal({
      title: '提示',
      content: '确定要删除这个任务吗？',
      success: async (res) => {
        if (res.confirm) {
          try {
            await request({
              url: `/api/task/delete/${id}`,
              method: 'DELETE'
            });
            that.loadTasks();
          } catch (error) {
            console.error('删除任务失败', error);
          }
        }
      }
    });
  },

  // 跳转到添加页面
  goToAdd() {
    wx.navigateTo({
      url: '/pages/add-task/add-task'
    });
  }
});