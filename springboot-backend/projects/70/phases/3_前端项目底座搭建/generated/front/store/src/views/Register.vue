<template>
  <div class="register">
    <van-nav-bar title="注册" left-arrow @click-left="goBack" />
    <div class="register-container">
      <h2 class="title">创建账号</h2>
      <van-form @submit="handleRegister">
        <van-cell-group inset>
          <van-field
            v-model="registerForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }, { min: 3, message: '用户名至少3个字符' }]"
          />
          <van-field
            v-model="registerForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6个字符' }]"
          />
          <van-field
            v-model="registerForm.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[{ required: true, message: '请确认密码' }, { validator: validateConfirmPassword, message: '两次输入的密码不一致' }]"
          />
        </van-cell-group>
        <div class="button-group">
          <van-button round block type="primary" native-type="submit" :loading="loading">注册</van-button>
          <van-button round block plain type="primary" @click="goLogin">已有账号？登录</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

function validateConfirmPassword(val) {
  return val === registerForm.password
}

function goBack() {
  router.back()
}

function goLogin() {
  router.push({ name: 'Login' })
}

async function handleRegister() {
  loading.value = true
  try {
    await userStore.handleRegister(registerForm)
    showToast('注册成功')
    router.push('/home')
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register {
  width: 100%;
  min-height: 100%;
  background-color: white;
}

.register-container {
  padding: 40px 20px;
}

.title {
  text-align: center;
  margin-bottom: 40px;
  color: #333;
  font-size: 24px;
}

.button-group {
  margin-top: 40px;
  padding: 0 16px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}
</style>