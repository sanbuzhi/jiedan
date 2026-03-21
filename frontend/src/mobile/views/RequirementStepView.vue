<template>
  <MobileLayout>
    <div class="step-container">
      <div class="step-header">
        <div class="step-progress">
          <div class="step-progress-bar" :style="{ width: progressWidth }"></div>
        </div>
        <div class="step-indicator">
          <span class="current-step">{{ currentStep }}</span>
          <span class="separator">/</span>
          <span class="total-step">{{ totalSteps }}</span>
        </div>
      </div>

      <div class="step-nav">
        <button
          v-if="currentStep > 1"
          class="nav-btn back-btn"
          @click="prevStep"
        >
          ← 上一步
        </button>
        <div v-else class="nav-placeholder"></div>
        <span class="step-title">{{ stepTitle }}</span>
        <button
          v-if="currentStep < totalSteps"
          class="nav-btn next-btn"
          @click="nextStep"
          :disabled="!canNext"
        >
          下一步 →
        </button>
        <button
          v-else
          class="nav-btn submit-btn"
          @click="submitRequirement"
          :disabled="isSubmitting"
        >
          {{ isSubmitting ? '提交中...' : '提交需求' }}
        </button>
      </div>

      <div class="step-content">
        <transition name="slide-fade" mode="out-in">
          <div :key="currentStep" class="step-panel">
            <div v-if="currentStep === 1" class="step1-form">
              <div class="form-title">明确您的需求</div>
              <div class="form-desc">告诉我们您想要开发什么类型的项目</div>

              <div class="form-item">
                <label class="form-label">项目类型</label>
                <div class="type-grid">
                  <div
                    v-for="type in projectTypes"
                    :key="type.value"
                    class="type-card"
                    :class="{ selected: formData.projectType === type.value }"
                    @click="formData.projectType = type.value"
                  >
                    <span class="type-icon">{{ type.icon }}</span>
                    <span class="type-name">{{ type.label }}</span>
                  </div>
                </div>
              </div>

              <div class="form-item">
                <label class="form-label">项目名称</label>
                <input
                  type="text"
                  class="form-input"
                  v-model="formData.projectName"
                  placeholder="给您的项目起个名字"
                  maxlength="50"
                />
              </div>

              <div class="form-item">
                <label class="form-label">预算范围</label>
                <div class="budget-options">
                  <div
                    v-for="budget in budgetOptions"
                    :key="budget.value"
                    class="budget-option"
                    :class="{ selected: formData.budget === budget.value }"
                    @click="formData.budget = budget.value"
                  >
                    {{ budget.label }}
                  </div>
                </div>
              </div>
            </div>

            <div v-else-if="currentStep === 2" class="step2-form">
              <div class="form-title">AI需求分析建议</div>
              <div class="form-desc">基于您的需求，AI为您提供专业建议</div>

              <div class="ai-suggestion-card">
                <div class="ai-header">
                  <span class="ai-avatar">🤖</span>
                  <span class="ai-name">AI助手</span>
                </div>
                <div class="ai-content">
                  <p v-if="suggestionLoading" class="ai-loading">正在分析您的需求...</p>
                  <div v-else class="ai-message">
                    <p><strong>根据您的{{ formData.projectType }}需求，我建议：</strong></p>
                    <ul>
                      <li>采用<span class="highlight">组件化架构</span>，便于后期维护扩展</li>
                      <li>建议使用<span class="highlight">响应式设计</span>，适配多端设备</li>
                      <li>考虑<span class="highlight">模块化开发</span>，提高开发效率</li>
                      <li>需要<span class="highlight">API接口设计</span>，确保前后端分离</li>
                    </ul>
                    <p class="ai-tip">💡 温馨提示：需求描述越详细，AI越能给出精准建议</p>
                  </div>
                </div>
              </div>

              <div class="form-item">
                <label class="form-label">补充说明（选填）</label>
                <textarea
                  class="form-textarea"
                  v-model="formData.additionalNotes"
                  placeholder="您还有什么特殊需求或想法？"
                  rows="4"
                ></textarea>
              </div>

              <button class="ai-refresh-btn" @click="refreshSuggestion" :disabled="suggestionLoading">
                {{ suggestionLoading ? '分析中...' : '🔄 重新分析' }}
              </button>
            </div>

            <div v-else-if="currentStep === 3" class="step3-form">
              <div class="form-title">选择UI风格</div>
              <div class="form-desc">为您推荐以下UI模板风格</div>

              <div v-if="templatesLoading" class="loading-state">
                <span class="loading-spinner"></span>
                <span>加载模板中...</span>
              </div>

              <div v-else class="template-grid">
                <div
                  v-for="template in templates"
                  :key="template.id"
                  class="template-card"
                  :class="{ selected: formData.selectedTemplate === template.id }"
                  @click="formData.selectedTemplate = template.id"
                >
                  <div class="template-preview">
                    <img v-if="template.previewImage" :src="template.previewImage" :alt="template.name" />
                    <div v-else class="template-placeholder">{{ template.name.charAt(0) }}</div>
                  </div>
                  <div class="template-info">
                    <span class="template-name">{{ template.name }}</span>
                    <span class="template-style">{{ template.style || '现代简约' }}</span>
                  </div>
                  <div v-if="formData.selectedTemplate === template.id" class="template-check">✓</div>
                </div>
              </div>

              <button class="view-more-btn" @click="goToTemplateGallery">
                查看更多模板 →
              </button>
            </div>

            <div v-else-if="currentStep === 4" class="step4-form">
              <div class="form-title">确认需求</div>
              <div class="form-desc">请确认以下需求信息是否正确</div>

              <div class="confirm-card">
                <div class="confirm-item">
                  <span class="confirm-label">项目类型</span>
                  <span class="confirm-value">{{ getProjectTypeName(formData.projectType) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">项目名称</span>
                  <span class="confirm-value">{{ formData.projectName || '未填写' }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">预算范围</span>
                  <span class="confirm-value">{{ getBudgetName(formData.budget) }}</span>
                </div>
                <div class="confirm-item">
                  <span class="confirm-label">UI模板</span>
                  <span class="confirm-value">{{ getTemplateName(formData.selectedTemplate) }}</span>
                </div>
                <div v-if="formData.additionalNotes" class="confirm-item full-width">
                  <span class="confirm-label">补充说明</span>
                  <span class="confirm-value">{{ formData.additionalNotes }}</span>
                </div>
              </div>

              <div class="terms-section">
                <label class="checkbox-label">
                  <input type="checkbox" v-model="formData.agreedToTerms" />
                  <span>我已阅读并同意<span class="link" @click.stop="showTerms">《需求提交协议》</span></span>
                </label>
              </div>
            </div>
          </div>
        </transition>
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

const currentStep = ref(1)
const totalSteps = ref(4)
const isSubmitting = ref(false)
const suggestionLoading = ref(false)
const templatesLoading = ref(false)

const formData = ref({
  projectType: '',
  projectName: '',
  budget: '',
  selectedTemplate: null,
  additionalNotes: '',
  agreedToTerms: false
})

const templates = ref([])

const projectTypes = [
  { value: 'weapp', label: '微信小程序', icon: '📱' },
  { value: 'douyin', label: '抖音小程序', icon: '🎵' },
  { value: 'website', label: '网站开发', icon: '🌐' },
  { value: 'h5', label: 'H5页面', icon: '📄' },
  { value: 'crawler', label: '爬虫程序', icon: '🕷️' },
  { value: 'other', label: '其他', icon: '📦' }
]

const budgetOptions = [
  { value: 'low', label: '预算 ≤ 500' },
  { value: 'medium', label: '500 - 2000' },
  { value: 'high', label: '2000 - 5000' },
  { value: 'vip', label: '5000+' }
]

const stepTitles = {
  1: '明确需求',
  2: 'AI建议',
  3: 'UI选择',
  4: '需求确认'
}

const stepTitle = computed(() => stepTitles[currentStep.value])

const progressWidth = computed(() => {
  return `${(currentStep.value / totalSteps.value) * 100}%`
})

const canNext = computed(() => {
  if (currentStep.value === 1) {
    return formData.value.projectType && formData.value.projectName && formData.value.budget
  }
  if (currentStep.value === 2) {
    return true
  }
  if (currentStep.value === 3) {
    return formData.value.selectedTemplate
  }
  return true
})

const getProjectTypeName = (value) => {
  const type = projectTypes.find(t => t.value === value)
  return type ? type.label : '未选择'
}

const getBudgetName = (value) => {
  const budget = budgetOptions.find(b => b.value === value)
  return budget ? budget.label : '未选择'
}

const getTemplateName = (id) => {
  const template = templates.value.find(t => t.id === id)
  return template ? template.name : '未选择'
}

const loadTemplates = async () => {
  templatesLoading.value = true
  try {
    const res = await api.getTemplates()
    templates.value = res.items || res.content || res.slice(0, 6)
    if (!formData.value.selectedTemplate && templates.value.length > 0) {
      formData.value.selectedTemplate = templates.value[0].id
    }
  } catch (err) {
    console.error('获取模板失败:', err)
    templates.value = [
      { id: 1, name: '科技蓝', style: '科技感', previewImage: '' },
      { id: 2, name: '活力橙', style: '活力感', previewImage: '' },
      { id: 3, name: '简约白', style: '简约风', previewImage: '' },
      { id: 4, name: '商务灰', style: '商务风', previewImage: '' },
      { id: 5, name: '清新绿', style: '自然风', previewImage: '' },
      { id: 6, name: '可爱粉', style: '少女风', previewImage: '' }
    ]
  } finally {
    templatesLoading.value = false
  }
}

const refreshSuggestion = () => {
  suggestionLoading.value = true
  setTimeout(() => {
    suggestionLoading.value = false
  }, 1500)
}

const nextStep = () => {
  if (currentStep.value < totalSteps.value && canNext.value) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 1) {
    currentStep.value--
  }
}

const goToTemplateGallery = () => {
  router.push({
    path: '/mobile/requirement/ui-gallery',
    query: { selected: formData.value.selectedTemplate }
  })
}

const showTerms = () => {
  alert('需求提交协议内容...\n\n1. 提交的需求将进入AI处理流程\n2. 请确保需求信息真实有效\n3. AI会基于需求进行智能分析和拆分')
}

const submitRequirement = async () => {
  if (!formData.value.agreedToTerms) {
    alert('请先阅读并同意《需求提交协议》')
    return
  }

  isSubmitting.value = true
  try {
    const requirementData = {
      projectType: formData.value.projectType,
      projectName: formData.value.projectName,
      budget: formData.value.budget,
      templateId: formData.value.selectedTemplate,
      additionalNotes: formData.value.additionalNotes,
      status: 'draft'
    }

    if (route.query.id) {
      await api.updateRequirementStatus(route.query.id, requirementData)
    } else {
      await api.createRequirement(requirementData)
    }

    alert('需求提交成功！')
    router.push('/client/home')
  } catch (err) {
    console.error('提交需求失败:', err)
    alert('提交失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  const step = route.query.step
  if (step) {
    currentStep.value = parseInt(step) || 1
  }

  const id = route.query.id
  if (id) {
    try {
      const detail = await api.getRequirementDetail(id)
      if (detail) {
        formData.value.projectType = detail.projectType || ''
        formData.value.projectName = detail.projectName || ''
        formData.value.budget = detail.budget || ''
        formData.value.selectedTemplate = detail.templateId || null
        formData.value.additionalNotes = detail.additionalNotes || ''
      }
    } catch (err) {
      console.error('获取需求详情失败:', err)
    }
  }

  await loadTemplates()
})
</script>

<style scoped>
.step-container {
  max-width: 420px;
  margin: 0 auto;
  min-height: 100vh;
  background: linear-gradient(180deg, #f8f9fa 0%, #e9ecef 100%);
}

.step-header {
  background: #ffffff;
  padding: 16px 20px;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.step-progress {
  height: 4px;
  background: #e9ecef;
  border-radius: 2px;
  overflow: hidden;
  margin-bottom: 8px;
}

.step-progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #07c160 0%, #10b981 100%);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.step-indicator {
  text-align: center;
  font-size: 14px;
  color: #666;
}

.current-step {
  color: #07c160;
  font-weight: bold;
}

.step-nav {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  background: #ffffff;
  border-bottom: 1px solid #f0f0f0;
}

.nav-btn {
  padding: 8px 16px;
  border-radius: 20px;
  border: none;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.back-btn {
  background: #f5f5f5;
  color: #666;
}

.back-btn:active {
  background: #e9ecef;
}

.next-btn, .submit-btn {
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
}

.next-btn:disabled, .submit-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.next-btn:not(:disabled):active, .submit-btn:not(:disabled):active {
  transform: scale(0.98);
}

.nav-placeholder {
  width: 80px;
}

.step-title {
  font-size: 16px;
  font-weight: bold;
  color: #1a1a2e;
}

.step-content {
  padding: 20px;
}

.step-panel {
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.3s ease-in;
}

.slide-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.slide-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

.form-title {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.form-desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 24px;
}

.form-item {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 10px;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.form-input:focus {
  border-color: #07c160;
}

.form-textarea {
  width: 100%;
  padding: 12px 16px;
  border: 1px solid #ddd;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  resize: none;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
}

.form-textarea:focus {
  border-color: #07c160;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.type-card {
  background: #ffffff;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  padding: 16px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-card:active {
  transform: scale(0.98);
}

.type-card.selected {
  border-color: #07c160;
  background: linear-gradient(135deg, rgba(7, 193, 96, 0.05) 0%, rgba(16, 185, 129, 0.05) 100%);
}

.type-icon {
  font-size: 28px;
  margin-bottom: 8px;
}

.type-name {
  font-size: 12px;
  color: #333;
  text-align: center;
}

.budget-options {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 10px;
}

.budget-option {
  background: #ffffff;
  border: 2px solid #e9ecef;
  border-radius: 8px;
  padding: 12px;
  text-align: center;
  font-size: 13px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.budget-option:active {
  transform: scale(0.98);
}

.budget-option.selected {
  border-color: #07c160;
  color: #07c160;
  font-weight: 500;
}

.ai-suggestion-card {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
}

.ai-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.ai-avatar {
  font-size: 24px;
  margin-right: 10px;
}

.ai-name {
  font-size: 14px;
  color: #00d4ff;
  font-weight: 500;
}

.ai-content {
  color: #ffffff;
}

.ai-loading {
  text-align: center;
  padding: 20px;
  color: rgba(255, 255, 255, 0.7);
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.5; }
  50% { opacity: 1; }
}

.ai-message p {
  margin-bottom: 12px;
  line-height: 1.6;
}

.ai-message ul {
  list-style: none;
  padding: 0;
  margin-bottom: 12px;
}

.ai-message li {
  padding: 8px 0;
  padding-left: 20px;
  position: relative;
  line-height: 1.6;
}

.ai-message li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #00d4ff;
}

.highlight {
  color: #00d4ff;
  font-weight: 500;
}

.ai-tip {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.ai-refresh-btn {
  width: 100%;
  height: 44px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 22px;
  color: #ffffff;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.ai-refresh-btn:active {
  background: rgba(255, 255, 255, 0.15);
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
  color: #666;
}

.loading-spinner {
  width: 32px;
  height: 32px;
  border: 3px solid #e9ecef;
  border-top-color: #07c160;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-bottom: 12px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.template-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.template-card {
  background: #ffffff;
  border: 2px solid #e9ecef;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.template-card:active {
  transform: scale(0.98);
}

.template-card.selected {
  border-color: #07c160;
}

.template-preview {
  height: 100px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.template-placeholder {
  font-size: 32px;
  color: #ffffff;
  font-weight: bold;
}

.template-info {
  padding: 12px;
}

.template-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 4px;
}

.template-style {
  font-size: 12px;
  color: #999;
}

.template-check {
  position: absolute;
  top: 8px;
  right: 8px;
  width: 24px;
  height: 24px;
  background: #07c160;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.view-more-btn {
  width: 100%;
  height: 44px;
  background: #f5f5f5;
  border: none;
  border-radius: 22px;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.view-more-btn:active {
  background: #e9ecef;
}

.confirm-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.confirm-item:last-child {
  border-bottom: none;
}

.confirm-item.full-width {
  flex-direction: column;
}

.confirm-label {
  font-size: 14px;
  color: #666;
}

.confirm-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.terms-section {
  padding: 16px 0;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  color: #666;
  cursor: pointer;
}

.checkbox-label input {
  width: 18px;
  height: 18px;
  accent-color: #07c160;
}

.link {
  color: #07c160;
}
</style>
