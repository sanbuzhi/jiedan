export interface User {
  id?: number
  username: string
  password?: string
  nickname?: string
  avatar?: string
  role?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
}