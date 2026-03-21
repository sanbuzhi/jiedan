===FILE:front/admin/src/api/product.js===
```javascript
import request from '@/utils/request'

// 获取商品列表
export function getProductList(params) {
  return request({
    url: '/admin/products',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'get'
  })
}

// 新增商品
export function addProduct(data) {
  return request({
    url: '/admin/products',
    method: 'post',
    data
  })
}

// 编辑商品
export function updateProduct(id, data) {
  return request({
    url: `/admin/products/${id}`,
    method: 'put',
    data
  })
}

// 删除商品
export function deleteProduct(id) {
  return request({
    url: `/admin/products/${id}`,
    method: 'delete'
  })
}

// 批量删除商品
export function batchDeleteProducts(ids) {
  return request({
    url: '/admin/products/batch',
    method: 'delete',
    data: { ids }
  })
}

// 上架/下架商品
export function toggleProductStatus(id, status) {
  return request({
    url: `/admin/products/${id}/status`,
    method: 'put',
    data: { status }
  })
}
```
===FILE:front/admin/src/api/order.js===
```javascript
import request from '@/utils/request'

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/admin/orders',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/admin/orders/${id}`,
    method: 'get'
  })
}

// 更新订单状态
export function updateOrderStatus(id, status, params = {}) {
  return request({
    url: `/admin/orders/${id}/status`,
    method: 'put',
    data: { status, ...params }
  })
}

// 导出订单
export function exportOrders(params) {
  return request({
    url: '/admin/orders/export',
    method: 'get',
    params,
    responseType: 'blob'
  })
}
```
===FILE:front/admin/src/api/user.js===
```javascript
import request from '@/utils/request'

// 获取用户列表
export function getUserList(params) {
  return request({
    url: '/admin/users',
    method: 'get',
    params
  })
}

// 获取用户详情
export function getUserDetail(id) {
  return request({
    url: `/admin/users/${id}`,
    method: 'get'
  })
}

// 冻结/解冻用户
export function toggleUserStatus(id, status) {
  return request({
    url: `/admin/users/${id}/status`,
    method: 'put',
    data: { status }
  })
}

// 管理员登录
export function login(data) {
  return request({
    url: '/admin/auth/login',
    method: 'post',
    data
  })
}

// 获取当前管理员信息
export function getCurrentAdmin() {
  return request({
    url: '/admin/auth/me',
    method: 'get'
  })
}

// 管理员退出登录
export function logout() {
  return request({
    url: '/admin/auth/logout',
    method: 'post'
  })
}
```
===FILE:front/admin/src/views/UserList.vue===
```vue
<template>
  <div class="user-list-container">
    <PageHeader title="用户管理" subtitle="管理平台注册用户" />
    
    <el-card class="table-container" shadow="hover">
      <!-- 搜索区域 -->
      <el-form :inline="true" :model="queryParams" class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="queryParams.phone" placeholder="请输入手机号" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="请选择状态" clearable>
            <el-option label="正常" :value="1" />
            <el-option label="已冻结" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleQuery">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      
      <!-- 表格区域 -->
      <el-table v-loading="loading" :data="userList" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="avatar" label="头像" width="100">
          <template #default="{ row }">
            <el-image :src="row.avatar || defaultAvatar" fit="cover" style="width: 50px; height: 50px; border-radius: 50%">
              <template #error>
                <div class="image-error">
                  <el-icon :size="30"><User /></el-icon>
                </div>
              </template>
            </el-image>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" min-width="130" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '已冻结' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="160" />
        <el-table-column label="操作" min-width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link :icon="View" @click="handleView(row)">查看</el-button>
            <el-button
              :type="row.status === 1 ? 'warning' : 'success'"
              link
              :icon="row.status === 1 ? Lock : Unlock"
              @click="handleToggleStatus(row)"
            >
              {{ row.status === 1 ? '冻结' : '解冻' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页区域 -->
      <Pagination
        :total="total"
        :page.sync="queryParams.page"
        :page-size.sync="queryParams.pageSize"
        @pagination="getList"
      />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, View, Lock, Unlock, User } from '@element-plus/icons-vue'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import { getUserList, toggleUserStatus } from '@/api/user'

// 默认头像
const defaultAvatar = 'data:image/svg+xml;base64,PHN2ZyB4bWxucz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC9zdmciIHZpZXdCb3g9IjAgMCAxMDAgMTAwIj48cmVjdCB3aWR0aD0iMTAwIiBoZWlnaHQ9IjEwMCIgZmlsbD0iI2UwZTBlMCIvPjxjaXJjbGUgY3g9IjUwIiBjeT0iMzUiIHI9IjIwIiBmaWxsPSIjYjBiMGIwIi8+PHBhdGggZD0iTTIwIDg1IGMwIC0yNSAzMCAtNDAgMzAgLTQwIHMzMCAxNSAzMCA0MCB6IiBmaWxsPSIjYjBiMGIwIi8+PC9zdmc+'

// 搜索参数
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  username: '',
  phone: '',
  status: undefined
})

// 数据
const loading = ref(false)
const total = ref(0)
const userList = ref([])

// 获取用户列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams)
    userList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 搜索
const handleQuery = () => {
  queryParams.page = 1
  getList()
}

// 重置
const handleReset = () => {
  queryParams.page = 1
  queryParams.username = ''
  queryParams.phone = ''
  queryParams.status = undefined
  getList()
}

// 查看用户
const handleView = (row) => {
  ElMessage.info(`查看用户：${row.username}`)
  // TODO: 打开用户详情弹窗或跳转页面
}

// 切换用户状态
const handleToggleStatus = async (row) => {
  const confirmText = row.status === 1 ? '确认冻结该用户吗？' : '确认解冻该用户吗？'
  try {
    await ElMessageBox.confirm(confirmText, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: row.status === 1 ? 'warning' : 'success'
    })
    await toggleUserStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success(row.status === 1 ? '冻结成功' : '解冻成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换用户状态失败:', error)
    }
  }
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.user-list-container {
  padding: 20px;
}

.table-container {
  margin-top: 20px;
}

.search-form {
  margin-bottom: 20px;
}

.image-error {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  background-color: #f5f7fa;
  color: #909399;
}
</style>
```
===FILE:front/store/src/api/product.js===
```javascript
import request from '@/utils/request'

// 获取商品列表（带分类、搜索、排序）
export function getProductList(params) {
  return request({
    url: '/products',
    method: 'get',
    params
  })
}

// 获取商品详情
export function getProductDetail(id) {
  return request({
    url: `/products/${id}`,
    method: 'get'
  })
}

// 获取商品分类列表
export function getCategoryList() {
  return request({
    url: '/categories',
    method: 'get'
  })
}
```
===FILE:front/store/src/api/order.js===
```javascript
import request from '@/utils/request'

// 创建订单
export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

// 获取订单列表
export function getOrderList(params) {
  return request({
    url: '/orders',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderDetail(id) {
  return request({
    url: `/orders/${id}`,
    method: 'get'
  })
}

// 取消订单
export function cancelOrder(id, reason = '') {
  return request({
    url: `/orders/${id}/cancel`,
    method: 'put',
    data: { reason }
  })
}

// 确认收货
export function confirmReceipt(id) {
  return request({
    url: `/orders/${id}/confirm`,
    method: 'put'
  })
}

// 订单支付
export function payOrder(id, payMethod = 'alipay') {
  return request({
    url: `/orders/${id}/pay`,
    method: 'post',
    data: { payMethod }
  })
}
```
===FILE:front/store/src/api/user.js===
```javascript
import request from '@/utils/request'

// 用户登录（用户名/手机号+密码）
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 用户注册
export function register(data) {
  return request({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 获取当前用户信息
export function getCurrentUser() {
  return request({
    url: '/auth/me',
    method: 'get'
  })
}

// 更新用户信息
export function updateUser(data) {
  return request({
    url: '/auth/me',
    method: 'put',
    data
  })
}

// 修改密码
export function updatePassword(data) {
  return request({
    url: '/auth/password',
    method: 'put',
    data
  })
}

// 用户退出登录
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}
```
===FILE:front/store/src/api/address.js===
```javascript
import request from '@/utils/request'

// 获取收货地址列表
export function getAddressList() {
  return request({
    url: '/addresses',
    method: 'get'
  })
}

// 获取收货地址详情
export function getAddressDetail(id) {
  return request({
    url: `/addresses/${id}`,
    method: 'get'
  })
}

// 新增收货地址
export function addAddress(data) {
  return request({
    url: '/addresses',
    method: 'post',
    data
  })
}

// 编辑收货地址
export function updateAddress(id, data) {
  return request({
    url: `/addresses/${id}`,
    method: 'put',
    data
  })
}

// 删除收货地址
export function deleteAddress(id) {
  return request({
    url: `/addresses/${id}`,
    method: 'delete'
  })
}

// 设置默认收货地址
export function setDefaultAddress(id) {
  return request({
    url: `/addresses/${id}/default`,
    method: 'put'
  })
}
```
===FILE:front/store/src/components/AddressForm.vue===
```vue
<template>
  <div class="address-form-container">
    <van-form ref="formRef" @submit="handleSubmit">
      <van-cell-group inset>
        <van-field
          v-model="formData.name"
          name="name"
          label="收货人"
          placeholder="请输入收货人姓名"
          :rules="[{ required: true, message: '请输入收货人姓名' }]"
        />
        <van-field
          v-model="formData.phone"
          name="phone"
          label="手机号"
          type="tel"
          maxlength="11"
          placeholder="请输入手机号"
          :rules="[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
          ]"
        />
        <van-field
          v-model="formData.address"
          name="address"
          type="textarea"
          label="详细地址"
          rows="3"
          placeholder="请输入详细地址"
          :rules="[{ required: true, message: '请输入详细地址' }]"
        />
      </van-cell-group>
      <div class="form-switch">
        <van-switch v-model="formData.isDefault" size="20px" />
        <span class="switch-text">设为默认地址</span>
      </div>
      <div class="form-buttons">
        <van-button block type="primary" native-type="submit">
          {{ isEdit ? '保存修改' : '新增地址' }}
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { ref, reactive, watch } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { addAddress, updateAddress } from '@/api/address'

const props = defineProps({
  isEdit: {
    type: Boolean,
    default: false
  },
  initialData: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['refresh'])

const router = useRouter()
const formRef = ref(null)

// 表单数据
const formData = reactive({
  id: undefined,
  name: '',
  phone: '',
  address: '',
  isDefault: false
})

// 监听初始数据变化
watch(() => props.initialData, (newVal) => {
  if (newVal && Object.keys(newVal).length > 0) {
    Object.assign(formData, newVal)
  }
}, { immediate: true, deep: true })

// 提交表单
const handleSubmit = async () => {
  try {
    showLoadingToast({ message: '提交中...', forbidClick: true, duration: 0 })
    if (props.isEdit) {
      await updateAddress(formData.id, formData)
      showToast('修改成功')
    } else {
      await addAddress(formData)
      showToast('新增成功')
    }
    emit('refresh')
    router.back()
  } catch (error) {
    console.error('提交地址失败:', error)
  } finally {
    closeToast()
  }
}
</script>

<style scoped>
.address-form-container {
  padding: 16px 0;
  background-color: #f7f8fa;
  min-height: 100vh;
}

.form-switch {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: #fff;
  margin: 12px 16px;
  border-radius: 8px;
}

.switch-text {
  margin-left: 12px;
  font-size: 14px;
  color: #323233;
}

.form-buttons {
  margin: 24px 16px;
}
</style>
```
===FILE:front/store/src/views/AddressList.vue===
```vue
<template>
  <div class="address-list-container">
    <van-nav-bar title="收货地址" left-arrow @click-left="goBack" right-text="新增" @click-right="goAdd" />
    
    <div v-loading="loading" class="address-content">
      <van-empty v-if="!loading && addressList.length === 0" description="暂无收货地址" />
      
      <van-radio-group v-model="selectedId" v-else>
        <van-cell-group inset class="address-group">
          <van-cell
            v-for="item in addressList"
            :key="item.id"
            class="address-item"
            is-link
            @click="goEdit(item)"
          >
            <template #icon>
              <van-radio
                :name="item.id"
                :disabled="!isSelectMode"
                @click.stop="handleSelect(item)"
              />
            </template>
            <template #title>
              <div class="address-title">
                <span class="name">{{ item.name }}</span>
                <span class="phone">{{ item.phone }}</span>
                <van-tag v-if="item.isDefault" type="danger" size="small" plain>默认</van-tag>
              </div>
            </template>
            <template #label>
              <div class="address-detail">{{ item.address }}</div>
            </template>
            <template #right-icon>
              <div class="address-actions">
                <van-icon name="edit" size="18" @click.stop="goEdit(item)" />
                <van-icon name="delete-o" size="18" class="delete-icon" @click.stop="handleDelete(item)" />
              </div>
            </template>
          </van-cell>
        </van-cell-group>
      </van-radio-group>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showConfirmDialog, showToast, showLoadingToast, closeToast } from 'vant'
import { getAddressList, deleteAddress, setDefaultAddress } from '@/api/address'
import { useAddressStore } from '@/stores/address'

const router = useRouter()
const route = useRoute()
const addressStore = useAddressStore()

// 是否是选择模式
const isSelectMode = ref(route.query.select === '1')
// 选中的地址ID
const selectedId = ref('')

// 数据
const loading = ref(false)
const addressList = ref([])

// 获取地址列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getAddressList()
    addressList.value = res.data || []
    // 选中默认地址
    if (isSelectMode.value) {
      const defaultAddress = addressList.value.find(item => item.isDefault)
      selectedId.value = defaultAddress ? defaultAddress.id : addressList.value[0]?.id || ''
    }
  } catch (error) {
    console.error('获取地址列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 选择地址
const handleSelect = (item) => {
  selectedId.value = item.id
  // 如果不是默认地址，设置为默认
  if (!item.isDefault) {
    setDefaultAddress(item.id).catch(err => console.error('设置默认地址失败:', err))
  }
  // 返回上一页并传递选中的地址
  router.back()
  addressStore.setSelectedAddress(item)
}

// 新增地址
const goAdd = () => {
  router.push({ name: 'AddressEdit', query: { isEdit: '0' } })
}

// 编辑地址
const goEdit = (item) => {
  router.push({ name: 'AddressEdit', query: { isEdit: '1', id: item.id } })
}

// 删除地址
const handleDelete = async (item) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认删除该收货地址吗？'
    })
    showLoadingToast({ message: '删除中...', forbidClick: true, duration: 0 })
    await deleteAddress(item.id)
    showToast('删除成功')
    getList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除地址失败:', error)
    }
  } finally {
    closeToast()
  }
}

// 返回
const goBack = () => {
  router.back()
}

onMounted(() => {
  getList()
})
</script>

<style scoped>
.address-list-container {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.address-content {
  padding: 12px 0;
}

.address-group {
  margin-bottom: 12px;
}

.address-item {
  margin-bottom: 12px;
  border-radius: 8px;
  overflow: hidden;
}

.address-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 6px;
}

.name, .phone {
  font-size: 15px;
  font-weight: 500;
  color: #323233;
}

.address-detail {
  font-size: 13px;
  color: #646566;
  line-height: 1.6;
}

.address-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #969799;
}

.delete-icon {
  color: #ee0a24;
}
</style>
```
===FILE:front/store/src/views/Register.vue===
```vue
<template>
  <div class="register-container">
    <van-nav-bar title="用户注册" left-arrow @click-left="goBack" />
    
    <div class="register-form">
      <van-form ref="formRef" @submit="handleSubmit">
        <van-cell-group inset>
          <van-field
            v-model="formData.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请输入用户名' }]"
          />
          <van-field
            v-model="formData.phone"
            name="phone"
            label="手机号"
            type="tel"
            maxlength="11"
            placeholder="请输入手机号"
            :rules="[
              { required: true, message: '请输入手机号' },
              { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号' }
            ]"
          />
          <van-field
            v-model="formData.password"
            name="password"
            type="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请输入密码' }, { min: 6, message: '密码至少6位' }]"
          />
          <van-field
            v-model="formData.confirmPassword"
            name="confirmPassword"
            type="password"
            label="确认密码"
            placeholder="请再次输入密码"
            :rules="[{ required: true, message: '请确认密码' }]"
          />
        </van-cell-group>
        
        <div class="register-agreement">
          <van-checkbox v-model="agreementChecked">
            我已阅读并同意
            <span class="agreement-link">《用户协议》</span>
            和
            <span class="agreement-link">《隐私政策》</span>
          </van-checkbox>
        </div>
        
        <div class="register-buttons">
          <van-button block type="primary" native-type="submit" :disabled="!agreementChecked">
            立即注册
          </van-button>
        </div>
      </van-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { register } from '@/api/user'

const router = useRouter()
const formRef = ref(null)

// 表单数据
const formData = reactive({
  username: '',
  phone: '',
  password: '',
  confirmPassword: ''
})

// 协议勾选
const agreementChecked = ref(false)

// 提交注册
const handleSubmit = async (values) => {
  if (values.password !== values.confirmPassword) {
    showToast('两次密码输入不一致')
    return
  }
  
  try {
    showLoadingToast({ message: '注册中...', forbidClick: true, duration: 0 })
    await register({
      username: values.username,
      phone: values.phone,
      password: values.password
    })
    showToast('注册成功，请登录')
    router.replace({ name: 'Login' })
  } catch (error) {
    console.error('注册失败:', error)
  } finally {
    closeToast()
  }
}

// 返回登录页
const goBack = () => {
  router.replace({ name: 'Login' })
}
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.register-form {
  padding: 24px 0;
}

.register-agreement {
  padding: 16px;
  background-color: #fff;
  margin: 12px 16px;
  border-radius: 8px;
}

.agreement-link {
  color: #1989fa;
}

.register-buttons {
  margin: 24px 16px;
}
</style>
```
===FILE:front/store/src/views/OrderList.vue===
```vue
<template>
  <div class="order-list-container">
    <van-nav-bar title="我的订单" left-arrow @click-left="goBack" />
    
    <van-tabs v-model:active="activeTab" sticky swipeable>
      <van-tab title="全部" name="" />
      <van-tab title="待付款" name="0" />
      <van-tab title="待发货" name="1" />
      <van-tab title="待收货" name="2" />
      <van-tab title="已完成" name="3" />
    </van-tabs>
    
    <div v-loading="loading" class="order-content">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="listLoading"
          :finished="finished"
          finished-text="没有更多了"
          @load="onLoad"
        >
          <van-empty v-if="!loading && !refreshing && orderList.length === 0" description="暂无订单" />
          
          <div v-else class="order-list">
            <div v-for="item in orderList" :key="item.id" class="order-item">
              <div class="order-header">
                <span class="order-sn">订单号：{{ item.orderSn }}</span>
                <van-tag :type="getStatusType(item.status)" size="small">{{ getStatusText(item.status) }}</van-tag>
              </div>
              
              <div class="order-goods" @click="goDetail(item)">
                <div v-for="goods in item.goodsList" :key="goods.id" class="goods-item">
                  <van-image :src="goods.image" width="80" height="80" fit="cover" round />
                  <div class="goods-info">
                    <div class="goods-name">{{ goods.name }}</div>
                    <div class="goods-spec">{{ goods.spec }}</div>
                    <div class="goods-bottom">
                      <span class="goods-price">¥{{ goods.price }}</span>
                      <span class="goods-num">x{{ goods.num }}</span>
                    </div>
                  </div>
                </div>
              </div>
              
              <div class="order-footer">
                <div class="order-total">
                  共{{ item.totalNum }}件商品，实付：<span class="total-price">¥{{ item.payAmount }}</span>
                </div>
                <div class="order-actions">
                  <van-button v-if="item.status === 0" size="small" type="danger" @click="handlePay(item)">立即付款</van-button>
                  <van-button v-if="item.status === 0" size="small" plain @click="handleCancel(item)">取消订单</van-button>
                  <van-button v-if="item.status === 2" size="small" type="primary" @click="handleConfirm(item)">确认收货</van-button>
                  <van-button size="small" plain @click="goDetail(item)">查看详情</van-button>
                </div>
              </div>
            </div>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { showConfirmDialog, showToast, showLoadingToast, closeToast } from 'vant'
import { getOrderList, cancelOrder, confirmReceipt, payOrder } from '@/api/order'

const router = useRouter()
const route = useRoute()

// 订单状态对应文字和颜色
const statusMap = {
  0: { text: '待付款', type: 'danger' },
  1: { text: '待发货', type: 'warning' },
  2: { text: '待收货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'default' }
}

// 数据
const activeTab = ref(route.query.status || '')
const loading = ref(false)
const refreshing = ref(false)
const listLoading = ref(false)
const finished = ref(false)
const queryParams = reactive({
  page: 1,
  pageSize: 10,
  status: computed(() => activeTab.value)
})
const orderList = ref([])

// 获取订单状态文字
const getStatusText = (status) => statusMap[status]?.text || '未知'
// 获取订单状态类型
const getStatusType = (status) => statusMap[status]?.type || 'default'

// 获取订单列表
const getList = async (isRefresh = false) => {
  if (isRefresh) {
    queryParams.page = 1
    finished.value = false
  }
  
  listLoading.value = true
  try {
    const res = await getOrderList(queryParams)
    const list = res.data?.list || []
    if (isRefresh) {
      orderList.value = list
    } else {
      orderList.value = [...orderList.value, ...list]
    }
    finished.value = list.length < queryParams.pageSize
    queryParams.page++
  } catch (error) {
    console.error('获取订单列表失败:', error)
  } finally {
    listLoading.value = false
    loading.value = false
    refreshing.value = false
  }
}

// 下拉刷新
const onRefresh = () => {
  refreshing.value = true
  getList(true)
}

// 上拉加载
const onLoad = () => {
  getList(false)
}

// 订单操作
const handlePay = async (item) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '模拟支付，确认支付吗？'
    })
    showLoadingToast({ message: '支付中...', forbidClick: true, duration: 0 })
    await payOrder(item.id)
    showToast('支付成功')
    getList(true)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('支付失败:', error)
    }
  } finally {
    closeToast()
  }
}

const handleCancel = async (item) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认取消该订单吗？'
    })
    showLoadingToast({ message: '取消中...', forbidClick: true, duration: 0 })
    await cancelOrder(item.id)
    showToast('取消成功')
    getList(true)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消订单失败:', error)
    }
  } finally {
    closeToast()
  }
}

const handleConfirm = async (item) => {
  try {
    await showConfirmDialog({
      title: '提示',
      message: '确认已收到商品吗？'
    })
    showLoadingToast({ message: '确认中...', forbidClick: true, duration: 0 })
    await confirmReceipt(item.id)
    showToast('确认成功')
    getList(true)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('确认收货失败:', error)
    }
  } finally {
    closeToast()
  }
}

// 跳转
const goDetail = (item) => {
  router.push({ name: 'OrderDetail', params: { id: item.id } })
}

const goBack = () => {
  router.back()
}

// 监听路由参数变化
onMounted(() => {
  loading.value = true
  getList(true)
})
</script>

<style scoped>
.order-list-container {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.order-content {
  padding: 12px 0;
}

.order-item {
  background-color: #fff;
  margin-bottom: 12px;
  padding: 12px 16px;
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.order-sn {
  font-size: 13px;
  color: #646566;
}

.order-goods {
  padding: 12px 0;
}

.goods-item {
  display: flex;
  gap: 12px;
  margin-bottom: 12px;
}

.goods-item:last-child {
  margin-bottom: 0;
}

.goods-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.goods-name {
  font-size: 14px;
  color: #323233;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.goods-spec {
  font-size: 12px;
  color: #969799;
  margin-top: 4px;
}

.goods-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.goods-price {
  font-size: 15px;
  font-weight: 500;
  color: #ee0a24;
}

.goods-num {
  font-size: 13px;
  color: #969799;
}

.order-footer {
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.order-total {
  text-align: right;
  font-size: 13px;
  color: #646566;
  margin-bottom: 12px;
}

.total-price {
  font-size: 16px;
  font-weight: 500;
  color: #ee0a24;
}

.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>
```