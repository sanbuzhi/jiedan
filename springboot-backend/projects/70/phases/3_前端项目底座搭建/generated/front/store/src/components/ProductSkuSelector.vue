<template>
  <div class="product-sku-selector">
    <div v-for="(spec, specIndex) in specs" :key="specIndex" class="spec-group">
      <div class="spec-title">{{ spec.name }}</div>
      <div class="spec-values">
        <van-button
          v-for="(value, valueIndex) in spec.values"
          :key="valueIndex"
          size="small"
          :type="selectedSpecs[spec.name] === value ? 'primary' : 'default'"
          @click="selectSpec(spec.name, value)"
        >
          {{ value }}
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, computed } from 'vue'

const props = defineProps({
  specs: {
    type: Array,
    default: () => []
  },
  skuList: {
    type: Array,
    default: () => []
  },
  modelValue: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'change'])

const selectedSpecs = ref({})

// 初始化选中规格
const initSelectedSpecs = () => {
  if (props.specs.length > 0) {
    props.specs.forEach(spec => {
      if (spec.values && spec.values.length > 0) {
        selectedSpecs.value[spec.name] = spec.values[0]
      }
    })
  }
}

// 根据选中规格查找对应的SKU
const currentSku = computed(() => {
  if (!props.skuList || props.skuList.length === 0) return null
  return props.skuList.find(sku => {
    const skuSpecs = JSON.parse(sku.specs || '{}')
    return Object.keys(selectedSpecs.value).every(
      key => skuSpecs[key] === selectedSpecs.value[key]
    )
  })
})

// 选择规格
const selectSpec = (specName, value) => {
  selectedSpecs.value[specName] = value
  emit('update:modelValue', currentSku.value)
  emit('change', currentSku.value)
}

watch(() => props.specs, () => {
  initSelectedSpecs()
}, { immediate: true })

watch(currentSku, (newSku) => {
  emit('update:modelValue', newSku)
  emit('change', newSku)
}, { immediate: true })
</script>

<style scoped>
.product-sku-selector {
  padding: 10px 0;
}

.spec-group {
  margin-bottom: 15px;
}

.spec-title {
  font-size: 14px;
  color: #333;
  margin-bottom: 10px;
  font-weight: 500;
}

.spec-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>