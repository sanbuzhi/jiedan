<template>
  <el-row :gutter="20" class="checkout-page">
    <el-col :span="16">
      <el-card class="product-card">
        <template #header>
          <div class="card-header">
            <span>商品列表</span>
          </div>
        </template>
        <el-empty description="收银台功能待完善" />
      </el-card>
    </el-col>
    <el-col :span="8">
      <el-card class="cart-card">
        <template #header>
          <div class="card-header">
            <span>购物车</span>
          </div>
        </template>
        <el-empty description="暂无商品" />
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
// 收银台待完善核心功能
</script>

<style scoped lang="scss">
@import '@/styles/variables.scss';

.checkout-page {
  height: 100%;

  .product-card, .cart-card {
    height: 100%;

    .card-header {
      font-weight: bold;
      font-size: 16px;
    }

    :deep(.el-card__body) {
      height: calc(100% - 57px);
      overflow-y: auto;
    }
  }
}
</style>