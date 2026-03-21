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