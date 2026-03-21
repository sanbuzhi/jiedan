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