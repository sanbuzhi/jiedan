<template>
  <el-dialog
    :title="isEdit ? '编辑产品' : '新增产品'"
    v-model="visible"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="产品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入产品名称" />
      </el-form-item>
      <el-form-item label="产品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择产品分类" style="width: 100%">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="产品主图" prop="mainImage">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleMainImageSuccess"
          :before-upload="beforeUpload"
        >
          <img v-if="form.mainImage" :src="form.mainImage" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="产品轮播图" prop="images">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          list-type="picture-card"
          :limit="5"
          :on-success="handleImagesSuccess"
          :on-remove="handleImagesRemove"
          :before-upload="beforeUpload"
          :file-list="imageList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="产品价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="库存数量" prop="stock">
        <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="产品状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">上架</el-radio>
          <el-radio :label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="产品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入产品描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  productData: {
    type: Object,
    default: null
  },
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'refresh'])

const formRef = ref(null)
const submitLoading = ref(false)
const imageList = ref([])

const uploadUrl = computed(() => import.meta.env.VITE_API_BASE_URL + '/api/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const isEdit = computed(() => !!props.productData?.id)

const defaultForm = {
  name: '',
  categoryId: null,
  mainImage: '',
  images: [],
  price: 0,
  stock: 0,
  status: 1,
  description: ''
}

const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  mainImage: [{ required: true, message: '请上传产品主图', trigger: 'change' }],
  price: [{ required: true, message: '请输入产品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, defaultForm)
  imageList.value = []
  formRef.value?.resetFields()
}

const handleClose = () => {
  resetForm()
  emit('update:visible', false)
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleMainImageSuccess = (response) => {
  if (response.code === 200) {
    form.mainImage = response.data.url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleImagesSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    const urls = fileList.map((f) => f.url || f.response?.data?.url)
    form.images = urls.filter(Boolean)
    imageList.value = fileList
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleImagesRemove = (file, fileList) => {
  const urls = fileList.map((f) => f.url || f.response?.data?.url)
  form.images = urls.filter(Boolean)
  imageList.value = fileList
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  // 这里调用父组件传递的API或者通过emit通知父组件
  submitLoading.value = true
  try {
    emit('refresh', { ...form, id: props.productData?.id })
    handleClose()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

watch(
  () => props.productData,
  (val) => {
    if (val) {
      Object.assign(form, {
        name: val.name || '',
        categoryId: val.categoryId || null,
        mainImage: val.mainImage || '',
        images: val.images || [],
        price: val.price || 0,
        stock: val.stock || 0,
        status: val.status ?? 1,
        description: val.description || ''
      })
      imageList.value = (val.images || []).map((url) => ({ url }))
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>