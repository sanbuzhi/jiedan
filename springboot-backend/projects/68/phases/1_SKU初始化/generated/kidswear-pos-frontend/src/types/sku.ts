export interface Sku {
  id?: number
  name: string
  code: string
  categoryId?: number
  categoryName?: string
  price: number
  costPrice?: number
  stock: number
  color?: string
  size?: string
  image?: string
  description?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface SkuQueryParams {
  keyword?: string
  categoryId?: number
  status?: number
  pageNum?: number
  pageSize?: number
}