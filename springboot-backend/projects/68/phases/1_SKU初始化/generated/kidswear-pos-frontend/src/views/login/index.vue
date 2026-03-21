<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h1>童趣衣橱</h1>
        <p>3-12岁儿童服装单店进销存POS管理系统</p>
      </div>
      <el-form :model="loginForm" :rules="loginRules" ref="loginFormRef" label-width="0" class="login-form">
        <el-form-item prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" size="large" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" size="large" show-password />
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" size="large" class="login-btn" :loading="loading" @click="handleLogin">登录</el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useUserStore } from '@/store/user'
// import request from '@/utils/request'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: 'admin',
  password: 'Kidswear@123',
  rememberMe: false
})

const loginRules: FormRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        // 临时模拟登录成功
        const mockToken = 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOjEsInVzZXJuYW1lIjoiYWRtaW4iLCJyb2xlQ29kZSI6IlNVUEVSX0FETUlOIiwiaWF0IjoxNzE3MjAwMDAwLCJleHAiOjE3MTcyMDM2MDB9.mock-signature'
        const mockUserInfo = {
          id: 1,
          username: 'admin',
          realName: '系统管理员',
          roleCode: 'SUPER_ADMIN'
        }
        userStore.setToken(mockToken)
        userStore.setUserInfo(mockUserInfo)
        ElMessage.success('登录成功')
        // 根据角色跳转
        if (mockUserInfo.roleCode === 'CASHIER') {
          router.push('/pos-layout')
        } else {
          router.push('/layout')
        }
      } catch (error) {
        console.error(error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.login-header {
  text-align: center;
  margin-bottom: 30px;
}

.login-header h1 {
  font-size: 28px;
  color: #333;
  margin-bottom: 10px;
}

.login-header p {
  font-size: 14px;
  color: #999;
}

.login-form .el-form-item {
  margin-bottom: 20px;
}

.login-btn {
  width: 100%;
}
</style>