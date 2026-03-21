<template>
  <div class="cart">
    <van-nav-bar title="购物车" />
    <van-checkbox v-model="cartStore.allChecked" @change="cartStore.toggleAllChecked">
      全选
    </van-checkbox>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div v-if="cartStore.cartList.length === 0" class="empty-cart">
        <van-empty description="购物车是空的" />
        <van-button type="primary" size="small" @click="goHome">去逛逛</van-button>
      </div>
      <div v-else class="cart-list">
        <div v-for="item in cartStore.cartList" :key="item.id" class="cart-item">
          <van-checkbox v-model="item.checked" @change="cartStore.toggleItemChecked(item.id)" />
          <img :src="item.product?.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" @click="goProductDetail(item.productId)" />
          <div class="item-info">
            <div class="item-name" @click="goProductDetail(item.productId)">{{ item.product?.name || '商品' }}</div>
            <div class="item-price">¥{{ item.product?.price?.toFixed(2) || '0.00' }}</div>
            <div class="item-actions">
              <van-stepper v-model="item.quantity" :min="1" :max="item.product?.stock || 999" @change="handleUpdateQuantity(item)" />
              <van-icon name="delete-o" size="20" @click="handleDelete(item.id)" />
            </div>
          </div>
        </div>
      </div>
    </van-pull-refresh>
    <div v-if="cartStore.cartList.length > 0" class="bottom-bar">
      <van-checkbox v-model="cartStore.allChecked" @change="cartStore.toggleAllChecked">
        全选
      </van-checkbox>
      <div class="total">
        <span>合计：</span>
        <span class="price">¥{{ cartStore.checkedPrice.toFixed(2) }}</span>
      </div>
      <van-button type="danger" :disabled="cartStore.checkedCount === 0" @click="goCheckout">
        结算({{ cartStore.checkedCount }})
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const cartStore = useCartStore()
const refreshing = ref(false)

async function onRefresh() {
  try {
    await cartStore.fetchCartList()
  } catch (error) {
    console.error(error)
  } finally {
    refreshing.value = false
  }
}

async function handleUpdateQuantity(item) {
  try {
    await cartStore.handleUpdateCart(item.id, item.quantity)
  } catch (error) {
    console.error(error)
  }
}

async function handleDelete(id) {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确定要删除这个商品吗？'
    })
    await cartStore.handleDeleteCart(id)
    showToast('删除成功')
  } catch {
  }
}

function goHome() {
  router.push({ name: 'Home' })
}

function goProductDetail(id) {
  router.push({ name: 'ProductDetail', params: { id } })
}

function goCheckout() {
  router.push({ name: 'Checkout' })
}

onMounted(() => {
  cartStore.fetchCartList().catch(() => {})
})
</script>

<style scoped>
.cart {
  width: 100%;
  min-height: 100%;
  padding-bottom: 60px;
  background-color: #f5f5f5;
}

.cart .van-checkbox {
  padding: 16px;
  background-color: white;
  margin-bottom: 10px;
}

.empty-cart {
  padding: 60px 20px;
  text-align: center;
}

.empty-cart .van-button {
  margin-top: 20px;
}

.cart-list {
  padding: 0 16px;
}

.cart-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background-color: white;
  border-radius: 8px;
  margin-bottom: 12px;
}

.cart-item img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
}

.item-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.item-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.item-price {
  font-size: 16px;
  font-weight: bold;
  color: #ff4444;
}

.item-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
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
  justify-content: space-between;
  padding: 0 16px;
  border-top: 1px solid #f0f0f0;
  z-index: 100;
}

.total {
  flex: 1;
  text-align: right;
  padding-right: 16px;
}

.price {
  font-size: 18px;
  font-weight: bold;
  color: #ff4444;
}
</style>