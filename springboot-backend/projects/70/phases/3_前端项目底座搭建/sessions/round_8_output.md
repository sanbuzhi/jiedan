===FILE:front/admin/src/components/StatusTag.vue===
```vue
<template>
  <el-tag :type="tagType" :effect="effect" size="small">
    {{ text }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: [String, Number],
    required: true
  },
  type: {
    type: String,
    default: ''
  },
  map: {
    type: Object,
    default: () => ({
      0: { text: '下架', type: 'info' },
      1: { text: '上架', type: 'success' },
      2: { text: '待付款', type: 'warning' },
      3: { text: '待发货', type: 'primary' },
      4: { text: '已发货', type: '' },
      5: { text: '已完成', type: 'success' },
      6: { text: '已取消', type: 'danger' }
    })
  },
  effect: {
    type: String,
    default: 'light'
  }
})

const tagType = computed(() => props.type || props.map[props.status]?.type || 'info')
const text = computed(() => props.map[props.status]?.text || props.status)
</script>
```
===FILE:front/admin/src/components/ConfirmDialog.vue===
```vue
<template>
  <el-dialog
    :title="title"
    v-model="visible"
    width="420px"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
  >
    <div class="confirm-content">
      <el-icon v-if="icon" :size="48" :color="iconColor" class="confirm-icon">
        <component :is="icon" />
      </el-icon>
      <span>{{ content }}</span>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleCancel">取消</el-button>
        <el-button type="primary" :loading="loading" @click="handleConfirm">确定</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, watch } from 'vue'
import { Warning, CircleCheck } from '@element-plus/icons-vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: '提示'
  },
  content: {
    type: String,
    default: '确定要执行此操作吗？'
  },
  loading: {
    type: Boolean,
    default: false
  },
  type: {
    type: String,
    default: 'warning', // warning, success
    validator: (val) => ['warning', 'success'].includes(val)
  }
})

const emit = defineEmits(['update:modelValue', 'confirm', 'cancel'])

const visible = ref(props.modelValue)
const iconMap = {
  warning: Warning,
  success: CircleCheck
}
const iconColorMap = {
  warning: '#E6A23C',
  success: '#67C23A'
}

const icon = computed(() => iconMap[props.type])
const iconColor = computed(() => iconColorMap[props.type])

watch(() => props.modelValue, (val) => {
  visible.value = val
})

watch(visible, (val) => {
  emit('update:modelValue', val)
})

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const handleConfirm = () => {
  emit('confirm')
}
</script>

<style scoped>
.confirm-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px 0;
  font-size: 15px;
}
.confirm-icon {
  flex-shrink: 0;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
```
===FILE:front/admin/src/components/Empty.vue===
```vue
<template>
  <el-empty :description="description" :image="image">
    <template #image v-if="imageUrl">
      <img :src="imageUrl" alt="empty" />
    </template>
    <template #default v-if="$slots.default">
      <slot />
    </template>
  </el-empty>
</template>

<script setup>
const props = defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  image: {
    type: String,
    default: ''
  },
  imageUrl: {
    type: String,
    default: ''
  }
})
</script>
```
===FILE:front/admin/src/views/ProductDetail.vue===
```vue
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
```
===FILE:front/store/src/components/Empty.vue===
```vue
<template>
  <van-empty :description="description" v-bind="$attrs">
    <template #image v-if="imageUrl">
      <img :src="imageUrl" alt="empty" />
    </template>
    <template #default v-if="$slots.default">
      <slot />
    </template>
  </van-empty>
</template>

<script setup>
const props = defineProps({
  description: {
    type: String,
    default: '暂无数据'
  },
  imageUrl: {
    type: String,
    default: ''
  }
})
</script>
```