<template>
  <el-dialog
    v-model="dialogVisible"
    title="需求详情"
    width="800px"
    :close-on-click-modal="false"
  >
    <div v-if="requirement" class="requirement-detail">
      <!-- 基本信息 -->
      <el-card class="detail-card" shadow="never">
        <template #header>
          <div class="card-header">
            <span>基本信息</span>
            <el-tag :type="getStatusType(requirement.status)">
              {{ getStatusLabel(requirement.status) }}
            </el-tag>
          </div>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="需求ID">{{ requirement.id }}</el-descriptions-item>
          <el-descriptions-item label="提交时间">{{ formatDate(requirement.created_at) }}</el-descriptions-item>
          <el-descriptions-item label="联系人">{{ requirement.contact_name }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ requirement.contact_phone }}</el-descriptions-item>
          <el-descriptions-item label="联系邮箱">{{ requirement.contact_email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="微信号">{{ requirement.wechat_id || '-' }}</el-descriptions-item>
          <el-descriptions-item label="用户类型">
            <el-tag :type="getUserTypeType(requirement.user_type)" size="small">
              {{ getUserTypeLabel(requirement.user_type) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="项目类型">
            {{ getProjectTypeLabel(requirement.project_type) }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 项目需求详情 -->
      <el-card class="detail-card" shadow="never">
        <template #header>
          <span>项目需求</span>
        </template>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="预算范围">{{ requirement.budget_range || '-' }}</el-descriptions-item>
          <el-descriptions-item label="预计流量">{{ formatTraffic(requirement.expected_traffic) }}</el-descriptions-item>
          <el-descriptions-item label="预计上线时间">{{ requirement.expected_launch_date || '-' }}</el-descriptions-item>
          <el-descriptions-item label="功能需求数量">{{ requirement.feature_count || '-' }}</el-descriptions-item>
          <el-descriptions-item label="是否需要设计" :span="2">
            <el-tag :type="requirement.need_design ? 'success' : 'info'" size="small">
              {{ requirement.need_design ? '是' : '否' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="参考网站" :span="2">
            {{ requirement.reference_urls || '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <el-divider content-position="left">功能需求</el-divider>
        <div class="feature-list">
          <el-tag 
            v-for="(feature, index) in parseFeatures(requirement.features)" 
            :key="index"
            class="feature-tag"
            size="large"
          >
            {{ feature }}
          </el-tag>
          <span v-if="!requirement.features" class="empty-text">暂无功能需求</span>
        </div>

        <el-divider content-position="left">需求描述</el-divider>
        <div class="description-content">
          {{ requirement.description || '暂无描述' }}
        </div>
      </el-card>

      <!-- 预算计算结果 -->
      <el-card class="detail-card" shadow="never" v-if="requirement.budget_calculation">
        <template #header>
          <span>预算计算结果</span>
        </template>
        <div class="budget-result">
          <div class="budget-item total">
            <span class="label">预估总价</span>
            <span class="value">¥{{ formatMoney(requirement.budget_calculation.total_price) }}</span>
          </div>
          <el-divider />
          <div class="budget-breakdown">
            <div class="budget-item" v-if="requirement.budget_calculation.base_price">
              <span class="label">基础费用</span>
              <span class="value">¥{{ formatMoney(requirement.budget_calculation.base_price) }}</span>
            </div>
            <div class="budget-item" v-if="requirement.budget_calculation.design_fee">
              <span class="label">设计费用</span>
              <span class="value">¥{{ formatMoney(requirement.budget_calculation.design_fee) }}</span>
            </div>
            <div class="budget-item" v-if="requirement.budget_calculation.development_fee">
              <span class="label">开发费用</span>
              <span class="value">¥{{ formatMoney(requirement.budget_calculation.development_fee) }}</span>
            </div>
            <div class="budget-item" v-if="requirement.budget_calculation.server_fee">
              <span class="label">服务器费用</span>
              <span class="value">¥{{ formatMoney(requirement.budget_calculation.server_fee) }}</span>
            </div>
            <div class="budget-item" v-if="requirement.budget_calculation.maintenance_fee">
              <span class="label">维护费用</span>
              <span class="value">¥{{ formatMoney(requirement.budget_calculation.maintenance_fee) }}</span>
            </div>
          </div>
        </div>
      </el-card>

      <!-- 处理记录 -->
      <el-card class="detail-card" shadow="never" v-if="requirement.remarks && requirement.remarks.length > 0">
        <template #header>
          <span>处理记录</span>
        </template>
        <el-timeline>
          <el-timeline-item
            v-for="(remark, index) in requirement.remarks"
            :key="index"
            :timestamp="formatDate(remark.created_at)"
            :type="remark.type || 'primary'"
          >
            <p>{{ remark.content }}</p>
            <p class="remark-author" v-if="remark.operator">操作人: {{ remark.operator }}</p>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </div>

    <template #footer>
      <div class="dialog-footer">
        <el-button @click="dialogVisible = false">关闭</el-button>
        <el-button 
          type="warning" 
          @click="handleUpdateStatus"
          v-if="requirement && requirement.status !== 'completed' && requirement.status !== 'cancelled'"
        >
          更新状态
        </el-button>
        <el-button 
          type="success" 
          @click="handleContact"
          v-if="requirement && requirement.status !== 'contacted' && requirement.status !== 'completed'"
        >
          联系客户
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  requirement: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:visible', 'contact', 'update-status'])

// 对话框可见性
const dialogVisible = computed({
  get: () => props.visible,
  set: (val) => emit('update:visible', val)
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

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  return new Date(dateStr).toLocaleString('zh-CN')
}

// 格式化金额
const formatMoney = (amount) => {
  if (amount === undefined || amount === null) return '0.00'
  return Number(amount).toFixed(2)
}

// 格式化流量
const formatTraffic = (traffic) => {
  if (!traffic) return '-'
  return traffic
}

// 解析功能需求列表
const parseFeatures = (features) => {
  if (!features) return []
  if (Array.isArray(features)) return features
  // 如果是逗号分隔的字符串
  if (typeof features === 'string') {
    return features.split(',').map(f => f.trim()).filter(f => f)
  }
  return []
}

// 联系客户
const handleContact = () => {
  if (props.requirement) {
    emit('contact', props.requirement)
  }
}

// 更新状态
const handleUpdateStatus = () => {
  if (props.requirement) {
    const nextStatusMap = {
      pending: 'processing',
      processing: 'contacted',
      contacted: 'completed'
    }
    const nextStatus = nextStatusMap[props.requirement.status] || 'processing'
    emit('update-status', {
      id: props.requirement.id,
      status: nextStatus,
      remark: ''
    })
  }
}
</script>

<style scoped>
.requirement-detail {
  max-height: 600px;
  overflow-y: auto;
}

.detail-card {
  margin-bottom: 16px;
}

.detail-card:last-child {
  margin-bottom: 0;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.feature-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  padding: 10px 0;
}

.feature-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.empty-text {
  color: #909399;
  font-size: 14px;
}

.description-content {
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
  line-height: 1.8;
  color: #606266;
  white-space: pre-wrap;
}

.budget-result {
  padding: 10px;
}

.budget-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
}

.budget-item.total {
  font-size: 18px;
  font-weight: bold;
}

.budget-item.total .value {
  color: #f56c6c;
  font-size: 24px;
}

.budget-breakdown .budget-item {
  padding: 8px 0;
  border-bottom: 1px dashed #ebeef5;
}

.budget-breakdown .budget-item:last-child {
  border-bottom: none;
}

.budget-item .label {
  color: #606266;
}

.budget-item .value {
  color: #303133;
  font-weight: 500;
}

.remark-author {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

:deep(.el-descriptions__label) {
  width: 120px;
  background-color: #f5f7fa;
}
</style>
