/**
 * UI Gallery 页面 - 交互视觉资源库
 * 展示多种UI设计范式供用户选择
 */

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 筛选分类
    categories: [
      { id: 'all', name: '全部' },
      { id: 'minimal', name: '极简' },
      { id: 'business', name: '商务' },
      { id: 'creative', name: '创意' },
      { id: 'tech', name: '科技' },
      { id: 'retro', name: '复古' }
    ],

    // 当前选中的分类
    selectedCategory: 'all',

    // UI设计范式列表
    styles: [
      { id: 'minimal-white', name: '极简白', category: 'minimal', color: '#f5f5f5', textColor: '#333333' },
      { id: 'minimal-black', name: '极简黑', category: 'minimal', color: '#1a1a1a', textColor: '#ffffff' },
      { id: 'business-blue', name: '商务蓝', category: 'business', color: '#1e3a5f', textColor: '#ffffff' },
      { id: 'business-gray', name: '商务灰', category: 'business', color: '#4a5568', textColor: '#ffffff' },
      { id: 'vibrant-orange', name: '活力橙', category: 'creative', color: '#ff6b35', textColor: '#ffffff' },
      { id: 'vibrant-pink', name: '活力粉', category: 'creative', color: '#ff6b9d', textColor: '#ffffff' },
      { id: 'tech-purple', name: '科技紫', category: 'tech', color: '#6b5ce7', textColor: '#ffffff' },
      { id: 'tech-cyan', name: '科技青', category: 'tech', color: '#00d4ff', textColor: '#000000' },
      { id: 'retro-brown', name: '复古棕', category: 'retro', color: '#8b6914', textColor: '#ffffff' },
      { id: 'retro-green', name: '复古绿', category: 'retro', color: '#2d5016', textColor: '#ffffff' },
      { id: 'nature-blue', name: '自然蓝', category: 'creative', color: '#4facfe', textColor: '#ffffff' },
      { id: 'warm-yellow', name: '温暖黄', category: 'creative', color: '#f6d365', textColor: '#333333' }
    ],

    // 当前选中的风格
    selectedStyle: null
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 可以接收外部传入的默认选中项
    if (options.selected) {
      this.setData({
        selectedStyle: options.selected
      });
    }
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 设置导航栏标题
    wx.setNavigationBarTitle({
      title: '交互视觉资源库'
    });
  },

  /**
   * 选择分类
   */
  onSelectCategory(e) {
    const categoryId = e.currentTarget.dataset.id;
    this.setData({
      selectedCategory: categoryId
    });
  },

  /**
   * 选择UI风格
   */
  onSelectStyle(e) {
    const styleId = e.currentTarget.dataset.id;
    const styleName = e.currentTarget.dataset.name;

    this.setData({
      selectedStyle: styleId
    });
  },

  /**
   * 确认采用此方案
   */
  onConfirmStyle() {
    const { selectedStyle, styles } = this.data;

    if (!selectedStyle) {
      wx.showToast({
        title: '请先选择一个方案',
        icon: 'none'
      });
      return;
    }

    // 查找选中的风格信息
    const selectedStyleInfo = styles.find(item => item.id === selectedStyle);

    if (!selectedStyleInfo) {
      wx.showToast({
        title: '选择失败',
        icon: 'none'
      });
      return;
    }

    // 返回上一页并传递选中的方案
    const pages = getCurrentPages();
    const prevPage = pages[pages.length - 2];

    if (prevPage) {
      // 调用上一页的方法更新数据
      prevPage.updateStageData(2, {
        visualStyle: selectedStyle,
        visualStyleFromGallery: true
      });
    }

    wx.showToast({
      title: '已采用此方案',
      icon: 'success'
    });

    // 延迟返回
    setTimeout(() => {
      wx.navigateBack();
    }, 1000);
  },

  /**
   * 返回上一页
   */
  onNavigateBack() {
    wx.navigateBack();
  }
});
