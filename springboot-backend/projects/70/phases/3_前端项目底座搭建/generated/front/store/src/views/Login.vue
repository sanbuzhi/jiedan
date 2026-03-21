<template>
  <div class="login">
    <van-nav-bar title="登录" left-arrow @click-left="goBack" />
    <div class="login-container">
      <h2 class="title">欢迎登录</h2>
      <van-form @submit="handleLogin">
        <van-cell-group inset>
          <van-field
            v-model="loginForm.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="loginForm.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }]"
          />
        </van-cell-group>
        <div class="button-group">
          <van-button round block type="primary" native-type="submit" :loading="loading">登录</van-button>
          <van-button round block plain type="primary" @click="goRegister">注册账号</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

function goBack() {
  router.back()
}

function goRegister() {
  router.push({ name: 'Register' })
}

async function handleLogin() {
  loading.value = true
  try {
    await userStore.handleLogin(loginForm)
    showToast('登录成功')
    const redirect = route.query.redirect || '/home'
    router.push(redirect)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  width: 100%;
  min-height: 100%;
  background-color: white;
}

.login-container {
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