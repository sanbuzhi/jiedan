export * from './sku'
export * from './category'
export * from './user'

// 通用响应类型
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

// 分页响应类型
export interface PageResponse<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}