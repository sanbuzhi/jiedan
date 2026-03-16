const api = require('../../../utils/api.js');
const util = require('../../../utils/util.js');

Page({
  data: {
    currentStep: 1,
    scrollTop: 0,
    // Step 1 & 2 数据
    selectedValue: '',
    otherValue: '',
    // Step 3 数据
    needDeploy: false,
    // Step 4 数据
    formData: {
      totalUsers: '',
      dailyActiveUsers: '',
      concurrentUsers: ''
    },
    errors: {},
    showSuggestion: false,
    suggestion: null,
    // Step 5 数据
    urgency: '',
    selectedDays: 30,
    dayIndex: 2,
    dayOptions: ['7天', '14天', '30天', '60天', '90天'],
    urgencyFee: 0,
    urgencyMultiplier: 1,
    dayHint: '',
    // 步骤配置
    stepConfig: {}
  },

  // 步骤配置数据
  stepConfigs: {
    1: {
      title: '选择项目角色',
      subtitle: '让我们更好地了解您的需求',
      tip: '选择正确的身份有助于我们为您提供更精准的服务和报价',
      storageKey: 'requirement_step1',
      hasOther: true,
      otherPlaceholder: '请输入您的身份类型',
      options: [
        { value: 'company', icon: '🏢', title: '公司/企业', desc: '有正式商业需求的企业客户' },
        { value: 'individual', icon: '👤', title: '个人经营者', desc: '自由职业者或个体户' },
        { value: 'student', icon: '🎓', title: '学生（毕设/大作业）', desc: '毕业设计或课程作业需求' },
        { value: 'unclear', icon: '❓', title: '不明确', desc: '暂时不确定自己的身份类型' },
        { value: 'other', icon: '✏️', title: '其他', desc: '自定义身份类型' }
      ]
    },
    2: {
      title: '选择项目类型',
      subtitle: '您需要开发什么类型的项目？',
      tip: '不同的项目类型会影响开发周期和报价，请根据实际需求选择',
      storageKey: 'requirement_step2',
      hasOther: true,
      otherPlaceholder: '请输入您的项目类型',
      options: [
        { value: 'wechat', icon: '💬', title: '微信小程序', desc: '基于微信生态的小程序开发' },
        { value: 'douyin', icon: '🎵', title: '抖音小程序', desc: '抖音/头条系小程序开发' },
        { value: 'website', icon: '🌐', title: '网站系统', desc: '企业官网、Web应用、后台系统' },
        { value: 'crawler', icon: '🕷️', title: '爬虫/数据采集', desc: '数据抓取、自动化采集脚本' },
        { value: 'unclear', icon: '❓', title: '不明确', desc: '暂时不确定项目类型' }, 
        { value: 'other', icon: '✏️', title: '其他', desc: '自定义项目类型' }
      ]
    },
    3: {
      title: '是否需要上线',
      subtitle: '配置系统的部署和上线需求',
      tip: '上线部署包含服务器、域名等基础设施费用',
      storageKey: 'requirement_step3',
      serviceItems: [
        '云服务器租用费用',
        '域名注册与备案',
        'SSL证书配置',
        'CDN加速服务',
        '数据库托管服务'
      ],
      feeItems: [
        { name: '基础配置', price: '¥200-500/月' },
        { name: '标准配置', price: '¥500-1500/月' },
        { name: '高级配置', price: '¥1500-5000/月' }
      ]
    },
    4: {
      title: '预估系统流量',
      subtitle: '用于评估服务器配置需求',
      tip: '流量预估用于评估服务器配置，影响项目预算',
      storageKey: 'requirement_step4',
      inputs: [
        { key: 'totalUsers', label: '系统总人数', unit: '人', placeholder: '请输入预计总用户数' },
        { key: 'dailyActiveUsers', label: '日活用户数', unit: '人', placeholder: '请输入预计日活用户数' },
        { key: 'concurrentUsers', label: '系统并发数', unit: '人', placeholder: '请输入预计最大并发数' }
      ],
      infoItems: [
        { key: 'total', title: '系统总人数', desc: '预计注册使用该系统的总用户数量' },
        { key: 'daily', title: '日活用户数', desc: '平均每天活跃使用的用户数量' },
        { key: 'concurrent', title: '系统并发数', desc: '同一时刻最多在线使用的用户数量' }
      ]
    },
    5: {
      title: '选择交付周期',
      subtitle: '根据紧急程度选择合适的交付时间',
      tip: '紧急程度会影响项目预算，建议根据实际需求选择',
      storageKey: 'requirement_step5',
      urgencyOptions: [
        { value: 'relaxed', icon: '🐢', name: '宽松', time: '1-3个月', tag: '', tagClass: '' },
        { value: 'normal', icon: '🚶', name: '正常', time: '2-4周', tag: '', tagClass: '' },
        { value: 'urgent', icon: '🏃', name: '加急', time: '1-2周', tag: '+30%', tagClass: '' },
        { value: 'emergency', icon: '🚀', name: '特急', time: '3-7天', tag: '+50%', tagClass: 'hot' }
      ],
      deliveryNotes: [
        '交付周期从需求确认后开始计算',
        '加急项目将优先分配开发资源',
        '特急项目可能需要增加开发人员',
        '具体交付时间以合同为准'
      ]
    }
  },

  onLoad(options) {
    // 获取步骤参数
    const step = parseInt(options.step) || 1;
    const stepConfig = this.stepConfigs[step] || {};
    this.setData({
      currentStep: step,
      stepConfig: stepConfig
    });

    // 加载对应步骤的数据
    this.loadStepData(step);
  },

  onShow() {
    // 检查登录状态
    if (!util.checkLogin()) {
      wx.redirectTo({ url: '/pages/login/login' });
      return;
    }
    
    // 设置导航栏标题
    const titles = {
      1: '选择身份',
      2: '选择类型',
      3: '上线需求',
      4: '流量预估',
      5: '交付周期'
    };
    wx.setNavigationBarTitle({
      title: titles[this.data.currentStep] || '需求收集'
    });
  },

  // 加载步骤数据
  loadStepData(step) {
    const config = this.stepConfigs[step];
    const savedData = wx.getStorageSync(config.storageKey);

    if (savedData) {
      switch (step) {
        case 1:
        case 2:
          this.setData({
            selectedValue: savedData.selectedType || '',
            otherValue: savedData.otherType || ''
          });
          break;
        case 3:
          this.setData({
            needDeploy: savedData.needDeploy || false
          });
          break;
        case 4:
          this.setData({
            formData: {
              totalUsers: savedData.totalUsers || '',
              dailyActiveUsers: savedData.dailyActiveUsers || '',
              concurrentUsers: savedData.concurrentUsers || ''
            }
          });
          // 如果有数据，计算建议
          if (savedData.totalUsers || savedData.dailyActiveUsers || savedData.concurrentUsers) {
            this.calculateSuggestion();
          }
          break;
        case 5:
          this.setData({
            urgency: savedData.urgency || '',
            selectedDays: savedData.selectedDays || 30,
            dayIndex: savedData.dayIndex || 2
          });
          this.calculateUrgencyFee();
          break;
      }
    }
  },

  // Step 1 & 2: 选择选项
  onSelect(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({
      selectedValue: value
    });

    if (value !== 'other') {
      this.setData({ otherValue: '' });
    }
  },

  // Step 1 & 2: 其他输入
  onOtherInput(e) {
    this.setData({
      otherValue: e.detail.value
    });
  },

  // Step 3: 部署开关
  onDeployChange(e) {
    this.setData({
      needDeploy: e.detail.value
    });
  },

  // Step 4: 输入变化
  onInputChange(e) {
    const key = e.currentTarget.dataset.key;
    const value = e.detail.value;
    this.setData({
      [`formData.${key}`]: value
    });
    // 清除错误
    if (this.data.errors[key]) {
      this.setData({
        [`errors.${key}`]: ''
      });
    }
  },

  // Step 4: 输入失焦验证
  onInputBlur(e) {
    const key = e.currentTarget.dataset.key;
    this.validateInput(key);
    this.calculateSuggestion();
  },

  // Step 4: 验证输入
  validateInput(key) {
    const value = this.data.formData[key];
    const errors = { ...this.data.errors };

    if (value && (isNaN(value) || parseInt(value) < 0)) {
      errors[key] = '请输入有效的数字';
    } else {
      delete errors[key];
    }

    this.setData({ errors });
    return Object.keys(errors).length === 0;
  },

  // Step 4: 计算配置建议
  calculateSuggestion() {
    const { totalUsers, dailyActiveUsers, concurrentUsers } = this.data.formData;
    const total = parseInt(totalUsers) || 0;
    const daily = parseInt(dailyActiveUsers) || 0;
    const concurrent = parseInt(concurrentUsers) || 0;

    if (!total && !daily && !concurrent) {
      this.setData({ showSuggestion: false });
      return;
    }

    let level, name, desc, specs, price;

    if (concurrent > 1000 || daily > 10000 || total > 100000) {
      level = 'enterprise';
      name = '企业级配置';
      desc = '适合大型应用，高并发场景';
      specs = [
        { label: 'CPU', value: '8核+' },
        { label: '内存', value: '16GB+' },
        { label: '带宽', value: '50Mbps+' },
        { label: '存储', value: '500GB SSD' }
      ];
      price = '¥3000+/月';
    } else if (concurrent > 100 || daily > 1000 || total > 10000) {
      level = 'advanced';
      name = '高级配置';
      desc = '适合中型应用，中等并发';
      specs = [
        { label: 'CPU', value: '4核' },
        { label: '内存', value: '8GB' },
        { label: '带宽', value: '20Mbps' },
        { label: '存储', value: '200GB SSD' }
      ];
      price = '¥1000-3000/月';
    } else if (concurrent > 10 || daily > 100 || total > 1000) {
      level = 'standard';
      name = '标准配置';
      desc = '适合小型应用，低并发';
      specs = [
        { label: 'CPU', value: '2核' },
        { label: '内存', value: '4GB' },
        { label: '带宽', value: '5Mbps' },
        { label: '存储', value: '100GB SSD' }
      ];
      price = '¥300-1000/月';
    } else {
      level = 'basic';
      name = '基础配置';
      desc = '适合个人项目，测试环境';
      specs = [
        { label: 'CPU', value: '1核' },
        { label: '内存', value: '2GB' },
        { label: '带宽', value: '1Mbps' },
        { label: '存储', value: '50GB SSD' }
      ];
      price = '¥100-300/月';
    }

    this.setData({
      showSuggestion: true,
      suggestion: { level, name, desc, specs, price }
    });
  },

  // Step 5: 紧急程度选择
  onUrgencyChange(e) {
    const value = e.currentTarget.dataset.value;
    this.setData({ urgency: value });
    this.calculateUrgencyFee();
  },

  // Step 5: 天数选择
  onDayChange(e) {
    const index = e.detail.value;
    const days = [7, 14, 30, 60, 90];
    const hints = [
      '7天：特急项目，需要额外加急费用',
      '14天：加急项目，需要额外加急费用',
      '30天：正常项目周期',
      '60天：宽松项目周期',
      '90天：非常宽松的项目周期'
    ];
    this.setData({
      dayIndex: index,
      selectedDays: days[index],
      dayHint: hints[index]
    });
  },

  // Step 5: 计算加急费用
  calculateUrgencyFee() {
    const { urgency } = this.data;
    let multiplier = 1;
    let fee = 0;

    switch (urgency) {
      case 'urgent':
        multiplier = 1.3;
        fee = 3000;
        break;
      case 'emergency':
        multiplier = 1.5;
        fee = 5000;
        break;
    }

    this.setData({
      urgencyMultiplier: multiplier,
      urgencyFee: fee
    });
  },

  // 上一步
  goBack() {
    const currentStep = this.data.currentStep;
    if (currentStep > 1) {
      // 保存当前步骤数据
      this.saveCurrentStepData();
      // 切换到上一步
      this.switchStep(currentStep - 1);
    }
  },

  // 下一步
  goNext() {
    const currentStep = this.data.currentStep;

    // 保存当前步骤数据
    this.saveCurrentStepData();

    if (currentStep < 5) {
      // 切换到下一步
      this.switchStep(currentStep + 1);
    } else {
      // 完成，跳转到结果页
      this.completeRequirement();
    }
  },

  // 切换步骤
  switchStep(step) {
    this.setData({
      currentStep: step,
      stepConfig: this.stepConfigs[step],
      scrollTop: 0
    });

    // 加载新步骤的数据
    this.loadStepData(step);

    // 更新导航栏标题
    const titles = {
      1: '选择身份',
      2: '选择类型',
      3: '上线需求',
      4: '流量预估',
      5: '交付周期'
    };
    wx.setNavigationBarTitle({ title: titles[step] || '需求收集' });
  },

  // 保存当前步骤数据
  saveCurrentStepData() {
    const step = this.data.currentStep;
    const config = this.stepConfigs[step];
    let dataToSave = { timestamp: Date.now() };

    switch (step) {
      case 1:
      case 2:
        dataToSave.selectedType = this.data.selectedValue;
        dataToSave.otherType = this.data.selectedValue === 'other' ? this.data.otherValue.trim() : '';
        break;
      case 3:
        dataToSave.needDeploy = this.data.needDeploy;
        break;
      case 4:
        dataToSave.totalUsers = this.data.formData.totalUsers;
        dataToSave.dailyActiveUsers = this.data.formData.dailyActiveUsers;
        dataToSave.concurrentUsers = this.data.formData.concurrentUsers;
        break;
      case 5:
        dataToSave.urgency = this.data.urgency;
        dataToSave.selectedDays = this.data.selectedDays;
        dataToSave.dayIndex = this.data.dayIndex;
        dataToSave.urgencyFee = this.data.urgencyFee;
        break;
    }

    wx.setStorageSync(config.storageKey, dataToSave);

    // 同时保存到全局数据
    const app = getApp();
    if (app) {
      app.globalData = app.globalData || {};
      app.globalData.requirementData = app.globalData.requirementData || {};
      Object.assign(app.globalData.requirementData, dataToSave);
    }
  },

  // 完成需求收集
  completeRequirement() {
    // 收集所有步骤的数据
    const step1 = wx.getStorageSync('requirement_step1') || {};
    const step2 = wx.getStorageSync('requirement_step2') || {};
    const step3 = wx.getStorageSync('requirement_step3') || {};
    const step4 = wx.getStorageSync('requirement_step4') || {};
    const step5 = wx.getStorageSync('requirement_step5') || {};

    // 构建请求数据（兼容后端RequirementCreate DTO）
    const requirementData = {
      // ========== 基础信息字段（step页面主要数据）==========
      userType: step1.selectedType || 'unclear',
      userTypeOther: step1.otherType || '',
      projectType: step2.selectedType || 'unclear',
      projectTypeOther: step2.otherType || '',
      needOnline: step3.needDeploy || false,
      urgency: step5.urgency || 'normal',
      deliveryPeriod: step5.selectedDays || 30,
      uiStyle: 'modern', // 默认UI风格
      traffic: {
        totalUsers: step4.totalUsers ? parseInt(step4.totalUsers) : 0,
        dau: step4.dailyActiveUsers ? parseInt(step4.dailyActiveUsers) : 0,
        concurrent: step4.concurrentUsers ? parseInt(step4.concurrentUsers) : 0
      },
      
      // ========== 详细需求字段（提供默认值以兼容后端）==========
      requirementDescription: '',  // step页面未收集详细描述
      selectedFunctions: [],       // step页面未收集功能点
      customFunctions: [],         // step页面未收集自定义功能
      materials: {},               // step页面未收集材料
      visualStyle: 'modern',       // 默认视觉风格
      deploymentMode: step3.needDeploy ? 'cloud' : 'none',
      quotation: {}                // step页面未收集报价
    };

    // 保存到全局
    const app = getApp();
    if (app) {
      app.globalData = app.globalData || {};
      app.globalData.requirementData = {
        step1, step2, step3, step4, step5
      };
    }

    // 显示加载中
    wx.showLoading({
      title: '创建项目中...',
      mask: true
    });

    try {
      // 调用后端API创建项目
      if (!api || !api.requirementApi || !api.requirementApi.createRequirement) {
        throw new Error('API模块加载失败');
      }
      
      api.requirementApi.createRequirement(requirementData)
        .then(res => {
          wx.hideLoading();
          console.log('项目创建成功:', res);
          
          // 获取创建的需求ID
          const requirementId = res.data?.id || res.id;
          
          if (requirementId) {
            // 清除step页面本地存储
            wx.removeStorageSync('requirement_step1');
            wx.removeStorageSync('requirement_step2');
            wx.removeStorageSync('requirement_step3');
            wx.removeStorageSync('requirement_step4');
            wx.removeStorageSync('requirement_step5');
            
            // 设置标记，通知index页面需要刷新
            wx.setStorageSync('index_need_refresh', true);
            
            // 显示创建成功提示
            wx.showToast({
              title: '项目创建成功',
              icon: 'success',
              duration: 1500
            });
            
            // 延迟跳转到首页，让用户看到成功提示
            setTimeout(() => {
              // 跳转到首页，用户可以在首页点击"明确需求"节点进入step_all
              wx.switchTab({
                url: '/pages/index/index'
              });
            }, 1500);
          } else {
            // 如果没有获取到ID，跳转到首页
            wx.switchTab({
              url: '/pages/index/index'
            });
          }
        })
        .catch(err => {
          wx.hideLoading();
          console.error('项目创建失败:', err);
          
          // 显示错误提示
          wx.showToast({
            title: '创建项目失败，请重试',
            icon: 'none',
            duration: 3000
          });
        });
    } catch (error) {
      wx.hideLoading();
      console.error('API调用失败:', error);
      
      // 显示错误提示
      wx.showToast({
        title: '系统错误，请重试',
        icon: 'none',
        duration: 3000
      });
    }
  }
});