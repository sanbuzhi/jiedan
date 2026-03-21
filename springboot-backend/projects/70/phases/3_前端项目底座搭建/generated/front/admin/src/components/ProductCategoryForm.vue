<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑分类' : '添加分类'"
    width="500px"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="formData" :rules="rules" label-width="100px">
      <el-form-item label="分类名称" prop="name">
        <el-input v-model="formData.name" placeholder="请输入分类名称" />
      </el-form-item>
      <el-form-item label="父级分类" prop="parentId">
        <el-tree-select
          v-model="formData.parentId"
          :data="categoryTree"
          :props="{ label: 'name', value: 'id', children: 'children' }"
          placeholder="请选择父级分类（不选为顶级分类）"
          clearable
          check-strictly
        />
      </el-form-item>
      <el-form-item label="排序" prop="sort">
        <el-input-number v-model="formData.sort" :min="0" />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-switch v-model="formData.status" :active-value="1" :inactive-value="0" />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="handleClose">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
        确定
      </el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useProductCategoryStore } from '@/stores/productCategory'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  editData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

const categoryStore = useProductCategoryStore()
const formRef = ref(null)
const submitLoading = ref(false)

const isEdit = computed(() => !!props.editData)

const formData = ref({
  name: '',
  parentId: null,
  sort: 0,
  status: 1
})

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const categoryTree = computed(() => categoryStore.categoryTree)

const initForm = () => {
  if (isEdit.value && props.editData) {
    formData.value = {
      ...props.editData,
      parentId: props.editData.parentId || null
    }
  } else {
    formData.value = {
      name: '',
      parentId: null,
      sort: 0,
      status: 1
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEdit.value) {
          await categoryStore.updateCategory(formData.value)
          ElMessage.success('更新成功')
        } else {
          await categoryStore.createCategory(formData.value)
          ElMessage.success('创建成功')
        }
        emit('success')
        visible.value = false
      } catch (error) {
        console.error(error)
      } finally {
        submitLoading.value = false
      }
    }
  })
}

const handleClose = () => {
  formRef.value?.resetFields()
  visible.value = false
}

watch(() => props.modelValue, (val) => {
  if (val) {
    initForm()
  }
})
</script>