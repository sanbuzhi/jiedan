<template>
  <div class="password-reset-page">
    <van-nav-bar title="重置密码" left-arrow @click-left="goBack" />
    <div class="reset-content">
      <van-steps :active="currentStep">
        <van-step>验证身份</van-step>
        <van-step>设置新密码</van-step>
        <van-step>完成</van-step>
      </van-steps>

      <!-- 验证身份 -->
      <div v-if="currentStep === 0" class="step-content">
        <van-form ref="verifyFormRef" @submit="handleVerify">
          <van-cell-group inset>
            <van-field
              v-model="verifyForm.phone"
              name="phone"
              label="手机号"
              placeholder="请输入手机号"
              :rules="[{ required: true, message: '请输入手机号' }, { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }]"
            />
            <van-field
              v-model="verifyForm.code"
              name="code"
              label="验证码"
              placeholder="请输入验证码"
              :rules="[{ required: true, message: '请输入验证码' }]"
            >
              <template #button>
                <van-button
                  size="small"
                  type="primary"
                  :disabled="codeDisabled"
                  @click="handleSendCode"
                >
                  {{ codeText }}
                </van-button>
              </template>
            </van-field>
          </van-cell-group>
          <div class="submit-btn">
            <van-button round block type="primary" native-type="submit" :loading="verifyLoading">下一步</van-button>
          </div>
        </van-form>
      </div>

      <!-- 设置新密码 -->
      <div v-if="currentStep === 1" class="step-content">
        <van-form ref="passwordFormRef" @submit="handleResetPassword">
          <van-cell-group inset>
            <van-field
              v-model="passwordForm.newPassword"
              type="password"
              name="newPassword"
              label="新密码"
              placeholder="请输入新密码（6-20位）"
              :rules="[{ required: true, message: '请输入新密码' }, { min: 6, max: 20, message: '密码长度在6-20位之间' }]"
            />
            <van-field
              v-model="passwordForm.confirmPassword"
              type="password"
              name="confirmPassword"
              label="确认密码"
              placeholder="请再次输入新密码"
              :rules="[{ required: true, message: '请再次输入新密码' }, { validator: validateConfirmPassword, message: '两次输入的密码不一致' }]"
            />
          </van-cell-group>
          <div class="submit-btn">
            <van-button round block type="primary" native-type="submit" :loading="passwordLoading">确认重置</van-button>
          </div>
        </van-form>
      </div>

      <!-- 完成 -->
      <div v-if="currentStep === 2" class="step-content">
        <div class="success-box">
          <van-icon name="checked" size="80" color="#07c160" />
          <p class="success-text">密码重置成功</p>
          <van-button round type="primary" @click="goLogin">立即登录</van-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { sendResetCode, verifyResetCode, resetPassword } from '@/api/passwordReset'

const router = useRouter()
const currentStep = ref(0)
const verifyFormRef = ref(null)
const passwordFormRef = ref(null)
const verifyLoading = ref(false)
const passwordLoading = ref(false)
const codeDisabled = ref(false)
const codeText = ref('获取验证码')
let codeTimer = null

const verifyForm = reactive({
  phone: '',
  code: ''
})

const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const goBack = () => {
  router.back()
}

const goLogin = () => {
  router.replace('/login')
}

const handleSendCode = async () => {
  if (!verifyForm.phone) {
    showToast('请先输入手机号')
    return
  }
  if (!/^1[3-9]\d{9}$/.test(verifyForm.phone)) {
    showToast('请输入正确的手机号')
    return
  }
  try {
    await sendResetCode({ phone: verifyForm.phone })
    showSuccessToast('验证码发送成功')
    startCodeTimer()
  } catch (error) {
    console.error('发送验证码失败', error)
  }
}

const startCodeTimer = () => {
  codeDisabled.value = true
  let count = 60
  codeText.value = `${count}s`
  codeTimer = setInterval(() => {
    count--
    if (count <= 0) {
      clearInterval(codeTimer)
      codeDisabled.value = false
      codeText.value = '获取验证码'
    } else {
      codeText.value = `${count}s`
    }
  }, 1000)
}

const handleVerify = async () => {
  verifyLoading.value = true
  try {
    await verifyResetCode(verifyForm)
    currentStep.value = 1
  } catch (error) {
    console.error('验证身份失败', error)
  } finally {
    verifyLoading.value = false
  }
}

const validateConfirmPassword = (val) => {
  return val === passwordForm.newPassword
}

const handleResetPassword = async () => {
  passwordLoading.value = true
  try {
    await resetPassword({
      phone: verifyForm.phone,
      code: verifyForm.code,
      newPassword: passwordForm.newPassword
    })
    currentStep.value = 2
  } catch (error) {
    console.error('重置密码失败', error)
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped lang="css">
.password-reset-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}
.reset-content {
  padding: 20px;
}
.step-content {
  margin-top: 40px;
}
.submit-btn {
  margin-top: 40px;
}
.success-box {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding-top: 60px;
}
.success-text {
  margin: 20px 0;
  font-size: 18px;
  color: #333;
}
</style>