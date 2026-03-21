<template>
  <div class="home">
    <van-nav-bar title="电商商城">
      <template #right>
        <van-icon name="search" size="20" @click="goSearch" />
      </template>
    </van-nav-bar>
    <van-swipe :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="(item, index) in bannerList" :key="index">
        <img :src="item" alt="banner" />
      </van-swipe-item>
    </van-swipe>
    <div class="category-section">
      <van-grid :column-num="5" :border="false">
        <van-grid-item v-for="(item, index) in categoryList" :key="index" :icon="item.icon" :text="item.text" @click="goCategory(item.text)" />
      </van-grid>
    </div>
    <div class="product-section">
      <div class="section-header">
        <span class="section-title">热门商品</span>
        <span class="section-more" @click="goProductList">更多 ></span>
      </div>
      <div class="product-grid">
        <ProductCard v-for="product in productList" :key="product.id" :product="product" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import ProductCard from '@/components/ProductCard.vue'
import { getProductList } from '@/api/product'

const router = useRouter()
const bannerList = ref([
  'https://fastly.jsdelivr.net/npm/@vant/assets/apple-1.jpeg',
  'https://fastly.jsdelivr.net/npm/@vant/assets/apple-2.jpeg'
])
const categoryList = ref([
  { icon: 'shopping-cart-o', text: '全部' },
  { icon: 'tv-o', text: '数码' },
  { icon: 'clothes-o', text: '服饰' },
  { icon: 'food-o', text: '美食' },
  { icon: 'flower-o', text: '美妆' }
])
const productList = ref([])

async function fetchProductList() {
  try {
    const res = await getProductList({ page: 1, pageSize: 10 })
    productList.value = res.data.list || []
  } catch (error) {
    console.error(error)
  }
}

function goSearch() {
  router.push({ name: 'ProductList' })
}

function goCategory(category) {
  router.push({ name: 'ProductList', query: { category } })
}

function goProductList() {
  router.push({ name: 'ProductList' })
}

onMounted(() => {
  fetchProductList()
})
</script>

<style scoped>
.home {
  width: 100%;
}

.van-swipe img {
  width: 100%;
  height: 200px;
  object-fit: cover;
}

.category-section {
  background-color: white;
  padding: 16px 0;
}

.product-section {
  margin-top: 10px;
  background-color: white;
  padding: 16px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.section-more {
  font-size: 14px;
  color: #999;
}

.product-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}
</style>