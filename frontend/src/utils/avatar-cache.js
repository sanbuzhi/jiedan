/**
 * 头像缓存工具类
 * 参考微信小程序端实现，实现头像的本地缓存和加载
 */

const AVATAR_CACHE_KEY = 'cachedAvatar'
const AVATAR_CACHE_URL_KEY = 'cachedAvatar_url'

/**
 * 获取完整的头像 URL
 * @param {string} avatarPath - 头像路径（可能是相对路径或完整 URL）
 * @returns {string|null} - 完整的头像 URL
 */
export const getFullAvatarUrl = (avatarPath) => {
  if (!avatarPath) {
    return null
  }
  // 如果已经是完整 URL，直接返回
  if (avatarPath.startsWith('http://') || avatarPath.startsWith('https://')) {
    return avatarPath
  }
  // 否则拼接服务器地址
  const baseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const fullUrl = baseUrl + avatarPath
  console.log('getFullAvatarUrl - avatarPath:', avatarPath)
  console.log('getFullAvatarUrl - fullUrl:', fullUrl)
  return fullUrl
}

/**
 * 判断是否是默认头像
 * @param {string} avatarPath - 头像路径
 * @returns {boolean} - 是否是默认头像
 */
export const isDefaultAvatar = (avatarPath) => {
  if (!avatarPath) return true
  // 检查是否包含默认头像标识
  return avatarPath.includes('default-avatar') || 
         avatarPath.includes('default_avatar') ||
         avatarPath.endsWith('default.png')
}

/**
 * 下载并缓存头像到本地
 * @param {string} avatarUrl - 头像 URL
 * @returns {Promise<string>} - 缓存的头像路径（blob URL）
 */
export const downloadAndCacheAvatar = async (avatarUrl) => {
  if (!avatarUrl) {
    throw new Error('头像 URL 为空')
  }

  const fullUrl = getFullAvatarUrl(avatarUrl)
  if (!fullUrl) {
    throw new Error('头像 URL 无效')
  }

  // 获取当前缓存的头像 URL
  const cachedAvatarUrl = localStorage.getItem(AVATAR_CACHE_URL_KEY)
  const cachedBlobUrl = localStorage.getItem(AVATAR_CACHE_KEY)
  
  // 如果 URL 变了，需要重新下载
  if (cachedAvatarUrl && cachedAvatarUrl !== fullUrl) {
    console.log('头像 URL 已变更，重新下载:', fullUrl)
    return await downloadAvatar(fullUrl)
  }

  // 检查是否已有缓存
  if (cachedBlobUrl) {
    // 验证 blob URL 是否有效（通过创建 Image 对象测试）
    try {
      const testImg = new Image()
      await new Promise((resolve, reject) => {
        testImg.onload = resolve
        testImg.onerror = reject
        testImg.src = cachedBlobUrl
      })
      console.log('使用本地缓存头像:', cachedBlobUrl)
      return cachedBlobUrl
    } catch (err) {
      console.log('缓存头像已失效，重新下载')
      return await downloadAvatar(fullUrl)
    }
  } else {
    // 没有缓存，下载头像
    return await downloadAvatar(fullUrl)
  }
}

/**
 * 下载头像
 * @param {string} url - 头像 URL
 * @returns {Promise<string>} - 缓存的头像路径（blob URL）
 */
const downloadAvatar = async (url) => {
  console.log('开始下载头像，url:', url)
  
  try {
    const response = await fetch(url)
    if (!response.ok) {
      throw new Error(`下载头像失败：HTTP ${response.status}`)
    }
    
    const blob = await response.blob()
    const blobUrl = URL.createObjectURL(blob)
    
    // 保存到 localStorage
    localStorage.setItem(AVATAR_CACHE_KEY, blobUrl)
    localStorage.setItem(AVATAR_CACHE_URL_KEY, url)
    
    console.log('头像下载并缓存成功:', blobUrl)
    return blobUrl
  } catch (err) {
    console.error('下载头像失败:', err)
    throw err
  }
}

/**
 * 更新本地缓存头像
 * @param {string} blobUrl - 新的头像 blob URL
 */
export const updateCachedAvatar = (blobUrl, avatarUrl) => {
  localStorage.setItem(AVATAR_CACHE_KEY, blobUrl)
  localStorage.setItem(AVATAR_CACHE_URL_KEY, avatarUrl)
  console.log('本地缓存头像已更新:', blobUrl)
}

/**
 * 获取本地缓存头像路径
 * @returns {string|null} - 缓存的头像路径
 */
export const getCachedAvatar = () => {
  return localStorage.getItem(AVATAR_CACHE_KEY) || null
}

/**
 * 清除本地缓存头像
 */
export const clearCachedAvatar = () => {
  localStorage.removeItem(AVATAR_CACHE_KEY)
  localStorage.removeItem(AVATAR_CACHE_URL_KEY)
}

export default {
  getFullAvatarUrl,
  isDefaultAvatar,
  downloadAndCacheAvatar,
  updateCachedAvatar,
  getCachedAvatar,
  clearCachedAvatar
}
