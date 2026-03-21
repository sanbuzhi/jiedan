<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑商品' : '新增商品'"
    width="600px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="商品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入商品名称" />
      </el-form-item>
      <el-form-item label="商品编码" prop="code">
        <el-input v-model="form.code" placeholder="请输入商品编码" />
      </el-form-item>
      <el-form-item label="商品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择商品分类" style="width: 100%">
          <el-option
            v-for="category in categoryList"
            :key="category.id"
            :label="category.name"
            :value="category.id"
          />
        </el-select>
      </el-form-item>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="销售价格" prop="price">
            <el-input-number v-model="form.price" :precision="2" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="成本价格" prop="costPrice">
            <el-input-number v-model="form.costPrice" :precision="2" :min="0" style="width: 100%" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-row :gutter="20">
        <el-col :span="12">
          <el-form-item label="颜色" prop="color">
            <el-input v-model="form.color" placeholder="请输入颜色" />
          </el-form-item>
        </el-col>
        <el-col :span="12">
          <el-form-item label="尺码" prop="size">
            <el-input v-model="form.size" placeholder="请输入尺码" />
          </el-form-item>
        </el-col>
      </el-row>
      <el-form-item label="库存数量" prop="stock">
        <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="商品图片" prop="image">
        <el-input v-model="form.image" placeholder="请输入图片URL" />
      </el-form-item>
      <el-form-item label="商品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="3"
          placeholder="请输入商品描述"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getCategoryList } from '@/api/category'
import type { SkuDTO } from '@/api/sku'

interface Props {
  modelValue: boolean
  skuData?: SkuDTO | null
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void
  (e: 'refresh'): void
}

const props = defineProps<Props>()
const emit = defineEmits<Emits>()

const formRef = ref()
const categoryList = ref<any[]>([])
const visible = ref(false)
const isEdit = ref(false)

const form = ref<SkuDTO>({
  name: '',
  code: '',
  categoryId: undefined,
  price: 0,
  costPrice: 0,
  color: '',
  size: '',
  stock: 0,
  image: '',
  description: '',
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  code: [{ required: true, message: '请输入商品编码', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  price: [{ required: true, message: '请输入销售价格', trigger: 'blur' }]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      if (props.skuData) {
        isEdit.value = true
        Object.assign(form.value, props.skuData)
      } else {
        isEdit.value = false
        resetForm()
      }
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

onMounted(() => {
  loadCategoryList()
})

const loadCategoryList = async () => {
  try {
    const res = await getCategoryList()
    categoryList.value = res.data
  } catch (error) {
    console.error('加载分类列表失败', error)
  }
}

const resetForm = () => {
  form.value = {
    name: '',
    code: '',
    categoryId: undefined,
    price: 0,
    costPrice: 0,
    color: '',
    size: '',
    stock: 0,
    image: '',
    description: '',
    status: 1
  }
}

const handleClose = () => {
  visible.value = false
  resetForm()
  if (formRef.value) {
    formRef.value.resetFields()
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    emit('refresh')
    ElMessage.success(isEdit.value ? '编辑成功' : '新增成功')
    handleClose()
  } catch (error) {
    ElMessage.error('表单验证失败')
  }
}
</script>