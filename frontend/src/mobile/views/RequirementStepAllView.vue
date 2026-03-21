<template>
  <MobileLayout>
    <div class="all-view-container">
      <div class="page-header">
        <h1 class="page-title">提交需求</h1>
        <p class="page-desc">一次性填写所有需求信息，快速提交</p>
      </div>

      <div class="form-section">
        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">📋</span>
            项目类型
          </label>
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
          <label class="form-label">
            <span class="label-icon">📝</span>
            项目名称
          </label>
          <input
            type="text"
            class="form-input"
            v-model="formData.projectName"
            placeholder="给您的项目起个名字"
            maxlength="50"
          />
          <span class="char-count">{{ formData.projectName.length }}/50</span>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">💰</span>
            预算范围
          </label>
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

        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">🎨</span>
            UI风格选择
          </label>
          <div v-if="templatesLoading" class="loading-state">
            <span class="loading-spinner"></span>
            <span>加载模板...</span>
          </div>
          <div v-else class="template-scroll">
            <div
              v-for="template in templates"
              :key="template.id"
              class="template-item"
              :class="{ selected: formData.selectedTemplate === template.id }"
              @click="formData.selectedTemplate = template.id"
            >
              <div class="template-thumb">
                <img v-if="template.previewImage" :src="template.previewImage" :alt="template.name" />
                <div v-else class="template-placeholder">{{ template.name.charAt(0) }}</div>
              </div>
              <span class="template-name">{{ template.name }}</span>
              <div v-if="formData.selectedTemplate === template.id" class="template-check">✓</div>
            </div>
          </div>
          <button class="view-more-link" @click="goToTemplateGallery">
            查看更多模板 →
          </button>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">📄</span>
            详细需求描述
          </label>
          <textarea
            class="form-textarea"
            v-model="formData.description"
            placeholder="请详细描述您的需求，包括功能要求、业务流程、特殊需求等..."
            rows="6"
            maxlength="1000"
          ></textarea>
          <span class="char-count">{{ formData.description.length }}/1000</span>
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">⚡</span>
            特殊要求（选填）
          </label>
          <input
            type="text"
            class="form-input"
            v-model="formData.specialRequirements"
            placeholder="如：需要兼容老版本、加载速度快等"
            maxlength="200"
          />
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">📱</span>
            联系方式
          </label>
          <input
            type="tel"
            class="form-input"
            v-model="formData.contactPhone"
            placeholder="方便我们与您联系"
            maxlength="11"
          />
        </div>

        <div class="form-item">
          <label class="form-label">
            <span class="label-icon">💬</span>
            备注（选填）
          </label>
          <input
            type="text"
            class="form-input"
            v-model="formData.remark"
            placeholder="还有什么想补充的吗？"
            maxlength="200"
          />
        </div>
      </div>

      <div class="terms-box">
        <label class="checkbox-label">
          <input type="checkbox" v-model="formData.agreedToTerms" />
          <span>我已阅读并同意</span>
        </label>
        <span class="link" @click="showTerms">《需求提交协议》</span>
        <span>和</span>
        <span class="link" @click="showPrivacy">《隐私政策》</span>
      </div>

      <div class="submit-section">
        <button
          class="submit-btn"
          :class="{ disabled: !canSubmit || isSubmitting }"
          @click="submitRequirement"
          :disabled="!canSubmit || isSubmitting"
        >
          <span v-if="isSubmitting" class="btn-loading">提交中...</span>
          <span v-else>提交需求</span>
        </button>
        <button class="save-draft-btn" @click="saveDraft" :disabled="isSubmitting">
          保存草稿
        </button>
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

const isSubmitting = ref(false)
const templatesLoading = ref(false)
const templates = ref([])

const formData = ref({
  projectType: '',
  projectName: '',
  budget: '',
  selectedTemplate: null,
  description: '',
  specialRequirements: '',
  contactPhone: '',
  remark: '',
  agreedToTerms: false
})

const projectTypes = [
  { value: 'weapp', label: '微信小程序', icon: '📱' },
  { value: 'douyin', label: '抖音小程序', icon: '🎵' },
  { value: 'website', label: '网站开发', icon: '🌐' },
  { value: 'h5', label: 'H5页面', icon: '📄' },
  { value: 'crawler', label: '爬虫程序', icon: '🕷️' },
  { value: 'other', label: '其他', icon: '📦' }
]

const budgetOptions = [
  { value: 'low', label: '≤ 500' },
  { value: 'medium', label: '500-2000' },
  { value: 'high', label: '2000-5000' },
  { value: 'vip', label: '5000+' }
]

const canSubmit = computed(() => {
  return (
    formData.value.projectType &&
    formData.value.projectName &&
    formData.value.budget &&
    formData.value.selectedTemplate &&
    formData.value.description &&
    formData.value.contactPhone &&
    formData.value.agreedToTerms
  )
})

const loadTemplates = async () => {
  templatesLoading.value = true
  try {
    const res = await api.getTemplates()
    templates.value = res.items || res.content || res.slice(0, 8)
    if (templates.value.length > 0 && !formData.value.selectedTemplate) {
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

const goToTemplateGallery = () => {
  router.push({
    path: '/mobile/requirement/ui-gallery',
    query: { selected: formData.value.selectedTemplate }
  })
}

const showTerms = () => {
  alert('需求提交协议\n\n1. 提交的需求将进入AI处理流程\n2. 请确保需求信息真实有效\n3. AI会基于需求进行智能分析和拆分')
}

const showPrivacy = () => {
  alert('隐私政策\n\n我们重视您的隐私保护，所有信息仅用于需求分析和项目开发沟通。')
}

const submitRequirement = async () => {
  if (!canSubmit.value) return

  isSubmitting.value = true
  try {
    const requirementData = {
      projectType: formData.value.projectType,
      projectName: formData.value.projectName,
      budget: formData.value.budget,
      templateId: formData.value.selectedTemplate,
      description: formData.value.description,
      specialRequirements: formData.value.specialRequirements,
      contactPhone: formData.value.contactPhone,
      remark: formData.value.remark,
      status: 'submitted'
    }

    if (route.query.id) {
      await api.updateRequirementStatus(route.query.id, requirementData)
    } else {
      await api.createRequirement(requirementData)
    }

    alert('需求提交成功！我们将尽快与您联系。')
    router.push('/client/home')
  } catch (err) {
    console.error('提交需求失败:', err)
    alert('提交失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

const saveDraft = async () => {
  isSubmitting.value = true
  try {
    const draftData = {
      projectType: formData.value.projectType,
      projectName: formData.value.projectName,
      budget: formData.value.budget,
      templateId: formData.value.selectedTemplate,
      description: formData.value.description,
      specialRequirements: formData.value.specialRequirements,
      contactPhone: formData.value.contactPhone,
      remark: formData.value.remark,
      status: 'draft'
    }

    if (route.query.id) {
      await api.updateRequirementStatus(route.query.id, draftData)
    } else {
      await api.createRequirement(draftData)
    }

    alert('草稿保存成功！')
    router.push('/client/home')
  } catch (err) {
    console.error('保存草稿失败:', err)
    alert('保存失败，请重试')
  } finally {
    isSubmitting.value = false
  }
}

onMounted(async () => {
  const stage = route.query.stage
  if (stage === '1') {
    formData.value.projectType = route.query.type || ''
    formData.value.projectName = route.query.name || ''
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
        formData.value.description = detail.description || ''
        formData.value.specialRequirements = detail.specialRequirements || ''
        formData.value.contactPhone = detail.contactPhone || ''
        formData.value.remark = detail.remark || ''
      }
    } catch (err) {
      console.error('获取需求详情失败:', err)
    }
  }

  await loadTemplates()
})
</script>

<style scoped>
.all-view-container {
  max-width: 420px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 100px;
}

.page-header {
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  padding: 24px 20px;
  color: #ffffff;
}

.page-title {
  font-size: 24px;
  font-weight: bold;
  margin: 0 0 8px 0;
}

.page-desc {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
}

.form-section {
  padding: 20px;
}

.form-item {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  margin-bottom: 12px;
  position: relative;
}

.form-label {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 12px;
}

.label-icon {
  margin-right: 8px;
  font-size: 16px;
}

.char-count {
  position: absolute;
  right: 16px;
  bottom: 8px;
  font-size: 12px;
  color: #999;
}

.type-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.type-card {
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 10px;
  padding: 12px 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.type-card:active {
  transform: scale(0.97);
}

.type-card.selected {
  border-color: #07c160;
  background: rgba(7, 193, 96, 0.08);
}

.type-icon {
  font-size: 24px;
  margin-bottom: 6px;
}

.type-name {
  font-size: 11px;
  color: #333;
  text-align: center;
}

.form-input {
  width: 100%;
  height: 44px;
  padding: 0 14px;
  border: 1px solid #e9ecef;
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
  padding: 12px 14px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  font-size: 14px;
  outline: none;
  resize: none;
  box-sizing: border-box;
  transition: border-color 0.3s ease;
  font-family: inherit;
}

.form-textarea:focus {
  border-color: #07c160;
}

.budget-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}

.budget-option {
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 8px;
  padding: 10px 4px;
  text-align: center;
  font-size: 12px;
  color: #333;
  cursor: pointer;
  transition: all 0.3s ease;
}

.budget-option:active {
  transform: scale(0.97);
}

.budget-option.selected {
  border-color: #07c160;
  color: #07c160;
  font-weight: 500;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  color: #999;
}

.loading-spinner {
  width: 20px;
  height: 20px;
  border: 2px solid #e9ecef;
  border-top-color: #07c160;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin-right: 10px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.template-scroll {
  display: flex;
  overflow-x: auto;
  gap: 10px;
  padding-bottom: 10px;
  -webkit-overflow-scrolling: touch;
}

.template-scroll::-webkit-scrollbar {
  display: none;
}

.template-item {
  flex-shrink: 0;
  width: 80px;
  background: #f5f5f5;
  border: 2px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
}

.template-item:active {
  transform: scale(0.97);
}

.template-item.selected {
  border-color: #07c160;
}

.template-thumb {
  height: 60px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.template-placeholder {
  font-size: 24px;
  color: #ffffff;
  font-weight: bold;
}

.template-name {
  display: block;
  font-size: 11px;
  color: #333;
  text-align: center;
  padding: 8px 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.template-check {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 18px;
  height: 18px;
  background: #07c160;
  color: #ffffff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 11px;
}

.view-more-link {
  background: none;
  border: none;
  color: #07c160;
  font-size: 13px;
  cursor: pointer;
  padding: 8px 0;
}

.terms-box {
  padding: 16px 20px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #666;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-label input {
  width: 16px;
  height: 16px;
  accent-color: #07c160;
}

.link {
  color: #07c160;
  cursor: pointer;
}

.submit-section {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 12px 20px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  max-width: 420px;
  margin: 0 auto;
}

.submit-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
  border: none;
  border-radius: 24px;
  font-size: 16px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.submit-btn:active:not(.disabled) {
  transform: scale(0.98);
}

.submit-btn.disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.btn-loading {
  display: flex;
  align-items: center;
  justify-content: center;
}

.save-draft-btn {
  width: 100%;
  height: 40px;
  background: #ffffff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  margin-top: 10px;
  transition: all 0.3s ease;
}

.save-draft-btn:active {
  background: #f5f5f5;
}
</style>
