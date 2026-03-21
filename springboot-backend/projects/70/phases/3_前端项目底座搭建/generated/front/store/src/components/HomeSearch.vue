<template>
  <div class="home-search">
    <van-search
      v-model="searchText"
      placeholder="搜索商品"
      shape="round"
      background="#fff"
      @search="handleSearch"
      @cancel="handleCancel"
      show-action
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchText = ref('')

const handleSearch = (value) => {
  router.push({
    name: 'ProductList',
    query: { keyword: value.trim() }
  })
}

const handleCancel = () => {
  searchText.value = ''
}
</script>

<style scoped lang="css">
.home-search {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
}
</style>