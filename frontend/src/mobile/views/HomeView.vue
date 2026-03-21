<template>
  <MobileLayout>
    <div class="home-view">
      <div class="section projects-section">
        <div class="section-header">
          <span class="section-title">我的项目</span>
          <span class="section-more" v-if="allRequirements.length > 0" @click="viewAllProjects">查看全部 ></span>
        </div>

        <div v-if="allRequirements.length === 0" class="empty-project-card" @click="createNewProject">
          <div class="empty-icon">+</div>
          <span class="empty-title">添加第一个项目</span>
          <span class="empty-desc">点击开始您的 AI 开发之旅</span>
        </div>

        <div v-if="allRequirements.length > 0" class="projects-list">
          <div class="project-flow-card-container" v-for="(req, reqIndex) in allRequirements" :key="req.id">
            <div class="project-flow-card">
              <div class="project-header">
                <span class="project-id">项目 #{{ req.id }} - {{ req.projectType }}</span>
                <div class="project-header-right">
                  <span class="project-status" :class="req.status">{{ statusText[req.status] || req.status }}</span>
                  <span
                    v-if="req.flowNodes && req.flowNodes[0] && req.flowNodes[0].status === 'active' && req.status === 'draft'"
                    class="delete-btn"
                    @click.stop="deleteProject(req.id)"
                  >删除</span>
                </div>
              </div>

              <div class="flow-timeline-scroll">
                <div class="flow-timeline-horizontal">
                  <div
                    v-for="(node, nodeIndex) in req.flowNodes"
                    :key="nodeIndex"
                    class="flow-node-wrapper"
                  >
                    <div
                      class="flow-node"
                      :class="node.status"
                      :id="`node-${reqIndex}-${nodeIndex}`"
                      @click="onFlowNodeTap(reqIndex, nodeIndex, req.id)"
                    >
                      <div class="flow-node-icon">
                        <div v-if="node.isAiNode && node.status === 'active'" class="ai-working-icon">
                          <div class="ai-robot">
                            <span class="ai-head">🤖</span>
                            <div class="ai-dots">
                              <span class="dot"></span>
                              <span class="dot"></span>
                              <span class="dot"></span>
                            </div>
                          </div>
                        </div>
                        <span v-else-if="node.status === 'completed'">✓</span>
                        <span v-else>{{ nodeIndex + 1 }}</span>
                      </div>
                      <span class="flow-node-label">{{ node.title }}</span>
                      <span v-if="node.clickHint" class="flow-node-hint">{{ node.clickHint }}</span>
                    </div>
                    <span v-if="nodeIndex < req.flowNodes.length - 1" class="flow-arrow">▶</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div class="add-project-btn" @click="createNewProject">
            <span class="add-project-icon">+</span>
            <span class="add-project-text">添加项目</span>
          </div>
        </div>
      </div>

      <div class="section deals-section">
        <div class="section-header">
          <span class="section-title">实时成交</span>
          <div class="live-indicator">
            <span class="live-dot"></span>
            <span class="live-text">LIVE</span>
          </div>
        </div>
        <div class="deals-container tech-card">
          <div class="deals-scroll">
            <div v-for="(deal, index) in recentDeals" :key="index" class="deal-item">
              <div class="deal-avatar tech-avatar">{{ deal.avatar }}</div>
              <div class="deal-info">
                <span class="deal-user">{{ deal.user }}</span>
                <span class="deal-type">{{ deal.projectType }}</span>
              </div>
              <div class="deal-amount">
                <span class="deal-price">¥{{ deal.amount }}</span>
                <span class="deal-time">{{ deal.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </MobileLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileLayout from '../components/MobileLayout.vue'
import api from '@/utils/api'
import ui from '@/utils/mobile-ui'

const router = useRouter()

const FLOW_NODES = [
  { title: '明确需求', desc: '客户提交初步需求' },
  { title: 'AI明确需求', desc: 'AI分析并完善需求' },
  { title: '需求确认验收', desc: '确认需求文档' },
  { title: 'AI拆分任务', desc: '自动拆分子任务' },
  { title: 'AI开发', desc: '智能编码实现' },
  { title: 'AI功能测试', desc: '自动化功能测试' },
  { title: 'AI安全测试', desc: '测试接口漏洞' },
  { title: '功能验收测试', desc: '客户功能验收' },
  { title: '打包交付', desc: '项目交付' },
  { title: '最终验收', desc: '客户最终验收' },
  { title: '项目完成', desc: '项目结束' }
]

const AI_NODE_INDEXES = [1, 3, 4, 5, 6]
const APPROVE_NODE_INDEXES = [2, 7, 9]

const STATUS_TEXT = {
  draft: '草稿',
  pending: '待处理',
  processing: '进行中',
  developing: '开发中',
  completed: '已完成'
}

const allRequirements = ref([])
const recentDeals = ref([])
const showProjects = ref(false)
const dealsTimer = ref(null)

const statusText = computed(() => STATUS_TEXT)

onMounted(() => {
  loadCurrentRequirement()
  initRecentDeals()
})

onUnmounted(() => {
  clearDealsTimer()
})

function calculateFlowNodes(currentStep) {
  return FLOW_NODES.map((node, index) => {
    let status = 'pending'
    if (index < currentStep) {
      status = 'completed'
    } else if (index === currentStep) {
      status = 'active'
    }

    return {
      ...node,
      status,
      isAiNode: AI_NODE_INDEXES.includes(index),
      clickHint: getClickHint(index, status)
    }
  })
}

function getClickHint(index, status) {
  if (status !== 'active') return null
  if (index === 0) return '点击编辑'
  if ([2, 7, 9].includes(index)) return '点击验收'
  if ([1, 3, 4, 5, 6].includes(index)) return 'AI处理中...'
  if (index === 8) return '等待交付'
  if (index === 10) return '已完成'
  return null
}

function loadCurrentRequirement() {
  api.getRequirements({ size: 10 })
    .then(res => {
      let content = []
      if (Array.isArray(res)) {
        content = res
      } else if (res && Array.isArray(res.items)) {
        content = res.items
      } else if (res && Array.isArray(res.content)) {
        content = res.content
      } else if (res && typeof res === 'object') {
        const keys = Object.keys(res)
        for (const key of keys) {
          if (Array.isArray(res[key])) {
            content = res[key]
            break
          }
        }
      }

      if (content && content.length > 0) {
        const requirements = content.map(req => {
          let currentStep = req.currentFlowNode || 0
          if (req.status === 'draft' && currentStep === 1) {
            currentStep = 0
          }
          const flowNodes = calculateFlowNodes(currentStep)

          const activeNodeIndex = flowNodes.findIndex(node => node.status === 'active')
          let scrollIntoViewId = null
          if (activeNodeIndex !== -1) {
            const reqIndex = allRequirements.value.length
            scrollIntoViewId = `node-${reqIndex}-${activeNodeIndex}`
          }

          return {
            ...req,
            currentFlowNode: currentStep,
            flowNodes,
            scrollIntoViewId
          }
        })

        allRequirements.value = requirements
        showProjects.value = false
        setTimeout(() => {
          showProjects.value = true
          if (scrollIntoViewId) {
            const element = document.getElementById(scrollIntoViewId)
            if (element) {
              element.scrollIntoView({ behavior: 'smooth', inline: 'center' })
            }
          }
        }, 100)
      } else {
        allRequirements.value = []
      }
    })
    .catch(err => {
      console.error('获取需求列表失败:', err)
      allRequirements.value = []
    })
}

function generateDealData(count = 1, isNew = false) {
  const projectTypes = ['微信小程序', '抖音小程序', '网站开发', '爬虫程序', 'H5页面']
  const avatars = ['李', '王', '张', '刘', '陈', '杨', '赵', '黄']
  const deals = []

  for (let i = 0; i < count; i++) {
    const amount = Math.floor(Math.random() * 500) + 499
    const minutesAgo = isNew ? 0 : Math.floor(Math.random() * 60)
    deals.push({
      user: avatars[Math.floor(Math.random() * avatars.length)] + '**',
      avatar: avatars[Math.floor(Math.random() * avatars.length)],
      projectType: projectTypes[Math.floor(Math.random() * projectTypes.length)],
      amount: amount.toLocaleString(),
      time: minutesAgo < 1 ? '刚刚' : `${minutesAgo}分钟前`
    })
  }

  return count === 1 ? deals[0] : deals
}

function initRecentDeals() {
  recentDeals.value = generateDealData(10)
  clearDealsTimer()
  dealsTimer.value = setInterval(() => {
    updateRecentDeals()
  }, 30000)
}

function clearDealsTimer() {
  if (dealsTimer.value) {
    clearInterval(dealsTimer.value)
    dealsTimer.value = null
  }
}

function updateRecentDeals() {
  const newDeal = generateDealData(1, true)
  recentDeals.value.unshift(newDeal)
  if (recentDeals.value.length > 10) {
    recentDeals.value.pop()
  }
  recentDeals.value.forEach((deal, index) => {
    if (index > 0 && deal.time !== '刚刚') {
      const minutes = parseInt(deal.time)
      if (!isNaN(minutes)) deal.time = `${minutes + 1}分钟前`
    }
  })
}

function createNewProject() {
  router.push('/client/requirement/step_all')
}

async function deleteProject(projectId) {
  const confirmed = await ui.showModal({
    title: '删除确认',
    content: '确定要删除这个项目吗？此操作不可恢复。',
    showCancel: true,
    cancelText: '取消',
    confirmText: '删除'
  })
  if (confirmed) {
    doDeleteProject(projectId)
  }
}

function doDeleteProject(projectId) {
  api.deleteRequirement(projectId)
    .then(() => {
      ui.showToast({ title: '删除成功', icon: 'success' })
      loadCurrentRequirement()
    })
    .catch(err => {
      console.error('删除失败:', err)
      loadCurrentRequirement()
      ui.showToast({ title: err.message || '操作完成', icon: 'info' })
    })
}

function viewAllProjects() {
  ui.showToast({ title: '功能开发中...', icon: 'info' })
}

function onFlowNodeTap(reqIndex, nodeIndex, reqId) {
  const req = allRequirements.value[reqIndex]
  if (!req) return

  const node = req.flowNodes[nodeIndex]
  if (!node) return

  if (nodeIndex === 0 && node.status === 'active') {
    router.push(`/client/requirement/step_all?id=${reqId}&stage=1`)
    return
  }

  if ([2, 7, 9].includes(nodeIndex) && node.status === 'active') {
    const stageName = nodeIndex === 2 ? '需求确认验收' : nodeIndex === 7 ? '功能验收测试' : '最终验收'
    const stageMap = { 2: 'clarify', 7: 'security', 9: 'final' }
    const stage = stageMap[nodeIndex]
    router.push(`/client/requirement/approve?requirementId=${reqId}&stage=${stage}&stageName=${encodeURIComponent(stageName)}`)
    return
  }

  if (AI_NODE_INDEXES.includes(nodeIndex) && node.status === 'active') {
    ui.showToast({ title: `${node.title}中，请稍候...`, icon: 'loading' })
    return
  }
}
</script>

<style scoped>
.home-view {
  padding: 15px;
  padding-bottom: 60px;
  background: linear-gradient(180deg, #f2a55e 0%, #c8aa4f 100%);
  min-height: 100vh;
  box-sizing: border-box;
}

.section {
  margin-bottom: 20px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a2e;
  position: relative;
  padding-left: 12px;
}

.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-50%);
  width: 4px;
  height: 20px;
  background: linear-gradient(180deg, #00d4ff 0%, #0099ff 100%);
  border-radius: 2px;
}

.section-more {
  font-size: 14px;
  color: #0099ff;
  transition: all 0.3s ease;
}

.section-more:active {
  opacity: 0.7;
}

.projects-section {
  margin-top: 10px;
}

.empty-project-card {
  background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
  border: 2px dashed #00d4ff;
  border-radius: 8px;
  padding: 30px 15px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  box-shadow: 0 4px 16px rgba(0, 212, 255, 0.1);
}

.empty-project-card:active {
  background: linear-gradient(135deg, #f0f9ff 0%, #e0f2fe 100%);
  border-color: #0099ff;
  transform: scale(0.98);
}

.empty-icon {
  width: 50px;
  height: 50px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #ffffff;
  font-weight: 300;
  margin-bottom: 12px;
  box-shadow: 0 4px 16px rgba(0, 212, 255, 0.3);
}

.empty-title {
  font-size: 14px;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 6px;
}

.empty-desc {
  font-size: 12px;
  color: #666666;
}

.project-flow-card-container {
  margin-bottom: 12px;
}

.add-project-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border-radius: 6px;
  background: linear-gradient(135deg, #8BC34A 0%, #7CB342 100%);
  margin-top: 6px;
  transition: all 0.3s ease;
}

.add-project-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.add-project-icon {
  font-size: 16px;
  color: #ffffff;
  font-weight: bold;
  margin-right: 6px;
}

.add-project-text {
  font-size: 12px;
  color: #ffffff;
  font-weight: 500;
}

.project-flow-card {
  background: #112740;
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 8px;
  padding: 12px;
  backdrop-filter: blur(20px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
}

.project-header-right {
  display: flex;
  align-items: center;
  gap: 6px;
}

.delete-btn {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 500;
  color: #ffffff;
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
}

.delete-btn:active {
  opacity: 0.8;
}

.project-id {
  font-size: 13px;
  font-weight: bold;
  color: #ffffff;
}

.project-status {
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 500;
}

.project-status.pending {
  background: linear-gradient(135deg, rgba(250, 140, 22, 0.2) 0%, rgba(250, 140, 22, 0.1) 100%);
  color: #fa8c16;
  border: 1px solid rgba(250, 140, 22, 0.3);
}

.project-status.processing {
  background: linear-gradient(135deg, rgba(0, 212, 255, 0.2) 0%, rgba(0, 153, 255, 0.1) 100%);
  color: #00d4ff;
  border: 1px solid rgba(0, 212, 255, 0.3);
}

.project-status.developing {
  background: linear-gradient(135deg, rgba(0, 212, 170, 0.2) 0%, rgba(0, 212, 170, 0.1) 100%);
  color: #00d4aa;
  border: 1px solid rgba(0, 212, 170, 0.3);
}

.project-status.completed {
  background: linear-gradient(135deg, rgba(0, 212, 100, 0.2) 0%, rgba(0, 212, 100, 0.1) 100%);
  color: #00d464;
  border: 1px solid rgba(0, 212, 100, 0.3);
}

.flow-timeline-scroll {
  width: 100%;
  overflow-x: auto;
  white-space: nowrap;
  -webkit-overflow-scrolling: touch;
}

.flow-timeline-scroll::-webkit-scrollbar {
  display: none;
}

.flow-timeline-horizontal {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.flow-node-wrapper {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.flow-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 12px;
  border-radius: 8px;
  min-width: 100px;
  position: relative;
  transition: all 0.3s ease;
}

.flow-node.completed {
  background: rgba(76, 175, 80, 0.15);
  border: 1px solid rgba(76, 175, 80, 0.4);
  color: #a5d6a7;
}

.flow-node.completed .flow-node-icon {
  color: #a5d6a7;
}

.flow-node.completed .flow-node-label {
  color: #a5d6a7;
}

.flow-node.active {
  background: rgba(0, 212, 255, 0.2);
  border: 1px solid #00d4ff;
  color: #00d4ff;
  animation: pulse 2s infinite;
}

.flow-node.active .flow-node-icon {
  color: #00d4ff;
}

.flow-node.active .flow-node-label {
  color: #00d4ff;
}

.flow-node.pending {
  background: rgba(107, 139, 163, 0.1);
  border: 1px solid rgba(107, 139, 163, 0.3);
  color: #6b8ba3;
}

.flow-node.pending .flow-node-icon {
  color: #6b8ba3;
}

.flow-node.pending .flow-node-label {
  color: #6b8ba3;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 5px rgba(0, 212, 255, 0.5);
  }
  50% {
    box-shadow: 0 0 20px rgba(0, 212, 255, 0.8);
  }
}

.flow-node-icon {
  font-size: 20px;
  margin-bottom: 4px;
  font-weight: bold;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-working-icon {
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-robot {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
}

.ai-head {
  font-size: 20px;
  animation: ai-bounce 1s ease-in-out infinite;
}

.ai-dots {
  display: flex;
  gap: 3px;
  margin-top: -4px;
}

.ai-dots .dot {
  width: 4px;
  height: 4px;
  background: #00d4ff;
  border-radius: 50%;
  animation: ai-dot-pulse 1.4s ease-in-out infinite;
}

.ai-dots .dot:nth-child(2) {
  animation-delay: 0.2s;
}

.ai-dots .dot:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes ai-bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-3px);
  }
}

@keyframes ai-dot-pulse {
  0%, 100% {
    opacity: 0.3;
    transform: scale(0.8);
  }
  50% {
    opacity: 1;
    transform: scale(1.2);
  }
}

.flow-node-label {
  font-size: 12px;
  font-weight: 500;
  text-align: center;
  line-height: 1.3;
  white-space: nowrap;
}

.flow-node-hint {
  font-size: 10px;
  color: #FF9800;
  margin-top: 2px;
  white-space: nowrap;
}

.flow-arrow {
  color: rgba(0, 212, 255, 0.3);
  font-size: 14px;
  margin: 0 8px;
}

.deals-section {
  margin-top: 20px;
}

.live-indicator {
  display: flex;
  align-items: center;
  background: linear-gradient(135deg, rgba(13, 205, 71, 0.1) 0%, rgba(13, 117, 35, 0.05) 100%);
  padding: 4px 10px;
  border-radius: 12px;
  border: 1px solid rgba(255, 77, 79, 0.2);
}

.live-dot {
  width: 8px;
  height: 8px;
  background: linear-gradient(135deg, #ff4d4f 0%, #ff7875 100%);
  border-radius: 50%;
  margin-right: 6px;
  animation: live-pulse 1.5s ease-in-out infinite;
  box-shadow: 0 0 6px rgba(255, 77, 79, 0.5);
}

@keyframes live-pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.9);
  }
}

.live-text {
  font-size: 12px;
  color: #ff4d4f;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.tech-card {
  background: linear-gradient(135deg, rgba(10, 10, 15, 0.95) 0%, rgba(26, 26, 46, 0.95) 100%);
  border: 1px solid rgba(0, 212, 255, 0.2);
  border-radius: 12px;
  padding: 12px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(20px);
}

.deals-container {
  height: 300px;
  overflow: hidden;
}

.deals-scroll {
  height: 100%;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.deal-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid rgba(0, 212, 255, 0.1);
  transition: all 0.3s ease;
}

.deal-item:last-child {
  border-bottom: none;
}

.deal-item:active {
  background: rgba(0, 212, 255, 0.05);
  border-radius: 8px;
}

.tech-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  color: #ffffff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: bold;
  margin-right: 12px;
  box-shadow: 0 2px 8px rgba(0, 212, 255, 0.3);
  flex-shrink: 0;
}

.deal-info {
  flex: 1;
}

.deal-user {
  display: block;
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
  margin-bottom: 4px;
}

.deal-type {
  display: block;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
}

.deal-amount {
  text-align: right;
}

.deal-price {
  display: block;
  font-size: 16px;
  color: #ff6b6b;
  font-weight: bold;
  margin-bottom: 3px;
  text-shadow: 0 0 6px rgba(255, 107, 107, 0.3);
}

.deal-time {
  display: block;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.4);
}
</style>