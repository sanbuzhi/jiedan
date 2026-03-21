// 格式化日期
const formatDate = (date, format = 'yyyy-MM-dd') => {
  if (!date) return '';
  
  const d = new Date(date);
  const year = d.getFullYear();
  const month = (d.getMonth() + 1).toString().padStart(2, '0');
  const day = d.getDate().toString().padStart(2, '0');
  const hour = d.getHours().toString().padStart(2, '0');
  const minute = d.getMinutes().toString().padStart(2, '0');
  const second = d.getSeconds().toString().padStart(2, '0');
  
  return format
    .replace('yyyy', year)
    .replace('MM', month)
    .replace('dd', day)
    .replace('HH', hour)
    .replace('mm', minute)
    .replace('ss', second);
};

// 格式化数字（千分位）
const formatNumber = (num) => {
  if (num === null || num === undefined) return '0';
  return num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// 复制文本到剪贴板
const copyToClipboard = (text, successMsg = '复制成功') => {
  wx.setClipboardData({
    data: text,
    success: () => {
      wx.showToast({
        title: successMsg,
        icon: 'success'
      });
    },
    fail: () => {
      wx.showToast({
        title: '复制失败',
        icon: 'none'
      });
    }
  });
};

// 显示确认对话框
const showConfirm = (title, content) => {
  return new Promise((resolve) => {
    wx.showModal({
      title: title,
      content: content,
      success: (res) => {
        resolve(res.confirm);
      }
    });
  });
};

// 显示提示
const showToast = (title, icon = 'none', duration = 2000) => {
  wx.showToast({
    title: title,
    icon: icon,
    duration: duration
  });
};

// 显示加载
const showLoading = (title = '加载中...') => {
  wx.showLoading({
    title: title,
    mask: true
  });
};

// 隐藏加载
const hideLoading = () => {
  wx.hideLoading();
};

// 检查登录状态
const checkLogin = () => {
  const token = wx.getStorageSync('token');
  return !!token;
};

// 获取推荐码（从存储或全局数据）
const getReferrerCode = () => {
  const app = getApp();
  let code = null;
  
  if (app && app.globalData.referrerCode) {
    code = app.globalData.referrerCode;
  } else {
    code = wx.getStorageSync('referrerCode');
  }
  
  return code;
};

// 清除推荐码
const clearReferrerCode = () => {
  const app = getApp();
  if (app) {
    app.globalData.referrerCode = null;
  }
  wx.removeStorageSync('referrerCode');
};

// 获取API基础URL（统一配置，确保全局一致）
// 返回格式: http://localhost:8080/v1
const getApiBaseUrl = () => {
  const app = getApp();
  // 优先从全局配置获取，否则使用默认开发环境地址
  const baseUrl = app && app.globalData.apiBaseUrl ? app.globalData.apiBaseUrl : 'http://192.168.1.3:8080';
  // 确保返回的URL包含 /v1 路径
  return baseUrl.endsWith('/v1') ? baseUrl : `${baseUrl}/v1`;
};

// 获取完整的头像URL（包含服务器地址）
const getFullAvatarUrl = (avatarPath) => {
  if (!avatarPath) {
    return null;
  }
  // 如果已经是完整URL，直接返回
  if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
    return avatarPath;
  }
  // 否则拼接服务器地址（保留 /api 前缀）
  const apiBaseUrl = getApiBaseUrl();
  const baseUrl = apiBaseUrl.replace('/v1', '');
  const fullUrl = baseUrl + avatarPath;
  console.log('getFullAvatarUrl - avatarPath:', avatarPath);
  console.log('getFullAvatarUrl - apiBaseUrl:', apiBaseUrl);
  console.log('getFullAvatarUrl - baseUrl:', baseUrl);
  console.log('getFullAvatarUrl - fullUrl:', fullUrl);
  return fullUrl;
};

// 下载并缓存头像到本地
const downloadAndCacheAvatar = (avatarUrl, cacheKey = 'cachedAvatar') => {
  return new Promise((resolve, reject) => {
    const fullUrl = getFullAvatarUrl(avatarUrl);
    if (!fullUrl) {
      reject(new Error('头像URL为空'));
      return;
    }

    // 获取当前缓存的头像URL
    const cachedAvatarUrl = wx.getStorageSync(cacheKey + '_url');
    const cachedPath = wx.getStorageSync(cacheKey);
    
    // 如果URL变了，需要重新下载
    if (cachedAvatarUrl && cachedAvatarUrl !== fullUrl) {
      console.log('头像URL已变更，重新下载:', fullUrl);
      downloadAvatar(fullUrl, cacheKey, resolve, reject);
      return;
    }

    // 先检查本地是否已有缓存
    if (cachedPath) {
      // 检查文件是否存在
      wx.getFileSystemManager().access({
        path: cachedPath,
        success: () => {
          console.log('使用本地缓存头像:', cachedPath);
          resolve(cachedPath);
        },
        fail: () => {
          // 缓存文件不存在，重新下载
          downloadAvatar(fullUrl, cacheKey, resolve, reject);
        }
      });
    } else {
      // 没有缓存，下载头像
      downloadAvatar(fullUrl, cacheKey, resolve, reject);
    }
  });
};

// 下载头像
const downloadAvatar = (url, cacheKey, resolve, reject) => {
  console.log('开始下载头像, url:', url);
  wx.downloadFile({
    url: url,
    success: (res) => {
      console.log('下载响应:', res);
      if (res.statusCode === 200) {
        // 保存到本地存储
        wx.saveFile({
          tempFilePath: res.tempFilePath,
          success: (saveRes) => {
            const savedPath = saveRes.savedFilePath;
            wx.setStorageSync(cacheKey, savedPath);
            wx.setStorageSync(cacheKey + '_url', url); // 保存对应的URL
            console.log('头像下载并缓存成功:', savedPath);
            resolve(savedPath);
          },
          fail: (err) => {
            console.error('保存头像失败:', err);
            // 保存失败时使用临时文件
            resolve(res.tempFilePath);
          }
        });
      } else {
        const errorMsg = '下载头像失败: HTTP ' + res.statusCode;
        console.error(errorMsg);
        reject(new Error(errorMsg));
      }
    },
    fail: (err) => {
      console.error('下载头像网络错误:', err);
      reject(err);
    }
  });
};

// 更新本地缓存头像
const updateCachedAvatar = (tempFilePath, cacheKey = 'cachedAvatar') => {
  return new Promise((resolve, reject) => {
    wx.saveFile({
      tempFilePath: tempFilePath,
      success: (saveRes) => {
        const savedPath = saveRes.savedFilePath;
        wx.setStorageSync(cacheKey, savedPath);
        console.log('本地缓存头像已更新:', savedPath);
        resolve(savedPath);
      },
      fail: (err) => {
        console.error('更新本地缓存头像失败:', err);
        reject(err);
      }
    });
  });
};

// 获取本地缓存头像路径
const getCachedAvatar = (cacheKey = 'cachedAvatar') => {
  return wx.getStorageSync(cacheKey) || null;
};

// 清除本地缓存头像
const clearCachedAvatar = (cacheKey = 'cachedAvatar') => {
  wx.removeStorageSync(cacheKey);
};

module.exports = {
  formatDate,
  formatNumber,
  copyToClipboard,
  showConfirm,
  showToast,
  showLoading,
  hideLoading,
  checkLogin,
  getReferrerCode,
  clearReferrerCode,
  getApiBaseUrl,
  getFullAvatarUrl,
  downloadAndCacheAvatar,
  updateCachedAvatar,
  getCachedAvatar,
  clearCachedAvatar
};
