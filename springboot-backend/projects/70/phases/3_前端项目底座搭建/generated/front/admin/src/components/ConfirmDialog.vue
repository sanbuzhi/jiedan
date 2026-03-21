<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="420px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="confirm-content">
      <el-icon v-if="icon" :size="48" :color="iconColor" class="confirm-icon">
        <component :is="icon" />
      </el-icon>
      <span>{{ content }}</span>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Warning, CircleCheck } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '提示'
  },
  content: {
    type: String,
    default: '确定要执行此操作吗？'
  },
  loading: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'warning', // warning, success
    validator: (val) => ['warning', 'success'].includes(val)
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(props.modelValue)
const iconMap = {
  warning: Warning,
  success: CircleCheck
}
const iconColorMap = {
  warning: '#E6A23C',
  success: '#67C23A'
}

const icon = computed(() => iconMap[props.type])
const iconColor = computed(() => iconColorMap[props.type])

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<style scoped>
.confirm-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  font-size: 15px;
}
.confirm-icon {
  flex-shrink: 0;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>