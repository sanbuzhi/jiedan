<template>
  <div class="product-sku-editor">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>规格库存</span>
          <el-button type="primary" size="small" @click="handleAddSpec">
            添加规格
          </el-button>
        </div>
      </template>
      <div class="spec-list">
        <ProductSkuForm
          v-for="(spec, index) in specs"
          :key="index"
          :spec="spec"
          :index="index"
          @update="handleUpdateSpec"
          @delete="handleDeleteSpec"
        />
      </div>
      <div v-if="skus.length" class="sku-table-wrapper">
        <ProductSkuTable
          :skus="skus"
          :specs="specs"
          @update="handleUpdateSku"
        />
      </div>
      <el-empty v-else description="请添加规格" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import ProductSkuForm from './ProductSkuForm.vue'
import ProductSkuTable from './ProductSkuTable.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ specs: [], skus: [] })
  }
})

const emit = defineEmits(['update:modelValue'])

const specs = ref(props.modelValue.specs || [])
const skus = ref(props.modelValue.skus || [])

watch(props.modelValue, (newVal) => {
  specs.value = newVal.specs || []
  skus.value = newVal.skus || []
}, { deep: true })

watch([specs, skus], ([newSpecs, newSkus]) => {
  emit('update:modelValue', { specs: newSpecs, skus: newSkus })
}, { deep: true })

const generateSkus = () => {
  const specValues = specs.value.map(spec => spec.values)
  const combinations = cartesianProduct(specValues)
  
  skus.value = combinations.map((comb, idx) => {
    const existingSku = props.modelValue.skus.find(sku => 
      JSON.stringify(sku.specs) === JSON.stringify(comb)
    )
    return existingSku || {
      id: null,
      specs: comb,
      price: 0,
      originalPrice: 0,
      costPrice: 0,
      stock: 0,
      weight: 0,
      image: '',
      code: ''
    }
  })
}

const cartesianProduct = (arrays) => {
  return arrays.reduce((a, b) => 
    a.flatMap(x => b.map(y => [...x, y])), [[]]
  )
}

const handleAddSpec = () => {
  specs.value.push({
    name: '',
    values: ['']
  })
}

const handleUpdateSpec = (index, newSpec) => {
  specs.value[index] = newSpec
  generateSkus()
}

const handleDeleteSpec = (index) => {
  specs.value.splice(index, 1)
  generateSkus()
}

const handleUpdateSku = (index, newSku) => {
  skus.value[index] = newSku
}
</script>

<style scoped>
.product-sku-editor {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.spec-list {
  margin-bottom: 20px;
}

.sku-table-wrapper {
  margin-top: 20px;
}
</style>