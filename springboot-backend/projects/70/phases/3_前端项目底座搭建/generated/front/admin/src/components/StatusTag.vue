<template>
  <el-tag :type="tagType" :effect="effect" size="small">
    {{ text }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: [String, Number],
    required: true
  },
  type: {
    type: String,
    default: ''
  },
  map: {
    type: Object,
    default: () => ({
      0: { text: '下架', type: 'info' },
      1: { text: '上架', type: 'success' },
      2: { text: '待付款', type: 'warning' },
      3: { text: '待发货', type: 'primary' },
      4: { text: '已发货', type: '' },
      5: { text: '已完成', type: 'success' },
      6: { text: '已取消', type: 'danger' }
    })
  },
  effect: {
    type: String,
    default: 'light'
  }
})

const tagType = computed(() => props.type || props.map[props.status]?.type || 'info')
const text = computed(() => props.map[props.status]?.text || props.status)
</script>