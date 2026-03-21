<template>
  <MobileLayout>
    <div class="profile-view">
      <div class="user-card">
        <div class="user-info" @click="showEditModal = true">
          <div class="avatar-container">
            <img
              v-if="cachedAvatarPath"
              :src="cachedAvatarPath"
              class="avatar-large"
              alt="avatar"
            />
            <img
              v-else-if="userInfo?.avatar && !userInfo?.isDefaultAvatar"
              :src="userInfo.avatar"
              class="avatar-large"
              alt="avatar"
              @load="onAvatarLoad"
              @error="onAvatarError"
            />
            <img
              v-else
              src="@/assets/pic/default-avatar.png"
              class="avatar-large"
              alt="default avatar"
            />
            <div class="avatar-overlay" v-if="userInfo?.isDefaultAvatar">
              <span class="camera-icon">📷</span>
            </div>
          </div>
          <div class="user-meta">
            <span class="nickname">{{ userInfo?.nickname || '未设置昵称' }}</span>
            <span class="user-id">ID: {{ userInfo?.id || '--' }}</span>
            <span class="avatar-tip" v-if="userInfo?.isDefaultAvatar">点击修改专属头像</span>
          </div>
        </div>
        <div class="points-section">
          <div class="points-item">
            <span class="points-value">{{ userInfo?.total_points || 0 }}</span>
            <span class="points-label">我的积分</span>
          </div>
          <div class="points-divider"></div>
          <div class="points-item">
            <span class="points-value">{{ referralCount }}</span>
            <span class="points-label">推荐人数</span>
          </div>
        </div>
        <div class="points-exchange-section">
          <div class="exchange-btn" @click="navigateToExchange">
            <span class="exchange-icon">🎁</span>
            <span class="exchange-text">积分兑换</span>
          </div>
        </div>
      </div>

      <div class="card referral-card">
        <div class="card-header">
          <span class="card-title">我的推荐码</span>
          <span class="share-btn" @click="showShareRules">分享赚积分</span>
        </div>
        <div class="referral-code-section">
          <div class="code-display">
            <span class="code-text">{{ userInfo?.referralCode || '--' }}</span>
          </div>
          <div class="code-actions">
            <button class="action-btn" @click="copyReferralCode">复制</button>
            <button class="action-btn primary" @click="showShareRules">分享</button>
          </div>
        </div>
        <div class="referral-tips">
          好友通过您的推荐码注册，您可获得100积分奖励
        </div>
      </div>

      <div class="card tree-card">
        <div class="card-header">
          <span class="card-title">我的推荐团队</span>
          <span class="view-all" @click="showCanvasView = !showCanvasView">
            {{ showCanvasView ? '列表视图' : '树状视图' }}
          </span>
        </div>

        <div class="tree-summary">
          <div class="summary-item">
            <span class="summary-value level-1">{{ treeStats.level1 }}</span>
            <span class="summary-label">一级推荐</span>
          </div>
          <div class="summary-item">
            <span class="summary-value level-2">{{ treeStats.level2 }}</span>
            <span class="summary-label">二级推荐</span>
          </div>
          <div class="summary-item">
            <span class="summary-value level-3">{{ treeStats.level3 }}</span>
            <span class="summary-label">三级推荐</span>
          </div>
        </div>

        <div class="tree-list" v-if="!showCanvasView && referralTree.children?.length > 0">
          <div v-for="(item, index) in referralTree.children" :key="item.id" class="tree-item level-1-item">
            <div class="tree-user" @click="toggleExpand(index)">
              <img v-if="item.user?.avatar && !isDefaultAvatar(item.user.avatar)" :src="getFullAvatarUrl(item.user.avatar)" class="tree-avatar" alt="avatar" @error="onTreeAvatarError($event)" />
              <img v-else src="@/assets/pic/default-avatar.png" class="tree-avatar" alt="default avatar" />
              <div class="tree-info">
                <span class="tree-name">{{ item.user?.nickname || '匿名用户' }}</span>
                <span class="tree-date">{{ formatDate(item.user?.created_at) }}</span>
              </div>
              <div class="tree-expand" v-if="item.children?.length > 0">
                <span class="expand-icon" :class="{ expanded: item.expanded }">▼</span>
              </div>
            </div>

            <div class="sub-tree" v-if="item.expanded && item.children?.length > 0">
              <div v-for="(child, childIndex) in item.children" :key="child.id" class="tree-item level-2-item">
                <div class="tree-user" @click="toggleChildExpand(index, childIndex)">
                  <img v-if="child.user?.avatar && !isDefaultAvatar(child.user.avatar)" :src="getFullAvatarUrl(child.user.avatar)" class="tree-avatar small" alt="avatar" @error="onTreeAvatarError($event)" />
                  <img v-else src="@/assets/pic/default-avatar.png" class="tree-avatar small" alt="default avatar" />
                  <div class="tree-info">
                    <span class="tree-name">{{ child.user?.nickname || '匿名用户' }}</span>
                    <span class="tree-date">{{ formatDate(child.user?.created_at) }}</span>
                  </div>
                  <div class="tree-expand" v-if="child.children?.length > 0">
                    <span class="expand-icon" :class="{ expanded: child.expanded }">▼</span>
                  </div>
                </div>

                <div class="sub-tree" v-if="child.expanded && child.children?.length > 0">
                  <div v-for="grandchild in child.children" :key="grandchild.id" class="tree-item level-3-item">
                    <div class="tree-user">
                      <img v-if="grandchild.user?.avatar && !isDefaultAvatar(grandchild.user.avatar)" :src="getFullAvatarUrl(grandchild.user.avatar)" class="tree-avatar small" alt="avatar" @error="onTreeAvatarError($event)" />
                      <img v-else src="@/assets/pic/default-avatar.png" class="tree-avatar small" alt="default avatar" />
                      <div class="tree-info">
                        <span class="tree-name">{{ grandchild.user?.nickname || '匿名用户' }}</span>
                        <span class="tree-date">{{ formatDate(grandchild.user?.created_at) }}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="empty-state" v-if="!referralTree.children?.length">
          暂无推荐记录，快去分享您的推荐码吧！
        </div>
      </div>

      <div class="card points-card">
        <div class="card-header">
          <span class="card-title">积分记录</span>
          <span class="view-all" @click="navigateToPoints">查看全部</span>
        </div>
        <div class="points-list" v-if="pointRecords.length > 0">
          <div v-for="item in pointRecords" :key="item.id" class="points-item-record">
            <div class="points-info">
              <span class="points-desc">{{ item.description }}</span>
              <span class="points-time">{{ formatDate(item.created_at) }}</span>
            </div>
            <span class="points-amount" :class="item.amount > 0 ? 'positive' : 'negative'">
              {{ item.amount > 0 ? '+' : '' }}{{ item.amount }}
            </span>
          </div>
        </div>
        <div class="empty-state" v-else>
          暂无积分记录
        </div>
      </div>

      <button class="logout-btn" @click="logout">退出登录</button>

      <div class="modal-mask" v-if="showEditModal" @click="showEditModal = false">
        <div class="modal-content edit-profile-modal" @click.stop>
          <div class="modal-header edit-header">
            <span class="modal-title">编辑个人资料</span>
            <div class="modal-close" @click="showEditModal = false">
              <span class="close-icon">×</span>
            </div>
          </div>
          <div class="modal-body edit-body">
            <div class="edit-avatar-section">
              <div class="edit-avatar-container" @click="chooseAvatar">
                <img
                  class="edit-avatar"
                  :src="editAvatar || cachedAvatarPath || userInfo?.avatar"
                  alt="avatar"
                />
                <div class="edit-avatar-overlay">
                  <span class="edit-avatar-text">点击更换</span>
                </div>
              </div>
              <input
                type="file"
                ref="avatarInput"
                accept="image/*"
                style="display: none"
                @change="onAvatarChange"
              />
            </div>
            <div class="edit-form-item">
              <span class="edit-label">昵称</span>
              <input
                class="edit-input"
                type="text"
                placeholder="请输入昵称"
                v-model="editNickname"
                maxlength="20"
              />
              <span class="edit-hint">2-20个字符，支持中文、英文、数字</span>
            </div>
          </div>
          <div class="modal-footer edit-footer">
            <button class="modal-btn" @click="showEditModal = false">取消</button>
            <button class="modal-btn primary" @click="saveProfile" :disabled="isSaving">
              {{ isSaving ? '保存中...' : '保存' }}
            </button>
          </div>
        </div>
      </div>

      <div class="modal-mask" v-if="showShareRulesModal" @click="showShareRulesModal = false">
        <div class="modal-content share-rules-modal" @click.stop>
          <div class="modal-header">
            <span class="modal-title">分享赚积分规则</span>
            <div class="modal-close" @click="showShareRulesModal = false">
              <span class="close-icon">×</span>
            </div>
          </div>
          <div class="modal-body">
            <div class="rules-section">
              <div class="rules-title">推荐奖励</div>
              <div class="rules-item">
                <span class="rules-dot level-1"></span>
                <span class="rules-text">一级推荐奖励 100 积分</span>
              </div>
              <div class="rules-item">
                <span class="rules-dot level-2"></span>
                <span class="rules-text">二级推荐奖励 50 积分</span>
              </div>
              <div class="rules-item">
                <span class="rules-dot level-3"></span>
                <span class="rules-text">三级推荐奖励 30 积分</span>
              </div>
            </div>
            <div class="rules-section">
              <div class="rules-title">积分使用说明</div>
              <div class="rules-desc">
                <span>积分可兑换平台奖励、抵扣开发费用</span>
                <span class="rules-highlight">100 积分 = 1 元</span>
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn" @click="copyShareLink">分享链接</button>
            <button class="modal-btn primary" @click="generateQRCode">生成二维码</button>
          </div>
        </div>
      </div>

      <div class="modal-mask" v-if="showQRCodeModal" @click="showQRCodeModal = false">
        <div class="modal-content qrcode-modal" @click.stop>
          <div class="modal-header">
            <span class="modal-title">推荐二维码</span>
            <div class="modal-close" @click="showQRCodeModal = false">
              <span class="close-icon">×</span>
            </div>
          </div>
          <div class="modal-body qrcode-body">
            <img v-if="qrCodeUrl" :src="qrCodeUrl" class="qrcode-image" alt="qrcode" />
            <div v-else class="qrcode-loading">生成中...</div>
          </div>
          <div class="modal-footer">
            <button class="modal-btn primary full-width" @click="saveQRCode">保存到相册</button>
          </div>
        </div>
      </div>
    </div>
  </MobileLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import MobileLayout from '../components/MobileLayout.vue'
import { useMobileUserStore } from '../stores/user'
import api from '@/utils/api'
import avatarCache from '@/utils/avatar-cache'
import ui from '@/utils/mobile-ui'

const router = useRouter()
const userStore = useMobileUserStore()

const userInfo = computed(() => userStore.userInfo)
const cachedAvatarPath = ref('')
const referralTree = ref({ children: [] })
const pointRecords = ref([])
const showCanvasView = ref(false)
const showEditModal = ref(false)
const showShareRulesModal = ref(false)
const showQRCodeModal = ref(false)
const editAvatar = ref('')
const editNickname = ref('')
const isSaving = ref(false)
const qrCodeUrl = ref('')
const avatarInput = ref(null)

const referralCount = computed(() => {
  if (!referralTree.value.children?.length) return 0
  let count = 0
  const countNodes = (nodes) => {
    for (const node of nodes) {
      count++
      if (node.children?.length) {
        countNodes(node.children)
      }
    }
  }
  countNodes(referralTree.value.children)
  return count
})

const treeStats = computed(() => {
  const stats = { level1: 0, level2: 0, level3: 0 }
  if (!referralTree.value.children?.length) return stats
  stats.level1 = referralTree.value.children.length
  for (const child of referralTree.value.children) {
    if (child.children?.length) {
      stats.level2 += child.children.length
      for (const grandchild of child.children) {
        if (grandchild.children?.length) {
          stats.level3 += grandchild.children.length
        }
      }
    }
  }
  return stats
})

const loadUserInfo = async () => {
  try {
    await userStore.fetchUserInfo()
  } catch (err) {
    console.error('获取用户信息失败:', err)
  }
}

const loadReferralTree = async () => {
  try {
    const res = await api.getReferralTree()
    referralTree.value = res || { children: [] }
  } catch (err) {
    console.error('获取推荐树失败:', err)
    referralTree.value = { children: [] }
  }
}

const loadPointRecords = async () => {
  try {
    const res = await api.getMyPoints(0, 5)
    pointRecords.value = res.items || res.content || res || []
  } catch (err) {
    console.error('获取积分记录失败:', err)
    pointRecords.value = []
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return '--'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

const toggleExpand = (index) => {
  if (referralTree.value.children[index]) {
    referralTree.value.children[index].expanded = !referralTree.value.children[index].expanded
  }
}

const toggleChildExpand = (parentIndex, childIndex) => {
  const child = referralTree.value.children[parentIndex]?.children?.[childIndex]
  if (child) {
    child.expanded = !child.expanded
  }
}

const copyReferralCode = async () => {
  const code = userInfo.value?.referralCode
  if (!code) return
  try {
    await navigator.clipboard.writeText(code)
    ui.showToast({ title: '推荐码已复制', icon: 'success' })
  } catch (err) {
    const input = document.createElement('input')
    input.value = code
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ui.showToast({ title: '推荐码已复制', icon: 'success' })
  }
}

const showShareRules = () => {
  showShareRulesModal.value = true
}

const copyShareLink = async () => {
  const link = `${window.location.origin}/register?ref=${userInfo.value?.referralCode}`
  try {
    await navigator.clipboard.writeText(link)
    ui.showToast({ title: '分享链接已复制', icon: 'success' })
    showShareRulesModal.value = false
  } catch (err) {
    const input = document.createElement('input')
    input.value = link
    document.body.appendChild(input)
    input.select()
    document.execCommand('copy')
    document.body.removeChild(input)
    ui.showToast({ title: '分享链接已复制', icon: 'success' })
    showShareRulesModal.value = false
  }
}

const generateQRCode = () => {
  showShareRulesModal.value = false
  qrCodeUrl.value = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(window.location.origin + '/register?ref=' + userInfo.value?.referralCode)}`
  showQRCodeModal.value = true
}

const saveQRCode = async () => {
  if (!qrCodeUrl.value) return
  try {
    const link = document.createElement('a')
    link.download = 'referral-qrcode.png'
    link.href = qrCodeUrl.value
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
  } catch (err) {
    console.error('保存二维码失败:', err)
    alert('保存失败，请长按图片保存')
  }
}

const chooseAvatar = () => {
  avatarInput.value?.click()
}

const onAvatarChange = (e) => {
  const file = e.target.files?.[0]
  if (!file) return
  const reader = new FileReader()
  reader.onload = (event) => {
    editAvatar.value = event.target?.result
  }
  reader.readAsDataURL(file)
}

const saveProfile = async () => {
  if (!editNickname.value || editNickname.value.length < 2) {
    ui.showToast({ title: '昵称长度不能少于 2 个字符', icon: 'error' })
    return
  }
  isSaving.value = true
  const loading = ui.showLoading({ title: '保存中...' })
  try {
    const data = { nickname: editNickname.value }
    if (editAvatar.value) {
      data.avatar = editAvatar.value
    }
    await userStore.updateUserInfo(data)
    showEditModal.value = false
    loading.hide()
    ui.showToast({ title: '保存成功', icon: 'success' })
  } catch (err) {
    console.error('保存失败:', err)
    loading.hide()
    ui.showToast({ title: '保存失败', icon: 'error' })
  } finally {
    isSaving.value = false
  }
}

const navigateToPoints = () => {
  router.push('/client/points')
}

const navigateToExchange = () => {
  router.push('/client/exchange')
}

const logout = async () => {
  const confirmed = await ui.showModal({
    title: '提示',
    content: '确定要退出登录吗？',
    showCancel: true,
    cancelText: '取消',
    confirmText: '确定'
  })
  if (confirmed) {
    userStore.logout()
    router.push('/client/login')
  }
}

// 头像相关方法
const loadCachedAvatar = async () => {
  const serverAvatarUrl = userInfo.value?.avatar
  const cachedAvatarUrl = localStorage.getItem('cachedAvatar_url')
  const cachedPath = avatarCache.getCachedAvatar()
  
  // 检查头像是否已变更
  if (serverAvatarUrl && cachedAvatarUrl && serverAvatarUrl !== cachedAvatarUrl) {
    console.log('检测到头像已变更，重新下载:', serverAvatarUrl)
    await downloadAndCacheAvatarFromServer()
    return
  }
  
  if (cachedPath) {
    console.log('使用本地缓存头像:', cachedPath)
    cachedAvatarPath.value = cachedPath
  } else {
    // 没有缓存，从服务器下载
    await downloadAndCacheAvatarFromServer()
  }
}

const downloadAndCacheAvatarFromServer = async () => {
  if (userInfo.value?.avatar && !userInfo.value?.isDefaultAvatar) {
    try {
      const cachedPath = await avatarCache.downloadAndCacheAvatar(userInfo.value.avatar)
      console.log('头像下载并缓存成功:', cachedPath)
      cachedAvatarPath.value = cachedPath
    } catch (err) {
      console.error('头像下载失败:', err)
    }
  }
}

const onAvatarLoad = () => {
  // 头像加载成功
  console.log('头像加载成功')
}

const onAvatarError = () => {
  // 头像加载失败，使用默认头像
  console.log('头像加载失败，使用默认头像')
}

const onTreeAvatarError = (e) => {
  // 树状列表头像加载失败，显示默认头像
  const img = e.target
  img.src = new URL('@/assets/pic/default-avatar.png', import.meta.url).href
  console.log('树状列表头像加载失败，使用默认头像')
}

// 工具方法：获取完整头像 URL
const getFullAvatarUrl = (avatarPath) => {
  return avatarCache.getFullAvatarUrl(avatarPath)
}

// 工具方法：判断是否是默认头像
const isDefaultAvatar = (avatarPath) => {
  return avatarCache.isDefaultAvatar(avatarPath)
}

onMounted(() => {
  loadUserInfo()
  loadReferralTree()
  loadPointRecords()
  editNickname.value = userInfo.value?.nickname || ''
  // 加载缓存头像（在用户信息加载后）
  setTimeout(() => {
    loadCachedAvatar()
  }, 100)
})
</script>

<style scoped>
.profile-view {
  padding: 10px 15px 30px;
  background-color: #f5f7fa;
  min-height: 100vh;
  box-sizing: border-box;
}

.user-card {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 12px;
  color: #ffffff;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.avatar-container {
  position: relative;
  width: 60px;
  height: 60px;
  margin-right: 12px;
}

.avatar-large {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid rgba(255, 255, 255, 0.5);
}

.avatar-placeholder {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: #ffffff;
  font-weight: bold;
  width: 60px;
  height: 60px;
  border-radius: 50%;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
}

.camera-icon {
  font-size: 24px;
}

.user-meta {
  display: flex;
  flex-direction: column;
}

.nickname {
  font-size: 18px;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 4px;
}

.user-id {
  font-size: 14px;
  color: #666666;
  margin-bottom: 2px;
}

.avatar-tip {
  font-size: 12px;
  color: #00d4ff;
}

.points-section {
  display: flex;
  align-items: center;
  justify-content: space-around;
  padding-top: 12px;
  border-top: 1px solid rgba(255, 255, 255, 0.2);
}

.points-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.points-value {
  font-size: 24px;
  font-weight: bold;
}

.points-label {
  font-size: 12px;
  opacity: 0.8;
  margin-top: 4px;
}

.points-divider {
  width: 1px;
  height: 40px;
  background: rgba(0, 0, 0, 0.1);
}

.points-exchange-section {
  margin-top: 16px;
}

.exchange-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 12px;
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4d4f 100%);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.exchange-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.exchange-icon {
  font-size: 18px;
  margin-right: 8px;
}

.exchange-text {
  font-size: 14px;
  color: #ffffff;
  font-weight: 500;
}

.card {
  background: linear-gradient(135deg, #ffffff 0%, #f0f9ff 100%);
  border-radius: 16px;
  padding: 16px;
  margin-bottom: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
  color: #1a1a2e;
}

.share-btn {
  font-size: 12px;
  color: #ff6b6b;
  cursor: pointer;
}

.view-all {
  font-size: 12px;
  color: #00d4ff;
  cursor: pointer;
}

.referral-code-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px 0;
}

.code-display {
  background: #f5f5f5;
  padding: 12px 24px;
  border-radius: 8px;
  margin-bottom: 12px;
}

.code-text {
  font-size: 20px;
  font-weight: bold;
  color: #1a1a2e;
  letter-spacing: 2px;
}

.code-actions {
  display: flex;
  gap: 12px;
}

.action-btn {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  border: 1px solid #ddd;
  background: #ffffff;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-btn:active {
  transform: scale(0.98);
}

.action-btn.primary {
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  color: #ffffff;
  border: none;
}

.referral-tips {
  font-size: 12px;
  color: #999999;
  text-align: center;
  margin-top: 8px;
}

.tree-summary {
  display: flex;
  justify-content: space-around;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  margin-bottom: 12px;
}

.summary-item {
  display: flex;
  flex-direction: column;
  align-items: center;
}

.summary-value {
  font-size: 20px;
  font-weight: bold;
}

.summary-value.level-1 {
  color: #00d464;
}

.summary-value.level-2 {
  color: #00d4ff;
}

.summary-value.level-3 {
  color: #ff9800;
}

.summary-label {
  font-size: 12px;
  color: #666666;
  margin-top: 4px;
}

.tree-list {
  max-height: 300px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.tree-item {
  margin-bottom: 8px;
}

.tree-user {
  display: flex;
  align-items: center;
  padding: 10px;
  background: #f9f9f9;
  border-radius: 10px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.tree-user:active {
  background: #f0f0f0;
}

.tree-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
  margin-right: 12px;
}

.tree-avatar.small {
  width: 32px;
  height: 32px;
}

.tree-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.tree-name {
  font-size: 14px;
  color: #1a1a2e;
  font-weight: 500;
}

.tree-date {
  font-size: 12px;
  color: #999999;
  margin-top: 2px;
}

.tree-expand {
  margin-left: 8px;
}

.expand-icon {
  font-size: 12px;
  color: #999999;
  transition: transform 0.3s ease;
}

.expand-icon.expanded {
  transform: rotate(180deg);
}

.level-2-item {
  margin-left: 20px;
}

.level-3-item {
  margin-left: 20px;
}

.sub-tree {
  margin-top: 8px;
}

.empty-state {
  text-align: center;
  padding: 32px 0;
  color: #999999;
  font-size: 14px;
}

.points-card .points-list {
  max-height: 200px;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.points-item-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.points-item-record:last-child {
  border-bottom: none;
}

.points-info {
  display: flex;
  flex-direction: column;
}

.points-desc {
  font-size: 14px;
  color: #1a1a2e;
}

.points-time {
  font-size: 12px;
  color: #999999;
  margin-top: 2px;
}

.points-amount {
  font-size: 16px;
  font-weight: bold;
}

.points-amount.positive {
  color: #00d464;
}

.points-amount.negative {
  color: #ff4d4f;
}

.logout-btn {
  width: 100%;
  padding: 14px;
  border-radius: 12px;
  background: linear-gradient(135deg, #ff4d4f 0%, #cf1322 100%);
  color: #ffffff;
  font-size: 16px;
  font-weight: 500;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 16px;
}

.logout-btn:active {
  transform: scale(0.98);
  opacity: 0.9;
}

.modal-mask {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #ffffff;
  border-radius: 16px;
  width: 90%;
  max-width: 340px;
  max-height: 80vh;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
}

.modal-title {
  font-size: 16px;
  font-weight: bold;
  color: #1a1a2e;
}

.modal-close {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
}

.close-icon {
  font-size: 24px;
  color: #999999;
}

.modal-body {
  padding: 16px;
  max-height: 50vh;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.modal-footer {
  display: flex;
  gap: 12px;
  padding: 16px;
  border-top: 1px solid rgba(0, 0, 0, 0.05);
}

.modal-btn {
  flex: 1;
  padding: 12px;
  border-radius: 10px;
  font-size: 14px;
  border: 1px solid #ddd;
  background: #ffffff;
  color: #666666;
  cursor: pointer;
  transition: all 0.3s ease;
}

.modal-btn:active {
  transform: scale(0.98);
}

.modal-btn.primary {
  background: linear-gradient(135deg, #00d4ff 0%, #0099ff 100%);
  color: #ffffff;
  border: none;
}

.modal-btn.full-width {
  width: 100%;
}

.rules-section {
  margin-bottom: 20px;
}

.rules-section:last-child {
  margin-bottom: 0;
}

.rules-title {
  font-size: 14px;
  font-weight: bold;
  color: #1a1a2e;
  margin-bottom: 12px;
}

.rules-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.rules-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 8px;
}

.rules-dot.level-1 {
  background: #00d464;
}

.rules-dot.level-2 {
  background: #00d4ff;
}

.rules-dot.level-3 {
  background: #ff9800;
}

.rules-text {
  font-size: 14px;
  color: #666666;
}

.rules-desc {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rules-highlight {
  color: #ff6b6b;
  font-weight: 500;
}

.qrcode-body {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.qrcode-image {
  width: 200px;
  height: 200px;
}

.qrcode-loading {
  color: #999999;
  font-size: 14px;
}

.edit-body {
  padding: 20px 16px;
}

.edit-avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 24px;
}

.edit-avatar-container {
  position: relative;
  width: 100px;
  height: 100px;
  cursor: pointer;
}

.edit-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
}

.edit-avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
}

.edit-avatar-container:hover .edit-avatar-overlay {
  opacity: 1;
}

.edit-avatar-text {
  font-size: 12px;
  color: #ffffff;
}

.edit-form-item {
  margin-bottom: 16px;
}

.edit-label {
  display: block;
  font-size: 14px;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.edit-input {
  width: 100%;
  padding: 12px;
  border: 1px solid #ddd;
  border-radius: 10px;
  font-size: 14px;
  box-sizing: border-box;
  outline: none;
  transition: border-color 0.3s ease;
}

.edit-input:focus {
  border-color: #00d4ff;
}

.edit-hint {
  display: block;
  font-size: 12px;
  color: #999999;
  margin-top: 6px;
}

.edit-footer {
  padding: 12px 16px;
}
</style>
