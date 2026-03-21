<template>
  <div class="product-detail">
    <van-nav-bar title="商品详情" left-arrow />
    <van-swipe :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="(img, index) in product.images || ['https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg']" :key="index">
        <img :src="img" alt="product" />
      </van-swipe-item>
    </van-swipe>
    <div class="product-info">
      <div class="price">
        <span class="symbol">¥</span>
        <span class="value">{{ product.price?.toFixed(2) || '0.00' }}</span>
      </div>
      <div class="name">{{ product.name || '加载中...' }}</div>
      <div class="desc">{{ product.description || '暂无描述' }}</div>
      <div class="extra">
        <span>库存：{{ product.stock || 0 }}</span>
        <span>销量：{{ product.sales || 0 }}</span>
      </div>
    </div>
    <div class="bottom-bar">
      <van-button icon="chat-o" size="small">客服</van-button>
      <van-button icon="shopping-cart-o" size="small" @click="goCart">购物车</van-button>
      <van-button type="warning" size="small" @click="handleAddCart">加入购物车</van-button>
      <van-button type="danger" size="small">立即购买</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog } from 'vant'
import { getProductDetail } from '@/api/product'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const userStore = useUserStore()
const product = ref({})

async function fetchProductDetail() {
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data
  } catch (error) {
    console.error(error)
  }
}

function goCart() {
  router.push({ name: 'Cart' })
}

async function handleAddCart() {
  if (!userStore.isLogin) {
    try {
      await showConfirmDialog({
        title: '提示',
        message: '请先登录'
      })
      router.push({ name: 'Login', query: { redirect: route.fullPath } })
    } catch {
    }
    return
  }
  try {
    await cartStore.handleAddCart(product.value.id, 1)
    showToast('已加入购物车')
  } catch (error) {
    console.error(error)
  }
}

onMounted(() => {
  fetchProductDetail()
})
</script>

<style scoped>
.product-detail {
  width: 100%;
  min-height: 100%;
  padding-bottom: 60px;
  background-color: white;
}

.van-swipe img {
  width: 100%;
  height: 300px;
  object-fit: cover;
}

.product-info {
  padding: 16px;
}

.price {
  display: flex;
  align-items: baseline;
  gap: 4px;
  margin-bottom: 12px;
}

.symbol {
  font-size: 16px;
  color: #ff4444;
}

.value {
  font-size: 28px;
  font-weight: bold;
  color: #ff4444;
}

.name {
  font-size: 18px;
  font-weight: bold;
  color: #333;
  margin-bottom: 8px;
}

.desc {
  font-size: 14px;
  color: #666;
  margin-bottom: 16px;
}

.extra {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: #999;
  border-top: 1px solid #f0f0f0;
  padding-top: 12px;
}

.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 50px;
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-around;
  border-top: 1px solid #f0f0f0;
  padding: 0 16px;
  z-index: 100;
}
</style>