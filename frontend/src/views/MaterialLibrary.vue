<template>
  <div class="material-library">
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
            <el-menu-item index="/profile">
              <el-icon><User /></el-icon>
              <span>个人中心</span>
            </el-menu-item>
            <el-menu-item index="/referral">
              <el-icon><Share /></el-icon>
              <span>推荐分享</span>
            </el-menu-item>
            <el-menu-item index="/referral-tree">
              <el-icon><Connection /></el-icon>
              <span>推荐关系</span>
            </el-menu-item>
            <el-menu-item index="/points">
              <el-icon><Coin /></el-icon>
              <span>积分记录</span>
            </el-menu-item>
          </el-menu>
        </el-aside>
        <el-main class="main">
          <el-card class="library-card">
            <template #header>
              <div class="card-header">
                <span>物料库管理</span>
              </div>
            </template>
            
            <div class="filter-section">
              <el-form :inline="true" :model="filterForm">
                <el-form-item label="平台">
                  <el-select v-model="filterForm.platform" placeholder="全部" clearable>
                    <el-option label="微信" value="wechat" />
                    <el-option label="抖音" value="douyin" />
                    <el-option label="小红书" value="xiaohongshu" />
                    <el-option label="知乎" value="zhihu" />
                    <el-option label="B站" value="bilibili" />
                  </el-select>
                </el-form-item>
                <el-form-item label="类型">
                  <el-select v-model="filterForm.material_type" placeholder="全部" clearable>
                    <el-option label="文案" value="copy" />
                    <el-option label="图片描述" value="image_prompt" />
                    <el-option label="视频脚本" value="video_script" />
                    <el-option label="成功案例" value="case_study" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearch">查询</el-button>
                  <el-button @click="handleReset">重置</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-table :data="materials" v-loading="loading" stripe>
              <el-table-column prop="title" label="标题" min-width="200" />
              <el-table-column prop="platform" label="平台" width="100">
                <template #default="{ row }">
                  {{ getPlatformName(row.platform) }}
                </template>
              </el-table-column>
              <el-table-column prop="material_type" label="类型" width="100">
                <template #default="{ row }">
                  {{ getMaterialTypeName(row.material_type) }}
                </template>
              </el-table-column>
              <el-table-column prop="keywords" label="关键词" min-width="150" show-overflow-tooltip />
              <el-table-column label="标签" min-width="150">
                <template #default="{ row }">
                  <el-tag v-for="tag in row.tags" :key="tag" size="small" style="margin-right: 5px; margin-bottom: 5px;">
                    {{ tag }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="created_at" label="创建时间" width="180">
                <template #default="{ row }">
                  {{ formatDate(row.created_at) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="220" fixed="right">
                <template #default="{ row }">
                  <el-button type="primary" link size="small" @click="handleView(row)">查看</el-button>
                  <el-button type="primary" link size="small" @click="handleEdit(row)">编辑</el-button>
                  <el-button type="danger" link size="small" @click="handleDelete(row)">删除</el-button>
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
    
    <el-dialog v-model="viewDialogVisible" title="查看物料" width="700px">
      <el-descriptions :column="1" border v-if="currentMaterial">
        <el-descriptions-item label="标题">{{ currentMaterial.title }}</el-descriptions-item>
        <el-descriptions-item label="平台">{{ getPlatformName(currentMaterial.platform) }}</el-descriptions-item>
        <el-descriptions-item label="类型">{{ getMaterialTypeName(currentMaterial.material_type) }}</el-descriptions-item>
        <el-descriptions-item label="关键词">{{ currentMaterial.keywords }}</el-descriptions-item>
        <el-descriptions-item label="标签">
          <el-tag v-for="tag in currentMaterial.tags" :key="tag" size="small" style="margin-right: 5px;">
            {{ tag }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="内容">
          <div style="white-space: pre-wrap;">{{ currentMaterial.content }}</div>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="handleCopyContent">复制内容</el-button>
        <el-button @click="handleExportMaterial">导出</el-button>
        <el-button @click="viewDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <el-dialog v-model="editDialogVisible" title="编辑物料" width="700px">
      <el-form :model="editForm" label-width="100px">
        <el-form-item label="标题">
          <el-input v-model="editForm.title" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="editForm.content" type="textarea" :rows="10" />
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="editForm.tags" multiple filterable allow-create placeholder="请选择或输入标签">
            <el-option v-for="tag in editForm.tags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="saving">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { House, User, Share, Connection, Coin, MagicStick, Files } from '@element-plus/icons-vue'
import api from '@/utils/api'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)
const loading = ref(false)
const saving = ref(false)
const materials = ref([])
const currentPage = ref(1)
const pageSize = ref(20)
const total = ref(0)
const viewDialogVisible = ref(false)
const editDialogVisible = ref(false)
const currentMaterial = ref(null)

const filterForm = ref({
  platform: '',
  material_type: ''
})

const editForm = ref({
  id: null,
  title: '',
  content: '',
  tags: []
})

const platformMap = {
  wechat: '微信',
  douyin: '抖音',
  xiaohongshu: '小红书',
  zhihu: '知乎',
  bilibili: 'B站'
}

const materialTypeMap = {
  copy: '文案',
  image_prompt: '图片描述',
  video_script: '视频脚本',
  case_study: '成功案例'
}

onMounted(async () => {
  if (!userStore.userInfo) {
    try {
      await userStore.fetchUserInfo()
    } catch (error) {
      console.error('获取用户信息失败:', error)
    }
  }
  await fetchMaterials()
})

async function fetchMaterials() {
  loading.value = true
  try {
    const params = {
      skip: (currentPage.value - 1) * pageSize.value,
      limit: pageSize.value
    }
    if (filterForm.value.platform) {
      params.platform = filterForm.value.platform
    }
    if (filterForm.value.material_type) {
      params.material_type = filterForm.value.material_type
    }
    
    const res = await api.getMaterials(params)
    materials.value = res.data
    total.value = res.data.length
  } catch (error) {
    ElMessage.error('获取物料列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

function getPlatformName(platform) {
  return platformMap[platform] || platform
}

function getMaterialTypeName(type) {
  return materialTypeMap[type] || type
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN')
}

function handleSearch() {
  currentPage.value = 1
  fetchMaterials()
}

function handleReset() {
  filterForm.value = {
    platform: '',
    material_type: ''
  }
  currentPage.value = 1
  fetchMaterials()
}

function handleSizeChange(size) {
  pageSize.value = size
  fetchMaterials()
}

function handleCurrentChange(page) {
  currentPage.value = page
  fetchMaterials()
}

function handleView(row) {
  currentMaterial.value = row
  viewDialogVisible.value = true
}

function handleEdit(row) {
  editForm.value = {
    id: row.id,
    title: row.title,
    content: row.content,
    tags: [...(row.tags || [])]
  }
  editDialogVisible.value = true
}

async function handleSaveEdit() {
  if (!editForm.value.title || !editForm.value.content) {
    ElMessage.warning('标题和内容不能为空')
    return
  }
  
  saving.value = true
  try {
    await api.updateMaterial(editForm.value.id, {
      title: editForm.value.title,
      content: editForm.value.content,
      tags: editForm.value.tags
    })
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    await fetchMaterials()
  } catch (error) {
    ElMessage.error('保存失败')
    console.error(error)
  } finally {
    saving.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定要删除这个物料吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await api.deleteMaterial(row.id)
    ElMessage.success('删除成功')
    await fetchMaterials()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

async function handleCopyContent() {
  if (!currentMaterial.value) return
  try {
    await navigator.clipboard.writeText(currentMaterial.value.content)
    ElMessage.success('已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败')
    console.error(error)
  }
}

function handleExportMaterial() {
  if (!currentMaterial.value) return
  const content = `# ${currentMaterial.value.title}\n\n${currentMaterial.value.content}\n\n标签：${(currentMaterial.value.tags || []).join(', ')}`
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${currentMaterial.value.title}.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
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
.material-library {
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

.library-card {
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
