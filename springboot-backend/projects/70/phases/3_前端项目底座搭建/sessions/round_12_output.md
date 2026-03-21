===FILE:front/admin/src/utils/validate.js===
```javascript
/**
 * 表单验证工具
 */

// 手机号验证
export const validatePhone = (rule, value, callback) => {
  const reg = /^1[3-9]\d{9}$/
  if (!value) {
    callback(new Error('请输入手机号'))
  } else if (!reg.test(value)) {
    callback(new Error('请输入正确的手机号'))
  } else {
    callback()
  }
}

// 密码验证（6-20位字母数字下划线）
export const validatePassword = (rule, value, callback) => {
  const reg = /^\w{6,20}$/
  if (!value) {
    callback(new Error('请输入密码'))
  } else if (!reg.test(value)) {
    callback(new Error('密码需为6-20位字母、数字或下划线'))
  } else {
    callback()
  }
}

// 用户名验证（4-16位字母数字下划线，不能纯数字）
export const validateUsername = (rule, value, callback) => {
  const reg = /^(?!\d+$)[a-zA-Z0-9_]{4,16}$/
  if (!value) {
    callback(new Error('请输入用户名'))
  } else if (!reg.test(value)) {
    callback(new Error('用户名需为4-16位字母、数字或下划线，且不能纯数字'))
  } else {
    callback()
  }
}

// 商品/分类名称验证（1-50位字符，不能包含特殊符号）
export const validateName = (rule, value, callback) => {
  const reg = /^[\u4e00-\u9fa5a-zA-Z0-9\s\-_]{1,50}$/
  if (!value?.trim()) {
    callback(new Error('请输入名称'))
  } else if (!reg.test(value.trim())) {
    callback(new Error('名称不能包含特殊符号，长度1-50位'))
  } else {
    callback()
  }
}

// 商品价格验证（大于0的数字，最多两位小数）
export const validatePrice = (rule, value, callback) => {
  const num = Number(value)
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入价格'))
  } else if (isNaN(num) || num <= 0) {
    callback(new Error('价格必须大于0'))
  } else if (!/^\d+(\.\d{1,2})?$/.test(value)) {
    callback(new Error('价格最多保留两位小数'))
  } else {
    callback()
  }
}

// 商品库存验证（非负整数）
export const validateStock = (rule, value, callback) => {
  const num = Number(value)
  if (value === '' || value === null || value === undefined) {
    callback(new Error('请输入库存'))
  } else if (isNaN(num) || num < 0 || !Number.isInteger(num)) {
    callback(new Error('库存必须为非负整数'))
  } else {
    callback()
  }
}

// 必填验证
export const validateRequired = (message = '此项为必填项') => {
  return (rule, value, callback) => {
    if (value === '' || value === null || value === undefined || (Array.isArray(value) && value.length === 0)) {
      callback(new Error(message))
    } else {
      callback()
    }
  }
}
```
===FILE:front/admin/src/utils/constants.js===
```javascript
/**
 * 系统常量定义
 */

// 产品状态
export const PRODUCT_STATUS = {
  ON_SALE: 1,
  OFF_SALE: 0,
  labels: {
    1: '上架',
    0: '下架'
  },
  types: {
    1: 'success',
    0: 'info'
  }
}

// 订单状态
export const ORDER_STATUS = {
  UNPAID: 0,
  PAID: 1,
  SHIPPED: 2,
  DELIVERED: 3,
  CANCELLED: 4,
  REFUNDED: 5,
  labels: {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  },
  types: {
    0: 'warning',
    1: 'primary',
    2: 'info',
    3: 'success',
    4: 'danger',
    5: 'warning'
  }
}

// 用户角色
export const USER_ROLE = {
  ADMIN: 1,
  NORMAL: 0,
  labels: {
    1: '管理员',
    0: '普通用户'
  }
}

// 分页默认值
export const DEFAULT_PAGE = {
  pageNum: 1,
  pageSize: 10
}
```
===FILE:front/admin/src/components/Uploader.vue===
```vue
<template>
  <el-upload
    :action="uploadUrl"
    :headers="uploadHeaders"
    :list-type="listType"
    :multiple="multiple"
    :limit="limit"
    :file-list="fileList"
    :on-success="handleSuccess"
    :on-remove="handleRemove"
    :on-exceed="handleExceed"
    :before-upload="beforeUpload"
    accept="image/jpeg,image/png,image/gif,image/webp"
  >
    <el-button type="primary">
      <el-icon><Upload /></el-icon>
      上传图片
    </el-button>
    <template #tip>
      <div class="el-upload__tip">
        {{ tipText }}
      </div>
    </template>
  </el-upload>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Upload } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/stores'

const props = defineProps({
  modelValue: {
    type: [String, Array],
    default: ''
  },
  listType: {
    type: String,
    default: 'text' // text/picture/picture-card
  },
  multiple: {
    type: Boolean,
    default: false
  },
  limit: {
    type: Number,
    default: 1
  },
  tipText: {
    type: String,
    default: '支持 jpg/png/gif/webp 格式，大小不超过 5MB'
  },
  maxSize: {
    type: Number,
    default: 5 // 单位MB
  }
})

const emit = defineEmits(['update:modelValue'])

const userStore = useUserStore()
const uploadUrl = computed(() => import.meta.env.VITE_BASE_API + '/admin/upload/image')
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${getToken()}`
}))

const fileList = ref([])

// 监听外部值变化，同步到fileList
watch(() => props.modelValue, (val) => {
  if (!val) {
    fileList.value = []
    return
  }
  if (props.multiple && Array.isArray(val)) {
    fileList.value = val.map(url => ({ url, name: url.split('/').pop() }))
  } else if (!props.multiple && typeof val === 'string') {
    fileList.value = [{ url: val, name: val.split('/').pop() }]
  }
}, { immediate: true, deep: true })

// 上传前的格式和大小校验
const beforeUpload = (file) => {
  const isImage = ['image/jpeg', 'image/png', 'image/gif', 'image/webp'].includes(file.type)
  const isLtMax = file.size / 1024 / 1024 < props.maxSize

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLtMax) {
    ElMessage.error(`图片大小不能超过 ${props.maxSize}MB!`)
    return false
  }
  return true
}

// 上传成功
const handleSuccess = (response) => {
  if (response.code === 200) {
    const newUrl = response.data.url
    if (props.multiple) {
      const newVal = [...(props.modelValue || []), newUrl]
      emit('update:modelValue', newVal)
    } else {
      emit('update:modelValue', newUrl)
    }
    ElMessage.success('上传成功!')
  } else {
    ElMessage.error(response.message || '上传失败!')
  }
}

// 删除图片
const handleRemove = (file) => {
  if (props.multiple) {
    const newVal = (props.modelValue || []).filter(url => url !== file.url)
    emit('update:modelValue', newVal)
  } else {
    emit('update:modelValue', '')
  }
}

// 超过限制数量
const handleExceed = () => {
  ElMessage.warning(`最多只能上传 ${props.limit} 张图片!`)
}
</script>

<style scoped>
</style>
```
===FILE:front/store/src/utils/validate.js===
```javascript
/**
 * 移动端表单验证工具
 */

// 手机号验证
export const validatePhone = (value) => {
  const reg = /^1[3-9]\d{9}$/
  if (!value) return '请输入手机号'
  if (!reg.test(value)) return '请输入正确的手机号'
  return ''
}

// 密码验证（6-20位字符）
export const validatePassword = (value) => {
  if (!value) return '请输入密码'
  if (value.length < 6 || value.length > 20) return '密码需为6-20位字符'
  return ''
}

// 确认密码验证
export const validateConfirmPassword = (password, confirmPassword) => {
  if (!confirmPassword) return '请再次输入密码'
  if (password !== confirmPassword) return '两次输入的密码不一致'
  return ''
}

// 用户名验证（可选，4-16位字母数字下划线）
export const validateUsername = (value) => {
  if (!value) return '' // 可选的话就返回空
  const reg = /^[a-zA-Z0-9_]{4,16}$/
  if (!reg.test(value)) return '用户名需为4-16位字母、数字或下划线'
  return ''
}

// 收货人姓名验证
export const validateReceiverName = (value) => {
  if (!value?.trim()) return '请输入收货人姓名'
  if (value.trim().length < 2 || value.trim().length > 20) return '姓名需为2-20位字符'
  return ''
}

// 详细地址验证
export const validateDetailAddress = (value) => {
  if (!value?.trim()) return '请输入详细地址'
  if (value.trim().length < 5 || value.trim().length > 100) return '详细地址需为5-100位字符'
  return ''
}

// 邮编验证（可选）
export const validateZipCode = (value) => {
  if (!value) return ''
  const reg = /^\d{6}$/
  if (!reg.test(value)) return '请输入正确的6位邮编'
  return ''
}
```
===FILE:front/store/src/utils/constants.js===
```javascript
/**
 * 移动端系统常量定义
 */

// 订单状态（和后端、管理端保持一致）
export const ORDER_STATUS = {
  UNPAID: 0,
  PAID: 1,
  SHIPPED: 2,
  DELIVERED: 3,
  CANCELLED: 4,
  REFUNDED: 5,
  labels: {
    0: '待付款',
    1: '待发货',
    2: '已发货',
    3: '已完成',
    4: '已取消',
    5: '已退款'
  },
  // Vant Tag类型
  types: {
    0: 'warning',
    1: 'primary',
    2: 'default',
    3: 'success',
    4: 'danger',
    5: 'warning'
  },
  // 可操作按钮
  actions: {
    0: ['cancel', 'pay'],
    1: [],
    2: ['confirm'],
    3: ['again'],
    4: ['again'],
    5: []
  },
  actionLabels: {
    cancel: '取消订单',
    pay: '立即支付',
    confirm: '确认收货',
    again: '再次购买'
  }
}

// 订单状态步骤
export const ORDER_STEPS = [
  { status: 0, text: '提交订单' },
  { status: 1, text: '支付成功' },
  { status: 2, text: '商家发货' },
  { status: 3, text: '确认收货' }
]

// 分页默认值
export const DEFAULT_PAGE = {
  pageNum: 1,
  pageSize: 10
}
```
===FILE:front/store/src/components/OrderSteps.vue===
```vue
<template>
  <div class="order-steps">
    <van-steps :active="activeStep" active-color="#07c160">
      <van-step v-for="(step, index) in steps" :key="index">
        {{ step.text }}
        <template #icon>
          <van-icon :name="step.icon || 'checked'" />
        </template>
      </van-step>
    </van-steps>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ORDER_STATUS, ORDER_STEPS } from '@/utils/constants'

const props = defineProps({
  orderStatus: {
    type: Number,
    required: true
  }
})

// 计算当前激活的步骤
const activeStep = computed(() => {
  const status = props.orderStatus
  if (status === ORDER_STATUS.UNPAID) return -1
  if (status === ORDER_STATUS.PAID) return 1
  if (status === ORDER_STATUS.SHIPPED) return 2
  if (status === ORDER_STATUS.DELIVERED) return 3
  if (status === ORDER_STATUS.CANCELLED || status === ORDER_STATUS.REFUNDED) return -2
  return -1
})

// 处理特殊状态的步骤
const steps = computed(() => {
  const status = props.orderStatus
  if (status === ORDER_STATUS.CANCELLED) {
    return [
      { text: '订单已取消', icon: 'close' }
    ]
  }
  if (status === ORDER_STATUS.REFUNDED) {
    return [
      { text: '退款成功', icon: 'balance-pay' }
    ]
  }
  return ORDER_STEPS
})
</script>

<style scoped>
.order-steps {
  padding: 16px;
  background-color: #fff;
}
</style>
```
===FILE:front/store/src/components/BackTop.vue===
```vue
<template>
  <div class="back-top" v-show="visible" @click="backToTop">
    <van-icon name="upgrade" size="24" color="#fff" />
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  visibilityHeight: {
    type: Number,
    default: 300
  },
  right: {
    type: [String, Number],
    default: 20
  },
  bottom: {
    type: [String, Number],
    default: 40
  }
})

const visible = ref(false)

const handleScroll = () => {
  visible.value = window.pageYOffset > props.visibilityHeight
}

const backToTop = () => {
  window.scrollTo({
    top: 0,
    behavior: 'smooth'
  })
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<style scoped>
.back-top {
  position: fixed;
  right: v-bind('typeof right === "number" ? right + "px" : right');
  bottom: v-bind('typeof bottom === "number" ? bottom + "px" : bottom');
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background-color: #07c160;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.15);
  cursor: pointer;
  z-index: 999;
  transition: opacity 0.3s;
}
</style>
```
===FILE:front/store/src/stores/home.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getHomeData } from '@/api/home'

export const useHomeStore = defineStore('home', () => {
  // 状态
  const banners = ref([])
  const hotCategories = ref([])
  const hotProducts = ref([])
  const newProducts = ref([])
  const loading = ref(false)
  const hasLoaded = ref(false) // 标记是否已加载过首页数据，避免重复请求

  // 加载首页数据
  const loadHomeData = async (force = false) => {
    // 如果不是强制刷新且已加载过，则直接返回
    if (!force && hasLoaded.value) return

    loading.value = true
    try {
      const res = await getHomeData()
      if (res.code === 200) {
        const data = res.data
        banners.value = data.banners || []
        hotCategories.value = data.hotCategories || []
        hotProducts.value = data.hotProducts || []
        newProducts.value = data.newProducts || []
        hasLoaded.value = true
      }
    } catch (error) {
      console.error('加载首页数据失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 重置状态（比如退出登录时）
  const resetHome = () => {
    banners.value = []
    hotCategories.value = []
    hotProducts.value = []
    newProducts.value = []
    hasLoaded.value = false
  }

  return {
    banners,
    hotCategories,
    hotProducts,
    newProducts,
    loading,
    hasLoaded,
    loadHomeData,
    resetHome
  }
})
```
===FILE:front/admin/src/stores/index.js===
```javascript
/**
 * 统一导出所有Pinia Store
 */
export { useUserStore } from './user'
export { useProductStore } from './product'
export { useProductCategoryStore } from './productCategory'
export { useOrderStore } from './order'
export { useDashboardStore } from './dashboard'
```
===FILE:front/store/src/stores/index.js===
```javascript
/**
 * 统一导出所有Pinia Store
 */
export { useUserStore } from './user'
export { useCartStore } from './cart'
export { useAddressStore } from './address'
export { useProductStore } from './product'
export { useHomeStore } from './home'
```