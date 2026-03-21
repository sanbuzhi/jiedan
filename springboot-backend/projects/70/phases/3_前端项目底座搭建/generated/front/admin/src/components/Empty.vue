<template>
  <el-empty :description="description" :image="image">
    <template #image v-if="imageUrl">
      <img :src="imageUrl" alt="empty" />
    </template>
    <template #default v-if="$slots.default">
      <slot />
    </template>
  </el-empty>
</template>

<script setup>
const props = defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  image: {
    type: String,
    default: ''
  },
  imageUrl: {
    type: String,
    default: ''
  }
})
</script>