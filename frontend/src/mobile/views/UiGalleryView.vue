<template>
  <MobileLayout>
    <div class="gallery-container">
      <div class="gallery-header">
        <div class="header-top">
          <button class="back-btn" @click="goBack">←</button>
          <h1 class="header-title">选择UI模板</h1>
          <div class="header-placeholder"></div>
        </div>
        <div class="search-bar">
          <input
            type="text"
            class="search-input"
            v-model="searchKeyword"
            placeholder="搜索模板名称..."
          />
          <button class="search-btn">🔍</button>
        </div>
        <div class="filter-tabs">
          <div
            v-for="category in categories"
            :key="category.value"
            class="filter-tab"
            :class="{ active: selectedCategory === category.value }"
            @click="selectedCategory = category.value"
          >
            {{ category.label }}
          </div>
        </div>
      </div>

      <div v-if="loading" class="loading-state">
        <div class="loading-spinner"></div>
        <span>加载模板中...</span>
      </div>

      <div v-else class="template-gallery">
        <div class="gallery-grid">
          <div
            v-for="template in filteredTemplates"
            :key="template.id"
            class="gallery-item"
            :class="{ selected: selectedId === template.id }"
            @click="selectTemplate(template)"
          >
            <div class="template-image">
              <img
                v-if="template.previewImage"
                :src="template.previewImage"
                :alt="template.name"
              />
              <div v-else class="template-placeholder">
                <span class="placeholder-text">{{ template.name }}</span>
              </div>
              <div v-if="selectedId === template.id" class="selected-overlay">
                <span class="check-icon">✓</span>
              </div>
            </div>
            <div class="template-details">
              <span class="template-name">{{ template.name }}</span>
              <div class="template-tags">
                <span class="template-tag">{{ template.style || '现代简约' }}</span>
                <span v-if="template.popular" class="template-tag hot">🔥 热门</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="filteredTemplates.length === 0 && !loading" class="empty-state">
          <span class="empty-icon">📋</span>
          <span class="empty-text">暂无相关模板</span>
        </div>
      </div>

      <div class="fixed-footer">
        <div class="selected-info" v-if="selectedTemplate">
          <span class="selected-label">已选择：</span>
          <span class="selected-name">{{ selectedTemplate.name }}</span>
        </div>
        <button
          class="confirm-btn"
          :class="{ disabled: !selectedId }"
          @click="confirmSelection"
          :disabled="!selectedId"
        >
          确认选择
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

const loading = ref(false)
const templates = ref([])
const searchKeyword = ref('')
const selectedCategory = ref('all')
const selectedId = ref(null)
const selectedTemplate = ref(null)

const categories = [
  { value: 'all', label: '全部' },
  { value: 'tech', label: '科技' },
  { value: 'business', label: '商务' },
  { value: 'simple', label: '简约' },
  { value: 'colorful', label: '多彩' },
  { value: 'dark', label: '暗色' }
]

const filteredTemplates = computed(() => {
  let result = templates.value

  if (selectedCategory.value !== 'all') {
    result = result.filter(t => t.style === selectedCategory.value)
  }

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      t.name.toLowerCase().includes(keyword) ||
      (t.style && t.style.toLowerCase().includes(keyword))
    )
  }

  return result
})

const loadTemplates = async () => {
  loading.value = true
  try {
    const res = await api.getTemplates()
    templates.value = res.items || res.content || res
    templates.value = templates.value.map(t => ({
      ...t,
      popular: Math.random() > 0.7
    }))
  } catch (err) {
    console.error('获取模板失败:', err)
    templates.value = [
      { id: 1, name: '科技蓝', style: '科技', previewImage: '', popular: true },
      { id: 2, name: '活力橙', style: '多彩', previewImage: '', popular: false },
      { id: 3, name: '简约白', style: '简约', previewImage: '', popular: true },
      { id: 4, name: '商务灰', style: '商务', previewImage: '', popular: false },
      { id: 5, name: '清新绿', style: '简约', previewImage: '', popular: false },
      { id: 6, name: '可爱粉', style: '多彩', previewImage: '', popular: false },
      { id: 7, name: '深邃紫', style: '暗色', previewImage: '', popular: true },
      { id: 8, name: '热情红', style: '多彩', previewImage: '', popular: false },
      { id: 9, name: '天空蓝', style: '科技', previewImage: '', popular: false },
      { id: 10, name: '经典黑', style: '暗色', previewImage: '', popular: true },
      { id: 11, name: '自然棕', style: '商务', previewImage: '', popular: false },
      { id: 12, name: '极简灰', style: '简约', previewImage: '', popular: false }
    ]
  } finally {
    loading.value = false
  }
}

const selectTemplate = (template) => {
  selectedId.value = template.id
  selectedTemplate.value = template
}

const confirmSelection = () => {
  if (!selectedId.value) return

  const redirect = route.query.redirect
  if (redirect === 'step') {
    router.replace({
      path: '/mobile/requirement/step',
      query: { step: 3, selected: selectedId.value }
    })
  } else if (redirect === 'step_all') {
    router.replace({
      path: '/mobile/requirement/step_all',
      query: { selected: selectedId.value }
    })
  } else {
    router.back()
  }
}

const goBack = () => {
  router.back()
}

onMounted(() => {
  const selected = route.query.selected
  if (selected) {
    selectedId.value = parseInt(selected)
  }

  loadTemplates()
})
</script>

<style scoped>
.gallery-container {
  max-width: 420px;
  margin: 0 auto;
  min-height: 100vh;
  background: #f5f5f5;
  padding-bottom: 80px;
}

.gallery-header {
  background: #ffffff;
  position: sticky;
  top: 0;
  z-index: 10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.header-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
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

.search-bar {
  display: flex;
  padding: 0 16px 12px;
  gap: 10px;
}

.search-input {
  flex: 1;
  height: 40px;
  padding: 0 14px;
  border: 1px solid #e9ecef;
  border-radius: 20px;
  font-size: 14px;
  outline: none;
  box-sizing: border-box;
}

.search-input:focus {
  border-color: #07c160;
}

.search-btn {
  width: 40px;
  height: 40px;
  background: #07c160;
  border: none;
  border-radius: 50%;
  font-size: 16px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.filter-tabs {
  display: flex;
  padding: 0 16px;
  gap: 8px;
  overflow-x: auto;
  -webkit-overflow-scrolling: touch;
}

.filter-tabs::-webkit-scrollbar {
  display: none;
}

.filter-tab {
  flex-shrink: 0;
  padding: 8px 16px;
  background: #f5f5f5;
  border-radius: 16px;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.filter-tab:active {
  transform: scale(0.97);
}

.filter-tab.active {
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
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

.template-gallery {
  padding: 16px;
}

.gallery-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.gallery-item {
  background: #ffffff;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  position: relative;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.gallery-item:active {
  transform: scale(0.98);
}

.gallery-item.selected {
  border-color: #07c160;
}

.template-image {
  height: 120px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
}

.template-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.template-placeholder {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 100%;
}

.placeholder-text {
  font-size: 24px;
  color: #ffffff;
  font-weight: bold;
}

.selected-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(7, 193, 96, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
}

.check-icon {
  width: 40px;
  height: 40px;
  background: #ffffff;
  color: #07c160;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: bold;
}

.template-details {
  padding: 12px;
}

.template-name {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.template-tags {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
}

.template-tag {
  padding: 2px 8px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 11px;
  color: #666;
}

.template-tag.hot {
  background: linear-gradient(135deg, #ff6b6b 0%, #ee5a5a 100%);
  color: #ffffff;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  font-size: 14px;
}

.fixed-footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background: #ffffff;
  padding: 12px 16px;
  padding-bottom: calc(12px + env(safe-area-inset-bottom));
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
  max-width: 420px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.selected-info {
  flex: 1;
}

.selected-label {
  font-size: 13px;
  color: #999;
}

.selected-name {
  font-size: 14px;
  color: #07c160;
  font-weight: 500;
}

.confirm-btn {
  padding: 12px 32px;
  background: linear-gradient(135deg, #07c160 0%, #10b981 100%);
  color: #ffffff;
  border: none;
  border-radius: 22px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.confirm-btn:active:not(.disabled) {
  transform: scale(0.98);
}

.confirm-btn.disabled {
  background: #cccccc;
  cursor: not-allowed;
}
</style>
