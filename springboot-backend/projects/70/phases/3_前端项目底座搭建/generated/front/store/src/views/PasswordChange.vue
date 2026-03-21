<template>
  <div class="password-change-page">
    <van-nav-bar title="修改密码" left-arrow @click-left="goBack" />
    <div class="change-content">
      <van-form ref="formRef" @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.oldPassword"
            type="password"
            name="oldPassword"
            label="旧密码"
            placeholder="请输入旧密码"
            :rules="[{ required: true, message: '请输入旧密码' }]"
          />
          <van-field
            v-model="form.newPassword"
            type="password"
            name="newPassword"
            label="新密码"
            placeholder="请输入新密码（6-20位）"
            :rules="[{ required: true, message: '请输入新密码' }, { min: 6, max: 20, message: '密码长度在6-20位之间' }]"
          />
          <van-field
            v-model="form.confirmPassword"
            type="password"
            name="confirmPassword"
            label="确认密码"
            placeholder="请再次输入新密码"
            :rules="[{ required: true, message: '请再次输入新密码' }, { validator: validateConfirmPassword, message: '两次输入的密码不一致' }]"
          />
        </van-cell-group>
        <div class="submit-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">确认修改</van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { updateCurrentUserPassword } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const goBack = () => {
  router.back()
}

const validateConfirmPassword = (val) => {
  return val === form.newPassword
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await updateCurrentUserPassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword
    })
    showSuccessToast('密码修改成功，请重新登录')
    await userStore.logout()
    router.replace('/login')
  } catch (error) {
    console.error('修改密码失败', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="css">
.password-change-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}
.change-content {
  padding: 20px;
}
.submit-btn {
  margin-top: 40px;
}
</style>