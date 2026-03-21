<template>
  <div class="product-detail">
    <PageHeader title="商品详情" :show-back="true" />
    <el-card v-loading="loading" class="detail-card">
      <template v-if="product.id">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品ID">{{ product.id }}</el-descriptions-item>
          <el-descriptions-item label="商品名称" :span="2">{{ product.name }}</el-descriptions-item>
          <el-descriptions-item label="商品分类">{{ product.categoryName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品状态">
            <StatusTag :status="product.status" />
          </el-descriptions-item>
          <el-descriptions-item label="商品价格" :span="2">
            <span class="price">¥{{ product.price?.toFixed(2) || '0.00' }}</span>
            <span class="original-price" v-if="product.originalPrice">¥{{ product.originalPrice.toFixed(2) }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="库存">{{ product.stock || 0 }}</el-descriptions-item>
          <el-descriptions-item label="销量">{{ product.sales || 0 }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ product.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="更新时间">{{ product.updateTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="商品主图" :span="2">
            <el-image
              v-if="product.mainImage"
              :src="product.mainImage"
              :preview-src-list="[product.mainImage]"
              fit="cover"
              style="width: 120px; height: 120px"
            />
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="商品轮播图" :span="2">
            <div class="image-list" v-if="product.images?.length">
              <el-image
                v-for="(img, idx) in product.images"
                :key="idx"
                :src="img"
                :preview-src-list="product.images"
                fit="cover"
                class="image-item"
              />
            </div>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="商品详情" :span="2">
            <div class="product-desc" v-html="product.description || '-'"></div>
          </el-descriptions-item>
        </el-descriptions>
        <div class="detail-footer">
          <el-button @click="goBack">返回</el-button>
          <el-button type="primary" @click="goEdit">编辑</el-button>
        </div>
      </template>
      <Empty v-else description="商品不存在或已被删除" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import StatusTag from '@/components/StatusTag.vue'
import Empty from '@/components/Empty.vue'
import { getProductDetail } from '@/api/product'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const product = ref({})

const loadProductDetail = async () => {
  loading.value = true
  try {
    const res = await getProductDetail(route.params.id)
    product.value = res.data || {}
  } catch (err) {
    ElMessage.error(err.message || '获取商品详情失败')
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.back()
}

const goEdit = () => {
  router.push(`/products/edit/${product.value.id}`)
}

onMounted(() => {
  if (route.params.id) {
    loadProductDetail()
  }
})
</script>

<style scoped>
.product-detail {
  padding: 20px;
}
.detail-card {
  margin-top: 16px;
}
.price {
  color: #f56c6c;
  font-size: 20px;
  font-weight: bold;
  margin-right: 12px;
}
.original-price {
  color: #909399;
  text-decoration: line-through;
}
.image-list {
  display: flex;
  gap: 12px;
}
.image-item {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  cursor: pointer;
}
.product-desc {
  max-height: 400px;
  overflow-y: auto;
}
.detail-footer {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>