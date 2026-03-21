<template>
  <el-tag :type="tagType" :size="size" :effect="effect">
    {{ tagText }}
  </el-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { SkuStatus } from '@/types/enums'

interface Props {
  status: SkuStatus | string | number
  type?: 'primary' | 'success' | 'warning' | 'danger' | 'info'
  size?: 'large' | 'default' | 'small'
  effect?: 'dark' | 'light' | 'plain'
  textMap?: Record<string | number, string>
  typeMap?: Record<string | number, 'primary' | 'success' | 'warning' | 'danger' | 'info'>
}

const props = withDefaults(defineProps<Props>(), {
  type: undefined,
  size: 'small',
  effect: 'light',
  textMap: () => ({
    [SkuStatus.ACTIVE]: '在售',
    [SkuStatus.INACTIVE]: '下架',
    [SkuStatus.OUT_OF_STOCK]: '缺货',
    [SkuStatus.DISCONTINUED]: '停产'
  }),
  typeMap: () => ({
    [SkuStatus.ACTIVE]: 'success',
    [SkuStatus.INACTIVE]: 'info',
    [SkuStatus.OUT_OF_STOCK]: 'warning',
    [SkuStatus.DISCONTINUED]: 'danger'
  })
})

const tagText = computed(() => {
  return props.textMap[props.status] || String(props.status)
})

const tagType = computed(() => {
  if (props.type) return props.type
  return props.typeMap[props.status] || 'info'
})
</script>

<style scoped lang="scss">
</style>