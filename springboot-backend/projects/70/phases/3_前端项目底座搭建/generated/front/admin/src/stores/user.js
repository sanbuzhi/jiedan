import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/user'
import { setToken, removeToken, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    avatar: (state) => state.userInfo?.avatar || ''
  },
  actions: {
    async login(loginData) {
      try {
        const res = await login(loginData)
        this.token = res.data.token
        setToken(res.data.token)
        await this.getUserInfo()
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async getUserInfo() {
      try {
        const res = await getUserInfo()
        this.userInfo = res.data
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async logout() {
      try {
        await logout()
      } finally {
        this.token = ''
        this.userInfo = null
        removeToken()
      }
    }
  },
  persist: {
    key: 'admin-user',
    storage: localStorage,
    paths: ['token']
  }
})