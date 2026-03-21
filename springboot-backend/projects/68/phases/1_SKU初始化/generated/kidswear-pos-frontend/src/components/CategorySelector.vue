<template>
  <el-select
    v-model="selectedId"
    placeholder="请选择商品分类"
    :clearable="clearable"
    :disabled="disabled"
    @change="handleChange"
    style="width: 100%"
  >
    <el-option
      v-for="item in categories"
      :key="item.id"
      :label="item.name"
      :value="item.id"
    />
  </el-select>
</template>

<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
import { useCategoryStore } from '@/store/category'
import type { Category } from '@/types/category'

interface Props {
  modelValue?: number | null
  clearable?: boolean
  disabled?: boolean
}

interface Emits {
  (e: 'update:modelValue', value: number | null): void
  (e: 'change', value: number | null, category: Category | null): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: null,
  clearable: true,
  disabled: false
})

const emit = defineEmits<Emits>()
const categoryStore = useCategoryStore()
const selectedId = ref<number | null>(props.modelValue)
const categories = ref<Category[]>([])

const loadCategories = async () => {
  try {
    await categoryStore.fetchCategoryList()
    categories.value = categoryStore.categoryList
  } catch (error) {
    console.error('加载分类失败:', error)
  }
}

const handleChange = (value: number | null) => {
  const category = categories.value.find(c => c.id === value) || null
  emit('update:modelValue', value)
  emit('change', value, category)
}

watch(() => props.modelValue, (newVal) => {
  selectedId.value = newVal
})

onMounted(() => {
  loadCategories()
})
</script>