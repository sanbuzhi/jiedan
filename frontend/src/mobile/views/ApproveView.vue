<template>
  <MobileLayout>
    <div class="approve-container">
      <div class="page-header">
        <button class="back-btn" @click="goBack">←</button>
        <h1 class="header-title">{{ stageName }}</h1>
        <div class="header-placeholder"></div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载中...</span>
      </div>

      <div v-else class="content-section">
        <div class="requirement-card">
          <div class="card-header">
            <span class="project-badge">{{ projectTypeName }}</span>
            <span class="status-badge" :class="requirement.status">
              {{ statusText[requirement.status] || requirement.status }}
            </span>
          </div>

          <h2 class="project-title">{{ requirement.projectName }}</h2>

          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">项目ID</span>
              <span class="info-value">#{{ requirement.id }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">预算</span>
              <span class="info-value">{{ budgetText }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="detail-title">详细描述</div>
            <div class="detail-content">
              {{ requirement.description || '暂无详细描述' }}
            </div>
          </div>

          <div v-if="requirement.specialRequirements" class="detail-section">
            <div class="detail-title">特殊要求</div>
            <div class="detail-content">
              {{ requirement.specialRequirements }}
            </div>
          </div>

          <div v-if="requirement.templateName" class="detail-section">
            <div class="detail-title">UI模板</div>
            <div class="template-preview">
              <div class="template-thumb">
                <span>{{ requirement.templateName.charAt(0) }}</span>
              </div>
              <span class="template-name">{{ requirement.templateName }}</span>
            </div>
          </div>

          <div v-if="requirement.additionalNotes" class="detail-section">
            <div class="detail-title">补充说明</div>
            <div class="detail-content">
              {{ requirement.additionalNotes }}
            </div>
          </div>
        </div>

        <div v-if="showAiAnalysis" class="ai-analysis-card">
          <div class="ai-header">
            <span class="ai-avatar">🤖</span>
            <span class="ai-title">AI分析结果</span>
          </div>
          <div class="ai-content">
            <div class="analysis-item">
              <span class="analysis-label">预计工期</span>
              <span class="analysis-value highlight">{{ requirement.estimatedDays || '5-7' }} 天</span>
            </div>
            <div class="analysis-item">
              <span class="analysis-label">技术方案</span>
              <span class="analysis-value">{{ requirement.techSolution || '组件化 + 模块化' }}</span>
            </div>
            <div class="analysis-item">
              <span class="analysis-label">预估费用</span>
              <span class="analysis-value highlight">¥ {{ requirement.estimatedCost || '1,500-3,000' }}</span>
            </div>
          </div>
        </div>

        <div class="action-section">
          <div class="action-title">您的操作</div>

          <div class="action-buttons">
            <button
              class="approve-btn"
              @click="handleApprove"
              :disabled="isProcessing"
            >
              <span class="btn-icon">✓</span>
              <span>{{ approveText }}</span>
            </button>

            <button
              class="reject-btn"
              @click="showRejectModal = true"
              :disabled="isProcessing"
            >
              <span class="btn-icon">✗</span>
              <span>需要修改</span>
            </button>
          </div>

          <button
            class="modify-btn"
            @click="goToModify"
            :disabled="isProcessing"
          >
            修改需求
          </button>
        </div>
      </div>

      <div v-if="showRejectModal" class="modal-overlay" @click="showRejectModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <span class="modal-title">请说明需要修改的内容</span>
          </div>
          <div class="modal-body">
            <textarea
              class="reject-reason"
              v-model="rejectReason"
              placeholder="请详细描述需要修改的内容，我们会尽快处理..."
              rows="4"
            ></textarea>
          </div>
          <div class="modal-footer">
            <button class="modal-cancel-btn" @click="showRejectModal = false">取消</button>
            <button
              class="modal-confirm-btn"
              @click="handleReject"
              :disabled="!rejectReason.trim()"
            >
              提交
            </button>
          </div>
        </div>
      </div>
    </div>
  </MobileLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import MobileLayout from '../components/MobileLayout.vue'
import api from '@/utils/api'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const isProcessing = ref(false)
const showRejectModal = ref(false)
const rejectReason = ref('')

const requirement = ref({
  id: null,
  projectName: '',
  projectType: '',
  budget: '',
  description: '',
  specialRequirements: '',
  templateId: null,
  templateName: '',
  additionalNotes: '',
  status: 'pending',
  estimatedDays: '',
  techSolution: '',
  estimatedCost: ''
})

const stageNames = {
  clarify: '需求确认验收',
  security: '功能验收测试',
  final: '最终验收'
}

const statusText = {
  pending: '待确认',
  approved: '已确认',
  rejected: '已驳回',
  processing: '进行中',
  completed: '已完成'
}

const stageName = computed(() => {
  return route.query.stageName || stageNames[route.query.stage] || '需求确认'
})

const projectTypeMap = {
  weapp: '微信小程序',
  douyin: '抖音小程序',
  website: '网站开发',
  h5: 'H5页面',
  crawler: '爬虫程序',
  other: '其他'
}

const projectTypeName = computed(() => {
  return projectTypeMap[requirement.value.projectType] || requirement.value.projectType || '未知类型'
})

const budgetText = computed(() => {
  const budgetMap = {
    low: '≤ 500元',
    medium: '500-2000元',
    high: '2000-5000元',
    vip: '5000元以上'
  }
  return budgetMap[requirement.value.budget] || requirement.value.budget || '未设置'
})

const approveText = computed(() => {
  const texts = {
    clarify: '确认需求',
    security: '验收通过',
    final: '最终验收'
  }
  return texts[route.query.stage] || '确认'
})

const showAiAnalysis = computed(() => {
  return route.query.stage === 'clarify'
})

const loadRequirement = async () => {
  loading.value = true
  const requirementId = route.query.requirementId

  if (!requirementId) {
    loading.value = false
    return
  }

  try {
    const detail = await api.getRequirementDetail(requirementId)
    if (detail) {
      requirement.value = {
        ...requirement.value,
        ...detail
      }
    }
  } catch (err) {
    console.error('获取需求详情失败:', err)
    requirement.value = {
      id: requirementId,
      projectName: '示例项目',
      projectType: 'weapp',
      budget: 'medium',
      description: '这是一个示例需求描述，包含项目的详细功能说明和技术要求。',
      specialRequirements: '需要兼容老版本手机',
      templateId: 1,
      templateName: '科技蓝',
      additionalNotes: '希望界面简洁大方',
      status: 'processing',
      estimatedDays: '5-7',
      techSolution: '组件化 + 模块化 + RESTful API',
      estimatedCost: '1,500-3,000'
    }
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const goToModify = () => {
  router.push({
    path: '/mobile/requirement/step_all',
    query: { id: requirement.value.id }
  })
}

const handleApprove = async () => {
  if (isProcessing.value) return

  const confirmed = confirm(`确定要${approveText.value}吗？`)
  if (!confirmed) return

  isProcessing.value = true
  try {
    await api.updateRequirementStatus(requirement.value.id, {
      status: 'approved',
      stage: route.query.stage
    })

    alert(`${approveText.value}成功！`)
    router.push('/client/home')
  } catch (err) {
    console.error('确认失败:', err)
    alert('操作失败，请重试')
  } finally {
    isProcessing.value = false
  }
}

const handleReject = async () => {
  if (!rejectReason.value.trim()) return

  isProcessing.value = true
  try {
    await api.updateRequirementStatus(requirement.value.id, {
      status: 'rejected',
      stage: route.query.stage,
      rejectReason: rejectReason.value
    })

    alert('已提交修改意见')
    showRejectModal.value = false
    router.push('/client/home')
  } catch (err) {
    console.error('提交失败:', err)
    alert('操作失败，请重试')
  } finally {
    isProcessing.value = false
  }
}

onMounted(() => {
  loadRequirement()
})
</script>

<style scoped>
.approve-container {
  max-width: 420px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f5f5;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #ffffff;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  position: sticky;
  top: 0;
  z-index: 10;
}

.back-btn {
  width: 36px;
  height: 36px;
  background: #f5f5f5;
  border: none;
  border-radius: 50%;
  font-size: 18px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-title {
  font-size: 17px;
  font-weight: bold;
  color: #1a1a2e;
  margin: 0;
}

.header-placeholder {
  width: 36px;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.loading-spinner {
  width: 36px;
  height: 36px;
  border: 3px solid #e9ecef;
  border-top-color: #07c160;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.content-section {
  padding: 16px;
}

.requirement-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.project-badge {
  padding: 4px 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-badge.pending {
  background: rgba(255, 152, 0, 0.1);
  color: #ff9800;
}

.status-badge.approved {
  background: rgba(76, 175, 80, 0.1);
  color: #4caf50;
}

.status-badge.rejected {
  background: rgba(244, 67, 54, 0.1);
  color: #f44336;
}

.status-badge.processing {
  background: rgba(33, 150, 243, 0.1);
  color: #2196f3;
}

.status-badge.completed {
  background: rgba(7, 193, 96, 0.1);
  color: #07c160;
}

.project-title {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a2e;
  margin: 0 0 16px 0;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 20px;
}

.info-item {
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.info-label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.info-value {
  display: block;
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.detail-section {
  margin-bottom: 16px;
}

.detail-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.detail-content {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.template-preview {
  display: flex;
  align-items: center;
  gap: 12px;
  background: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
}

.template-thumb {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-thumb span {
  font-size: 20px;
  color: #ffffff;
  font-weight: bold;
}

.template-name {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.ai-analysis-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.ai-header {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.ai-avatar {
  font-size: 24px;
  margin-right: 10px;
}

.ai-title {
  font-size: 15px;
  color: #00d4ff;
  font-weight: 500;
}

.ai-content {
  color: #ffffff;
}

.analysis-item {
  display: flex;
  justify-content: space-between;
  padding: 10px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.analysis-item:last-child {
  border-bottom: none;
}

.analysis-label {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
}

.analysis-value {
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
}

.analysis-value.highlight {
  color: #00d4ff;
}

.action-section {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.action-title {
  font-size: 15px;
  font-weight: 500;
  color: #333;
  margin-bottom: 16px;
}

.action-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 12px;
}

.approve-btn, .reject-btn {
  height: 48px;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.approve-btn {
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
}

.approve-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.approve-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.reject-btn {
  background: #ffffff;
  color: #666;
  border: 1px solid #ddd;
}

.reject-btn:active:not(:disabled) {
  background: #f5f5f5;
}

.reject-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 16px;
}

.modify-btn {
  width: 100%;
  height: 44px;
  background: #f5f5f5;
  border: none;
  border-radius: 22px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modify-btn:active:not(:disabled) {
  background: #e9ecef;
}

.modify-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal-content {
  background: #ffffff;
  border-radius: 16px;
  width: 100%;
  max-width: 340px;
  overflow: hidden;
}

.modal-header {
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
}

.modal-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.modal-body {
  padding: 16px 20px;
}

.reject-reason {
  width: 100%;
  padding: 12px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  resize: none;
  outline: none;
  box-sizing: border-box;
  font-family: inherit;
}

.reject-reason:focus {
  border-color: #07c160;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px 20px;
  border-top: 1px solid #f0f0f0;
}

.modal-cancel-btn, .modal-confirm-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 22px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-cancel-btn {
  background: #f5f5f5;
  color: #666;
}

.modal-cancel-btn:active {
  background: #e9ecef;
}

.modal-confirm-btn {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #ffffff;
}

.modal-confirm-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.modal-confirm-btn:not(:disabled):active {
  transform: scale(0.98);
}
</style>
