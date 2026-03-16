import { defineStore } from 'pinia'
import { ref } from 'vue'
import api from '@/utils/api'

export const useUserStore = defineStore('user', () => {
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

  async function login(phone, code, referralCode = null) {
    const data = { phone, code }
    if (referralCode) {
      data.referral_code = referralCode
    }
    const response = await api.login(data)
    setToken(response.access_token)
    await fetchUserInfo()
    return response
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
    login,
    fetchUserInfo,
    updateUserInfo,
    logout
  }
})
