===FILE:front/admin/src/views/ProductList.vue===
```vue
<template>
  <div class="product-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品列表</span>
          <el-button type="primary">新增商品</el-button>
        </div>
      </template>
      <el-table :data="[]" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="商品名称" />
        <el-table-column prop="price" label="价格" />
        <el-table-column prop="stock" label="库存" />
        <el-table-column prop="status" label="状态">
          <template #default>
            <el-tag type="success">上架</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button type="primary" link>编辑</el-button>
            <el-button type="danger" link>删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 20px; justify-content: flex-end"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="100"
      />
    </el-card>
  </div>
</template>

<script setup>
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```
===FILE:front/admin/src/views/OrderList.vue===
```vue
<template>
  <div class="order-list-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>订单列表</span>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="订单号">
          <el-input v-model="searchForm.orderNo" placeholder="请输入订单号" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态">
            <el-option label="全部" value="" />
            <el-option label="待付款" value="pending" />
            <el-option label="已付款" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary">查询</el-button>
          <el-button>重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="[]" stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="订单号" />
        <el-table-column prop="userName" label="下单人" />
        <el-table-column prop="totalAmount" label="总金额" />
        <el-table-column prop="status" label="状态">
          <template #default>
            <el-tag type="warning">待付款</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" />
        <el-table-column label="操作" width="200">
          <template #default>
            <el-button type="primary" link>详情</el-button>
            <el-button type="info" link>发货</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination
        style="margin-top: 20px; justify-content: flex-end"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="100"
      />
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const searchForm = reactive({
  orderNo: '',
  status: ''
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.search-form {
  margin-bottom: 20px;
}
</style>
```
===FILE:front/admin/src/components/PageHeader.vue===
```vue
<template>
  <div class="page-header">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item v-if="title" :to="currentPath">{{ title }}</el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

defineProps({
  title: String
})

const route = useRoute()
const currentPath = computed(() => route.path)
</script>

<style scoped>
.page-header {
  margin-bottom: 20px;
  padding: 10px 0;
}
</style>
```
===FILE:front/store/src/views/ProductList.vue===
```vue
<template>
  <div class="product-list-container">
    <van-nav-bar title="商品列表" />
    <van-tabs v-model:active="activeTab">
      <van-tab title="全部" name="all" />
      <van-tab title="电子产品" name="electronics" />
      <van-tab title="服装鞋包" name="clothing" />
    </van-tabs>
    <div class="product-grid">
      <van-grid :gutter="10">
        <van-grid-item v-for="i in 10" :key="i">
          <van-card
            title="示例商品"
            desc="商品描述"
            price="99.00"
            :thumb="`https://picsum.photos/200/200?random=${i}`"
          >
            <template #footer>
              <van-button size="small" type="primary">加入购物车</van-button>
              <van-button size="small" type="default">立即购买</van-button>
            </template>
          </van-card>
        </van-grid-item>
      </van-grid>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('all')
</script>

<style scoped>
.product-grid {
  padding: 10px;
}
</style>
```
===FILE:front/store/src/views/ProductDetail.vue===
```vue
<template>
  <div class="product-detail-container">
    <van-nav-bar
      title="商品详情"
      left-text="返回"
      left-arrow
      @click-left="goBack"
    />
    <van-swipe :autoplay="3000" indicator-color="white">
      <van-swipe-item v-for="i in 3" :key="i">
        <img :src="`https://picsum.photos/750/400?random=${i}`" alt="商品图片" />
      </van-swipe-item>
    </van-swipe>
    <div class="product-info">
      <van-goods-action>
        <van-goods-action-icon icon="chat-o" text="客服" />
        <van-goods-action-icon icon="cart-o" text="购物车" :badge="5" />
        <van-goods-action-button type="warning" text="加入购物车" />
        <van-goods-action-button type="danger" text="立即购买" />
      </van-goods-action>
      <div class="price">
        <span class="current">¥99.00</span>
        <span class="original">¥199.00</span>
      </div>
      <h3 class="title">示例商品名称示例商品名称示例商品名称</h3>
      <p class="desc">示例商品描述示例商品描述示例商品描述示例商品描述</p>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

const goBack = () => {
  router.back()
}
</script>

<style scoped>
.product-info {
  background-color: white;
  padding: 16px;
  position: relative;
  z-index: 1;
}
.price {
  margin-bottom: 12px;
}
.current {
  font-size: 24px;
  color: #ee0a24;
  font-weight: bold;
}
.original {
  font-size: 14px;
  color: #969799;
  text-decoration: line-through;
  margin-left: 8px;
}
.title {
  font-size: 18px;
  color: #333;
  margin-bottom: 12px;
}
.desc {
  font-size: 14px;
  color: #666;
  line-height: 1.6;
}
</style>
```
===FILE:front/store/src/views/Checkout.vue===
```vue
<template>
  <div class="checkout-container">
    <van-nav-bar
      title="确认订单"
      left-text="返回"
      left-arrow
      @click-left="goBack"
    />
    <div class="content">
      <van-cell-group inset>
        <van-cell title="收货人" value="张三" is-link />
        <van-cell title="联系电话" value="13800138000" />
        <van-cell title="收货地址" value="北京市朝阳区xxx街道xxx号" />
      </van-cell-group>
      <div class="section-title">商品清单</div>
      <van-cell-group inset>
        <van-cell v-for="i in 2" :key="i">
          <div class="product-item">
            <img :src="`https://picsum.photos/100/100?random=${i}`" alt="商品图片" />
            <div class="product-info">
              <h4>示例商品名称</h4>
              <p>商品规格</p>
              <div class="price-row">
                <span class="price">¥99.00</span>
                <span class="count">x1</span>
              </div>
            </div>
          </div>
        </van-cell>
      </van-cell-group>
      <van-cell-group inset>
        <van-cell title="商品金额" value="¥198.00" />
        <van-cell title="运费" value="¥0.00" />
        <van-cell title="优惠券" value="-¥10.00" />
        <van-cell title="实付金额" :value="totalAmount" />
      </van-cell-group>
    </div>
    <van-submit-bar :price="18800" button-text="提交订单" @submit="onSubmit" />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const totalAmount = ref('¥188.00')

const goBack = () => {
  router.back()
}

const onSubmit = () => {
  alert('订单提交成功！')
  router.push('/user')
}
</script>

<style scoped>
.content {
  padding-bottom: 50px;
}
.section-title {
  padding: 16px;
  font-size: 16px;
  color: #333;
}
.product-item {
  display: flex;
  gap: 12px;
}
.product-item img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
}
.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}
.product-info h4 {
  font-size: 14px;
  color: #333;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.product-info p {
  font-size: 12px;
  color: #969799;
  margin: 4px 0;
}
.price-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.price {
  font-size: 16px;
  color: #ee0a24;
  font-weight: bold;
}
.count {
  font-size: 14px;
  color: #666;
}
</style>
```