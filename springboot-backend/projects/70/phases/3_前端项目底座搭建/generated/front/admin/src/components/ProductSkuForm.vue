<template>
  <div class="product-sku-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品规格设置</span>
        </div>
      </template>
      <div v-for="(spec, index) in skuSpecs" :key="index" class="spec-item">
        <div class="spec-header">
          <span>规格{{ index + 1 }}</span>
          <el-button type="danger" size="small" link @click="handleDeleteSpec(index)">
            删除规格
          </el-button>
        </div>
        <el-form :model="spec" label-width="80px">
          <el-form-item label="规格名称">
            <el-input v-model="spec.name" placeholder="如：颜色、尺码" />
          </el-form-item>
          <el-form-item label="规格值">
            <el-tag
              v-for="(val, valIndex) in spec.values"
              :key="valIndex"
              closable
              @close="handleDeleteSpecValue(index, valIndex)"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ val }}
            </el-tag>
            <el-input
              v-model="newSpecValue"
              placeholder="输入规格值后按回车添加"
              @keyup.enter="handleAddSpecValue(index)"
              style="width: 200px;"
            />
          </el-form-item>
        </el-form>
      </div>
      <el-button type="primary" @click="handleAddSpec">添加规格</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const newSpecValue = ref('')
const skuSpecs = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleAddSpec = () => {
  skuSpecs.value.push({
    name: '',
    values: []
  })
}

const handleDeleteSpec = (index) => {
  skuSpecs.value.splice(index, 1)
}

const handleAddSpecValue = (specIndex) => {
  if (!newSpecValue.value.trim()) return
  if (!skuSpecs.value[specIndex].values.includes(newSpecValue.value.trim())) {
    skuSpecs.value[specIndex].values.push(newSpecValue.value.trim())
  }
  newSpecValue.value = ''
}

const handleDeleteSpecValue = (specIndex, valIndex) => {
  skuSpecs.value[specIndex].values.splice(valIndex, 1)
}
</script>

<style scoped lang="css">
.product-sku-form {
  margin-top: 20px;
}
.spec-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #dcdfe6;
}
.spec-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.spec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: bold;
}
</style>