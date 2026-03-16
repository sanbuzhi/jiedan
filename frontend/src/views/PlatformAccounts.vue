<template>
  <div class="platform-accounts">
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
          <el-card class="accounts-card">
            <template #header>
              <div class="card-header">
                <span>平台账号配置</span>
                <el-button type="primary" @click="handleAdd">添加账号</el-button>
              </div>
            </template>
            
            <el-table :data="accounts" v-loading="loading" stripe>
              <el-table-column prop="account_name" label="账号名称" min-width="150" />
              <el-table-column prop="platform" label="平台" width="120">
                <template #default="{ row }">
                  {{ getPlatformName(row.platform) }}
                </template>
              </el-table-column>
              <el-table-column label="状态" width="100">
                <template #default="{ row }">
                  <el-tag :type="row.is_active ? 'success' : 'danger'">
                    {{ row.is_active ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.created_at) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="200" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-main>
      </el-container>
    </el-container>
    
    <el-dialog v-model="dialogVisible" :title="editingAccount ? '编辑账号' : '添加账号'" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="120px">
        <el-form-item label="平台" prop="platform">
          <el-select v-model="form.platform" placeholder="请选择平台" style="width: 100%;" :disabled="!!editingAccount">
            <el-option v-for="platform in platforms" :key="platform" :label="getPlatformName(platform)" :value="platform" />
          </el-select>
        </el-form-item>
        <el-form-item label="账号名称" prop="account_name">
          <el-input v-model="form.account_name" placeholder="请输入账号名称" />
        </el-form-item>
        <el-form-item label="API Key">
          <el-input v-model="form.api_key" placeholder="请输入API Key" type="password" show-password />
        </el-form-item>
        <el-form-item label="API Secret">
          <el-input v-model="form.api_secret" placeholder="请输入API Secret" type="password" show-password />
        </el-form-item>
        <el-form-item label="Access Token">
          <el-input v-model="form.access_token" placeholder="请输入Access Token" type="password" show-password />
        </el-form-item>
        <el-form-item label="Refresh Token">
          <el-input v-model="form.refresh_token" placeholder="请输入Refresh Token" type="password" show-password />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.is_active" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">保存</el-button>
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

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const loading = ref(false)
const submitting = ref(false)
const accounts = ref([])
const platforms = ref([])
const dialogVisible = ref(false)
const editingAccount = ref(null)
const formRef = ref(null)

const form = ref({
  platform: '',
  account_name: '',
  api_key: '',
  api_secret: '',
  access_token: '',
  refresh_token: '',
  is_active: true
})

const rules = {
  platform: [{ required: true, message: '请选择平台', trigger: 'change' }],
  account_name: [{ required: true, message: '请输入账号名称', trigger: 'blur' }]
}

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
  await fetchPlatforms()
  await fetchAccounts()
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

async function fetchAccounts() {
  loading.value = true
  try {
    const res = await api.getPlatformAccounts()
    accounts.value = res.data
  } catch (error) {
    ElMessage.error('获取账号列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function getPlatformName(platform) {
  return platformMap[platform] || platform
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function handleAdd() {
  editingAccount.value = null
  form.value = {
    platform: '',
    account_name: '',
    api_key: '',
    api_secret: '',
    access_token: '',
    refresh_token: '',
    is_active: true
  }
  dialogVisible.value = true
}

function handleEdit(row) {
  editingAccount.value = row
  form.value = {
    platform: row.platform,
    account_name: row.account_name,
    api_key: row.api_key || '',
    api_secret: row.api_secret || '',
    access_token: row.access_token || '',
    refresh_token: row.refresh_token || '',
    is_active: row.is_active
  }
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (editingAccount.value) {
        await api.updatePlatformAccount(editingAccount.value.id, form.value)
        ElMessage.success('更新成功')
      } else {
        await api.createPlatformAccount(form.value)
        ElMessage.success('创建成功')
      }
      dialogVisible.value = false
      await fetchAccounts()
    } catch (error) {
      ElMessage.error(editingAccount.value ? '更新失败' : '创建失败')
      console.error(error)
    } finally {
      submitting.value = false
    }
  })
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这个账号吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.deletePlatformAccount(row.id)
    ElMessage.success('删除成功')
    await fetchAccounts()
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
.platform-accounts {
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

.accounts-card {
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
  font-size: 16px;
}
</style>
