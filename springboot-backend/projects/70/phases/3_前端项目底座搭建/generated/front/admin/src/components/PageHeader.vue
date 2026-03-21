<template>
  <el-page-header @back="goBack">
    <template #content>
      <span class="page-title">{{ title }}</span>
    </template>
    <template #extra>
      <slot name="extra"></slot>
    </template>
  </el-page-header>
</template>

<script setup>
import { useRouter } from 'vue-router'

defineProps({
  title: {
    type: String,
    default: ''
  }
})

const router = useRouter()

function goBack() {
  router.back()
}
</script>

<style scoped>
.page-title {
  font-size: 18px;
  font-weight: bold;
}
</style>