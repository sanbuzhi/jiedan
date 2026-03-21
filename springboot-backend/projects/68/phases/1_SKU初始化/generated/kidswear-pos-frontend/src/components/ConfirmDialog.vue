<template>
  <el-dialog
    v-model="visible"
    :title="title"
    width="400px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    @closed="handleClosed"
  >
    <div class="confirm-content">
      <el-icon v-if="icon" :size="48" :color="iconColor" class="confirm-icon">
        <component :is="icon" />
      </el-icon>
      <div class="confirm-text">{{ content }}</div>
    </div>
    <template #footer>
      <div class="dialog-footer">
        <el-button @click="handleCancel" :disabled="loading">
          {{ cancelText }}
        </el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">
          {{ confirmText }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { Warning, Info, Success, Error } from '@element-plus/icons-vue'

interface Props {
  modelValue: boolean
  title?: string
  content?: string
  confirmText?: string
  cancelText?: string
  icon?: 'warning' | 'info' | 'success' | 'error' | null
  iconColor?: string
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  title: '提示',
  content: '确定执行此操作吗？',
  confirmText: '确定',
  cancelText: '取消',
  icon: 'warning',
  iconColor: '',
  loading: false
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel', 'closed'])

const visible = ref(props.modelValue)
const iconMap = {
  warning: Warning,
  info: Info,
  success: Success,
  error: Error
}
const defaultIconColorMap = {
  warning: '#e6a23c',
  info: '#409eff',
  success: '#67c23a',
  error: '#f56c6c'
}

const currentIcon = computed(() => {
  if (!props.icon) return null
  return iconMap[props.icon] || null
})

const currentIconColor = computed(() => {
  if (props.iconColor) return props.iconColor
  if (!props.icon) return ''
  return defaultIconColorMap[props.icon] || ''
})

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleConfirm = () => {
  emit('confirm')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleClosed = () => {
  emit('closed')
}
</script>

<style scoped lang="scss">
.confirm-content {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 16px 0;

  .confirm-icon {
    flex-shrink: 0;
    margin-top: 4px;
  }

  .confirm-text {
    flex: 1;
    font-size: 14px;
    line-height: 1.6;
    color: #606266;
    word-break: break-word;
  }
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>