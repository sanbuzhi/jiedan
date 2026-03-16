<template>
  <div class="referral-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>推荐分享</span>
        </div>
      </template>
      <div v-if="userStore.userInfo" class="referral-content">
        <div class="referral-code-section">
          <h3>您的推荐码</h3>
          <div class="code-display">
            <el-tag type="success" size="large">{{ userStore.userInfo.referral_code }}</el-tag>
            <el-button type="primary" @click="copyReferralCode">复制</el-button>
          </div>
        </div>
        
        <el-divider />
        
        <div class="share-link-section">
          <h3>推荐链接</h3>
          <div class="link-display">
            <el-input :value="shareLink" readonly>
              <template #append>
                <el-button @click="copyShareLink">复制链接</el-button>
              </template>
            </el-input>
          </div>
        </div>
        
        <el-divider />
        
        <div class="rules-section">
          <h3>奖励规则</h3>
          <el-empty v-if="rules.length === 0" description="暂无奖励规则" />
          <el-table :data="rules" stripe style="width: 100%">
            <el-table-column prop="name" label="规则名称" />
            <el-table-column prop="description" label="描述" />
            <el-table-column prop="points" label="奖励积分">
              <template #default="{ row }">
                <span class="points">{{ row.points }} 分</span>
              </template>
            </el-table-column>
            <el-table-column prop="max_level" label="最多级数" />
          </el-table>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'
import api from '@/utils/api'
import { trackView, trackShare, trackClick } from '@/utils/analytics'

const userStore = useUserStore()
const rules = ref([])

const shareLink = computed(() => {
  if (!userStore.userInfo) return ''
  return `${window.location.origin}/login?code=${userStore.userInfo.referral_code}`
})

onMounted(async () => {
  try {
    rules.value = await api.getRules()
  } catch (error) {
    console.error('获取规则失败:', error)
  }
  trackView('referral_page')
})

async function copyReferralCode() {
  navigator.clipboard.writeText(userStore.userInfo.referral_code)
  await trackClick('copy_referral_code', { referralCode: userStore.userInfo.referral_code })
  ElMessage.success('推荐码已复制')
}

async function copyShareLink() {
  navigator.clipboard.writeText(shareLink.value)
  await trackShare('referral_link', { referralCode: userStore.userInfo.referral_code })
  ElMessage.success('推荐链接已复制')
}
</script>

<style scoped>
.referral-container {
  max-width: 800px;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.referral-content {
  padding: 20px 0;
}

.referral-code-section h3,
.share-link-section h3,
.rules-section h3 {
  margin-bottom: 15px;
  color: #333;
}

.code-display {
  display: flex;
  align-items: center;
  gap: 15px;
}

.points {
  color: #f56c6c;
  font-weight: bold;
}
</style>
