<template>
  <div class="price-display">
    <span class="currency">¥</span>
    <span class="price">{{ formattedPrice }}</span>
    <span v-if="originalPrice" class="original-price">¥{{ originalPrice }}</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  price: {
    type: [Number, String],
    required: true
  },
  originalPrice: {
    type: [Number, String],
    default: null
  }
})

const formattedPrice = computed(() => {
  const price = parseFloat(props.price)
  return isNaN(price) ? '0.00' : price.toFixed(2)
})
</script>

<style scoped>
.price-display {
  display: flex;
  align-items: baseline;
}

.currency {
  font-size: 12px;
  color: #ee0a24;
  margin-right: 2px;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #ee0a24;
}

.original-price {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 8px;
}
</style>