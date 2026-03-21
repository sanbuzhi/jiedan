import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/utils/api'

export const useMobileUserStore = defineStore('mobileUser', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null)

  function setToken(newToken) {
    token.value = newToken
    if (newToken) {
      localStorage.setItem('token', newToken)
    } else {
      localStorage.removeItem('token')
    }
  }

  function setUserInfo(info) {
    userInfo.value = info
  }

  async function fetchUserInfo() {
    const info = await api.getUserInfo()
    setUserInfo(info)
    return info
  }

  async function updateUserInfo(data) {
    const info = await api.updateUserInfo(data)
    setUserInfo(info)
    return info
  }

  function logout() {
    setToken('')
    setUserInfo(null)
  }

  return {
    token,
    userInfo,
    setToken,
    setUserInfo,
    fetchUserInfo,
    updateUserInfo,
    logout
  }
})