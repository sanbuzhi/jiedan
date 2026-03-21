<template>
  <div class="user">
    <div class="user-header">
      <div class="user-info" @click="userStore.isLogin ? null : goLogin">
        <van-avatar :size="60" :src="userStore.userInfo.avatar || ''">
          <van-icon name="user-o" size="30" />
        </van-avatar>
        <div class="info-text">
          <div class="username">{{ userStore.userInfo.nickname || userStore.userInfo.username || '点击登录' }}</div>
          <div class="phone" v-if="userStore.isLogin">{{ userStore.userInfo.phone || '未绑定手机号' }}</div>
        </div>
      </div>
    </div>
    <div class="order-section">
      <van-cell-group inset>
        <van-cell title="我的订单" is-link @click="goOrderList">
          <template #right-icon>
            <span class="more-text">全部订单 ></span>
          </template>
        </van-cell>
        <van-cell-center>
          <div class="order-grid">
            <div class="order-item" @click="goOrderList('0')">
              <van-icon name="pending-payment" size="24" />
              <span>待付款</span>
            </div>
            <div class="order-item" @click="goOrderList('1')">
              <van-icon name="logistics" size="24" />
              <span>待发货</span>
            </div>
            <div class="order-item" @click="goOrderList('2')">
              <van-icon name="goods-collect-o" size="24" />
              <span>已发货</span>
            </div>
            <div class="order-item" @click="goOrderList('3')">
              <van-icon name="description" size="24" />
              <span>已完成</span>
            </div>
          </div>
        </van-cell-center>
      </van-cell-group>
    </div>
    <div class="service-section">
      <van-cell-group inset>
        <van-cell title="收货地址" icon="location-o" is-link @click="goAddressList" />
        <van-cell title="联系客服" icon="service-o" is-link />
        <van-cell title="关于我们" icon="info-o" is-link />
      </van-cell-group>
    </div>
    <div class="logout-section" v-if="userStore.isLogin">
      <van-button block plain type="danger" @click="handleLogout">退出登录</van-button>
    </div>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useCartStore } from '@/stores/cart'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const cartStore = useCartStore()

function goLogin() {
  router.push({ name: 'Login' })
}

function goOrderList(status = '') {
  router.push({ name: 'OrderList', query: { status } })
}

function goAddressList() {
  router.push({ name: 'AddressList' })
}

async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要退出登录吗？'
    })
    await userStore.logout()
    cartStore.cartList.value = []
    showToast('退出成功')
  } catch {
  }
}

onMounted(() => {
  if (userStore.isLogin && !userStore.userInfo.id) {
    userStore.getUserInfo().catch(() => {})
  }
})
</script>

<style scoped>
.user {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
  padding-bottom: 20px;
}

.user-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 40px 20px 30px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.info-text {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.username {
  font-size: 18px;
  font-weight: bold;
  color: white;
}

.phone {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.8);
}

.order-section, .service-section {
  margin-top: 10px;
}

.more-text {
  font-size: 14px;
  color: #999;
}

.order-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  padding: 16px 0;
}

.order-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #333;
}

.logout-section {
  margin-top: 20px;
  padding: 0 16px;
}
</style>