<template>
  <van-swipe :autoplay="3000" indicator-color="#ff6034">
    <van-swipe-item v-for="banner in banners" :key="banner.id">
      <img :src="banner.imageUrl || placeholderImg" alt="banner" class="banner-img" />
    </van-swipe-item>
  </van-swipe>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getHomeBanners } from '@/api/home'
import placeholderImg from '@/assets/images/placeholder.jpg'

const banners = ref([])

onMounted(async () => {
  try {
    const res = await getHomeBanners()
    banners.value = res.data || []
  } catch (error) {
    console.error('获取轮播图失败', error)
  }
})
</script>

<style scoped>
.banner-img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}
</style>