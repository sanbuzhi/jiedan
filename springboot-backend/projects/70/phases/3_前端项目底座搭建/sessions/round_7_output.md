===FILE:front/admin/.gitignore===
```
# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
dist
dist-ssr
*.local

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
.DS_Store
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
```
===FILE:front/store/.gitignore===
```
# Logs
logs
*.log
npm-debug.log*
yarn-debug.log*
yarn-error.log*
pnpm-debug.log*
lerna-debug.log*

node_modules
dist
dist-ssr
*.local

# Editor directories and files
.vscode/*
!.vscode/extensions.json
.idea
.DS_Store
*.suo
*.ntvs*
*.njsproj
*.sln
*.sw?
```
===FILE:front/admin/src/components/ProductForm.vue===
```vue
<template>
  <el-dialog
    :title="isEdit ? '编辑产品' : '新增产品'"
    v-model="visible"
    width="700px"
    :close-on-click-modal="false"
    @close="handleClose"
  >
    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="产品名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入产品名称" />
      </el-form-item>
      <el-form-item label="产品分类" prop="categoryId">
        <el-select v-model="form.categoryId" placeholder="请选择产品分类" style="width: 100%">
          <el-option
            v-for="cat in categories"
            :key="cat.id"
            :label="cat.name"
            :value="cat.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="产品主图" prop="mainImage">
        <el-upload
          class="avatar-uploader"
          :action="uploadUrl"
          :headers="uploadHeaders"
          :show-file-list="false"
          :on-success="handleMainImageSuccess"
          :before-upload="beforeUpload"
        >
          <img v-if="form.mainImage" :src="form.mainImage" class="avatar" />
          <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="产品轮播图" prop="images">
        <el-upload
          :action="uploadUrl"
          :headers="uploadHeaders"
          list-type="picture-card"
          :limit="5"
          :on-success="handleImagesSuccess"
          :on-remove="handleImagesRemove"
          :before-upload="beforeUpload"
          :file-list="imageList"
        >
          <el-icon><Plus /></el-icon>
        </el-upload>
      </el-form-item>
      <el-form-item label="产品价格" prop="price">
        <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
      </el-form-item>
      <el-form-item label="库存数量" prop="stock">
        <el-input-number v-model="form.stock" :min="0" style="width: 100%" />
      </el-form-item>
      <el-form-item label="产品状态" prop="status">
        <el-radio-group v-model="form.status">
          <el-radio :label="1">上架</el-radio>
          <el-radio :label="0">下架</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="产品描述" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          :rows="4"
          placeholder="请输入产品描述"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">
          确定
        </el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  productData: {
    type: Object,
    default: null
  },
  categories: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:visible', 'refresh'])

const formRef = ref(null)
const submitLoading = ref(false)
const imageList = ref([])

const uploadUrl = computed(() => import.meta.env.VITE_API_BASE_URL + '/api/upload')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const isEdit = computed(() => !!props.productData?.id)

const defaultForm = {
  name: '',
  categoryId: null,
  mainImage: '',
  images: [],
  price: 0,
  stock: 0,
  status: 1,
  description: ''
}

const form = reactive({ ...defaultForm })

const rules = {
  name: [{ required: true, message: '请输入产品名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择产品分类', trigger: 'change' }],
  mainImage: [{ required: true, message: '请上传产品主图', trigger: 'change' }],
  price: [{ required: true, message: '请输入产品价格', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存数量', trigger: 'blur' }]
}

const resetForm = () => {
  Object.assign(form, defaultForm)
  imageList.value = []
  formRef.value?.resetFields()
}

const handleClose = () => {
  resetForm()
  emit('update:visible', false)
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB!')
    return false
  }
  return true
}

const handleMainImageSuccess = (response) => {
  if (response.code === 200) {
    form.mainImage = response.data.url
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleImagesSuccess = (response, file, fileList) => {
  if (response.code === 200) {
    const urls = fileList.map((f) => f.url || f.response?.data?.url)
    form.images = urls.filter(Boolean)
    imageList.value = fileList
  } else {
    ElMessage.error(response.message || '上传失败')
  }
}

const handleImagesRemove = (file, fileList) => {
  const urls = fileList.map((f) => f.url || f.response?.data?.url)
  form.images = urls.filter(Boolean)
  imageList.value = fileList
}

const handleSubmit = async () => {
  await formRef.value?.validate()
  // 这里调用父组件传递的API或者通过emit通知父组件
  submitLoading.value = true
  try {
    emit('refresh', { ...form, id: props.productData?.id })
    handleClose()
  } catch (error) {
    console.error(error)
  } finally {
    submitLoading.value = false
  }
}

watch(
  () => props.productData,
  (val) => {
    if (val) {
      Object.assign(form, {
        name: val.name || '',
        categoryId: val.categoryId || null,
        mainImage: val.mainImage || '',
        images: val.images || [],
        price: val.price || 0,
        stock: val.stock || 0,
        status: val.status ?? 1,
        description: val.description || ''
      })
      imageList.value = (val.images || []).map((url) => ({ url }))
    }
  },
  { immediate: true }
)
</script>

<style scoped>
.avatar-uploader .avatar {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
  border-radius: 4px;
}

.avatar-uploader :deep(.el-upload) {
  border: 1px dashed var(--el-border-color);
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: var(--el-transition-duration-fast);
}

.avatar-uploader :deep(.el-upload:hover) {
  border-color: var(--el-color-primary);
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
}
</style>
```
===FILE:front/store/src/components/ProductSkuSelector.vue===
```vue
<template>
  <div class="sku-selector">
    <div v-for="(item, index) in skuList" :key="index" class="sku-item">
      <div class="sku-label">{{ item.name }}</div>
      <div class="sku-values">
        <van-button
          v-for="val in item.values"
          :key="val"
          size="small"
          :type="currentSku[index] === val ? 'primary' : 'default'"
          @click="selectSku(index, val)"
        >
          {{ val }}
        </van-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  skus: {
    type: Array,
    required: true,
    default: () => []
  },
  defaultSku: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['change'])

// 解析sku列表，提取规格名称和可选值
const skuList = computed(() => {
  if (!props.skus.length) return []
  const specsMap = new Map()
  props.skus.forEach((sku) => {
    const specs = sku.specs || []
    specs.forEach((spec) => {
      if (!specsMap.has(spec.name)) {
        specsMap.set(spec.name, new Set())
      }
      specsMap.get(spec.name).add(spec.value)
    })
  })
  return Array.from(specsMap.entries()).map(([name, values]) => ({
    name,
    values: Array.from(values)
  }))
})

// 当前选中的规格值数组
const currentSku = ref([])

// 初始化默认选中规格
const initDefaultSku = () => {
  if (props.defaultSku?.specs) {
    currentSku.value = props.defaultSku.specs.map((s) => s.value)
  } else if (skuList.value.length) {
    currentSku.value = skuList.value.map((item) => item.values[0])
  }
  emitChange()
}

// 选中规格
const selectSku = (index, value) => {
  currentSku.value[index] = value
  emitChange()
}

// 找到匹配的sku数据
const findMatchedSku = () => {
  if (!currentSku.value.length || !props.skus.length) return null
  return props.skus.find((sku) => {
    const specs = sku.specs || []
    if (specs.length !== currentSku.value.length) return false
    return specs.every((spec, i) => spec.value === currentSku.value[i])
  })
}

// 触发change事件
const emitChange = () => {
  const matchedSku = findMatchedSku()
  emit('change', {
    specs: currentSku.value,
    sku: matchedSku
  })
}

watch(() => props.skus, initDefaultSku, { immediate: true, deep: true })
watch(() => props.defaultSku, initDefaultSku, { immediate: true, deep: true })
</script>

<style scoped>
.sku-selector {
  padding: 12px 0;
}

.sku-item {
  margin-bottom: 16px;
}

.sku-item:last-child {
  margin-bottom: 0;
}

.sku-label {
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin-bottom: 10px;
}

.sku-values {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
</style>
```
===FILE:front/admin/src/views/OrderDetail.vue===
```vue
<template>
  <div class="order-detail">
    <PageHeader title="订单详情" :show-back="true" />
    <el-card v-loading="loading" class="order-card">
      <template v-if="orderInfo.id">
        <!-- 订单基本信息 -->
        <div class="order-section">
          <h4>订单信息</h4>
          <el-descriptions :column="2" border>
            <el-descriptions-item label="订单编号">{{ orderInfo.orderNo }}</el-descriptions-item>
            <el-descriptions-item label="订单状态">
              <el-tag :type="getStatusType(orderInfo.status)">{{ getStatusText(orderInfo.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="下单时间">{{ formatTime(orderInfo.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="支付时间">{{ formatTime(orderInfo.paidAt) }}</el-descriptions-item>
            <el-descriptions-item label="发货时间">{{ formatTime(orderInfo.shippedAt) }}</el-descriptions-item>
            <el-descriptions-item label="完成时间">{{ formatTime(orderInfo.completedAt) }}</el-descriptions-item>
            <el-descriptions-item label="订单金额" :span="2">
              <span class="price">¥{{ orderInfo.totalAmount?.toFixed(2) }}</span>
            </el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 收货地址 -->
        <div class="order-section">
          <h4>收货地址</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="收货人">{{ orderInfo.address?.name }}</el-descriptions-item>
            <el-descriptions-item label="联系电话">{{ orderInfo.address?.phone }}</el-descriptions-item>
            <el-descriptions-item label="收货地址">{{ orderInfo.address?.fullAddress }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 商品列表 -->
        <div class="order-section">
          <h4>商品信息</h4>
          <el-table :data="orderInfo.items" border>
            <el-table-column prop="productName" label="商品名称" />
            <el-table-column prop="productImage" label="商品图片" width="100">
              <template #default="{ row }">
                <el-image
                  :src="row.productImage"
                  fit="cover"
                  style="width: 60px; height: 60px"
                  :preview-src-list="[row.productImage]"
                />
              </template>
            </el-table-column>
            <el-table-column prop="specs" label="商品规格" width="150" />
            <el-table-column prop="price" label="单价" width="120">
              <template #default="{ row }">
                ¥{{ row.price?.toFixed(2) }}
              </template>
            </el-table-column>
            <el-table-column prop="quantity" label="数量" width="100" />
            <el-table-column prop="subtotal" label="小计" width="120">
              <template #default="{ row }">
                ¥{{ row.subtotal?.toFixed(2) }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 操作按钮 -->
        <div class="order-actions">
          <el-button @click="$router.back()">返回</el-button>
          <template v-if="orderInfo.status === 1">
            <el-button type="primary" @click="handleShip">发货</el-button>
          </template>
        </div>
      </template>
      <template v-else>
        <el-empty description="订单不存在" />
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/PageHeader.vue'
import { getOrderDetail, updateOrderStatus } from '@/api/order'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orderInfo = ref({})

const getStatusType = (status) => {
  const map = {
    0: 'info',    // 待支付
    1: 'warning', // 待发货
    2: 'primary', // 待收货
    3: 'success', // 已完成
    4: 'danger'   // 已取消
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200) {
      orderInfo.value = res.data
    } else {
      ElMessage.error(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleShip = async () => {
  try {
    await ElMessageBox.confirm('确认发货吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await updateOrderStatus(orderInfo.value.id, 2)
    if (res.code === 200) {
      ElMessage.success('发货成功')
      fetchOrderDetail()
    } else {
      ElMessage.error(res.message || '发货失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error(error)
    }
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail {
  padding: 20px;
}

.order-card {
  margin-top: 20px;
}

.order-section {
  margin-bottom: 24px;
}

.order-section h4 {
  margin-bottom: 12px;
  color: #333;
}

.price {
  font-size: 20px;
  font-weight: bold;
  color: #f56c6c;
}

.order-actions {
  margin-top: 24px;
  text-align: right;
}
</style>
```
===FILE:front/store/src/views/OrderDetail.vue===
```vue
<template>
  <div class="order-detail">
    <van-nav-bar title="订单详情" left-arrow @click-left="$router.back()" />
    
    <div v-loading="loading" class="content">
      <template v-if="orderInfo.id">
        <!-- 订单状态 -->
        <div class="status-card">
          <van-icon :name="getStatusIcon(orderInfo.status)" size="48" :color="getStatusColor(orderInfo.status)" />
          <div class="status-text" :style="{ color: getStatusColor(orderInfo.status) }">
            {{ getStatusText(orderInfo.status) }}
          </div>
        </div>

        <!-- 收货地址 -->
        <div class="section">
          <van-cell-group inset>
            <van-cell title="收货人" :value="orderInfo.address?.name" />
            <van-cell title="联系电话" :value="orderInfo.address?.phone" />
            <van-cell title="收货地址" :value="orderInfo.address?.fullAddress" />
          </van-cell-group>
        </div>

        <!-- 商品列表 -->
        <div class="section">
          <van-cell-group inset>
            <van-cell title="商品信息" />
            <div v-for="item in orderInfo.items" :key="item.id" class="product-item">
              <img :src="item.productImage" alt="" class="product-img" />
              <div class="product-info">
                <div class="product-name">{{ item.productName }}</div>
                <div class="product-specs">{{ item.specs }}</div>
                <div class="product-bottom">
                  <span class="price">¥{{ item.price?.toFixed(2) }}</span>
                  <span class="quantity">x{{ item.quantity }}</span>
                </div>
              </div>
            </div>
            <van-cell title="订单金额" :value="`¥${orderInfo.totalAmount?.toFixed(2)}`" />
          </van-cell-group>
        </div>

        <!-- 订单信息 -->
        <div class="section">
          <van-cell-group inset>
            <van-cell title="订单编号" :value="orderInfo.orderNo" />
            <van-cell title="下单时间" :value="formatTime(orderInfo.createdAt)" />
          </van-cell-group>
        </div>

        <!-- 底部操作栏 -->
        <div class="bottom-actions">
          <template v-if="orderInfo.status === 0">
            <van-button type="danger" size="small" @click="handleCancel">取消订单</van-button>
            <van-button type="primary" size="small" @click="handlePay">立即支付</van-button>
          </template>
          <template v-else-if="orderInfo.status === 2">
            <van-button type="primary" size="small" @click="handleConfirm">确认收货</van-button>
          </template>
        </div>
      </template>
      <template v-else>
        <van-empty description="订单不存在" />
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { getOrderDetail, cancelOrder, confirmOrder, payOrder } from '@/api/order'
import dayjs from 'dayjs'

const route = useRoute()
const router = useRouter()
const loading = ref(false)
const orderInfo = ref({})

const getStatusIcon = (status) => {
  const map = {
    0: 'clock-o',
    1: 'logistics',
    2: 'description',
    3: 'checked',
    4: 'cross'
  }
  return map[status] || 'info-o'
}

const getStatusColor = (status) => {
  const map = {
    0: '#ff976a',
    1: '#1989fa',
    2: '#07c160',
    3: '#07c160',
    4: '#969799'
  }
  return map[status] || '#969799'
}

const getStatusText = (status) => {
  const map = {
    0: '待支付',
    1: '待发货',
    2: '待收货',
    3: '已完成',
    4: '已取消'
  }
  return map[status] || '未知'
}

const formatTime = (time) => {
  return time ? dayjs(time).format('YYYY-MM-DD HH:mm:ss') : '-'
}

const fetchOrderDetail = async () => {
  loading.value = true
  try {
    const res = await getOrderDetail(route.params.id)
    if (res.code === 200) {
      orderInfo.value = res.data
    } else {
      showToast(res.message || '获取订单详情失败')
    }
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

const handleCancel = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认取消订单吗？'
    })
    const res = await cancelOrder(orderInfo.value.id)
    if (res.code === 200) {
      showToast('取消成功')
      fetchOrderDetail()
    } else {
      showToast(res.message || '取消失败')
    }
  } catch (error) {
    // 取消操作
  }
}

const handlePay = async () => {
  try {
    const res = await payOrder(orderInfo.value.id)
    if (res.code === 200) {
      showToast('支付成功')
      fetchOrderDetail()
    } else {
      showToast(res.message || '支付失败')
    }
  } catch (error) {
    console.error(error)
  }
}

const handleConfirm = async () => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认收货吗？'
    })
    const res = await confirmOrder(orderInfo.value.id)
    if (res.code === 200) {
      showToast('确认成功')
      fetchOrderDetail()
    } else {
      showToast(res.message || '确认失败')
    }
  } catch (error) {
    // 取消操作
  }
}

onMounted(() => {
  fetchOrderDetail()
})
</script>

<style scoped>
.order-detail {
  min-height: 100vh;
  background-color: #f7f8fa;
  padding-bottom: 80px;
}

.content {
  padding: 12px 0;
}

.status-card {
  background-color: #fff;
  padding: 30px 0;
  text-align: center;
  margin-bottom: 12px;
}

.status-text {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 500;
}

.section {
  margin-bottom: 12px;
}

.product-item {
  display: flex;
  padding: 12px;
  gap: 12px;
}

.product-img {
  width: 80px;
  height: 80px;
  border-radius: 4px;
  object-fit: cover;
  flex-shrink: 0;
}

.product-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.product-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.product-specs {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.product-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.price {
  font-size: 16px;
  font-weight: bold;
  color: #f56c6c;
}

.quantity {
  font-size: 14px;
  color: #969799;
}

.bottom-actions {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  background-color: #fff;
  padding: 12px 16px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}
</style>
```