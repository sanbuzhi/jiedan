import axios from 'axios'
import { showToast, showConfirmDialog } from 'vant'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000
})

request.interceptors.request.use(
  (config) => {
    const userStore = useUserStore()
    if (userStore.token) {
      config.headers.Authorization = `Bearer ${userStore.token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code !== 200) {
      showToast(res.message || '请求失败')
      if (res.code === 401) {
        showConfirmDialog({
          title: '提示',
          message: '登录已过期，请重新登录'
        }).then(() => {
          const userStore = useUserStore()
          userStore.logout()
          router.push('/login')
        }).catch(() => {})
      }
      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      switch (status) {
        case 401:
          showConfirmDialog({
            title: '提示',
            message: '登录已过期，请重新登录'
          }).then(() => {
            const userStore = useUserStore()
            userStore.logout()
            router.push('/login')
          }).catch(() => {})
          break
        case 404:
          showToast('请求的资源不存在')
          break
        case 500:
          showToast('服务器错误')
          break
        default:
          showToast(data.message || '请求失败')
      }
    } else if (error.request) {
      showToast('网络错误，请检查网络连接')
    } else {
      showToast(error.message || '请求失败')
    }
    return Promise.reject(error)
  }
)

export default request