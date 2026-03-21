<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleChange"
      @current-change="handleChange"
    />
  </div>
</template>

<script setup>
import { watch } from 'vue'

const props = defineProps({
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:currentPage', 'update:pageSize', 'change'])

function handleChange() {
  emit('update:currentPage', props.currentPage)
  emit('update:pageSize', props.pageSize)
  emit('change')
}

watch(() => props.total, () => {
  if (props.currentPage > Math.ceil(props.total / props.pageSize)) {
    emit('update:currentPage', 1)
  }
})
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}
</style>