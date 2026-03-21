<template>
  <van-empty :description="description" v-bind="$attrs">
    <template #image v-if="imageUrl">
      <img :src="imageUrl" alt="empty" />
    </template>
    <template #default v-if="$slots.default">
      <slot />
    </template>
  </van-empty>
</template>

<script setup>
const props = defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  imageUrl: {
    type: String,
    default: ''
  }
})
</script>