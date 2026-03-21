<template>
  <van-tag :type="tagType" size="small">{{ statusText }}</van-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: [String, Number],
    required: true
  }
})

const statusMap = {
  0: { text: '待付款', type: 'danger' },
  1: { text: '待发货', type: 'warning' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'default' },
  5: { text: '退款中', type: 'warning' },
  6: { text: '已退款', type: 'default' }
}

const statusText = computed(() => statusMap[props.status]?.text || '未知状态')
const tagType = computed(() => statusMap[props.status]?.type || 'default')
</script>

<style scoped>
</style>