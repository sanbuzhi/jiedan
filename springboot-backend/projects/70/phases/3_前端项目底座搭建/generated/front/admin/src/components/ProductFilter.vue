<template>
  <el-card class="product-filter">
    <el-form :inline="true" :model="filterForm" class="demo-form-inline">
      <el-form-item label="商品名称">
        <el-input v-model="filterForm.name" placeholder="请输入商品名称" clearable />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="filterForm.categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="上架状态">
        <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
          <el-option label="上架中" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useProductCategoryStore } from '@/stores/productCategory'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const productCategoryStore = useProductCategoryStore()
const categoryList = computed(() => productCategoryStore.categoryList)

const filterForm = ref({
  name: '',
  categoryId: null,
  status: null,
  ...props.modelValue
})

watch(() => props.modelValue, (val) => {
  Object.assign(filterForm.value, val)
}, { deep: true })

const handleSearch = () => {
  emit('update:modelValue', { ...filterForm.value })
  emit('search')
}

const handleReset = () => {
  filterForm.value = {
    name: '',
    categoryId: null,
    status: null
  }
  emit('update:modelValue', { ...filterForm.value })
  emit('reset')
}

onMounted(() => {
  productCategoryStore.getCategoryList()
})
</script>

<style scoped lang="css">
.product-filter {
  margin-bottom: 20px;
}
</style>