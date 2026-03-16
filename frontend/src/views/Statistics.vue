<template>
  <div class="statistics">
    <el-container class="container">
      <el-header class="header">
        <div class="header-left">
          <h1>智能获客系统</h1>
        </div>
        <div class="header-right">
          <span v-if="userStore.userInfo" class="user-info">
            <el-icon><User /></el-icon>
            {{ userStore.userInfo.nickname || userStore.userInfo.phone }}
          </span>
          <el-button type="primary" link @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>
      <el-container>
        <el-aside width="200px" class="aside">
          <el-menu
            :default-active="activeMenu"
            router
            background-color="#545c64"
            text-color="#fff"
            active-text-color="#ffd04b"
          >
            <el-menu-item index="/">
              <el-icon><House /></el-icon>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="/material-generator">
              <el-icon><MagicStick /></el-icon>
              <span>物料生成</span>
            </el-menu-item>
            <el-menu-item index="/material-library">
              <el-icon><Files /></el-icon>
              <span>物料库</span>
            </el-menu-item>
            <el-menu-item index="/platform-accounts">
              <el-icon><Key /></el-icon>
              <span>平台账号</span>
            </el-menu-item>
            <el-menu-item index="/publish-tasks">
              <el-icon><Document /></el-icon>
              <span>发布任务</span>
            </el-menu-item>
            <el-menu-item index="/publish-history">
              <el-icon><Clock /></el-icon>
              <span>发布历史</span>
            </el-menu-item>
            <el-menu-item index="/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <span>数据统计</span>
            </el-menu-item>
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <el-row :gutter="20" class="summary-row">
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-value">{{ statistics.total_tasks }}</div>
                  <div class="stat-label">总任务数</div>
                </div>
                <el-icon class="stat-icon" style="color: #409eff;"><Document /></el-icon>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-value success">{{ statistics.success_tasks }}</div>
                  <div class="stat-label">成功任务</div>
                </div>
                <el-icon class="stat-icon" style="color: #67c23a;"><CircleCheck /></el-icon>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-value danger">{{ statistics.failed_tasks }}</div>
                  <div class="stat-label">失败任务</div>
                </div>
                <el-icon class="stat-icon" style="color: #f56c6c;"><CircleClose /></el-icon>
              </el-card>
            </el-col>
            <el-col :span="6">
              <el-card class="stat-card">
                <div class="stat-content">
                  <div class="stat-value" :style="{ color: statistics.success_rate >= 80 ? '#67c23a' : statistics.success_rate >= 50 ? '#e6a23c' : '#f56c6c' }">
                    {{ statistics.success_rate.toFixed(1) }}%
                  </div>
                  <div class="stat-label">成功率</div>
                </div>
                <el-icon class="stat-icon" style="color: #e6a23c;"><TrendCharts /></el-icon>
              </el-card>
            </el-col>
          </el-row>
          
          <el-card class="platform-stats-card" v-loading="loading">
            <template #header>
              <div class="card-header">
                <span>各平台发布统计</span>
              </div>
            </template>
            
            <el-table :data="platformStatsList" stripe>
              <el-table-column prop="platform" label="平台" width="180">
                <template #default="{ row }">
                  {{ getPlatformName(row.platform) }}
                </template>
              </el-table-column>
              <el-table-column prop="total" label="总发布数" width="120" align="center" />
              <el-table-column prop="success" label="成功数" width="120" align="center">
                <template #default="{ row }">
                  <span style="color: #67c23a;">{{ row.success }}</span>
                </template>
              </el-table-column>
              <el-table-column prop="failed" label="失败数" width="120" align="center">
                <template #default="{ row }">
                  <span style="color: #f56c6c;">{{ row.failed }}</span>
                </template>
              </el-table-column>
              <el-table-column label="成功率" width="150" align="center">
                <template #default="{ row }">
                  <el-progress 
                    :percentage="row.total > 0 ? (row.success / row.total * 100).toFixed(1) : 0" 
                    :color="getProgressColor(row.total > 0 ? (row.success / row.total * 100) : 0)"
                  />
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, User, MagicStick, Files, Key, Document, Clock, DataAnalysis, CircleCheck, CircleClose, TrendCharts } from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const loading = ref(false)
const statistics = ref({
  total_tasks: 0,
  success_tasks: 0,
  failed_tasks: 0,
  success_rate: 0,
  platform_statistics: {}
})

const platformStatsList = computed(() => {
  const list = []
  const platforms = ['wechat', 'xiaohongshu', 'zhihu', 'douyin', 'bilibili']
  
  platforms.forEach(platform => {
    const stats = statistics.value.platform_statistics[platform] || { total: 0, success: 0, failed: 0 }
    list.push({
      platform,
      total: stats.total,
      success: stats.success,
      failed: stats.failed
    })
  })
  
  return list
})

const platformMap = {
  wechat: '微信公众号/视频号',
  xiaohongshu: '小红书',
  zhihu: '知乎',
  douyin: '抖音',
  bilibili: 'B站'
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  await fetchStatistics()
})

async function fetchStatistics() {
  loading.value = true
  try {
    const res = await api.getPublishStatistics()
    statistics.value = res.data
  } catch (error) {
    ElMessage.error('获取统计数据失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function getPlatformName(platform) {
  return platformMap[platform] || platform
}

function getProgressColor(percentage) {
  if (percentage >= 80) return '#67c23a'
  if (percentage >= 50) return '#e6a23c'
  return '#f56c6c'
}

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch {
  }
}
</script>

<style scoped>
.statistics {
  width: 100%;
  height: 100%;
}

.container {
  height: 100%;
}

.header {
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
}

.header h1 {
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 5px;
}

.aside {
  background-color: #545c64;
}

.main {
  padding: 20px;
  background-color: #f5f7fa;
}

.summary-row {
  margin-bottom: 20px;
}

.stat-card {
  position: relative;
  overflow: hidden;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
}

.stat-content {
  z-index: 1;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 8px;
}

.stat-value.success {
  color: #67c23a;
}

.stat-value.danger {
  color: #f56c6c;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.stat-icon {
  font-size: 60px;
  opacity: 0.1;
}

.platform-stats-card {
  height: calc(100% - 140px);
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}
</style>
