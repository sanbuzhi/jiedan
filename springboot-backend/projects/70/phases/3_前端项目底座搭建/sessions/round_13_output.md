===FILE:front/admin/src/components/ProductFilter.vue===
```vue
<template>
  <el-card class="product-filter">
    <el-form :inline="true" :model="filterForm" class="demo-form-inline">
      <el-form-item label="商品名称">
        <el-input v-model="filterForm.name" placeholder="请输入商品名称" clearable />
      </el-form-item>
      <el-form-item label="商品分类">
        <el-select v-model="filterForm.categoryId" placeholder="请选择分类" clearable>
          <el-option
            v-for="item in categoryList"
            :key="item.id"
            :label="item.name"
            :value="item.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="上架状态">
        <el-select v-model="filterForm.status" placeholder="请选择状态" clearable>
          <el-option label="上架中" :value="1" />
          <el-option label="已下架" :value="0" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="handleSearch">搜索</el-button>
        <el-button @click="handleReset">重置</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useProductCategoryStore } from '@/stores/productCategory'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({})
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'reset'])

const productCategoryStore = useProductCategoryStore()
const categoryList = computed(() => productCategoryStore.categoryList)

const filterForm = ref({
  name: '',
  categoryId: null,
  status: null,
  ...props.modelValue
})

watch(() => props.modelValue, (val) => {
  Object.assign(filterForm.value, val)
}, { deep: true })

const handleSearch = () => {
  emit('update:modelValue', { ...filterForm.value })
  emit('search')
}

const handleReset = () => {
  filterForm.value = {
    name: '',
    categoryId: null,
    status: null
  }
  emit('update:modelValue', { ...filterForm.value })
  emit('reset')
}

onMounted(() => {
  productCategoryStore.getCategoryList()
})
</script>

<style scoped lang="css">
.product-filter {
  margin-bottom: 20px;
}
</style>
```

===FILE:front/admin/src/components/ProductSkuTable.vue===
```vue
<template>
  <div class="product-sku-table">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品规格库存</span>
        </div>
      </template>
      <el-button type="primary" size="small" @click="handleAddSku" style="margin-bottom: 16px;">
        添加规格
      </el-button>
      <el-table :data="tableData" border style="width: 100%">
        <el-table-column
          v-for="(col, index) in skuColumnList"
          :key="`col-${index}`"
          :prop="col.prop"
          :label="col.label"
        />
        <el-table-column prop="price" label="售价（元）" width="140">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.price"
              :min="0"
              :precision="2"
              :step="0.1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="originalPrice" label="原价（元）" width="140">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.originalPrice"
              :min="0"
              :precision="2"
              :step="0.1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="stock" label="库存" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.stock"
              :min="0"
              :step="1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="weight" label="重量（kg）" width="120">
          <template #default="scope">
            <el-input-number
              v-model="scope.row.weight"
              :min="0"
              :precision="2"
              :step="0.1"
              size="small"
              style="width: 100%;"
            />
          </template>
        </el-table-column>
        <el-table-column prop="image" label="规格图片" width="120">
          <template #default="scope">
            <Uploader
              v-model="scope.row.image"
              :limit="1"
              list-type="picture"
            />
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" fixed="right">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              link
              @click="handleDeleteSku(scope.$index)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Uploader from './Uploader.vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  },
  skuSpecs: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const tableData = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const skuColumnList = computed(() => {
  return props.skuSpecs.map((spec, index) => ({
    prop: `spec${index + 1}`,
    label: spec.name
  }))
})

const handleAddSku = () => {
  const newSku = {
    id: null,
    price: 0,
    originalPrice: 0,
    stock: 0,
    weight: 0,
    image: ''
  }
  props.skuSpecs.forEach((_, index) => {
    newSku[`spec${index + 1}`] = ''
  })
  tableData.value.push(newSku)
}

const handleDeleteSku = (index) => {
  tableData.value.splice(index, 1)
}
</script>

<style scoped lang="css">
.product-sku-table {
  margin-top: 20px;
}
</style>
```

===FILE:front/admin/src/components/ProductSkuForm.vue===
```vue
<template>
  <div class="product-sku-form">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>商品规格设置</span>
        </div>
      </template>
      <div v-for="(spec, index) in skuSpecs" :key="index" class="spec-item">
        <div class="spec-header">
          <span>规格{{ index + 1 }}</span>
          <el-button type="danger" size="small" link @click="handleDeleteSpec(index)">
            删除规格
          </el-button>
        </div>
        <el-form :model="spec" label-width="80px">
          <el-form-item label="规格名称">
            <el-input v-model="spec.name" placeholder="如：颜色、尺码" />
          </el-form-item>
          <el-form-item label="规格值">
            <el-tag
              v-for="(val, valIndex) in spec.values"
              :key="valIndex"
              closable
              @close="handleDeleteSpecValue(index, valIndex)"
              style="margin-right: 8px; margin-bottom: 8px;"
            >
              {{ val }}
            </el-tag>
            <el-input
              v-model="newSpecValue"
              placeholder="输入规格值后按回车添加"
              @keyup.enter="handleAddSpecValue(index)"
              style="width: 200px;"
            />
          </el-form-item>
        </el-form>
      </div>
      <el-button type="primary" @click="handleAddSpec">添加规格</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue'])

const newSpecValue = ref('')
const skuSpecs = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const handleAddSpec = () => {
  skuSpecs.value.push({
    name: '',
    values: []
  })
}

const handleDeleteSpec = (index) => {
  skuSpecs.value.splice(index, 1)
}

const handleAddSpecValue = (specIndex) => {
  if (!newSpecValue.value.trim()) return
  if (!skuSpecs.value[specIndex].values.includes(newSpecValue.value.trim())) {
    skuSpecs.value[specIndex].values.push(newSpecValue.value.trim())
  }
  newSpecValue.value = ''
}

const handleDeleteSpecValue = (specIndex, valIndex) => {
  skuSpecs.value[specIndex].values.splice(valIndex, 1)
}
</script>

<style scoped lang="css">
.product-sku-form {
  margin-top: 20px;
}
.spec-item {
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px dashed #dcdfe6;
}
.spec-item:last-child {
  margin-bottom: 0;
  padding-bottom: 0;
  border-bottom: none;
}
.spec-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  font-weight: bold;
}
</style>
```

===FILE:front/store/src/components/HomeSearch.vue===
```vue
<template>
  <div class="home-search">
    <van-search
      v-model="searchText"
      placeholder="搜索商品"
      shape="round"
      background="#fff"
      @search="handleSearch"
      @cancel="handleCancel"
      show-action
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const searchText = ref('')

const handleSearch = (value) => {
  router.push({
    name: 'ProductList',
    query: { keyword: value.trim() }
  })
}

const handleCancel = () => {
  searchText.value = ''
}
</script>

<style scoped lang="css">
.home-search {
  position: sticky;
  top: 0;
  z-index: 100;
  background-color: #fff;
}
</style>
```

===FILE:front/store/src/components/ProductListSearch.vue===
```vue
<template>
  <div class="product-list-search">
    <van-nav-bar
      title="商品列表"
      left-arrow
      @click-left="handleBack"
    >
      <template #right>
        <van-icon name="search" size="18" @click="showSearch = true" />
      </template>
    </van-nav-bar>
    <van-search
      v-model="searchText"
      v-show="showSearch"
      placeholder="搜索商品"
      shape="round"
      @search="handleSearch"
    />
  </div>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const showSearch = ref(false)
const searchText = ref(route.query.keyword || '')

watch(() => route.query.keyword, (val) => {
  searchText.value = val || ''
})

const handleBack = () => {
  router.back()
}

const handleSearch = (value) => {
  router.replace({
    query: { ...route.query, keyword: value.trim(), page: 1 }
  })
}
</script>

<style scoped lang="css">
.product-list-search {
  background-color: #fff;
}
</style>
```

===FILE:front/admin/public/favicon.ico===
```
iVBORw0KGgoAAAANSUhEUgAAABAAAAAQCAYAAAAf8/9hAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACJSURBVHgB7ZI7CgIxEIZf5c0cC1uJg0uLuJgcRNsLuwN3IQE3IQEbvAWvAWp9gK2OZ2eZ2e1+fz+P8/n83y+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+Xy+