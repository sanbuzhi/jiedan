<template>
  <div class="ai-strategy-container">
    <el-card class="page-header">
      <template #header>
        <div class="header-content">
          <div class="title-section">
            <el-icon class="title-icon"><Cpu /></el-icon>
            <div>
              <h2 class="page-title">AI策略配置</h2>
              <p class="page-subtitle">为每个AI接口配置不同的模型和参数</p>
            </div>
          </div>
          <div class="header-actions">
            <el-button @click="loadConfigs" :icon="Refresh" :loading="loading">刷新</el-button>
            <el-button type="primary" @click="handleAdd" :icon="Plus">新增策略</el-button>
          </div>
        </div>
      </template>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card" v-loading="loading">
      <el-table :data="configList" stripe style="width: 100%">
        <el-table-column type="index" width="50" />
        <el-table-column label="接口名称" min-width="150">
          <template #default="{ row }">
            <div class="api-name-cell">
              <el-icon class="api-icon"><component :is="row.icon || 'Document'" /></el-icon>
              <span>{{ row.apiName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="apiCode" label="接口代码" min-width="150" />
        <el-table-column label="AI提供商" width="120">
          <template #default="{ row }">
            <el-tag size="small">{{ getProviderLabel(row.provider) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="AI模型" min-width="180">
          <template #default="{ row }">
            <span>{{ getModelLabel(row.model) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="温度" width="100">
          <template #default="{ row }">
            <el-tag size="small" type="info">{{ row.temperature }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Max Tokens" width="120">
          <template #default="{ row }">
            <span>{{ row.maxTokens }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80">
          <template #default="{ row }">
            <el-switch
              v-model="row.enabled"
              @change="(val) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑AI策略配置' : '新增AI策略配置'"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="接口名称" prop="apiName">
          <el-input v-model="formData.apiName" placeholder="请输入接口名称" />
        </el-form-item>
        <el-form-item label="接口代码" prop="apiCode">
          <el-input
            v-model="formData.apiCode"
            placeholder="请输入接口代码"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="AI提供商" prop="provider">
          <el-select v-model="formData.provider" placeholder="选择AI提供商" style="width: 100%">
            <el-option
              v-for="provider in providerList"
              :key="provider.value"
              :label="provider.label"
              :value="provider.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="AI模型" prop="model">
          <el-select v-model="formData.model" placeholder="选择AI模型" style="width: 100%">
            <el-option
              v-for="model in modelList"
              :key="model.value"
              :label="model.label"
              :value="model.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="温度参数" prop="temperature">
          <div class="slider-container">
            <el-slider v-model="formData.temperature" :min="0" :max="2" :step="0.1" show-stops />
            <span class="slider-value">{{ formData.temperature }}</span>
          </div>
        </el-form-item>
        <el-form-item label="最大Token" prop="maxTokens">
          <el-input-number
            v-model="formData.maxTokens"
            :min="500"
            :max="8000"
            :step="100"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="enabled">
          <el-switch v-model="formData.enabled" />
        </el-form-item>
        <el-form-item label="图标" prop="icon">
          <el-input v-model="formData.icon" placeholder="请输入图标名称（如：Document）" />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="formData.sortOrder" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="接口描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入接口描述"
          />
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Cpu,
  Refresh,
  Plus
} from '@element-plus/icons-vue'
import {
  getAllConfigs,
  createConfig,
  updateConfig,
  deleteConfig,
  getAvailableModels,
  getProviders
} from '@/api/aiStrategy'

// 数据列表
const configList = ref([])
const loading = ref(false)
const modelList = ref([])
const providerList = ref([])

// 对话框相关
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: null,
  apiCode: '',
  apiName: '',
  provider: 'huoshan',
  model: 'doubao-seed-2.0-code',
  temperature: 0.7,
  maxTokens: 2000,
  enabled: true,
  description: '',
  icon: 'Document',
  sortOrder: 0
})

// 表单校验规则
const formRules = {
  apiCode: [
    { required: true, message: '请输入接口代码', trigger: 'blur' },
    { pattern: /^[a-z0-9-]+$/, message: '只能包含小写字母、数字和连字符', trigger: 'blur' }
  ],
  apiName: [
    { required: true, message: '请输入接口名称', trigger: 'blur' }
  ],
  provider: [
    { required: true, message: '请选择AI提供商', trigger: 'change' }
  ],
  model: [
    { required: true, message: '请选择AI模型', trigger: 'change' }
  ]
}

// 获取配置列表
const loadConfigs = async () => {
  loading.value = true
  try {
    const res = await getAllConfigs()
    if (res.code === 0) {
      configList.value = res.data || []
    } else {
      ElMessage.error(res.message || '获取配置失败')
    }
  } catch (error) {
    console.error('获取配置失败:', error)
    ElMessage.error('获取配置失败')
  } finally {
    loading.value = false
  }
}

// 获取模型列表
const loadModels = async () => {
  try {
    const res = await getAvailableModels()
    if (res.code === 0) {
      modelList.value = res.data || []
    }
  } catch (error) {
    console.error('获取模型列表失败:', error)
  }
}

// 获取提供商列表
const loadProviders = async () => {
  try {
    const res = await getProviders()
    if (res.code === 0) {
      providerList.value = res.data || []
    }
  } catch (error) {
    console.error('获取提供商列表失败:', error)
  }
}

// 获取提供商标签
const getProviderLabel = (value) => {
  const provider = providerList.value.find(p => p.value === value)
  return provider ? provider.label : value
}

// 获取模型标签
const getModelLabel = (value) => {
  const model = modelList.value.find(m => m.value === value)
  return model ? model.label : value
}

// 新增
const handleAdd = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(formData, row)
  dialogVisible.value = true
}

// 删除
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 "${row.apiName}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const res = await deleteConfig(row.id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      loadConfigs()
    } else {
      ElMessage.error(res.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 状态变更
const handleStatusChange = async (row, enabled) => {
  try {
    const res = await updateConfig(row.id, { ...row, enabled })
    if (res.code === 0) {
      ElMessage.success(enabled ? '已启用' : '已禁用')
    } else {
      ElMessage.error(res.message || '操作失败')
      row.enabled = !enabled
    }
  } catch (error) {
    console.error('状态变更失败:', error)
    ElMessage.error('操作失败')
    row.enabled = !enabled
  }
}

// 提交表单
const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const res = isEdit.value
      ? await updateConfig(formData.id, formData)
      : await createConfig(formData)
    
    if (res.code === 0) {
      ElMessage.success(isEdit.value ? '更新成功' : '创建成功')
      dialogVisible.value = false
      loadConfigs()
    } else {
      ElMessage.error(res.message || (isEdit.value ? '更新失败' : '创建失败'))
    }
  } catch (error) {
    console.error(isEdit.value ? '更新失败:' : '创建失败:', error)
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  } finally {
    submitting.value = false
  }
}

// 重置表单
const resetForm = () => {
  formData.id = null
  formData.apiCode = ''
  formData.apiName = ''
  formData.provider = 'huoshan'
  formData.model = 'doubao-seed-2.0-code'
  formData.temperature = 0.7
  formData.maxTokens = 2000
  formData.enabled = true
  formData.description = ''
  formData.icon = 'Document'
  formData.sortOrder = configList.value.length
  formRef.value?.resetFields()
}

// 页面加载
onMounted(() => {
  loadConfigs()
  loadModels()
  loadProviders()
})
</script>

<style scoped>
.ai-strategy-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.header-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.title-section {
  display: flex;
  align-items: center;
  gap: 15px;
}

.title-icon {
  font-size: 32px;
  color: #409EFF;
}

.page-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #303133;
}

.page-subtitle {
  margin: 5px 0 0 0;
  font-size: 14px;
  color: #909399;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.table-card {
  margin-bottom: 20px;
}

.api-name-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.api-icon {
  font-size: 18px;
  color: #409EFF;
}

.slider-container {
  display: flex;
  align-items: center;
  gap: 15px;
  width: 100%;
}

.slider-container :deep(.el-slider) {
  flex: 1;
}

.slider-value {
  min-width: 40px;
  text-align: center;
  font-weight: 600;
  color: #409EFF;
}
</style>
