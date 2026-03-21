<template>
  <div class="product-list">
    <van-nav-bar title="商品列表" left-arrow />
    <van-search v-model="keyword" placeholder="搜索商品" shape="round" @search="handleSearch" />
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-list v-model:loading="loading" :finished="finished" finished-text="没有更多了" @load="onLoad">
        <div class="product-grid">
          <ProductCard v-for="product in productList" :key="product.id" :product="product" />
        </div>
      </van-list>
    </van-pull-refresh>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { getProductList } from '@/api/product'

const route = useRoute()
const keyword = ref('')
const productList = ref([])
const loading = ref(false)
const refreshing = ref(false)
const finished = ref(false)

const queryForm = reactive({
  keyword: '',
  category: '',
  page: 1,
  pageSize: 10
})

async function fetchProductList() {
  try {
    const res = await getProductList(queryForm)
    if (queryForm.page === 1) {
      productList.value = res.data.list || []
    } else {
      productList.value.push(...(res.data.list || []))
    }
    finished.value = productList.value.length >= (res.data.total || 0)
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
    refreshing.value = false
  }
}

function onLoad() {
  fetchProductList()
  queryForm.page++
}

function onRefresh() {
  queryForm.page = 1
  finished.value = false
  fetchProductList()
}

function handleSearch() {
  queryForm.keyword = keyword.value
  queryForm.page = 1
  finished.value = false
  productList.value = []
  onLoad()
}

onMounted(() => {
  queryForm.category = route.query.category || ''
  queryForm.keyword = route.query.keyword || ''
  keyword.value = queryForm.keyword
  onLoad()
})
</script>

<style scoped>
.product-list {
  width: 100%;
  min-height: 100%;
  background-color: #f5f5f5;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
  padding: 12px;
}
</style>