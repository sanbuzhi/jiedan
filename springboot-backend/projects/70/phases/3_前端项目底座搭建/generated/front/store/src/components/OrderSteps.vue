<template>
  <div class="order-steps">
    <van-steps :active="activeStep" active-color="#07c160">
      <van-step v-for="(step, index) in steps" :key="index">
        {{ step.text }}
        <template #icon>
          <van-icon :name="step.icon || 'checked'" />
        </template>
      </van-step>
    </van-steps>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ORDER_STATUS, ORDER_STEPS } from '@/utils/constants'

const props = defineProps({
  orderStatus: {
    type: Number,
    required: true
  }
})

// 计算当前激活的步骤
const activeStep = computed(() => {
  const status = props.orderStatus
  if (status === ORDER_STATUS.UNPAID) return -1
  if (status === ORDER_STATUS.PAID) return 1
  if (status === ORDER_STATUS.SHIPPED) return 2
  if (status === ORDER_STATUS.DELIVERED) return 3
  if (status === ORDER_STATUS.CANCELLED || status === ORDER_STATUS.REFUNDED) return -2
  return -1
})

// 处理特殊状态的步骤
const steps = computed(() => {
  const status = props.orderStatus
  if (status === ORDER_STATUS.CANCELLED) {
    return [
      { text: '订单已取消', icon: 'close' }
    ]
  }
  if (status === ORDER_STATUS.REFUNDED) {
    return [
      { text: '退款成功', icon: 'balance-pay' }
    ]
  }
  return ORDER_STEPS
})
</script>

<style scoped>
.order-steps {
  padding: 16px;
  background-color: #fff;
}
</style>