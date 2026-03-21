import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import authApi, { UserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'admin')

  const login = async (username: string, password: string) => {
    const res = await authApi.login({ username, password })
    token.value = res.token
    setToken(res.token)
    await getUserInfo()
  }

  const getUserInfo = async () => {
    const res = await authApi.getUserInfo()
    userInfo.value = res
  }

  const logout = async () => {
    await authApi.logout()
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    login,
    getUserInfo,
    logout
  }
})