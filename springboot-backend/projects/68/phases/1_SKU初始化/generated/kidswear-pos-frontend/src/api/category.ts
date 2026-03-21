import request from '@/utils/request'

export interface Category {
  id: number
  name: string
  parentId: number
  sortOrder: number
  status: number
  createTime: string
  updateTime: string
}

export interface CategoryQuery {
  name?: string
  status?: number
}

export interface CategoryApi {
  list: (query?: CategoryQuery) => Promise<Category[]>
  add: (data: Partial<Category>) => Promise<void>
  update: (data: Partial<Category>) => Promise<void>
  delete: (id: number) => Promise<void>
  updateStatus: (id: number, status: number) => Promise<void>
}

const categoryApi: CategoryApi = {
  list: (query) => request.get('/api/category/list', { params: query }),
  add: (data) => request.post('/api/category/add', data),
  update: (data) => request.put('/api/category/update', data),
  delete: (id) => request.delete(`/api/category/delete/${id}`),
  updateStatus: (id, status) => request.put(`/api/category/status/${id}`, { status })
}

export default categoryApi