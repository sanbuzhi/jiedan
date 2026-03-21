<template>
  <div class="product-card" @click="goDetail">
    <img :src="product.images?.[0] || 'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg'" alt="product" />
    <div class="card-info">
      <div class="card-name">{{ product.name || '商品' }}</div>
      <div class="card-price">
        <span class="symbol">¥</span>
        <span class="value">{{ product.price?.toFixed(2) || '0.00' }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    default: () => ({})
  }
})

const router = useRouter()

function goDetail() {
  router.push({ name: 'ProductDetail', params: { id: props.product.id } })
}
</script>

<style scoped>
.product-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
}

.product-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
}

.card-info {
  padding: 12px;
}

.card-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-bottom: 8px;
}

.card-price {
  display: flex;
  align-items: baseline;
  gap: 2px;
}

.card-price .symbol {
  font-size: 12px;
  color: #ff4444;
}

.card-price .value {
  font-size: 18px;
  font-weight: bold;
  color: #ff4444;
}
</style>