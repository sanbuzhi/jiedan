===FILE:front/admin/src/stores/app.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 侧边栏折叠状态
  const sidebarCollapsed = ref(false)
  // 页面全局加载状态
  const pageLoading = ref(false)

  // 切换侧边栏
  const toggleSidebar = () => {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }
  // 设置页面加载状态
  const setPageLoading = (loading) => {
    pageLoading.value = loading
  }

  return {
    sidebarCollapsed,
    pageLoading,
    toggleSidebar,
    setPageLoading
  }
}, {
  persist: {
    key: 'admin-app-store',
    storage: localStorage,
    paths: ['sidebarCollapsed']
  }
})
```

===FILE:front/admin/src/views/403.vue===
```vue
<template>
  <div class="error-page">
    <el-result icon="warning" title="403" sub-title="抱歉，您没有权限访问此页面">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
const goHome = () => {
  router.push('/')
}
</script>

<style scoped lang="css">
.error-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 120px);
}
</style>
```

===FILE:front/admin/src/api/settings.js===
```javascript
import request from '@/utils/request'

// 获取当前管理员信息
export const getCurrentAdmin = () => {
  return request({
    url: '/admin/settings/profile',
    method: 'get'
  })
}

// 修改当前管理员信息
export const updateCurrentAdmin = (data) => {
  return request({
    url: '/admin/settings/profile',
    method: 'put',
    data
  })
}

// 修改当前管理员密码
export const updateCurrentAdminPassword = (data) => {
  return request({
    url: '/admin/settings/password',
    method: 'put',
    data
  })
}
```

===FILE:front/admin/src/views/Settings.vue===
```vue
<template>
  <div class="settings-page">
    <PageHeader title="系统设置" />
    <el-card class="settings-card">
      <el-tabs v-model="activeTab">
        <!-- 个人信息 -->
        <el-tab-pane label="个人信息" name="profile">
          <el-form ref="profileFormRef" :model="profileForm" :rules="profileRules" label-width="100px" class="settings-form">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="profileForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称" prop="nickname">
              <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
            </el-form-item>
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
            </el-form-item>
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdateProfile" :loading="profileLoading">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        <!-- 修改密码 -->
        <el-tab-pane label="修改密码" name="password">
          <el-form ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" label-width="100px" class="settings-form">
            <el-form-item label="旧密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword" :loading="passwordLoading">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getCurrentAdmin, updateCurrentAdmin, updateCurrentAdminPassword } from '@/api/settings'
import { validateConfirmPassword } from '@/utils/validate'

const activeTab = ref('profile')
const profileFormRef = ref(null)
const passwordFormRef = ref(null)
const profileLoading = ref(false)
const passwordLoading = ref(false)

const profileForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const profileRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword(passwordForm, 'newPassword'), trigger: 'blur' }
  ]
}

const getProfile = async () => {
  try {
    const res = await getCurrentAdmin()
    Object.assign(profileForm, res.data)
  } catch (error) {
    console.error('获取管理员信息失败', error)
  }
}

const handleUpdateProfile = async () => {
  if (!profileFormRef.value) return
  await profileFormRef.value.validate(async (valid) => {
    if (valid) {
      profileLoading.value = true
      try {
        await updateCurrentAdmin(profileForm)
        ElMessage.success('修改成功')
        getProfile()
      } catch (error) {
        console.error('修改管理员信息失败', error)
      } finally {
        profileLoading.value = false
      }
    }
  })
}

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return
  await passwordFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordLoading.value = true
      try {
        await updateCurrentAdminPassword({
          oldPassword: passwordForm.oldPassword,
          newPassword: passwordForm.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        passwordFormRef.value.resetFields()
        // 可以在这里触发退出登录
      } catch (error) {
        console.error('修改密码失败', error)
      } finally {
        passwordLoading.value = false
      }
    }
  })
}

onMounted(() => {
  getProfile()
})
</script>

<style scoped lang="css">
.settings-page {
  padding: 20px;
}
.settings-card {
  margin-top: 20px;
}
.settings-form {
  max-width: 600px;
  margin: 20px auto;
}
</style>
```

===FILE:front/store/src/stores/app.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  // 底部导航栏显示状态
  const showTabBar = ref(true)
  // 页面全局加载状态
  const pageLoading = ref(false)
  // 返回顶部按钮显示状态
  const showBackTop = ref(false)

  // 设置底部导航栏显示
  const setShowTabBar = (show) => {
    showTabBar.value = show
  }
  // 设置页面加载状态
  const setPageLoading = (loading) => {
    pageLoading.value = loading
  }
  // 设置返回顶部按钮显示
  const setShowBackTop = (show) => {
    showBackTop.value = show
  }

  return {
    showTabBar,
    pageLoading,
    showBackTop,
    setShowTabBar,
    setPageLoading,
    setShowBackTop
  }
}, {
  persist: {
    key: 'store-app-store',
    storage: localStorage,
    paths: [] // 这些状态不需要持久化
  }
})
```

===FILE:front/store/src/api/passwordReset.js===
```javascript
import request from '@/utils/request'

// 发送密码重置验证码
export const sendResetCode = (data) => {
  return request({
    url: '/user/password-reset/send-code',
    method: 'post',
    data
  })
}

// 验证密码重置验证码
export const verifyResetCode = (data) => {
  return request({
    url: '/user/password-reset/verify-code',
    method: 'post',
    data
  })
}

// 重置密码
export const resetPassword = (data) => {
  return request({
    url: '/user/password-reset',
    method: 'post',
    data
  })
}
```

===FILE:front/store/src/views/PasswordReset.vue===
```vue
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
```

===FILE:front/store/src/views/PasswordChange.vue===
```vue
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
```

===FILE:front/store/src/views/UserProfileEdit.vue===
```vue
<template>
  <div class="user-profile-edit-page">
    <van-nav-bar title="编辑个人信息" left-arrow @click-left="goBack" />
    <div class="edit-content">
      <van-form ref="formRef" @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="form.nickname"
            name="nickname"
            label="昵称"
            placeholder="请输入昵称"
            :rules="[{ required: true, message: '请输入昵称' }]"
          />
          <van-field
            v-model="form.avatar"
            name="avatar"
            label="头像"
            readonly
            is-link
            placeholder="请选择头像"
            @click="showAvatarPicker = true"
          >
            <template #input>
              <img v-if="form.avatar" :src="form.avatar" class="avatar-preview" />
            </template>
          </van-field>
          <van-field
            v-model="form.gender"
            name="gender"
            label="性别"
            is-link
            readonly
            :placeholder="genderOptions.find(item => item.value === form.gender)?.label || '请选择性别'"
            @click="showGenderPicker = true"
          />
          <van-field
            v-model="form.birthday"
            name="birthday"
            label="生日"
            is-link
            readonly
            placeholder="请选择生日"
            @click="showBirthdayPicker = true"
          />
        </van-cell-group>
        <div class="submit-btn">
          <van-button round block type="primary" native-type="submit" :loading="loading">保存修改</van-button>
        </div>
      </van-form>

      <!-- 性别选择器 -->
      <van-popup v-model:show="showGenderPicker" position="bottom">
        <van-picker
          :columns="genderOptions"
          @confirm="onGenderConfirm"
          @cancel="showGenderPicker = false"
        />
      </van-popup>

      <!-- 生日选择器 -->
      <van-popup v-model:show="showBirthdayPicker" position="bottom">
        <van-date-picker
          v-model="birthdayValue"
          :max-date="new Date()"
          @confirm="onBirthdayConfirm"
          @cancel="showBirthdayPicker = false"
        />
      </van-popup>

      <!-- 头像选择器（占位，后续可替换为上传组件） -->
      <van-popup v-model:show="showAvatarPicker" position="bottom">
        <div class="avatar-picker">
          <van-cell title="拍照" is-link @click="handleAvatarSelect('https://via.placeholder.com/100')" />
          <van-cell title="从相册选择" is-link @click="handleAvatarSelect('https://via.placeholder.com/100')" />
          <van-cell title="取消" is-link @click="showAvatarPicker = false" />
        </div>
      </van-popup>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showSuccessToast } from 'vant'
import { getCurrentUser, updateCurrentUser } from '@/api/user'
import { useUserStore } from '@/stores/user'
import { formatDate } from '@/utils/format'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const showGenderPicker = ref(false)
const showBirthdayPicker = ref(false)
const showAvatarPicker = ref(false)
const birthdayValue = ref([new Date().getFullYear(), new Date().getMonth() + 1, new Date().getDate()])

const genderOptions = [
  { text: '男', value: 1 },
  { text: '女', value: 2 },
  { text: '保密', value: 0 }
]

const form = reactive({
  nickname: '',
  avatar: '',
  gender: 0,
  birthday: ''
})

const goBack = () => {
  router.back()
}

const getProfile = async () => {
  try {
    const res = await getCurrentUser()
    Object.assign(form, res.data)
    if (form.birthday) {
      const [year, month, day] = form.birthday.split('-').map(Number)
      birthdayValue.value = [year, month, day]
    }
  } catch (error) {
    console.error('获取用户信息失败', error)
  }
}

const onGenderConfirm = ({ selectedOptions }) => {
  form.gender = selectedOptions[0].value
  showGenderPicker.value = false
}

const onBirthdayConfirm = ({ selectedValues }) => {
  form.birthday = formatDate(new Date(selectedValues[0], selectedValues[1] - 1, selectedValues[2]))
  showBirthdayPicker.value = false
}

const handleAvatarSelect = (url) => {
  form.avatar = url
  showAvatarPicker.value = false
}

const handleSubmit = async () => {
  loading.value = true
  try {
    const res = await updateCurrentUser(form)
    showSuccessToast('修改成功')
    // 更新本地用户信息
    await userStore.getUserInfo()
    router.back()
  } catch (error) {
    console.error('修改用户信息失败', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  getProfile()
})
</script>

<style scoped lang="css">
.user-profile-edit-page {
  min-height: 100vh;
  background-color: #f7f8fa;
}
.edit-content {
  padding: 20px;
}
.avatar-preview {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}
.submit-btn {
  margin-top: 40px;
}
.avatar-picker {
  padding-bottom: env(safe-area-inset-bottom);
}
</style>
```