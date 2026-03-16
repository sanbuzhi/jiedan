<template>
  <div class="publish-tasks">
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
          <el-card class="tasks-card">
            <template #header>
              <div class="card-header">
                <span>发布任务管理</span>
                <el-button type="primary" @click="handleCreate">创建任务</el-button>
              </div>
            </template>
            
            <div class="filter-section">
              <el-form :inline="true" :model="filterForm">
                <el-form-item label="状态">
                  <el-select v-model="filterForm.status" placeholder="全部" clearable>
                    <el-option label="待处理" value="pending" />
                    <el-option label="已调度" value="scheduled" />
                    <el-option label="执行中" value="running" />
                    <el-option label="成功" value="success" />
                    <el-option label="失败" value="failed" />
                    <el-option label="已取消" value="cancelled" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-table :data="tasks" v-loading="loading" stripe>
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column label="发布平台" min-width="200">
                <template #default="{ row }">
                  <el-tag v-for="platform in row.platforms" :key="platform" size="small" style="margin-right: 5px;">
                    {{ getPlatformName(platform) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="status" label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="getStatusType(row.status)">
                    {{ getStatusName(row.status) }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column label="重试次数" width="100">
                <template #default="{ row }">
                  {{ row.retry_count }} / {{ row.max_retries }}
                </template>
              </el-table-column>
              <el-table-column prop="scheduled_time" label="计划时间" width="180">
                <template #default="{ row }">
                  {{ row.scheduled_time ? formatDate(row.scheduled_time) : '立即执行' }}
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.created_at) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="280" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
                  <el-button 
                    v-if="row.status === 'pending' || row.status === 'scheduled'" 
                    type="primary" 
                    link 
                    size="small" 
                    @click="handleEdit(row)"
                  >编辑</el-button>
                  <el-button 
                    v-if="row.status === 'pending' || row.status === 'scheduled'" 
                    type="warning" 
                    link 
                    size="small" 
                    @click="handleCancel(row)"
                  >取消</el-button>
                  <el-button 
                    v-if="row.status === 'failed' && row.retry_count < row.max_retries" 
                    type="success" 
                    link 
                    size="small" 
                    @click="handleRetry(row)"
                  >重试</el-button>
                </template>
              </el-table-column>
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
    
    <el-dialog v-model="createDialogVisible" title="创建发布任务" width="700px">
      <el-form :model="createForm" :rules="createRules" ref="createFormRef" label-width="100px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="createForm.content" type="textarea" :rows="8" placeholder="请输入内容" />
        </el-form-item>
        <el-form-item label="发布平台" prop="platforms">
          <el-checkbox-group v-model="createForm.platforms">
            <el-checkbox v-for="platform in platforms" :key="platform" :label="platform">
              {{ getPlatformName(platform) }}
            </el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item label="计划时间">
          <el-date-picker
            v-model="createForm.scheduled_time"
            type="datetime"
            placeholder="选择时间（留空则立即执行）"
            style="width: 100%;"
            :disabled-date="disabledDate"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateSubmit" :loading="creating">创建</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="viewDialogVisible" title="任务详情" width="800px">
      <el-descriptions :column="1" border v-if="currentTask">
        <el-descriptions-item label="标题">{{ currentTask.title }}</el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap; max-height: 200px; overflow-y: auto;">{{ currentTask.content }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="发布平台">
          <el-tag v-for="platform in currentTask.platforms" :key="platform" size="small" style="margin-right: 5px;">
            {{ getPlatformName(platform) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentTask.status)">
            {{ getStatusName(currentTask.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="重试次数">{{ currentTask.retry_count }} / {{ currentTask.max_retries }}</el-descriptions-item>
        <el-descriptions-item label="计划时间">{{ currentTask.scheduled_time ? formatDate(currentTask.scheduled_time) : '立即执行' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentTask.created_at) }}</el-descriptions-item>
        <el-descriptions-item v-if="currentTask.error_message" label="错误信息">
          <div style="color: #f56c6c;">{{ currentTask.error_message }}</div>
        </el-descriptions-item>
      </el-descriptions>
      
      <el-divider />
      <h4>发布记录</h4>
      <el-table :data="currentTask.records || []" stripe size="small">
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
        <el-table-column label="链接" min-width="200">
          <template #default="{ row }">
            <el-link v-if="row.publish_url" :href="row.publish_url" target="_blank" type="primary">查看</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="published_at" label="发布时间" width="180">
          <template #default="{ row }">
            {{ row.published_at ? formatDate(row.published_at) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="error_message" label="错误信息" min-width="200" show-overflow-tooltip />
      </el-table>
      
      <template #footer>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, User, MagicStick, Files, Key, Document, Clock, DataAnalysis } from '@element-plus/icons-vue'
import api from '@/utils/api'
import { trackView, trackClick, trackPublish } from '@/utils/analytics'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const loading = ref(false)
const creating = ref(false)
const tasks = ref([])
const platforms = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const createDialogVisible = ref(false)
const viewDialogVisible = ref(false)
const currentTask = ref(null)
const createFormRef = ref(null)

const filterForm = ref({
  status: ''
})

const createForm = ref({
  title: '',
  content: '',
  platforms: [],
  scheduled_time: null
})

const createRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入内容', trigger: 'blur' }],
  platforms: [{ required: true, message: '请选择发布平台', trigger: 'change' }]
}

const platformMap = {
  wechat: '微信公众号/视频号',
  xiaohongshu: '小红书',
  zhihu: '知乎',
  douyin: '抖音',
  bilibili: 'B站'
}

const statusMap = {
  pending: { name: '待处理', type: 'info' },
  scheduled: { name: '已调度', type: 'warning' },
  running: { name: '执行中', type: 'primary' },
  success: { name: '成功', type: 'success' },
  failed: { name: '失败', type: 'danger' },
  cancelled: { name: '已取消', type: 'info' }
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  await fetchPlatforms()
  await fetchTasks()
  trackView('publish_tasks_page')
})

async function fetchPlatforms() {
  try {
    const res = await api.getSupportedPlatforms()
    platforms.value = res.data
  } catch (error) {
    ElMessage.error('获取平台列表失败')
    console.error(error)
  }
}

async function fetchTasks() {
  loading.value = true
  try {
    const params = {
      skip: (currentPage.value - 1) * pageSize.value,
      limit: pageSize.value
    }
    if (filterForm.value.status) {
      params.status = filterForm.value.status
    }
    
    const res = await api.getPublishTasks(params)
    tasks.value = res.data
    total.value = res.data.length
  } catch (error) {
    ElMessage.error('获取任务列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function getPlatformName(platform) {
  return platformMap[platform] || platform
}

function getStatusName(status) {
  return statusMap[status]?.name || status
}

function getStatusType(status) {
  return statusMap[status]?.type || 'info'
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function disabledDate(time) {
  return time.getTime() < Date.now() - 8.64e7
}

function handleSearch() {
  currentPage.value = 1
  fetchTasks()
}

function handleReset() {
  filterForm.value = { status: '' }
  currentPage.value = 1
  fetchTasks()
}

function handleSizeChange(size) {
  pageSize.value = size
  fetchTasks()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchTasks()
}

function handleCreate() {
  createForm.value = {
    title: '',
    content: '',
    platforms: [],
    scheduled_time: null
  }
  createDialogVisible.value = true
}

async function handleCreateSubmit() {
  if (!createFormRef.value) return
  
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    creating.value = true
    try {
      const res = await api.createPublishTask(createForm.value)
      
      for (const platform of createForm.value.platforms) {
        await trackPublish(platform, null, { 
          taskId: res.data?.id,
          isScheduled: !!createForm.value.scheduled_time
        })
      }
      
      ElMessage.success('创建成功')
      createDialogVisible.value = false
      await fetchTasks()
    } catch (error) {
      ElMessage.error('创建失败')
      console.error(error)
    } finally {
      creating.value = false
    }
  })
}

async function handleView(row) {
  try {
    const res = await api.getPublishTask(row.id)
    currentTask.value = res.data
    viewDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取任务详情失败')
    console.error(error)
  }
}

function handleEdit(row) {
  ElMessage.info('编辑功能待实现')
}

async function handleCancel(row) {
  try {
    await ElMessageBox.confirm('确定要取消这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.cancelPublishTask(row.id)
    ElMessage.success('已取消')
    await fetchTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

async function handleRetry(row) {
  try {
    await ElMessageBox.confirm('确定要重试这个任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.retryPublishTask(row.id)
    ElMessage.success('已重新调度')
    await fetchTasks()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
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
.publish-tasks {
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

.tasks-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
