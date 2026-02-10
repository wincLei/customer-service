import api from './index'
import { StorageKeys } from '@/constants'

export interface LoginRequest {
  username: string
  password: string
  captcha?: string
  captchaKey?: string
}

export interface UserPermissions {
  menus: string[]
  actions: string[]
}

export interface LoginResponse {
  token: string
  user: {
    id?: number
    username?: string
    email?: string
    role?: string
    avatar?: string
    permissions?: UserPermissions
    projectIds?: number[]  // 关联的项目ID列表
  }
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}

export const authService = {
  // 登录
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await api.post<any, ApiResponse<LoginResponse>>('/admin/auth/login', credentials)
    return response.data
  },

  // 登出
  logout: async (): Promise<void> => {
    await api.post('/admin/auth/logout')
    localStorage.removeItem(StorageKeys.AUTH_TOKEN)
    localStorage.removeItem(StorageKeys.USER_INFO)
  },

  // 获取当前用户信息
  getCurrentUser: async () => {
    const response = await api.get('/admin/auth/me')
    return response.data
  },

  // 刷新令牌
  refreshToken: async (): Promise<string> => {
    const response = await api.post<any, ApiResponse<LoginResponse>>('/pub/auth/refresh')
    return response.data.token
  },

  // 获取保存的令牌
  getToken: (): string | null => {
    return localStorage.getItem(StorageKeys.AUTH_TOKEN)
  },

  // 获取保存的用户信息
  getUserInfo: () => {
    const info = localStorage.getItem(StorageKeys.USER_INFO)
    return info ? JSON.parse(info) : null
  },

  // 检查是否已认证
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem(StorageKeys.AUTH_TOKEN)
  },
}

export default authService
