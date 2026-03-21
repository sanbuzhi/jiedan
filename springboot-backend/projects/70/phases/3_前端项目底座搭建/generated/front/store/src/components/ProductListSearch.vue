<template>
  <div class="product-list-search">
    <van-nav-bar
      title="商品列表"
      left-arrow
      @click-left="handleBack"
    >
      <template #right>
        <van-icon name="search" size="18" @click="showSearch = true" />
      </template>
    </van-nav-bar>
    <van-search
      v-model="searchText"
      v-show="showSearch"
      placeholder="搜索商品"
      shape="round"
      @search="handleSearch"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const showSearch = ref(false)
const searchText = ref(route.query.keyword || '')

watch(() => route.query.keyword, (val) => {
  searchText.value = val || ''
})

const handleBack = () => {
  router.back()
}

const handleSearch = (value) => {
  router.replace({
    query: { ...route.query, keyword: value.trim(), page: 1 }
  })
}
</script>

<style scoped lang="css">
.product-list-search {
  background-color: #fff;
}
</style>