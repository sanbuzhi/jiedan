===FILE:front/admin/index.html===
```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <link rel="icon" href="/favicon.ico">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>电商后台管理系统</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```
===FILE:front/admin/src/assets/index.css===
```css
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
}

a {
  text-decoration: none;
  color: inherit;
}

ul, ol {
  list-style: none;
}

/* 工具类 */
.mt-10 { margin-top: 10px; }
.mt-20 { margin-top: 20px; }
.mb-10 { margin-bottom: 10px; }
.mb-20 { margin-bottom: 20px; }
.ml-10 { margin-left: 10px; }
.mr-10 { margin-right: 10px; }
.text-center { text-align: center; }
.text-right { text-align: right; }
```
===FILE:front/admin/src/components/Pagination.vue===
```vue
<template>
  <div class="pagination-container">
    <el-pagination
      v-model:current-page="currentPage"
      v-model:page-size="pageSize"
      :page-sizes="[10, 20, 50, 100]"
      :total="total"
      layout="total, sizes, prev, pager, next, jumper"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  page: {
    type: Number,
    default: 1
  },
  size: {
    type: Number,
    default: 10
  },
  total: {
    type: Number,
    default: 0
  }
})

const emit = defineEmits(['update:page', 'update:size', 'change'])

const currentPage = computed({
  get: () => props.page,
  set: (val) => emit('update:page', val)
})

const pageSize = computed({
  get: () => props.size,
  set: (val) => emit('update:size', val)
})

const handleSizeChange = (val) => {
  emit('change', { page: 1, size: val })
}

const handleCurrentChange = (val) => {
  emit('change', { page: val, size: props.size })
}
</script>

<style scoped>
.pagination-container {
  display: flex;
  justify-content: flex-end;
  padding: 20px 0;
}
</style>
```
===FILE:front/store/index.html===
```html
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <link rel="icon" href="/favicon.ico">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
  <title>电商前台商城</title>
</head>
<body>
  <div id="app"></div>
  <script type="module" src="/src/main.js"></script>
</body>
</html>
```
===FILE:front/store/src/assets/index.css===
```css
/* 全局样式重置 */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  -webkit-tap-highlight-color: transparent;
}

html, body, #app {
  width: 100%;
  height: 100%;
  background-color: #f7f8fa;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', Arial, sans-serif;
  overflow-x: hidden;
}

a {
  text-decoration: none;
  color: inherit;
}

img {
  display: block;
  max-width: 100%;
}

/* 固定底部按钮 */
.fixed-bottom-btn {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 20px;
  padding-bottom: calc(10px + env(safe-area-inset-bottom));
  background-color: #fff;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
}
```
===FILE:front/store/src/components/ProductCard.vue===
```vue
<template>
  <div class="product-card" @click="goDetail">
    <div class="product-image">
      <img :src="product.image || '/placeholder.png'" :alt="product.name" />
      <div v-if="product.stock <= 10" class="stock-tag">仅剩{{ product.stock }}件</div>
    </div>
    <div class="product-info">
      <div class="product-name ellipsis-2">{{ product.name }}</div>
      <div class="product-price">
        <span class="price-symbol">¥</span>
        <span class="price-value">{{ product.price }}</span>
        <span v-if="product.originalPrice" class="price-original">¥{{ product.originalPrice }}</span>
      </div>
      <div class="product-sales">已售{{ product.sales }}件</div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const props = defineProps({
  product: {
    type: Object,
    required: true
  }
})

const router = useRouter()

const goDetail = () => {
  router.push(`/product/${props.product.id}`)
}
</script>

<style scoped>
.product-card {
  background-color: #fff;
  border-radius: 8px;
  overflow: hidden;
  padding-bottom: 10px;
}

.product-image {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}

.product-card:hover .product-image img {
  transform: scale(1.05);
}

.stock-tag {
  position: absolute;
  top: 8px;
  right: 8px;
  background-color: #ff976a;
  color: #fff;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.product-info {
  padding: 8px;
}

.product-name {
  font-size: 14px;
  color: #333;
  line-height: 1.4;
  margin-bottom: 6px;
}

.ellipsis-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.product-price {
  display: flex;
  align-items: baseline;
  margin-bottom: 4px;
}

.price-symbol {
  font-size: 12px;
  color: #ee0a24;
  margin-right: 2px;
}

.price-value {
  font-size: 18px;
  font-weight: bold;
  color: #ee0a24;
}

.price-original {
  font-size: 12px;
  color: #999;
  text-decoration: line-through;
  margin-left: 6px;
}

.product-sales {
  font-size: 12px;
  color: #999;
}
</style>
```