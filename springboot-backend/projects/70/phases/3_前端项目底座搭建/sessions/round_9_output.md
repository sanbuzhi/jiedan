===FILE:front/admin/src/api/productCategory.js===
```javascript
import request from '@/utils/request'

export function getProductCategoryList(params) {
  return request({
    url: '/admin/product-categories',
    method: 'get',
    params
  })
}

export function getProductCategory(id) {
  return request({
    url: `/admin/product-categories/${id}`,
    method: 'get'
  })
}

export function createProductCategory(data) {
  return request({
    url: '/admin/product-categories',
    method: 'post',
    data
  })
}

export function updateProductCategory(id, data) {
  return request({
    url: `/admin/product-categories/${id}`,
    method: 'put',
    data
  })
}

export function deleteProductCategory(id) {
  return request({
    url: `/admin/product-categories/${id}`,
    method: 'delete'
  })
}

export function updateProductCategoryStatus(id, status) {
  return request({
    url: `/admin/product-categories/${id}/status`,
    method: 'patch',
    data: { status }
  })
}
```

===FILE:front/admin/src/stores/productCategory.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProductCategoryList, createProductCategory, updateProductCategory, deleteProductCategory, updateProductCategoryStatus } from '@/api/productCategory'

export const useProductCategoryStore = defineStore('productCategory', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getProductCategoryList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取分类列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const addCategory = async (data) => {
    const res = await createProductCategory(data)
    return res
  }

  const editCategory = async (id, data) => {
    const res = await updateProductCategory(id, data)
    return res
  }

  const removeCategory = async (id) => {
    const res = await deleteProductCategory(id)
    return res
  }

  const changeStatus = async (id, status) => {
    const res = await updateProductCategoryStatus(id, status)
    return res
  }

  return {
    list,
    total,
    loading,
    fetchList,
    addCategory,
    editCategory,
    removeCategory,
    changeStatus
  }
})
```

===FILE:front/admin/src/views/ProductCategoryList.vue===
```vue
<template>
  <div class="product-category-list">
    <PageHeader title="产品分类管理">
      <template #extra>
        <el-button type="primary" @click="handleAdd">新增分类</el-button>
      </template>
    </PageHeader>

    <el-card class="table-card">
      <el-table :data="store.list" v-loading="store.loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="分类名称" />
        <el-table-column prop="sort" label="排序" width="100" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="{ row }">
            <StatusTag :status="row.status" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="warning" link @click="handleToggleStatus(row)">
              {{ row.status === 1 ? '禁用' : '启用' }}
            </el-button>
            <el-button type="danger" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-model:current-page="queryParams.page"
        v-model:page-size="queryParams.pageSize"
        :total="store.total"
        @pagination="store.fetchList(queryParams)"
      />
    </el-card>

    <!-- 表单弹窗 -->
    <el-dialog
      v-model="formVisible"
      :title="formType === 'add' ? '新增分类' : '编辑分类'"
      width="500px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入分类名称" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="formData.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="formData.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 删除确认弹窗 -->
    <ConfirmDialog
      v-model="deleteVisible"
      title="删除确认"
      content="确定要删除该分类吗？此操作不可恢复！"
      @confirm="confirmDelete"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useProductCategoryStore } from '@/stores/productCategory'
import PageHeader from '@/components/PageHeader.vue'
import Pagination from '@/components/Pagination.vue'
import StatusTag from '@/components/StatusTag.vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

const store = useProductCategoryStore()
const queryParams = reactive({
  page: 1,
  pageSize: 10
})

// 表单相关
const formVisible = ref(false)
const formType = ref('add')
const formRef = ref(null)
const formData = reactive({
  id: null,
  name: '',
  sort: 0,
  status: 1
})
const formRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

// 删除相关
const deleteVisible = ref(false)
const deleteId = ref(null)

const handleAdd = () => {
  formType.value = 'add'
  resetForm()
  formVisible.value = true
}

const handleEdit = (row) => {
  formType.value = 'edit'
  Object.assign(formData, row)
  formVisible.value = true
}

const handleToggleStatus = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要${row.status === 1 ? '禁用' : '启用'}该分类吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await store.changeStatus(row.id, row.status === 1 ? 0 : 1)
    ElMessage.success('状态更新成功')
    store.fetchList(queryParams)
  } catch (error) {
    console.error(error)
  }
}

const handleDelete = (row) => {
  deleteId.value = row.id
  deleteVisible.value = true
}

const confirmDelete = async () => {
  try {
    await store.removeCategory(deleteId.value)
    ElMessage.success('删除成功')
    store.fetchList(queryParams)
  } catch (error) {
    console.error(error)
  } finally {
    deleteVisible.value = false
    deleteId.value = null
  }
}

const handleSubmit = async () => {
  await formRef.value.validate()
  try {
    if (formType.value === 'add') {
      await store.addCategory(formData)
      ElMessage.success('新增成功')
    } else {
      await store.editCategory(formData.id, formData)
      ElMessage.success('编辑成功')
    }
    formVisible.value = false
    store.fetchList(queryParams)
  } catch (error) {
    console.error(error)
  }
}

const resetForm = () => {
  formRef.value?.resetFields()
  Object.assign(formData, {
    id: null,
    name: '',
    sort: 0,
    status: 1
  })
}

onMounted(() => {
  store.fetchList(queryParams)
})
</script>

<style scoped>
.product-category-list {
  padding: 20px;
}
.table-card {
  margin-top: 20px;
}
</style>
```

===FILE:front/admin/src/stores/product.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProductList, getProduct, createProduct, updateProduct, deleteProduct, updateProductStatus } from '@/api/product'

export const useProductStore = defineStore('product', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const currentProduct = ref(null)

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getProductList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取产品列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await getProduct(id)
      currentProduct.value = res.data
      return res.data
    } catch (error) {
      console.error('获取产品详情失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const addProduct = async (data) => {
    const res = await createProduct(data)
    return res
  }

  const editProduct = async (id, data) => {
    const res = await updateProduct(id, data)
    return res
  }

  const removeProduct = async (id) => {
    const res = await deleteProduct(id)
    return res
  }

  const changeStatus = async (id, status) => {
    const res = await updateProductStatus(id, status)
    return res
  }

  return {
    list,
    total,
    loading,
    currentProduct,
    fetchList,
    fetchDetail,
    addProduct,
    editProduct,
    removeProduct,
    changeStatus
  }
})
```

===FILE:front/admin/src/stores/order.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getOrderList, getOrderDetail, updateOrderStatus, shipOrder } from '@/api/order'

export const useOrderStore = defineStore('order', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const currentOrder = ref(null)

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getOrderList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取订单列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await getOrderDetail(id)
      currentOrder.value = res.data
      return res.data
    } catch (error) {
      console.error('获取订单详情失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const changeStatus = async (id, status) => {
    const res = await updateOrderStatus(id, status)
    return res
  }

  const ship = async (id, data) => {
    const res = await shipOrder(id, data)
    return res
  }

  return {
    list,
    total,
    loading,
    currentOrder,
    fetchList,
    fetchDetail,
    changeStatus,
    ship
  }
})
```

===FILE:front/store/src/stores/product.js===
```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { getProductList, getProductDetail, getHotProducts, getNewProducts } from '@/api/product'

export const useProductStore = defineStore('product', () => {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)
  const currentProduct = ref(null)
  const hotProducts = ref([])
  const newProducts = ref([])
  const categories = ref([])

  const fetchList = async (params = {}) => {
    loading.value = true
    try {
      const res = await getProductList(params)
      list.value = res.data.list || res.data || []
      total.value = res.data.total || 0
    } catch (error) {
      console.error('获取商品列表失败', error)
    } finally {
      loading.value = false
    }
  }

  const fetchDetail = async (id) => {
    loading.value = true
    try {
      const res = await getProductDetail(id)
      currentProduct.value = res.data
      return res.data
    } catch (error) {
      console.error('获取商品详情失败', error)
      throw error
    } finally {
      loading.value = false
    }
  }

  const fetchHotProducts = async () => {
    try {
      const res = await getHotProducts()
      hotProducts.value = res.data || []
    } catch (error) {
      console.error('获取热门商品失败', error)
    }
  }

  const fetchNewProducts = async () => {
    try {
      const res = await getNewProducts()
      newProducts.value = res.data || []
    } catch (error) {
      console.error('获取新品失败', error)
    }
  }

  return {
    list,
    total,
    loading,
    currentProduct,
    hotProducts,
    newProducts,
    categories,
    fetchList,
    fetchDetail,
    fetchHotProducts,
    fetchNewProducts
  }
})
```

===FILE:front/store/src/components/OrderStatusTag.vue===
```vue
<template>
  <van-tag :type="tagType" size="small">{{ statusText }}</van-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: [String, Number],
    required: true
  }
})

const statusMap = {
  0: { text: '待付款', type: 'danger' },
  1: { text: '待发货', type: 'warning' },
  2: { text: '已发货', type: 'primary' },
  3: { text: '已完成', type: 'success' },
  4: { text: '已取消', type: 'default' },
  5: { text: '退款中', type: 'warning' },
  6: { text: '已退款', type: 'default' }
}

const statusText = computed(() => statusMap[props.status]?.text || '未知状态')
const tagType = computed(() => statusMap[props.status]?.type || 'default')
</script>

<style scoped>
</style>
```