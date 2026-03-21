<template>
  <MobileLayout>
    <div class="exchange-view">
      <div class="exchange-header">
        <div class="header-title">积分兑换</div>
        <div class="header-desc">用积分兑换精美礼品</div>
      </div>

      <div class="exchange-list">
        <div v-if="loading" class="loading-state">
          <div class="loading-text">加载中...</div>
        </div>

        <div v-else-if="rules.length === 0" class="empty-state">
          <div class="empty-icon">🎁</div>
          <div class="empty-text">暂无可兑换礼品</div>
        </div>

        <div v-else class="items-container">
          <div
            v-for="rule in rules"
            :key="rule.id"
            class="exchange-item"
          >
            <div class="item-info">
              <div class="item-name">{{ rule.name || rule.reward_name || '礼品' }}</div>
              <div class="item-desc">{{ rule.description || rule.desc || '积分兑换礼品' }}</div>
              <div class="item-points">
                <span class="points-icon">⭐</span>
                <span class="points-value">{{ rule.points || rule.cost || 0 }} 积分</span>
              </div>
            </div>
            <div
              class="exchange-btn"
              :class="{ disabled: !canExchange(rule) }"
              @click="handleExchange(rule)"
            >
              兑换
            </div>
          </div>
        </div>
      </div>

      <div v-if="showExchangeModal" class="modal-overlay" @click="closeModal">
        <div class="modal-content" @click.stop>
          <div class="modal-title">确认兑换</div>
          <div class="modal-info">
            <div class="modal-item-name">{{ selectedRule?.name || selectedRule?.reward_name }}</div>
            <div class="modal-points">
              需要 <span>{{ selectedRule?.points || selectedRule?.cost }}</span> 积分
            </div>
          </div>
          <div class="modal-buttons">
            <div class="modal-btn cancel" @click="closeModal">取消</div>
            <div class="modal-btn confirm" @click="confirmExchange">确认兑换</div>
          </div>
        </div>
      </div>

      <div v-if="message" class="toast" :class="messageType">
        {{ message }}
      </div>
    </div>
  </MobileLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import MobileLayout from '../components/MobileLayout.vue'
import api from '@/utils/api'
import request from '@/utils/request'

const rules = ref([])
const loading = ref(false)
const showExchangeModal = ref(false)
const selectedRule = ref(null)
const message = ref('')
const messageType = ref('info')
const userPoints = ref(0)

const fetchRules = async () => {
  loading.value = true
  try {
    const res = await api.getRules()
    rules.value = res.items || res.content || res || []
  } catch (err) {
    console.error('获取兑换规则失败:', err)
    rules.value = []
  } finally {
    loading.value = false
  }
}

const fetchUserPoints = async () => {
  try {
    const res = await api.getMyPoints(0, 1)
    userPoints.value = res.total || res.totalPoints || 0
  } catch (err) {
    console.error('获取用户积分失败:', err)
    userPoints.value = 0
  }
}

const canExchange = (rule) => {
  const cost = rule.points || rule.cost || 0
  return userPoints.value >= cost
}

const handleExchange = (rule) => {
  if (!canExchange(rule)) {
    showToast('积分不足，无法兑换', 'error')
    return
  }
  selectedRule.value = rule
  showExchangeModal.value = true
}

const closeModal = () => {
  showExchangeModal.value = false
  selectedRule.value = null
}

const confirmExchange = async () => {
  if (!selectedRule.value) return

  try {
    await request.post('/users/exchange', {
      rule_id: selectedRule.value.id
    })
    showToast('兑换成功', 'success')
    userPoints.value -= selectedRule.value.points || selectedRule.value.cost || 0
    closeModal()
  } catch (err) {
    console.error('兑换失败:', err)
    showToast('兑换失败，请稍后重试', 'error')
  }
}

const showToast = (msg, type = 'info') => {
  message.value = msg
  messageType.value = type
  setTimeout(() => {
    message.value = ''
  }, 2000)
}

onMounted(() => {
  fetchRules()
  fetchUserPoints()
})
</script>

<style scoped>
.exchange-view {
  min-height: 100vh;
  background: linear-gradient(180deg, #f093fb 0%, #f5576c 100%);
  padding: 16px;
  padding-bottom: 80px;
}

.exchange-header {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 20px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.header-title {
  font-size: 24px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.header-desc {
  font-size: 14px;
  color: #666;
}

.exchange-list {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  padding: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
}

.loading-state {
  padding: 40px 0;
  text-align: center;
}

.loading-text {
  color: #999;
  font-size: 14px;
}

.empty-state {
  padding: 40px 0;
  text-align: center;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.empty-text {
  color: #999;
  font-size: 14px;
}

.items-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.exchange-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: #f8f8f8;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.exchange-item:active {
  background: #f0f0f0;
}

.item-info {
  flex: 1;
}

.item-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.item-desc {
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.item-points {
  display: flex;
  align-items: center;
  gap: 4px;
}

.points-icon {
  font-size: 14px;
}

.points-value {
  font-size: 14px;
  color: #f5576c;
  font-weight: bold;
}

.exchange-btn {
  padding: 8px 20px;
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  color: #fff;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
}

.exchange-btn:active {
  transform: scale(0.95);
}

.exchange-btn.disabled {
  background: #ccc;
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
  z-index: 1000;
}

.modal-content {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
  width: 300px;
  max-width: 90%;
}

.modal-title {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  text-align: center;
  margin-bottom: 20px;
}

.modal-info {
  text-align: center;
  margin-bottom: 24px;
}

.modal-item-name {
  font-size: 16px;
  color: #333;
  margin-bottom: 8px;
}

.modal-points {
  font-size: 14px;
  color: #666;
}

.modal-points span {
  color: #f5576c;
  font-weight: bold;
}

.modal-buttons {
  display: flex;
  gap: 12px;
}

.modal-btn {
  flex: 1;
  padding: 12px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-btn:active {
  transform: scale(0.95);
}

.modal-btn.cancel {
  background: #f5f5f5;
  color: #666;
}

.modal-btn.confirm {
  background: linear-gradient(135deg, #f5576c 0%, #f093fb 100%);
  color: #fff;
}

.toast {
  position: fixed;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  background: rgba(0, 0, 0, 0.75);
  color: #fff;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  z-index: 2000;
  animation: fadeIn 0.3s ease;
}

.toast.success {
  background: rgba(7, 193, 96, 0.9);
}

.toast.error {
  background: rgba(255, 77, 79, 0.9);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translate(-50%, -50%) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translate(-50%, -50%) scale(1);
  }
}
</style>
