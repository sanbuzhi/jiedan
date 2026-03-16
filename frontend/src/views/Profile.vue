<template>
  <div class="profile-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人中心</span>
        </div>
      </template>
      <el-form :model="profileForm" label-width="100px" v-if="userStore.userInfo">
        <el-form-item label="手机号">
          <el-input v-model="profileForm.phone" disabled />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="profileForm.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item label="头像">
          <el-input v-model="profileForm.avatar" placeholder="请输入头像URL" />
        </el-form-item>
        <el-form-item label="推荐码">
          <el-input v-model="profileForm.referralCode" disabled>
            <template #append>
              <el-button @click="copyReferralCode">复制</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="当前积分">
          <el-statistic :value="profileForm.totalPoints">
            <template #suffix>分</template>
          </el-statistic>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleUpdate">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()

const profileForm = ref({
  phone: '',
  nickname: '',
  avatar: '',
  referralCode: '',
  totalPoints: 0
})

onMounted(() => {
  if (userStore.userInfo) {
    profileForm.value = {
      phone: userStore.userInfo.phone,
      nickname: userStore.userInfo.nickname || '',
      avatar: userStore.userInfo.avatar || '',
      referralCode: userStore.userInfo.referral_code,
      totalPoints: userStore.userInfo.total_points
    }
  }
})

function copyReferralCode() {
  navigator.clipboard.writeText(profileForm.value.referralCode)
  ElMessage.success('推荐码已复制')
}

async function handleUpdate() {
  try {
    await userStore.updateUserInfo({
      nickname: profileForm.value.nickname,
      avatar: profileForm.value.avatar
    })
    ElMessage.success('更新成功')
  } catch (error) {
    console.error(error)
  }
}
</script>

<style scoped>
.profile-container {
  max-width: 600px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}
</style>
