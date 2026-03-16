<template>
  <div class="requirement-management">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>需求管理</span>
          <div class="header-actions">
            <el-button type="primary" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
              刷新
            </el-button>
          </div>
        </div>
      </template>

      <!-- 筛选区域 -->
      <el-form :model="filterForm" inline class="filter-form">
        <el-form-item label="状态">
          <el-select v-model="filterForm.status" placeholder="全部状态" clearable style="width: 120px">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已联系" value="contacted" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="用户类型">
          <el-select v-model="filterForm.user_type" placeholder="全部类型" clearable style="width: 120px">
            <el-option label="个人" value="individual" />
            <el-option label="企业" value="enterprise" />
            <el-option label="代理商" value="agency" />
          </el-select>
        </el-form-item>
        <el-form-item label="项目类型">
          <el-select v-model="filterForm.project_type" placeholder="全部类型" clearable style="width: 150px">
            <el-option label="网站开发" value="website" />
            <el-option label="小程序" value="miniprogram" />
            <el-option label="APP开发" value="app" />
            <el-option label="系统定制" value="custom_system" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">
            <el-icon><Search /></el-icon>
            筛选
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 数据表格 -->
      <el-table :data="requirements" stripe style="width: 100%" v-loading="loading">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="contact_name" label="联系人" width="120" />
        <el-table-column prop="contact_phone" label="联系电话" width="130" />
        <el-table-column prop="user_type" label="用户类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getUserTypeType(row.user_type)">
              {{ getUserTypeLabel(row.user_type) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="project_type" label="项目类型" width="120">
          <template #default="{ row }">
            {{ getProjectTypeLabel(row.project_type) }}
          </template>
        </el-table-column>
        <el-table-column prop="budget_range" label="预算范围" width="120">
          <template #default="{ row }">
            {{ formatBudget(row.budget_range) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="created_at" label="提交时间" width="160">
          <template #default="{ row }">
            {{ formatDate(row.created_at) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="handleViewDetail(row)">
              详情
            </el-button>
            <el-button 
              size="small" 
              :type="row.status === 'pending' ? 'success' : 'warning'"
              @click="handleUpdateStatus(row)"
            >
              {{ getActionLabel(row.status) }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        style="margin-top: 20px; justify-content: flex-end;"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </el-card>

    <!-- 需求详情对话框 -->
    <RequirementDetailDialog
      v-model:visible="detailDialogVisible"
      :requirement="currentRequirement"
      @contact="handleContact"
      @update-status="handleStatusUpdateFromDialog"
    />

    <!-- 状态更新对话框 -->
    <el-dialog v-model="statusDialogVisible" title="更新状态" width="400px">
      <el-form :model="statusForm" label-width="100px">
        <el-form-item label="新状态">
          <el-select v-model="statusForm.status" placeholder="请选择状态" style="width: 100%">
            <el-option label="待处理" value="pending" />
            <el-option label="处理中" value="processing" />
            <el-option label="已联系" value="contacted" />
            <el-option label="已完成" value="completed" />
            <el-option label="已取消" value="cancelled" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input 
            v-model="statusForm.remark" 
            type="textarea" 
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleStatusSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Refresh, Search } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import api from '@/utils/api'
import RequirementDetailDialog from '@/components/RequirementDetailDialog.vue'

// 数据
const requirements = ref([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 筛选表单
const filterForm = reactive({
  status: '',
  user_type: '',
  project_type: ''
})

// 详情对话框
const detailDialogVisible = ref(false)
const currentRequirement = ref(null)

// 状态更新对话框
const statusDialogVisible = ref(false)
const statusForm = reactive({
  id: null,
  status: '',
  remark: ''
})

// 用户类型映射
const userTypeMap = {
  individual: { label: '个人', type: '' },
  enterprise: { label: '企业', type: 'success' },
  agency: { label: '代理商', type: 'warning' }
}

// 项目类型映射
const projectTypeMap = {
  website: '网站开发',
  miniprogram: '小程序',
  app: 'APP开发',
  custom_system: '系统定制',
  other: '其他'
}

// 状态映射
const statusMap = {
  pending: { label: '待处理', type: 'info' },
  processing: { label: '处理中', type: 'warning' },
  contacted: { label: '已联系', type: 'success' },
  completed: { label: '已完成', type: 'success' },
  cancelled: { label: '已取消', type: 'danger' }
}

// 获取用户类型标签
const getUserTypeLabel = (type) => {
  return userTypeMap[type]?.label || type
}

// 获取用户类型样式
const getUserTypeType = (type) => {
  return userTypeMap[type]?.type || ''
}

// 获取项目类型标签
const getProjectTypeLabel = (type) => {
  return projectTypeMap[type] || type
}

// 获取状态标签
const getStatusLabel = (status) => {
  return statusMap[status]?.label || status
}

// 获取状态样式
const getStatusType = (status) => {
  return statusMap[status]?.type || ''
}

// 获取操作按钮标签
const getActionLabel = (status) => {
  const actionMap = {
    pending: '处理',
    processing: '联系',
    contacted: '完成',
    completed: '重开',
    cancelled: '重开'
  }
  return actionMap[status] || '更新'
}

// 格式化预算
const formatBudget = (budget) => {
  if (!budget) return '-'
  return budget
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 获取需求列表
const fetchRequirements = async () => {
  loading.value = true
  try {
    const params = {
      skip: (currentPage.value - 1) * pageSize.value,
      limit: pageSize.value,
      ...filterForm
    }
    // 移除空值
    Object.keys(params).forEach(key => {
      if (params[key] === '' || params[key] === null || params[key] === undefined) {
        delete params[key]
      }
    })
    
    const response = await api.getRequirements(params)
    // 确保数据是数组
    const items = response.items || response
    requirements.value = Array.isArray(items) ? items : []
    total.value = response.total || requirements.value.length
  } catch (error) {
    ElMessage.error('获取需求列表失败')
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 筛选
const handleFilter = () => {
  currentPage.value = 1
  fetchRequirements()
}

// 重置筛选
const handleReset = () => {
  filterForm.status = ''
  filterForm.user_type = ''
  filterForm.project_type = ''
  currentPage.value = 1
  fetchRequirements()
}

// 刷新
const handleRefresh = () => {
  fetchRequirements()
}

// 查看详情
const handleViewDetail = async (row) => {
  try {
    const detail = await api.getRequirementDetail(row.id)
    currentRequirement.value = detail
    detailDialogVisible.value = true
  } catch (error) {
    ElMessage.error('获取需求详情失败')
    console.error(error)
  }
}

// 更新状态
const handleUpdateStatus = (row) => {
  statusForm.id = row.id
  // 根据当前状态设置默认新状态
  const nextStatusMap = {
    pending: 'processing',
    processing: 'contacted',
    contacted: 'completed',
    completed: 'pending',
    cancelled: 'pending'
  }
  statusForm.status = nextStatusMap[row.status] || 'pending'
  statusForm.remark = ''
  statusDialogVisible.value = true
}

// 提交状态更新
const handleStatusSubmit = async () => {
  try {
    await api.updateRequirementStatus(statusForm.id, {
      status: statusForm.status,
      remark: statusForm.remark
    })
    ElMessage.success('状态更新成功')
    statusDialogVisible.value = false
    fetchRequirements()
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error(error)
  }
}

// 从详情对话框联系客户
const handleContact = async (requirement) => {
  try {
    await ElMessageBox.confirm(
      `确定要联系客户 ${requirement.contact_name} (${requirement.contact_phone}) 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'info' }
    )
    await api.updateRequirementStatus(requirement.id, {
      status: 'contacted',
      remark: '已联系客户'
    })
    ElMessage.success('已标记为已联系')
    detailDialogVisible.value = false
    fetchRequirements()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 从详情对话框更新状态
const handleStatusUpdateFromDialog = async ({ id, status, remark }) => {
  try {
    await api.updateRequirementStatus(id, { status, remark })
    ElMessage.success('状态更新成功')
    detailDialogVisible.value = false
    fetchRequirements()
  } catch (error) {
    ElMessage.error('状态更新失败')
    console.error(error)
  }
}

// 分页大小变化
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchRequirements()
}

// 页码变化
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchRequirements()
}

onMounted(() => {
  fetchRequirements()
})
</script>

<style scoped>
.requirement-management {
  padding: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.filter-form {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}
</style>
