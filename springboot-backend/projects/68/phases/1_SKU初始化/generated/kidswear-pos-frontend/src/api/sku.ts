import request from '@/utils/request'

export interface Sku {
  id?: number
  skuCode: string
  spuName: string
  categoryId: number
  categoryName?: string
  size: string
  color: string
  image?: string
  costPrice: number
  retailPrice: number
  memberPrice: number
  stock: number
  safetyStock: number
  status: number
  createTime?: string
  updateTime?: string
}

export interface Category {
  id?: number
  name: string
  sortOrder: number
  status: number
}

export interface PageQuery {
  current: number
  size: number
  spuName?: string
  categoryId?: number
  status?: number
}

export interface PageResult<T> {
  total: number
  records: T[]
}

// 获取分类列表（启用）
export const getEnabledCategoryList = () => {
  return request.get<Category[]>('/api/category/list/enabled')
}

// 获取SKU分页
export const getSkuPage = (params: PageQuery) => {
  return request.get<PageResult<Sku>>('/api/sku/page', { params })
}

// 生成SKU编码
export const generateSkuCode = () => {
  return request.get<string>('/api/sku/code')
}

// 根据ID获取SKU
export const getSkuById = (id: number) => {
  return request.get<Sku>(`/api/sku/${id}`)
}

// 新增SKU
export const addSku = (data: Sku) => {
  return request.post('/api/sku', data)
}

// 修改SKU
export const updateSku = (data: Sku) => {
  return request.put('/api/sku', data)
}

// 删除SKU
export const deleteSku = (id: number) => {
  return request.delete(`/api/sku/${id}`)
}