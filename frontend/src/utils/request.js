import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  baseURL: '/api/v1',
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    // 从 localStorage 获取 token（确保最新）
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    const status = error.response?.status
    const url = error.config?.url || ''
    
    // 401/403 时跳转到登录页（但排除 analytics 接口）
    if (status === 401 || status === 403) {
      // analytics 接口失败不跳转，只记录错误
      if (url.includes('/analytics/')) {
        console.log('Analytics request failed, skipping redirect')
        return Promise.reject(error)
      }
      
      // 其他接口 401/403 跳转到登录页
      const userStore = useUserStore()
      userStore.logout()
      window.location.href = '/login'
    }
    
    // 只显示非 analytics 接口的错误
    if (!url.includes('/analytics/')) {
      ElMessage.error(error.response?.data?.detail || error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request
