<template>
  <div class="login-layout">
    <div class="login-container">
      <router-view />
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.login-layout {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.login-container {
  width: 400px;
  padding: 40px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.2);
}
</style>