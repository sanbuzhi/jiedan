<template>
  <div class="mobile-tabbar">
    <div
      class="tabbar-item"
      :class="{ active: activeTab === 'home' }"
      @click="goTo('/client/home')"
    >
      <div class="tabbar-icon">🏠</div>
      <div class="tabbar-text">首页</div>
    </div>
    <div
      class="tabbar-item"
      :class="{ active: activeTab === 'profile' }"
      @click="goTo('/client/profile')"
    >
      <div class="tabbar-icon">👤</div>
      <div class="tabbar-text">我的</div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useMobileUserStore } from '@/mobile/stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useMobileUserStore()

const activeTab = computed(() => {
  const path = route.path
  if (path.includes('/home')) return 'home'
  if (path.includes('/profile')) return 'profile'
  return 'home'
})

function goTo(path) {
  router.push(path)
}
</script>

<style scoped>
.mobile-tabbar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 420px;
  height: 56px;
  background: #fff;
  display: flex;
  justify-content: space-around;
  align-items: center;
  border-top: 1px solid #e5e5e5;
  z-index: 1000;
}

.tabbar-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 4px 12px;
  cursor: pointer;
}

.tabbar-icon {
  font-size: 20px;
  margin-bottom: 2px;
}

.tabbar-text {
  font-size: 11px;
  color: #999;
}

.tabbar-item.active .tabbar-text {
  color: #07c160;
}
</style>
