/**
 * 通用API响应类型
 */
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应类型
 */
export interface PageResponse<T = any> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

/**
 * 分页请求参数类型
 */
export interface PageParams {
  current: number
  size: number
}

/**
 * 通用排序参数类型
 */
export interface SortParams {
  sortField?: string
  sortOrder?: 'asc' | 'desc'
}

/**
 * 通用查询参数类型
 */
export interface QueryParams extends PageParams, SortParams {
  [key: string]: any
}

/**
 * 通用下拉选项类型
 */
export interface Option<T = string | number> {
  label: string
  value: T
  disabled?: boolean
  children?: Option<T>[]
}

/**
 * 通用树形结构类型
 */
export interface TreeNode<T = any> {
  id: string | number
  parentId: string | number
  name: string
  children?: TreeNode<T>[]
  [key: string]: any
}