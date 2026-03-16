<template>
  <div class="publish-history">
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
          <el-card class="history-card">
            <template #header>
              <div class="card-header">
                <span>发布历史记录</span>
              </div>
            </template>
            
            <div class="filter-section">
              <el-form :inline="true" :model="filterForm">
                <el-form-item label="平台">
                  <el-select v-model="filterForm.platform" placeholder="全部" clearable>
                    <el-option label="微信" value="wechat" />
                    <el-option label="小红书" value="xiaohongshu" />
                    <el-option label="知乎" value="zhihu" />
                    <el-option label="抖音" value="douyin" />
                    <el-option label="B站" value="bilibili" />
                  </el-select>
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="filterForm.status" placeholder="全部" clearable>
                    <el-option label="成功" value="success" />
                    <el-option label="失败" value="failed" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-table :data="records" v-loading="loading" stripe>
              <el-table-column label="任务标题" min-width="200">
                <template #default="{ row }">
                  {{ getTaskTitle(row.task_id) }}
                </template>
              </el-table-column>
              <el-table-column prop="platform" label="平台" width="120">
                <template #default="{ row }">
                  {{ getPlatformName(row.platform) }}
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.status === 'success' ? 'success' : 'danger'" size="small">
                    {{ row.status === 'success' ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="platform_post_id" label="平台ID" min-width="150" />
              <el-table-column label="链接" min-width="150">
                <template #default="{ row }">
                  <el-link v-if="row.publish_url" :href="row.publish_url" target="_blank" type="primary">查看</el-link>
                </template>
              </el-table-column>
              <el-table-column prop="published_at" label="发布时间" width="180">
                <template #default="{ row }">
                  {{ row.published_at ? formatDate(row.published_at) : '-' }}
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="记录时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.created_at) }}
                </template>
              </el-table-column>
              <el-table-column prop="error_message" label="错误信息" min-width="200" show-overflow-tooltip />
            </el-table>
            
            <el-pagination
              v-model:current-page="currentPage"
              v-model:page-size="pageSize"
              :total="total"
              :page-sizes="[10, 20, 50, 100]"
              layout="total, sizes, prev, pager, next, jumper"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              class="pagination"
            />
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
import { House, User, MagicStick, Files, Key, Document, Clock, DataAnalysis } from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const loading = ref(false)
const records = ref([])
const tasks = ref({})
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)

const filterForm = ref({
  platform: '',
  status: ''
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
  await fetchTasks()
  await fetchRecords()
})

async function fetchTasks() {
  try {
    const res = await api.getPublishTasks({ limit: 1000 })
    const taskMap = {}
    res.data.forEach(task => {
      taskMap[task.id] = task.title
    })
    tasks.value = taskMap
  } catch (error) {
    console.error('获取任务列表失败:', error)
  }
}

async function fetchRecords() {
  loading.value = true
  try {
    const params = {
      skip: (currentPage.value - 1) * pageSize.value,
      limit: pageSize.value
    }
    if (filterForm.value.platform) {
      params.platform = filterForm.value.platform
    }
    if (filterForm.value.status) {
      params.status = filterForm.value.status
    }
    
    const res = await api.getPublishRecords(params)
    records.value = res.data
    total.value = res.data.length
  } catch (error) {
    ElMessage.error('获取历史记录失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function getPlatformName(platform) {
  return platformMap[platform] || platform
}

function getTaskTitle(taskId) {
  return tasks.value[taskId] || `任务 #${taskId}`
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function handleSearch() {
  currentPage.value = 1
  fetchRecords()
}

function handleReset() {
  filterForm.value = {
    platform: '',
    status: ''
  }
  currentPage.value = 1
  fetchRecords()
}

function handleSizeChange(size) {
  pageSize.value = size
  fetchRecords()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchRecords()
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
.publish-history {
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

.history-card {
  height: 100%;
}

.card-header {
  font-weight: 500;
  font-size: 16px;
}

.filter-section {
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  justify-content: flex-end;
}
</style>
