<template>
  <el-upload
    :action="uploadUrl"
    :headers="uploadHeaders"
    :list-type="listType"
    :multiple="multiple"
    :limit="limit"
    :file-list="fileList"
    :on-success="handleSuccess"
    :on-remove="handleRemove"
    :on-exceed="handleExceed"
    :before-upload="beforeUpload"
    accept="image/jpeg,image/png,image/gif,image/webp"
  >
    <el-button type="primary">
      <el-icon><Upload /></el-icon>
      上传图片
    </el-button>
    <template #tip>
      <div class="el-upload__tip">
        {{ tipText }}
      </div>
    </template>
  </el-upload>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: ''
  },
  listType: {
    type: String,
    default: 'text' // text/picture/picture-card
  },
  multiple: {
    type: Boolean,
    default: false
  },
  limit: {
    type: Number,
    default: 1
  },
  tipText: {
    type: String,
    default: '支持 jpg/png/gif/webp 格式，大小不超过 5MB'
  },
  maxSize: {
    type: Number,
    default: 5 // 单位MB
  }
})

const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const uploadUrl = computed(() => import.meta.env.VITE_BASE_API + '/admin/upload/image')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const fileList = ref([])

// 监听外部值变化，同步到fileList
watch(() => props.modelValue, (val) => {
  if (!val) {
    fileList.value = []
    return
  }
  if (props.multiple && Array.isArray(val)) {
    fileList.value = val.map(url => ({ url, name: url.split('/').pop() }))
  } else if (!props.multiple && typeof val === 'string') {
    fileList.value = [{ url: val, name: val.split('/').pop() }]
  }
}, { immediate: true, deep: true })

// 上传前的格式和大小校验
const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLtMax = file.size / 1024 / 1024 < props.maxSize

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLtMax) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB!`)
    return false
  }
  return true
}

// 上传成功
const handleSuccess = (response) => {
  if (response.code === 200) {
    const newUrl = response.data.url
    if (props.multiple) {
      const newVal = [...(props.modelValue || []), newUrl]
      emit('update:modelValue', newVal)
    } else {
      emit('update:modelValue', newUrl)
    }
    ElMessage.success('上传成功!')
  } else {
    ElMessage.error(response.message || '上传失败!')
  }
}

// 删除图片
const handleRemove = (file) => {
  if (props.multiple) {
    const newVal = (props.modelValue || []).filter(url => url !== file.url)
    emit('update:modelValue', newVal)
  } else {
    emit('update:modelValue', '')
  }
}

// 超过限制数量
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片!`)
}
</script>

<style scoped>
</style>