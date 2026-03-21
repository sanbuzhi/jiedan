<template>
  <div class="store-layout">
    <router-view />
    <van-tabbar v-model="activeTab" route v-if="showTabbar">
      <van-tabbar-item replace to="/home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item replace to="/products" icon="search">商品</van-tabbar-item>
      <van-tabbar-item replace to="/cart" icon="shopping-cart-o" :badge="cartStore.totalCount > 99 ? '99+' : cartStore.totalCount || ''">购物车</van-tabbar-item>
      <van-tabbar-item replace to="/user" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useCartStore } from '@/stores/cart'

const route = useRoute()
const cartStore = useCartStore()
const activeTab = ref(0)

const showTabbar = computed(() => {
  const noTabbarPaths = ['/product', '/checkout', '/orders', '/addresses', '/login', '/register', '/404']
  return !noTabbarPaths.some(path => route.path.startsWith(path))
})

if (cartStore.isLogin) {
  cartStore.fetchCartList().catch(() => {})
}
</script>

<style scoped>
.store-layout {
  width: 100%;
  min-height: 100%;
  padding-bottom: 50px;
}
</style>