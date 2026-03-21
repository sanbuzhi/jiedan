<template>
  <div class="mobile-layout">
    <div class="mobile-content">
      <slot></slot>
    </div>
    <MobileTabBar />
  </div>
</template>

<script setup>
import MobileTabBar from './MobileTabBar.vue'
</script>

<style scoped>
.mobile-layout {
  width: 100%;
  min-height: 100vh;
  background-color: #f5f5f5;
}

.mobile-content {
  max-width: 420px;
  margin: 0 auto;
  background-color: #f5f5f5;
  min-height: 100vh;
  padding-bottom: 60px;
}
</style>
