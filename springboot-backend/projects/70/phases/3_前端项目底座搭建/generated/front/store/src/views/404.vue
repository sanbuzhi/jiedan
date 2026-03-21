<template>
  <div class="not-found">
    <van-empty description="页面不存在">
      <van-button type="primary" @click="router.push('/')">返回首页</van-button>
    </van-empty>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<style lang="scss" scoped>
.not-found {
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #fff;
}
</style>