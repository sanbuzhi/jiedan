import { defineStore } from 'pinia'
import { ref } from 'vue'
import productCategoryApi from '@/api/productCategory'

export const useProductCategoryStore = defineStore('productCategory', () => {
  const categoryList = ref([])
  const categoryTree = ref([])
  const loading = ref(false)

  // 构建树形结构
  const buildTree = (list, parentId = null) => {
    const tree = []
    list.forEach(item => {
      if (item.parentId === parentId) {
        const children = buildTree(list, item.id)
        if (children.length) {
          item.children = children
        }
        tree.push(item)
      }
    })
    return tree.sort((a, b) => a.sort - b.sort)
  }

  // 获取分类列表
  const fetchCategories = async () => {
    loading.value = true
    try {
      const res = await productCategoryApi.getList()
      categoryList.value = res.data || []
      categoryTree.value = buildTree([...categoryList.value])
    } catch (error) {
      console.error('获取分类列表失败:', error)
    } finally {
      loading.value = false
    }
  }

  // 创建分类
  const createCategory = async (data) => {
    const res = await productCategoryApi.create(data)
    await fetchCategories()
    return res
  }

  // 更新分类
  const updateCategory = async (data) => {
    const res = await productCategoryApi.update(data.id, data)
    await fetchCategories()
    return res
  }

  // 删除分类
  const deleteCategory = async (id) => {
    const res = await productCategoryApi.delete(id)
    await fetchCategories()
    return res
  }

  return {
    categoryList,
    categoryTree,
    loading,
    fetchCategories,
    createCategory,
    updateCategory,
    deleteCategory
  }
})