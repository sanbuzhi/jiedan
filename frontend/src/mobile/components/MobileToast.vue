<template>
  <div v-if="visible" class="mobile-toast" :class="`mobile-toast-${type}`">
    <div class="mobile-toast-icon">
      <span v-if="type === 'success'">✓</span>
      <span v-else-if="type === 'error'">✕</span>
      <span v-else-if="type === 'loading'">⏳</span>
      <span v-else>ℹ</span>
    </div>
    <div class="mobile-toast-message">{{ message }}</div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    default: ''
  },
  type: {
    type: String,
    default: 'info',
    validator: (value) => ['success', 'error', 'loading', 'info'].includes(value)
  }
})
</script>

<style scoped>
.mobile-toast {
  position: fixed;
  top: 20%;
  left: 50%;
  transform: translateX(-50%);
  background: rgba(0, 0, 0, 0.75);
  color: #ffffff;
  padding: 12px 24px;
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 120px;
  max-width: 80%;
  z-index: 10000;
  animation: fadeInOut 2s ease-in-out;
  backdrop-filter: blur(4px);
}

.mobile-toast-icon {
  font-size: 32px;
  margin-bottom: 8px;
}

.mobile-toast-message {
  font-size: 14px;
  text-align: center;
  line-height: 1.4;
}

.mobile-toast-success .mobile-toast-icon {
  color: #52c41a;
}

.mobile-toast-error .mobile-toast-icon {
  color: #ff4d4f;
}

.mobile-toast-loading .mobile-toast-icon {
  animation: spin 1s linear infinite;
}

@keyframes fadeInOut {
  0% {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  20% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  80% {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
  100% {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
