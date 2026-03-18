const { request } = require('../../utils/request');

Page({
  data: {
    isEdit: false,
    formData: {
      id: null,
      title: '',
      content: '',
      priority: 1,
      deadline: ''
    },
    priorityList: [
      { label: '低优先级', value: 1 },
      { label: '中优先级', value: 2 },
      { label: '高优先级', value: 3 }
    ],
    priorityIndex: 0,
    timeValue: '00:00'
  },

  onLoad(options) {
    if (options.task) {
      const task = JSON.parse(decodeURIComponent(options.task));
      const priorityIndex = this.data.priorityList.findIndex(p => p.value === task.priority);
      let timeValue = '00:00';
      if (task.deadline) {
        const parts = task.deadline.split(' ');
        if (parts.length === 2) {
          timeValue = parts[1].substring(0, 5);
        }
        task.deadline = parts[0];
      }
      this.setData({
        isEdit: true,
        formData: task,
        priorityIndex,
        timeValue
      });
      wx.setNavigationBarTitle({ title: '编辑任务' });
    }
  },

  onTitleInput(e) {
    this.setData({ 'formData.title': e.detail.value });
  },

  onContentInput(e) {
    this.setData({ 'formData.content': e.detail.value });
  },

  onPriorityChange(e) {
    const index = parseInt(e.detail.value);
    this.setData({
      priorityIndex: index,
      'formData.priority': this.data.priorityList[index].value
    });
  },

  onDateChange(e) {
    this.setData({ 'formData.deadline': e.detail.value });
  },

  onTimeChange(e) {
    this.setData({ timeValue: e.detail.value });
  },

  async onSubmit() {
    if (!this.data.formData.title.trim()) {
      wx.showToast({ title: '请输入任务标题', icon: 'none' });
      return;
    }
    try {
      let deadline = null;
      if (this.data.formData.deadline) {
        deadline = `${this.data.formData.deadline} ${this.data.timeValue}:00`;
      }
      const data = {
        ...this.data.formData,
        deadline
      };
      if (this.data.isEdit) {
        await request({
          url: '/api/task/update',
          method: 'PUT',
          data
        });
        wx.showToast({ title: '修改成功', icon: 'success' });
      } else {
        await request({
          url: '/api/task/add',
          method: 'POST',
          data
        });
        wx.showToast({ title: '添加成功', icon: 'success' });
      }
      setTimeout(() => {
        wx.navigateBack();
      }, 1500);
    } catch (error) {
      console.error('操作失败', error);
    }
  },

  goBack() {
    wx.navigateBack();
  }
});