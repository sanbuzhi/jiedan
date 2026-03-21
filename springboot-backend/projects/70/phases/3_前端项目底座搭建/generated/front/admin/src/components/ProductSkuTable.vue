<template>
  <div class="product-sku-table">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品规格库存</span>
        </div>
      </template>
      <el-button type="primary" size="small" @click="handleAddSku" style="margin-bottom: 16px;">
        添加规格
      </el-button>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column
          v-for="(col, index) in skuColumnList"
          :key="`col-${index}`"
          :prop="col.prop"
          :label="col.label"
        />
        <el-table-column prop="price" label="售价（元）" width="140">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.price"
              :min="0"
              :precision="2"
              :step="0.1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价（元）" width="140">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.originalPrice"
              :min="0"
              :precision="2"
              :step="0.1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.stock"
              :min="0"
              :step="1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="重量（kg）" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.weight"
              :min="0"
              :precision="2"
              :step="0.1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="image" label="规格图片" width="120">
          <template #default="scope">
            <Uploader
              v-model="scope.row.image"
              :limit="1"
              list-type="picture"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              link
              @click="handleDeleteSku(scope.$index)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Uploader from './Uploader.vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  skuSpecs: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const tableData = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const skuColumnList = computed(() => {
  return props.skuSpecs.map((spec, index) => ({
    prop: `spec${index + 1}`,
    label: spec.name
  }))
})

const handleAddSku = () => {
  const newSku = {
    id: null,
    price: 0,
    originalPrice: 0,
    stock: 0,
    weight: 0,
    image: ''
  }
  props.skuSpecs.forEach((_, index) => {
    newSku[`spec${index + 1}`] = ''
  })
  tableData.value.push(newSku)
}

const handleDeleteSku = (index) => {
  tableData.value.splice(index, 1)
}
</script>

<style scoped lang="css">
.product-sku-table {
  margin-top: 20px;
}
</style>