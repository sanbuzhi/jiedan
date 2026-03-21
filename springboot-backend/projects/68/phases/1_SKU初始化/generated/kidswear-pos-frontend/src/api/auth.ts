import request from '@/utils/request'

export interface LoginParams {
  username: string
  password: string
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  avatar: string
  role: string
}

export interface AuthApi {
  login: (params: LoginParams) => Promise<{ token: string }>
  getUserInfo: () => Promise<UserInfo>
  logout: () => Promise<void>
}

const authApi: AuthApi = {
  login: (params) => request.post('/api/auth/login', params),
  getUserInfo: () => request.get('/api/auth/userinfo'),
  logout: () => request.post('/api/auth/logout')
}

export default authApi