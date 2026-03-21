<template>
  <div v-if="visible" class="mobile-dialog-overlay" @click="handleOverlayClick">
    <div class="mobile-dialog" :class="dialogClass" @click.stop>
      <!-- 标题 -->
      <div v-if="title" class="mobile-dialog-header">
        <span class="mobile-dialog-title">{{ title }}</span>
        <button v-if="showClose" class="mobile-dialog-close" @click="close">
          <span>×</span>
        </button>
      </div>
      
      <!-- 内容 -->
      <div class="mobile-dialog-body">
        <slot>
          <p v-if="message" class="mobile-dialog-message">{{ message }}</p>
        </slot>
      </div>
      
      <!-- 底部按钮 -->
      <div v-if="showFooter" class="mobile-dialog-footer">
        <button
          v-if="showCancel"
          class="mobile-dialog-btn mobile-dialog-btn-cancel"
          @click="handleCancel"
        >
          {{ cancelText }}
        </button>
        <button
          v-if="showConfirm"
          class="mobile-dialog-btn mobile-dialog-btn-confirm"
          @click="handleConfirm"
        >
          {{ confirmText }}
        </button>
        <button
          v-if="showCloseBtn"
          class="mobile-dialog-btn mobile-dialog-btn-close"
          @click="close"
        >
          {{ closeText }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    default: ''
  },
  dialogClass: {
    type: String,
    default: ''
  },
  showClose: {
    type: Boolean,
    default: true
  },
  showFooter: {
    type: Boolean,
    default: true
  },
  showCancel: {
    type: Boolean,
    default: false
  },
  showConfirm: {
    type: Boolean,
    default: false
  },
  showCloseBtn: {
    type: Boolean,
    default: true
  },
  cancelText: {
    type: String,
    default: '取消'
  },
  confirmText: {
    type: String,
    default: '确定'
  },
  closeText: {
    type: String,
    default: '关闭'
  },
  closeOnClickOverlay: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:visible', 'confirm', 'cancel', 'close'])

const close = () => {
  emit('update:visible', false)
  emit('close')
}

const handleOverlayClick = () => {
  if (props.closeOnClickOverlay) {
    close()
  }
}

const handleCancel = () => {
  emit('cancel')
  close()
}

const handleConfirm = () => {
  emit('confirm')
  close()
}
</script>

<style scoped>
.mobile-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.2s ease-out;
}

.mobile-dialog {
  background: #ffffff;
  border-radius: 12px;
  width: 85%;
  max-width: 320px;
  max-height: 80vh;
  overflow: hidden;
  animation: slideUp 0.3s ease-out;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.mobile-dialog-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.mobile-dialog-title {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
}

.mobile-dialog-close {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.2s;
}

.mobile-dialog-close:hover {
  background: #f5f5f5;
  color: #333;
}

.mobile-dialog-body {
  padding: 20px 16px;
  overflow-y: auto;
  max-height: calc(80vh - 120px);
}

.mobile-dialog-message {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  margin: 0;
  text-align: center;
}

.mobile-dialog-footer {
  display: flex;
  justify-content: space-around;
  padding: 12px 16px 16px;
  border-top: 1px solid #f0f0f0;
  gap: 12px;
}

.mobile-dialog-btn {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.mobile-dialog-btn:active {
  transform: scale(0.98);
}

.mobile-dialog-btn-cancel {
  background: #f5f5f5;
  color: #666;
}

.mobile-dialog-btn-cancel:hover {
  background: #e8e8e8;
}

.mobile-dialog-btn-confirm {
  background: linear-gradient(135deg, #07c160 0%, #06ad56 100%);
  color: #ffffff;
}

.mobile-dialog-btn-confirm:hover {
  background: linear-gradient(135deg, #06ad56 0%, #059a4d 100%);
}

.mobile-dialog-btn-close {
  background: #f5f5f5;
  color: #666;
}

.mobile-dialog-btn-close:hover {
  background: #e8e8e8;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    transform: translateY(30px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}
</style>
