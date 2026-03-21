<template>
  <MobileLayout>
    <div class="suggestion-container">
      <div class="page-header">
        <button class="back-btn" @click="goBack">←</button>
        <h1 class="header-title">AI建议</h1>
        <button class="refresh-btn" @click="refreshSuggestion" :disabled="isLoading">
          🔄
        </button>
      </div>

      <div v-if="isLoading" class="loading-state">
        <div class="ai-thinking">
          <div class="thinking-avatar">🤖</div>
          <div class="thinking-dots">
            <span class="dot"></span>
            <span class="dot"></span>
            <span class="dot"></span>
          </div>
          <span class="thinking-text">AI正在分析您的需求...</span>
        </div>
      </div>

      <div v-else class="content-section">
        <div class="requirement-summary">
          <div class="summary-header">
            <span class="summary-title">基于您的需求</span>
            <span class="project-type-badge">{{ projectTypeName }}</span>
          </div>
          <div class="summary-text">
            {{ requirementDescription || '开发一个微信小程序项目' }}
          </div>
        </div>

        <div class="suggestion-card main-suggestion">
          <div class="card-header">
            <span class="ai-badge">🤖 AI建议</span>
          </div>

          <div class="suggestion-content">
            <div class="suggestion-section">
              <div class="section-icon">🏗️</div>
              <div class="section-info">
                <div class="section-title">技术架构建议</div>
                <div class="section-desc">{{ suggestion.architecture || '推荐采用前后端分离架构，使用组件化开发模式，提高代码复用性和可维护性。' }}</div>
              </div>
            </div>

            <div class="suggestion-section">
              <div class="section-icon">📱</div>
              <div class="section-info">
                <div class="section-title">UI设计建议</div>
                <div class="section-desc">{{ suggestion.uiDesign || '建议采用简洁大方的设计风格，注重用户体验，确保界面美观且易于操作。' }}</div>
              </div>
            </div>

            <div class="suggestion-section">
              <div class="section-icon">⚡</div>
              <div class="section-info">
                <div class="section-title">性能优化建议</div>
                <div class="section-desc">{{ suggestion.performance || '采用懒加载和代码分割策略，优化首屏加载速度，确保流畅的用户体验。' }}</div>
              </div>
            </div>

            <div class="suggestion-section">
              <div class="section-icon">🔒</div>
              <div class="section-info">
                <div class="section-title">安全建议</div>
                <div class="section-desc">{{ suggestion.security || '建议对敏感数据进行加密处理，使用HTTPS协议，防止XSS和SQL注入攻击。' }}</div>
              </div>
            </div>
          </div>
        </div>

        <div class="suggestion-card timeline-suggestion">
          <div class="card-header">
            <span class="timeline-badge">📅 工期预估</span>
          </div>

          <div class="timeline-content">
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-info">
                <span class="timeline-label">需求分析</span>
                <span class="timeline-value">{{ suggestion.phase1Days || '1-2' }}天</span>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-info">
                <span class="timeline-label">UI设计</span>
                <span class="timeline-value">{{ suggestion.phase2Days || '2-3' }}天</span>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-info">
                <span class="timeline-label">开发实现</span>
                <span class="timeline-value">{{ suggestion.phase3Days || '5-7' }}天</span>
              </div>
            </div>
            <div class="timeline-item">
              <div class="timeline-dot"></div>
              <div class="timeline-info">
                <span class="timeline-label">测试优化</span>
                <span class="timeline-value">{{ suggestion.phase4Days || '2-3' }}天</span>
              </div>
            </div>
            <div class="timeline-total">
              <span class="total-label">总工期</span>
              <span class="total-value">{{ totalDays }}天</span>
            </div>
          </div>
        </div>

        <div class="suggestion-card cost-suggestion">
          <div class="card-header">
            <span class="cost-badge">💰 费用预估</span>
          </div>

          <div class="cost-content">
            <div class="cost-item">
              <span class="cost-label">基础开发</span>
              <span class="cost-value">¥ {{ suggestion.baseCost || '1,000-2,000' }}</span>
            </div>
            <div class="cost-item">
              <span class="cost-label">UI设计</span>
              <span class="cost-value">¥ {{ suggestion.uiCost || '500-1,000' }}</span>
            </div>
            <div class="cost-item">
              <span class="cost-label">测试维护</span>
              <span class="cost-value">¥ {{ suggestion.testCost || '300-500' }}</span>
            </div>
            <div class="cost-item total">
              <span class="cost-label">总计</span>
              <span class="cost-value highlight">¥ {{ suggestion.totalCost || '1,800-3,500' }}</span>
            </div>
          </div>
        </div>

        <div class="tips-section">
          <div class="tips-header">
            <span class="tips-icon">💡</span>
            <span class="tips-title">AI小贴士</span>
          </div>
          <ul class="tips-list">
            <li>需求描述越详细，AI给出的建议越精准</li>
            <li>可以在下一步选择喜欢的UI模板风格</li>
            <li>如有问题，可以随时联系客服获取帮助</li>
          </ul>
        </div>

        <div class="action-section">
          <button class="accept-btn" @click="acceptSuggestion" :disabled="isProcessing">
            接受建议，继续
          </button>
          <button class="reject-btn" @click="showCustomModal = true">
            我有其他想法
          </button>
        </div>
      </div>

      <div v-if="showCustomModal" class="modal-overlay" @click="showCustomModal = false">
        <div class="modal-content" @click.stop>
          <div class="modal-header">
            <span class="modal-title">补充您的想法</span>
          </div>
          <div class="modal-body">
            <textarea
              class="custom-idea"
              v-model="customIdea"
              placeholder="请描述您的其他想法或特殊需求..."
              rows="4"
            ></textarea>
          </div>
          <div class="modal-footer">
            <button class="modal-cancel-btn" @click="showCustomModal = false">取消</button>
            <button
              class="modal-confirm-btn"
              @click="submitCustomIdea"
              :disabled="!customIdea.trim()"
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

const isLoading = ref(false)
const isProcessing = ref(false)
const showCustomModal = ref(false)
const customIdea = ref('')

const requirementDescription = ref('')

const suggestion = ref({
  architecture: '',
  uiDesign: '',
  performance: '',
  security: '',
  phase1Days: '',
  phase2Days: '',
  phase3Days: '',
  phase4Days: '',
  baseCost: '',
  uiCost: '',
  testCost: '',
  totalCost: ''
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
  return projectTypeMap[route.query.type] || '项目开发'
})

const totalDays = computed(() => {
  const phase1 = parseInt(suggestion.value.phase1Days) || 2
  const phase2 = parseInt(suggestion.value.phase2Days) || 3
  const phase3 = parseInt(suggestion.value.phase3Days) || 6
  const phase4 = parseInt(suggestion.value.phase4Days) || 3
  return phase1 + phase2 + phase3 + phase4
})

const loadSuggestion = async () => {
  isLoading.value = true

  try {
    const requirementId = route.query.requirementId
    if (requirementId) {
      const detail = await api.getRequirementDetail(requirementId)
      if (detail) {
        requirementDescription.value = detail.description || detail.additionalNotes || ''
      }
    }

    await new Promise(resolve => setTimeout(resolve, 1500))

    suggestion.value = {
      architecture: '采用Vue3 + Vite构建前端，Node.js + Express搭建后端服务，使用MySQL数据库。组件化 + 模块化设计，支持后续功能扩展。',
      uiDesign: '推荐使用简约现代的设计风格，采用扁平化图标和渐变色方案，确保视觉效果年轻化且专业。',
      performance: '使用路由懒加载和组件异步加载，图片采用CDN加速和WebP格式，接口请求进行数据缓存。',
      security: '使用JWT进行身份认证，对密码加密存储，SQL参数化查询防止注入，XSS过滤处理。',
      phase1Days: '1-2',
      phase2Days: '2-3',
      phase3Days: '5-7',
      phase4Days: '2-3',
      baseCost: '1,000-2,000',
      uiCost: '500-1,000',
      testCost: '300-500',
      totalCost: '1,800-3,500'
    }
  } catch (err) {
    console.error('获取AI建议失败:', err)
    suggestion.value = {
      architecture: '推荐采用主流技术栈，组件化开发模式，确保项目可维护性和扩展性。',
      uiDesign: '建议采用简洁大方的设计风格，注重用户体验。',
      performance: '优化首屏加载速度，采用懒加载策略。',
      security: '数据加密传输，防止常见Web攻击。',
      phase1Days: '1-2',
      phase2Days: '2-3',
      phase3Days: '5-7',
      phase4Days: '2-3',
      baseCost: '1,000-2,000',
      uiCost: '500-1,000',
      testCost: '300-500',
      totalCost: '1,800-3,500'
    }
  } finally {
    isLoading.value = false
  }
}

const refreshSuggestion = () => {
  loadSuggestion()
}

const goBack = () => {
  router.back()
}

const acceptSuggestion = () => {
  router.push({
    path: '/mobile/requirement/step',
    query: { step: 3 }
  })
}

const submitCustomIdea = async () => {
  if (!customIdea.value.trim()) return

  isProcessing.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    alert('感谢您的反馈！我们会认真考虑您的想法。')
    showCustomModal.value = false
    customIdea.value = ''
  } catch (err) {
    console.error('提交失败:', err)
    alert('提交失败，请重试')
  } finally {
    isProcessing.value = false
  }
}

onMounted(() => {
  loadSuggestion()
})
</script>

<style scoped>
.suggestion-container {
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

.back-btn, .refresh-btn {
  width: 36px;
  height: 36px;
  background: #f5f5f5;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
}

.back-btn:active, .refresh-btn:active {
  background: #e9ecef;
}

.refresh-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.header-title {
  font-size: 17px;
  font-weight: bold;
  color: #1a1a2e;
  margin: 0;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
  padding: 20px;
}

.ai-thinking {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.thinking-avatar {
  font-size: 48px;
  margin-bottom: 16px;
  animation: bounce 1s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.thinking-dots {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
}

.dot {
  width: 8px;
  height: 8px;
  background: #07c160;
  border-radius: 50%;
  animation: dot-pulse 1.4s ease-in-out infinite;
}

.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes dot-pulse {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; }
  40% { transform: scale(1); opacity: 1; }
}

.thinking-text {
  font-size: 14px;
  color: #666;
}

.content-section {
  padding: 16px;
}

.requirement-summary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
}

.summary-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.summary-title {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.project-type-badge {
  padding: 4px 12px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  font-size: 12px;
  color: #ffffff;
}

.summary-text {
  font-size: 15px;
  color: #ffffff;
  line-height: 1.6;
}

.suggestion-card {
  background: #ffffff;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.card-header {
  margin-bottom: 16px;
}

.ai-badge, .timeline-badge, .cost-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.ai-badge {
  background: linear-gradient(135deg, rgba(7, 193, 96, 0.1) 0%, rgba(16, 185, 129, 0.1) 100%);
  color: #07c160;
}

.timeline-badge {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.1) 0%, rgba(118, 75, 162, 0.1) 100%);
  color: #667eea;
}

.cost-badge {
  background: linear-gradient(135deg, rgba(255, 152, 0, 0.1) 0%, rgba(255, 87, 34, 0.1) 100%);
  color: #ff9800;
}

.suggestion-section {
  display: flex;
  gap: 14px;
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
}

.suggestion-section:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

.section-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.section-info {
  flex: 1;
}

.section-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 6px;
}

.section-desc {
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}

.timeline-content {
  padding-left: 8px;
}

.timeline-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 10px 0;
  position: relative;
}

.timeline-item::before {
  content: '';
  position: absolute;
  left: 5px;
  top: 24px;
  bottom: -10px;
  width: 2px;
  background: #e9ecef;
}

.timeline-item:last-of-type::before {
  display: none;
}

.timeline-dot {
  width: 12px;
  height: 12px;
  background: #07c160;
  border-radius: 50%;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

.timeline-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
}

.timeline-label {
  font-size: 14px;
  color: #333;
}

.timeline-value {
  font-size: 14px;
  color: #07c160;
  font-weight: 500;
}

.timeline-total {
  display: flex;
  justify-content: space-between;
  padding: 14px 0 0;
  margin-top: 10px;
  border-top: 2px dashed #e9ecef;
}

.total-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.total-value {
  font-size: 16px;
  font-weight: bold;
  color: #07c160;
}

.cost-content {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cost-item {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
}

.cost-item.total {
  padding-top: 12px;
  margin-top: 4px;
  border-top: 1px solid #f0f0f0;
}

.cost-label {
  font-size: 14px;
  color: #666;
}

.cost-value {
  font-size: 14px;
  color: #333;
  font-weight: 500;
}

.cost-value.highlight {
  color: #ff6b6b;
  font-size: 16px;
}

.tips-section {
  background: linear-gradient(135deg, #fff9e6 0%, #fff3cd 100%);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
}

.tips-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}

.tips-icon {
  font-size: 18px;
}

.tips-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  font-size: 13px;
  color: #666;
  line-height: 1.8;
  padding-left: 16px;
  position: relative;
}

.tips-list li::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #ff9800;
}

.action-section {
  padding: 8px 0;
}

.accept-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  margin-bottom: 12px;
  transition: all 0.3s ease;
}

.accept-btn:active:not(:disabled) {
  transform: scale(0.98);
}

.accept-btn:disabled {
  background: #cccccc;
  cursor: not-allowed;
}

.reject-btn {
  width: 100%;
  height: 44px;
  background: #ffffff;
  color: #666;
  border: 1px solid #ddd;
  border-radius: 22px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.reject-btn:active {
  background: #f5f5f5;
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

.custom-idea {
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

.custom-idea:focus {
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
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
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
