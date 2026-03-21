<template>
  <div class="checkout">
    <van-nav-bar title="结算" left-arrow />
    <div class="address-section" @click="goAddressList">
      <van-cell-group inset>
        <van-cell v-if="addressStore.defaultAddress" :title="`${addressStore.defaultAddress.name} ${addressStore.defaultAddress.phone}`" :value="addressStore.defaultAddress.detail" is-link />
        <van-cell v-else title="请选择收货地址" is-link />
      </van-cell-group>
    </div>
    <div class="product-section">
      <van-cell-group inset>
        <van-cell v-for="item in cartStore.checkedItems" :key="item.id">
          <template #icon>
            <img :src="item.product?.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" class="product-img" />
          </template>
          <template #title>
            <div class="product-name">{{ item.product?.name || '商品' }}</div>
          </template>
          <template #label>
            <div class="product-meta">
              <span>¥{{ item.product?.price?.toFixed(2) || '0.00' }}</span>
              <span>x{{ item.quantity }}</span>
            </div>
          </template>
        </van-cell>
      </van-cell-group>
    </div>
    <div class="price-section">
      <van-cell-group inset>
        <van-cell title="商品金额" :value="`¥${cartStore.checkedPrice.toFixed(2)}`" />
        <van-cell title="运费" value="¥0.00" />
        <van-cell title="实付金额" :value="`¥${cartStore.checkedPrice.toFixed(2)}`" />
      </van-cell-group>
    </div>
    <div class="bottom-bar">
      <div class="total">
        <span>实付：</span>
        <span class="price">¥{{ cartStore.checkedPrice.toFixed(2) }}</span>
      </div>
      <van-button type="danger" :disabled="!addressStore.defaultAddress || cartStore.checkedItems.length === 0" @click="handleSubmitOrder">提交订单</van-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'
import { useAddressStore } from '@/stores/address'
import { showToast, showLoading, hideLoading } from 'vant'
import { createOrder } from '@/api/order'

const router = useRouter()
const cartStore = useCartStore()
const addressStore = useAddressStore()

async function handleSubmitOrder() {
  showLoading({ message: '提交中...' })
  try {
    const orderItems = cartStore.checkedItems.map(item => ({
      productId: item.productId,
      quantity: item.quantity
    }))
    const res = await createOrder({
      addressId: addressStore.defaultAddress.id,
      items: orderItems
    })
    hideLoading()
    showToast('订单提交成功')
    await cartStore.resetCheckedItems()
    router.push({ name: 'OrderDetail', params: { id: res.data.id } })
  } catch (error) {
    hideLoading()
    console.error(error)
  }
}

function goAddressList() {
  router.push({ name: 'AddressList', query: { select: '1' } })
}

onMounted(() => {
  addressStore.fetchAddressList().catch(() => {})
})
</script>

<style scoped>
.checkout {
  width: 100%;
  min-height: 100%;
  padding-bottom: 60px;
  background-color: #f5f5f5;
}

.address-section, .product-section, .price-section {
  margin-top: 10px;
}

.product-img {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  object-fit: cover;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 4px;
  font-size: 14px;
  color: #999;
}

.product-meta span:first-child {
  color: #ff4444;
  font-weight: bold;
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
  font-size: 20px;
  font-weight: bold;
  color: #ff4444;
}
</style>