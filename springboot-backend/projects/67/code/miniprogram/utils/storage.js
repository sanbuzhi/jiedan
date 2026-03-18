const setStorage = (key, value) => {
  try {
    wx.setStorageSync(key, value);
  } catch (e) {
    console.error('存储失败', e);
  }
};

const getStorage = (key) => {
  try {
    return wx.getStorageSync(key);
  } catch (e) {
    console.error('获取存储失败', e);
    return null;
  }
};

const removeStorage = (key) => {
  try {
    wx.removeStorageSync(key);
  } catch (e) {
    console.error('删除存储失败', e);
  }
};

module.exports = { setStorage, getStorage, removeStorage };