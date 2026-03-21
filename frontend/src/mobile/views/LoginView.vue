<template>
  <div class="login-container">
    <div class="logo-section">
      <div class="logo-placeholder">接单</div>
      <div class="app-name">接单助手</div>
      <div class="app-slogan">智能获客，轻松接单</div>
    </div>

    <div class="login-card">
      <div class="login-title">欢迎登录</div>
      <div class="login-desc">手机号验证码登录，开启智能获客之旅</div>

      <div class="form-item">
        <input
          type="tel"
          class="form-input"
          placeholder="请输入手机号"
          v-model="phone"
          maxlength="11"
          @input="handlePhoneInput"
        />
      </div>

      <div class="form-item code-item">
        <input
          type="text"
          class="form-input"
          placeholder="请输入验证码"
          v-model="code"
          maxlength="6"
        />
        <button
          class="code-btn"
          :class="{ disabled: countdown > 0 || !phoneValid }"
          @click="sendCode"
          :disabled="countdown > 0 || !phoneValid"
        >
          {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
        </button>
      </div>

      <div class="form-item" v-if="referrerCode">
        <input
          type="text"
          class="form-input"
          placeholder="推荐码（选填）"
          v-model="referrerCode"
          disabled
        />
      </div>

      <div class="feature-list">
        <div class="feature-item">
          <span class="feature-icon">✓</span>
          <span class="feature-text">AI智能生成营销物料</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">✓</span>
          <span class="feature-text">一键发布多平台获客</span>
        </div>
        <div class="feature-item">
          <span class="feature-icon">✓</span>
          <span class="feature-text">推荐好友赚取积分</span>
        </div>
      </div>

      <button
        class="login-btn"
        :class="{ disabled: !canLogin || isLoading }"
        @click="handleLogin"
        :disabled="!canLogin || isLoading"
      >
        {{ isLoading ? '登录中...' : '登录' }}
      </button>

      <div class="privacy-tips">
        <span>登录即表示您同意</span>
        <span class="link" @click="showUserAgreement">《用户协议》</span>
        <span>和</span>
        <span class="link" @click="showPrivacyPolicy">《隐私政策》</span>
      </div>
    </div>

    <div class="referral-section" v-if="referrerCode">
      <div class="referral-info">
        <span class="referral-label">您通过好友推荐进入</span>
        <span class="referral-code">推荐码：{{ referrerCode }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()

const phone = ref('')
const code = ref('')
const countdown = ref(0)
const isLoading = ref(false)
const referrerCode = ref('')

const isDev = import.meta.env.DEV

const phoneValid = computed(() => {
  return /^1[3-9]\d{9}$/.test(phone.value)
})

const canLogin = computed(() => {
  return phoneValid.value && code.value.length === 6
})

function handlePhoneInput(e) {
  phone.value = e.target.value.replace(/\D/g, '')
}

async function sendCode() {
  if (countdown.value > 0 || !phoneValid.value) return

  try {
    if (isDev) {
      countdown.value = 60
      code.value = '123456'
      const timer = setInterval(() => {
        countdown.value--
        if (countdown.value <= 0) {
          clearInterval(timer)
        }
      }, 1000)
      alert('开发模式：验证码已自动填充为 123456')
      return
    }

    await api.sendVerificationCode(phone.value)
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error('发送验证码失败:', error)
  }
}

async function handleLogin() {
  if (!canLogin.value || isLoading.value) return

  isLoading.value = true

  try {
    const loginData = {
      phone: phone.value,
      code: code.value
    }

    const res = await api.webLogin(loginData)

    const token = res.accessToken || res.access_token
    if (token) {
      localStorage.setItem('token', token)
      localStorage.setItem('userInfo', JSON.stringify(res.user || {}))

      if (referrerCode.value) {
        localStorage.removeItem('referrerCode')
      }

      router.push('/client/home')
    }
  } catch (error) {
    console.error('登录失败:', error)
    if (isDev && code.value === '123456') {
      simulateLogin()
    } else {
      alert(error.message || '登录失败，请重试')
    }
  } finally {
    isLoading.value = false
  }
}

function simulateLogin() {
  const mockUser = {
    id: 1,
    phone: phone.value,
    nickname: '用户' + phone.value.slice(-4),
    points: 100,
    referralCode: 'TEST' + Math.random().toString(36).slice(2, 8).toUpperCase()
  }

  const mockToken = 'mock_token_' + Date.now()

  localStorage.setItem('token', mockToken)
  localStorage.setItem('userInfo', JSON.stringify(mockUser))

  if (referrerCode.value) {
    localStorage.removeItem('referrerCode')
  }

  router.push('/client/home')
}

function showUserAgreement() {
  alert('用户协议内容...')
}

function showPrivacyPolicy() {
  alert('隐私政策内容...')
}

onMounted(() => {
  referrerCode.value = route.query.referrer || route.query.code || ''

  if (referrerCode.value) {
    localStorage.setItem('referrerCode', referrerCode.value)
  }
})
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(180deg, #07c160 0%, #06ad56 100%);
  padding: 30px 20px;
  box-sizing: border-box;
}

.logo-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 30px;
}

.logo-placeholder {
  width: 80px;
  height: 80px;
  border-radius: 16px;
  background-color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  font-weight: bold;
  color: #07c160;
  margin-bottom: 12px;
}

.app-name {
  font-size: 24px;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 6px;
}

.app-slogan {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.login-card {
  background-color: #ffffff;
  border-radius: 12px;
  padding: 30px 20px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

.login-title {
  font-size: 20px;
  font-weight: bold;
  color: #333333;
  text-align: center;
  margin-bottom: 8px;
}

.login-desc {
  font-size: 14px;
  color: #999999;
  text-align: center;
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 16px;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 12px;
  border: 1px solid #e5e5e5;
  border-radius: 6px;
  font-size: 15px;
  outline: none;
  box-sizing: border-box;
  background-color: #fff;
}

.form-input:focus {
  border-color: #07c160;
}

.form-input:disabled {
  background-color: #f5f5f5;
  color: #999;
}

.code-item {
  display: flex;
  gap: 10px;
}

.code-item .form-input {
  flex: 1;
}

.code-btn {
  width: 100px;
  height: 44px;
  background-color: #07c160;
  color: #ffffff;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  white-space: nowrap;
  cursor: pointer;
  padding: 0 10px;
}

.code-btn:active {
  background-color: #06ad56;
}

.code-btn.disabled {
  background-color: #cccccc;
  cursor: not-allowed;
}

.feature-list {
  margin-bottom: 24px;
}

.feature-item {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.feature-icon {
  width: 20px;
  height: 20px;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  margin-right: 10px;
}

.feature-text {
  font-size: 15px;
  color: #333333;
}

.login-btn {
  width: 100%;
  height: 48px;
  line-height: 48px;
  background-color: #07c160;
  color: #ffffff;
  border-radius: 6px;
  border: none;
  font-size: 17px;
  font-weight: 500;
  text-align: center;
  cursor: pointer;
  margin-bottom: 16px;
}

.login-btn:active {
  background-color: #06ad56;
}

.login-btn.disabled {
  background-color: #cccccc;
  color: #ffffff;
  cursor: not-allowed;
}

.privacy-tips {
  text-align: center;
  font-size: 12px;
  color: #999999;
}

.privacy-tips .link {
  color: #07c160;
}

.referral-section {
  margin-top: 20px;
  display: flex;
  justify-content: center;
}

.referral-info {
  background-color: rgba(255, 255, 255, 0.2);
  border-radius: 6px;
  padding: 12px 20px;
  text-align: center;
}

.referral-label {
  display: block;
  font-size: 13px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 4px;
}

.referral-code {
  display: block;
  font-size: 16px;
  color: #ffffff;
  font-weight: 500;
}
</style>
