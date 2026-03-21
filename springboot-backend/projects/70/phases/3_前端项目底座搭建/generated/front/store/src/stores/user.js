import { defineStore } from 'pinia'
import { login, register, logout, getUserInfo, updateUserInfo, changePassword } from '@/api/user'
import { setToken, removeToken, getToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken() || '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    username: (state) => state.userInfo?.username || '',
    nickname: (state) => state.userInfo?.nickname || '',
    avatar: (state) => state.userInfo?.avatar || '',
    phone: (state) => state.userInfo?.phone || ''
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
    async register(registerData) {
      try {
        const res = await register(registerData)
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
    async updateUserInfo(data) {
      try {
        const res = await updateUserInfo(data)
        this.userInfo = { ...this.userInfo, ...data }
        return res
      } catch (error) {
        return Promise.reject(error)
      }
    },
    async changePassword(data) {
      try {
        const res = await changePassword(data)
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
    key: 'store-user',
    storage: localStorage,
    paths: ['token']
  }
})