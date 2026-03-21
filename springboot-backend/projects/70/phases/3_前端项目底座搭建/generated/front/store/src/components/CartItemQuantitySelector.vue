<template>
  <div class="quantity-selector">
    <van-button
      size="small"
      icon="minus"
      :disabled="quantity <= min"
      @click="handleChange(quantity - 1)"
    />
    <input
      type="number"
      :min="min"
      :max="max"
      :value="quantity"
      class="quantity-input"
      @input="handleInputChange"
    />
    <van-button
      size="small"
      icon="plus"
      :disabled="quantity >= max"
      @click="handleChange(quantity + 1)"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'

const props = defineProps({
  modelValue: {
    type: Number,
    default: 1
  },
  min: {
    type: Number,
    default: 1
  },
  max: {
    type: Number,
    default: 999
  }
})

const emit = defineEmits(['update:modelValue'])

const quantity = ref(props.modelValue)

watch(() => props.modelValue, (val) => {
  quantity.value = val
})

const handleChange = (val) => {
  let newVal = val
  if (newVal < props.min) newVal = props.min
  if (newVal > props.max) newVal = props.max
  quantity.value = newVal
  emit('update:modelValue', newVal)
}

const handleInputChange = (e) => {
  let newVal = parseInt(e.target.value) || props.min
  if (newVal < props.min) newVal = props.min
  if (newVal > props.max) newVal = props.max
  quantity.value = newVal
  emit('update:modelValue', newVal)
}
</script>

<style scoped>
.quantity-selector {
  display: flex;
  align-items: center;
  gap: 8px;
}
.quantity-input {
  width: 50px;
  height: 32px;
  text-align: center;
  border: 1px solid #ebedf0;
  border-radius: 4px;
  font-size: 14px;
  outline: none;
}
</style>