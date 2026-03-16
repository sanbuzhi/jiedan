const util = require('../../../utils/util.js');

Page({
  /**
   * 页面的初始数据
   */
  data: {
    // 当前阶段 (1-3)
    currentStage: 1,

    // 阶段数据
    stageData: {
      // 阶段1：需求智能解析
      stage1: {
        requirementDescription: '',
        roles: [],
        selectedFunctionIds: [],
        customFunctions: [],
        materials: {
          referenceImages: [],
          sourceCodePackage: '',
          repositoryUrl: ''
        }
      },
      // 阶段2：交互视觉定制
      stage2: {
        visualStyle: '',
        colorScheme: '',
        layoutPreference: '',
        deploymentMode: 'cloud'  // 部署模式：cloud-云托管, local-本地部署, none-不需要
      },
      // 阶段3：AI智能报价
      stage3: {
        quotation: {
          totalAmount: 0,
          currency: 'CNY',
          estimatedDays: 14,
          breakdown: {
            aiDevelopmentCost: 0,
            platformServiceFee: 0,
            infrastructureCost: 0,
            developmentPhases: [],
            infrastructure: {
              server: { name: '', cost: 0, config: '' },
              domain: { name: '', cost: 0 },
              ssl: { name: '', cost: 0 },
              cdn: { name: '', cost: 0, optional: false }
            }
          }
        },
        confirmed: false
      }
    },

    // 加载状态
    isLoading: false,

    // 阶段3：AI估价加载状态
    isQuotationLoading: false,
    quotationLoadError: false,
    quotationErrorMsg: '',

    // 阶段3：费用卡片展开状态
    expandedCards: {
      aiDevelopment: false,
      infrastructure: false
    },

    // 需求ID（用于调用API）
    requirementId: null,

    // 阶段1：智能识别加载状态
    isAnalyzing: false,

    // 阶段1：展开的角色索引
    expandedRoleIndex: -1,

    // 阶段1：预估价格（供WXML显示）
    estimatedPrice: 0,

    // 阶段1：自定义功能弹窗显示状态
    showCustomFunctionModal: false,
    customFunctionForm: {
      roleIndex: -1,
      name: '',
      description: '',
      complexity: 'medium'
    }
  },

  /**
   * 生命周期函数--监听页面加载
   */
  onLoad(options) {
    // 从页面参数获取需求ID
    if (options.id) {
      this.setData({ requirementId: options.id });
    }
    
    // 尝试从本地存储恢复数据
    this.loadStageData();
  },

  /**
   * 生命周期函数--监听页面显示
   */
  onShow() {
    // 检查登录状态
    if (!util.checkLogin()) {
      return;
    }
  },

  /**
   * 更新阶段数据
   * @param {number} stage - 阶段号 (1-3)
   * @param {object} data - 要更新的数据
   */
  updateStageData(stage, data) {
    const key = `stageData.stage${stage}`;
    const currentData = this.data.stageData[`stage${stage}`];
    const newData = { ...currentData, ...data };
    this.setData({
      [`${key}`]: newData
    });
  },

  /**
   * 保存阶段数据到本地存储
   */
  saveStageData() {
    try {
      wx.setStorageSync('step_all_data', this.data.stageData);
    } catch (e) {
      console.error('保存阶段数据失败:', e);
    }
  },

  /**
   * 从本地存储恢复阶段数据
   */
  loadStageData() {
    try {
      const data = wx.getStorageSync('step_all_data');
      if (data) {
        this.setData({
          'stageData': { ...this.data.stageData, ...data }
        });
      }
    } catch (e) {
      console.error('恢复阶段数据失败:', e);
    }
  },

  /**
   * 切换阶段
   * @param {number} stage - 目标阶段 (1-3)
   */
  switchStage(stage) {
    if (stage < 1 || stage > 3) return;

    // 保存当前阶段数据
    this.saveStageData();

    this.setData({
      currentStage: stage
    });

    // 如果切换到阶段3，自动获取AI估价
    if (stage === 3 && !this.data.stageData.stage3.quotation.totalAmount) {
      this.fetchAIQuotation();
    }
  },

  /**
   * 点击上一步
   */
  onPrevStep() {
    const { currentStage } = this.data;
    if (currentStage > 1) {
      this.switchStage(currentStage - 1);
    }
  },

  /**
   * 点击下一步
   */
  onNextStep() {
    const { currentStage } = this.data;
    if (currentStage < 3) {
      // 验证当前阶段
      if (this.validateCurrentStage()) {
        this.switchStage(currentStage + 1);
      }
    } else {
      // 完成提交
      this.onSubmit();
    }
  },

  /**
   * 验证当前阶段数据
   * @returns {boolean} 是否通过验证
   */
  validateCurrentStage() {
    const { currentStage, stageData } = this.data;

    switch (currentStage) {
      case 1:
        // 验证需求描述
        if (!stageData.stage1.requirementDescription.trim()) {
          wx.showToast({
            title: '请描述您的业务场景',
            icon: 'none'
          });
          return false;
        }
        return true;

      case 2:
        // 验证视觉方案选择
        if (!stageData.stage2.visualStyle) {
          wx.showToast({
            title: '请选择视觉方案',
            icon: 'none'
          });
          return false;
        }
        return true;

      default:
        return true;
    }
  },

  /**
   * 提交需求
   */
  onSubmit() {
    wx.showModal({
      title: '确认提交',
      content: '确认提交需求？提交后将进入开发流程。',
      success: (res) => {
        if (res.confirm) {
          this.doSubmitRequirement();
        }
      }
    });
  },

  /**
   * 执行提交需求
   * 如果有requirementId则更新需求，否则创建新需求
   */
  doSubmitRequirement() {
    const { stageData, requirementId } = this.data;

    // 构建提交数据（兼容后端RequirementCreate/Update DTO）
    const submitData = {
      // ========== 详细需求字段（step_all页面主要数据）==========
      requirementDescription: stageData.stage1.requirementDescription,
      selectedFunctions: stageData.stage1.selectedFunctionIds || [],
      customFunctions: stageData.stage1.customFunctions || [],
      materials: stageData.stage1.materials || {},
      visualStyle: stageData.stage2.visualStyle,
      deploymentMode: stageData.stage2.deploymentMode || 'cloud',
      quotation: stageData.stage3.quotation || {},
      
      // ========== 基础信息字段（提供默认值以兼容后端）==========
      userType: 'individual',  // 默认值，step_all页面未收集
      projectType: 'website',  // 默认值，step_all页面未收集
      needOnline: stageData.stage2.deploymentMode !== 'none',
      urgency: 'normal',       // 默认值
      deliveryPeriod: stageData.stage3.quotation?.estimatedDays || 30,
      uiStyle: stageData.stage2.visualStyle || 'modern',
      traffic: {
        totalUsers: 0,
        dau: 0,
        concurrent: 0
      }
    };

    // 显示加载中
    wx.showLoading({
      title: requirementId ? '更新中...' : '提交中...',
      mask: true
    });

    // 根据是否有requirementId决定是创建还是更新
    const isUpdate = !!requirementId;
    const url = isUpdate 
      ? `${util.getApiBaseUrl()}/requirements/${requirementId}` 
      : `${util.getApiBaseUrl()}/requirements`;
    const method = isUpdate ? 'PUT' : 'POST';

    console.log(`${isUpdate ? '更新' : '创建'}需求:`, { url, method, data: submitData });

    // 调用API
    wx.request({
      url: url,
      method: method,
      data: submitData,
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        wx.hideLoading();
        // 更新操作返回200，创建操作返回201
        const successStatus = isUpdate ? 200 : 201;
        // 后端返回的数据可能没有code字段，直接判断状态码和数据是否存在
        const isSuccess = res.statusCode === successStatus && res.data && res.data.id;
        
        if (isSuccess) {
          // 清除本地存储
          wx.removeStorageSync('step_all_data');
          
          // 获取需求ID
          const updatedRequirementId = res.data.id;
          
          wx.showToast({
            title: isUpdate ? '需求更新成功' : '需求提交成功',
            icon: 'success',
            duration: 2000,
            complete: () => {
              // 延迟后跳转到首页并自动触发AI明确需求
              setTimeout(() => {
                // 保存需要自动触发的AI节点信息
                wx.setStorageSync('auto_trigger_ai', {
                  requirementId: updatedRequirementId,
                  nodeIndex: 1, // AI明确需求节点
                  timestamp: Date.now()
                });
                
                wx.switchTab({
                  url: '/pages/index/index'
                });
              }, 2000);
            }
          });
        } else {
          wx.showToast({
            title: (res.data && res.data.message) || (isUpdate ? '更新失败' : '提交失败'),
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        wx.hideLoading();
        console.error(isUpdate ? '更新需求失败:' : '提交需求失败:', err);
        wx.showToast({
          title: '网络错误，请重试',
          icon: 'none'
        });
      }
    });
  },

  // ==================== 阶段1：需求智能解析 ====================

  /**
   * 需求描述输入
   */
  onRequirementDescInput(e) {
    this.updateStageData(1, { requirementDescription: e.detail.value });
  },

  /**
   * 智能识别功能点
   */
  analyzeRequirement() {
    const { stageData } = this.data;
    const keyword = stageData.stage1.requirementDescription.slice(0, 20);

    this.setData({ isAnalyzing: true });

    wx.request({
      url: `${util.getApiBaseUrl()}/system-templates/search`,
      method: 'GET',
      data: { keyword },
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data) {
          // 处理返回数据，添加展开状态和选中状态
          const selectedFunctionIds = this.data.stageData.stage1.selectedFunctionIds || [];
          const roles = (res.data.roles || []).map((role, index) => {
            const functionList = (role.functionList || []).map(func => ({
              ...func,
              selected: false
            }));
            // 计算是否全选
            const isAllSelected = functionList.length > 0 && functionList.every(func => selectedFunctionIds.includes(func.id));
            return {
              ...role,
              expanded: index === 0, // 默认展开第一个
              functionList: functionList,
              isAllSelected: isAllSelected
            };
          });

          this.updateStageData(1, { roles });
          this.setData({
            expandedRoleIndex: 0,
            isAnalyzing: false
          });

          wx.showToast({
            title: '分析完成',
            icon: 'success'
          });
        } else {
          this.setData({ isAnalyzing: false });
          wx.showToast({
            title: '分析失败，请重试',
            icon: 'none'
          });
        }
      },
      fail: (err) => {
        console.error('智能识别请求失败:', err);
        this.setData({ isAnalyzing: false });
        wx.showToast({
          title: '网络错误，请稍后重试',
          icon: 'none'
        });
      }
    });
  },

  /**
   * 切换角色展开状态
   */
  onToggleRole(e) {
    const index = e.currentTarget.dataset.index;
    const { stageData } = this.data;
    const newRoles = stageData.stage1.roles.map((role, i) => ({
      ...role,
      expanded: i === index ? !role.expanded : false
    }));

    this.updateStageData(1, { roles: newRoles });
    this.setData({
      expandedRoleIndex: newRoles[index].expanded ? index : -1
    });
  },

  /**
   * 点击阶段指示器
   */
  onStageTap(e) {
    const stage = parseInt(e.currentTarget.dataset.stage);
    const { currentStage } = this.data;
    
    // 只能点击已完成的阶段回退
    if (stage < currentStage) {
      this.switchStage(stage);
    }
  },

  /**
   * 选择配色方案
   */
  onSelectColorScheme(e) {
    const scheme = e.currentTarget.dataset.scheme;
    this.updateStageData(2, { colorScheme: scheme });
  },

  /**
   * 选择复杂度（弹窗中）
   */
  onSelectComplexity(e) {
    const complexity = e.currentTarget.dataset.complexity;
    this.setData({
      'customFunctionForm.complexity': complexity
    });
  },

  /**
   * 确认添加自定义功能（别名）
   */
  onConfirmCustomFunction() {
    this.onConfirmAddCustomFunction();
  },

  /**
   * 删除源码包（别名）
   */
  onDeleteSourceCodePackage() {
    this.onDeleteSourceCode();
  },

  /**
   * 选择源码包（别名）
   */
  onChooseSourceCodePackage() {
    this.onChooseSourceCode();
  },

  /**
   * 上一步（别名）
   */
  onPrev() {
    this.onPrevStep();
  },

  /**
   * 分析需求（别名）
   */
  onAnalyzeFunctions() {
    this.analyzeRequirement();
  },

  /**
   * 切换功能点选中状态
   */
  onToggleFunction(e) {
    const { roleIndex, funcId } = e.currentTarget.dataset;
    const { stageData } = this.data;
    const selectedFunctionIds = [...stageData.stage1.selectedFunctionIds];

    const index = selectedFunctionIds.indexOf(funcId);
    if (index > -1) {
      selectedFunctionIds.splice(index, 1);
    } else {
      selectedFunctionIds.push(funcId);
    }

    // 更新当前角色的isAllSelected状态
    const roles = stageData.stage1.roles.map((role, index) => {
      if (index === parseInt(roleIndex)) {
        const isAllSelected = role.functionList.length > 0 && role.functionList.every(func => selectedFunctionIds.includes(func.id));
        return { ...role, isAllSelected };
      }
      return role;
    });

    this.updateStageData(1, { selectedFunctionIds, roles });
    
    // 更新预估价格
    this.calculateEstimatedPrice();
  },

  /**
   * 全选/取消全选功能点
   */
  onToggleSelectAll(e) {
    const roleIndex = e.currentTarget.dataset.index;
    const { stageData } = this.data;
    const role = stageData.stage1.roles[roleIndex];
    const functionIds = role.functionList.map(func => func.id);
    let selectedFunctionIds = [...stageData.stage1.selectedFunctionIds];

    // 检查是否全部选中
    const allSelected = functionIds.every(id => selectedFunctionIds.includes(id));

    if (allSelected) {
      // 取消全选
      selectedFunctionIds = selectedFunctionIds.filter(id => !functionIds.includes(id));
    } else {
      // 全选
      functionIds.forEach(id => {
        if (!selectedFunctionIds.includes(id)) {
          selectedFunctionIds.push(id);
        }
      });
    }

    // 更新当前角色的isAllSelected状态
    const roles = stageData.stage1.roles.map((role, index) => {
      if (index === parseInt(roleIndex)) {
        return { ...role, isAllSelected: !allSelected };
      }
      return role;
    });

    this.updateStageData(1, { selectedFunctionIds, roles });
    
    // 更新预估价格
    this.calculateEstimatedPrice();
  },

  /**
   * 计算预估价格并更新到data
   */
  calculateEstimatedPrice() {
    const { stageData } = this.data;
    const { roles, selectedFunctionIds } = stageData.stage1;

    let totalPrice = 0;
    roles.forEach(role => {
      role.functionList.forEach(func => {
        if (selectedFunctionIds.includes(func.id)) {
          // 根据复杂度计算价格：低=500，中=1000，高=2000
          const priceMap = { low: 500, medium: 1000, high: 2000 };
          totalPrice += priceMap[func.complexity] || 1000;
        }
      });
    });

    // 更新到data中，供WXML显示
    this.setData({
      estimatedPrice: totalPrice
    });

    return totalPrice;
  },

  /**
   * 显示自定义功能弹窗
   */
  onShowCustomFunctionModal(e) {
    const roleIndex = e.currentTarget.dataset.index;
    this.setData({
      showCustomFunctionModal: true,
      customFunctionForm: {
        roleIndex: roleIndex,
        name: '',
        description: '',
        complexity: 'medium'
      }
    });
  },

  /**
   * 关闭自定义功能弹窗
   */
  onCloseCustomFunctionModal() {
    this.setData({
      showCustomFunctionModal: false,
      customFunctionForm: {
        roleIndex: -1,
        name: '',
        description: '',
        complexity: 'medium'
      }
    });
  },

  /**
   * 自定义功能名称输入
   */
  onCustomFunctionNameInput(e) {
    this.setData({
      'customFunctionForm.name': e.detail.value
    });
  },

  /**
   * 自定义功能描述输入
   */
  onCustomFunctionDescInput(e) {
    this.setData({
      'customFunctionForm.description': e.detail.value
    });
  },

  /**
   * 选择自定义功能复杂度
   */
  onSelectCustomComplexity(e) {
    const complexity = e.currentTarget.dataset.complexity;
    this.setData({
      'customFunctionForm.complexity': complexity
    });
  },

  /**
   * 确认添加自定义功能
   */
  onConfirmAddCustomFunction() {
    const { customFunctionForm, stageData } = this.data;

    if (!customFunctionForm.name.trim()) {
      wx.showToast({
        title: '请输入功能名称',
        icon: 'none'
      });
      return;
    }

    // 生成自定义功能ID（负数，避免与系统功能冲突）
    const customId = -Date.now();

    // 添加到对应角色的功能列表
    const roles = [...stageData.stage1.roles];
    const roleIndex = parseInt(customFunctionForm.roleIndex);
    const role = roles[roleIndex];

    role.functionList.push({
      id: customId,
      name: customFunctionForm.name,
      description: customFunctionForm.description,
      complexity: customFunctionForm.complexity,
      estimatedHours: customFunctionForm.complexity === 'low' ? 4 : customFunctionForm.complexity === 'medium' ? 8 : 16,
      isCustom: true
    });

    // 自动选中该功能
    const selectedFunctionIds = [...stageData.stage1.selectedFunctionIds, customId];

    this.updateStageData(1, {
      roles: roles,
      selectedFunctionIds: selectedFunctionIds
    });
    
    // 更新预估价格
    this.calculateEstimatedPrice();

    this.onCloseCustomFunctionModal();

    wx.showToast({
      title: '添加成功',
      icon: 'success'
    });
  },

  /**
   * 选择参考图片
   */
  onChooseReferenceImages() {
    const { stageData } = this.data;
    const currentImages = stageData.stage1.materials.referenceImages;

    if (currentImages.length >= 5) {
      wx.showToast({
        title: '最多选择5张图片',
        icon: 'none'
      });
      return;
    }

    wx.chooseImage({
      count: 5 - currentImages.length,
      sizeType: ['compressed'],
      sourceType: ['album', 'camera'],
      success: (res) => {
        const newImages = [...currentImages, ...res.tempFilePaths];
        this.updateStageData(1, {
          materials: { ...stageData.stage1.materials, referenceImages: newImages }
        });
      }
    });
  },

  /**
   * 删除参考图片
   */
  onDeleteReferenceImage(e) {
    const index = e.currentTarget.dataset.index;
    const { stageData } = this.data;
    const images = [...stageData.stage1.materials.referenceImages];
    images.splice(index, 1);

    this.updateStageData(1, {
      materials: { ...stageData.stage1.materials, referenceImages: images }
    });
  },

  /**
   * 选择源码包
   */
  onChooseSourceCode() {
    wx.chooseMessageFile({
      count: 1,
      type: 'file',
      extension: ['zip'],
      success: (res) => {
        const { stageData } = this.data;
        this.updateStageData(1, {
          materials: { ...stageData.stage1.materials, sourceCodePackage: res.tempFiles[0].name }
        });
      }
    });
  },

  /**
   * 删除源码包
   */
  onDeleteSourceCode() {
    const { stageData } = this.data;
    this.updateStageData(1, {
      materials: { ...stageData.stage1.materials, sourceCodePackage: '' }
    });
  },

  /**
   * 代码仓库链接输入
   */
  onRepositoryUrlInput(e) {
    const { stageData } = this.data;
    this.updateStageData(1, {
      materials: { ...stageData.stage1.materials, repositoryUrl: e.detail.value }
    });
  },

  // ==================== 阶段2：交互视觉定制 ====================

  /**
   * 选择视觉方案
   */
  onSelectVisualStyle(e) {
    const style = e.currentTarget.dataset.value;
    this.updateStageData(2, { visualStyle: style });
  },

  /**
   * 跳转到UI资源库
   */
  onNavigateToGallery() {
    wx.navigateTo({
      url: '/pages/requirement/ui_gallery/ui_gallery'
    });
  },

  // ==================== 阶段3：AI智能报价 ====================

  /**
   * 获取AI估价 - 先尝试获取缓存，如果没有则生成新的
   */
  fetchAIQuotation() {
    const { requirementId } = this.data;

    this.setData({
      isQuotationLoading: true,
      quotationLoadError: false,
      quotationErrorMsg: ''
    });

    // 第一步：尝试获取缓存的估价数据
    wx.request({
      url: `${util.getApiBaseUrl()}/requirements/${requirementId}/ai-quotation`,
      method: 'GET',
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          // 缓存数据存在，直接使用
          console.log('使用缓存的AI估价数据');
          this.updateStageData(3, {
            quotation: res.data.data
          });
          this.setData({
            isQuotationLoading: false
          });
        } else if (res.statusCode === 404) {
          // 缓存不存在，生成新的估价
          console.log('缓存不存在，生成新的AI估价');
          this.generateAIQuotation();
        } else {
          // 其他错误，尝试生成新的
          console.warn('获取缓存失败，尝试生成新的:', res.data.message);
          this.generateAIQuotation();
        }
      },
      fail: (err) => {
        console.error('获取缓存失败:', err);
        // 网络错误时也尝试生成新的
        this.generateAIQuotation();
      }
    });
  },

  /**
   * 生成新的AI估价
   */
  generateAIQuotation() {
    const { stageData, requirementId } = this.data;

    // 构建请求数据
    const requestData = {
      requirementDescription: stageData.stage1.requirementDescription,
      selectedFunctionIds: stageData.stage1.selectedFunctionIds,
      customFunctions: stageData.stage1.customFunctions,
      visualStyle: stageData.stage2.visualStyle,
      deploymentMode: stageData.stage2.deploymentMode,
      materials: stageData.stage1.materials
    };

    // 调用AI估价API
    wx.request({
      url: `${util.getApiBaseUrl()}/requirements/${requirementId}/ai-quotation`,
      method: 'POST',
      data: requestData,
      header: {
        'Authorization': `Bearer ${wx.getStorageSync('token')}`,
        'Content-Type': 'application/json'
      },
      success: (res) => {
        if (res.statusCode === 200 && res.data.code === 0) {
          this.updateStageData(3, {
            quotation: res.data.data
          });
          this.setData({
            isQuotationLoading: false
          });
        } else {
          this.setData({
            isQuotationLoading: false,
            quotationLoadError: true,
            quotationErrorMsg: res.data.message || '估价失败，请稍后重试'
          });
        }
      },
      fail: (err) => {
        console.error('AI估价请求失败:', err);
        let errorMsg = '网络错误，请稍后重试';
        
        // 判断错误类型
        if (err.errMsg && err.errMsg.includes('CONNECTION_REFUSED')) {
          errorMsg = '服务器未启动，请检查后端服务';
        } else if (err.errMsg && err.errMsg.includes('TIMEOUT')) {
          errorMsg = '请求超时，请检查网络连接';
        }
        
        this.setData({
          isQuotationLoading: false,
          quotationLoadError: true,
          quotationErrorMsg: errorMsg
        });
        
        wx.showToast({
          title: errorMsg,
          icon: 'none',
          duration: 3000
        });
      }
    });
  },

  /**
   * 重试获取AI估价
   */
  onRetryQuotation() {
    this.generateAIQuotation();
  },

  /**
   * 切换费用卡片展开状态
   */
  onToggleCard(e) {
    const card = e.currentTarget.dataset.card;
    const { expandedCards } = this.data;
    this.setData({
      [`expandedCards.${card}`]: !expandedCards[card]
    });
  },

  /**
   * 重新生成报价
   */
  onRegenerateQuote() {
    // 重置报价数据
    this.updateStageData(3, {
      quotation: {
        totalAmount: 0,
        currency: 'CNY',
        estimatedDays: 14,
        breakdown: {
          aiDevelopmentCost: 0,
          platformServiceFee: 0,
          infrastructureCost: 0,
          developmentPhases: []
        }
      },
      confirmed: false
    });

    this.generateAIQuotation();
  },

  /**
   * 格式化金额显示
   * @param {number} amount - 金额
   * @returns {string} 格式化后的金额
   */
  formatAmount(amount) {
    if (!amount && amount !== 0) return '0';
    return amount.toLocaleString('zh-CN');
  }
});