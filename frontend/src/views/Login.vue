<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>智能获客系统</h2>
        </div>
      </template>
      <el-form :model="loginForm" label-width="80px">
        <el-form-item label="手机号">
          <el-input v-model="loginForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="验证码">
          <el-input v-model="loginForm.code" placeholder="请输入验证码">
            <template #append>
              <el-button @click="sendCode" :disabled="countdown > 0">
                {{ countdown > 0 ? `${countdown}秒后重发` : '发送验证码' }}
              </el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="推荐码">
          <el-input v-model="loginForm.referralCode" placeholder="如有推荐码请输入" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" style="width: 100%">登录 / 注册</el-button>
        </el-form-item>
      </el-form>

    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginForm = ref({
  phone: '',
  code: '',
  referralCode: ''
})

const countdown = ref(0)

onMounted(() => {
  if (route.query.code) {
    loginForm.value.referralCode = route.query.code
  }
})

async function sendCode() {
  if (!loginForm.value.phone) {
    ElMessage.warning('请先输入手机号')
    return
  }
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(loginForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    // 开发模式：直接使用模拟验证码，不调用后端接口
    const mockCode = '123456'
    loginForm.value.code = mockCode
    ElMessage.success('验证码已发送')
    ElMessage.info(`开发模式：验证码为 ${mockCode}`)
    
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) {
        clearInterval(timer)
      }
    }, 1000)
  } catch (error) {
    console.error(error)
    const errorMsg = error.response?.data?.detail || '发送失败，请稍后重试'
    ElMessage.error(errorMsg)
  }
}

async function handleLogin() {
  if (!loginForm.value.phone || !loginForm.value.code) {
    ElMessage.warning('请输入手机号和验证码')
    return
  }
  // 验证手机号格式
  const phoneRegex = /^1[3-9]\d{9}$/
  if (!phoneRegex.test(loginForm.value.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    // 调用后端网页版登录接口
    const response = await api.webLogin({
      phone: loginForm.value.phone,
      code: loginForm.value.code
    })
    
    // 保存 token 到 localStorage（确保请求拦截器能获取到）
    const token = response.access_token || response.accessToken
    localStorage.setItem('token', token)
    userStore.setToken(token)
    
    // 获取用户信息
    await userStore.fetchUserInfo()
    
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error) {
    console.error(error)
    const errorMsg = error.response?.data?.detail || '登录失败，请检查验证码'
    ElMessage.error(errorMsg)
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: #333;
}

.tips {
  text-align: center;
  color: #999;
  font-size: 12px;
  margin-top: 20px;
}
</style>
