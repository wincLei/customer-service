import api from './index'

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
    localStorage.removeItem('auth_token')
    localStorage.removeItem('user_info')
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
    return localStorage.getItem('auth_token')
  },

  // 获取保存的用户信息
  getUserInfo: () => {
    const info = localStorage.getItem('user_info')
    return info ? JSON.parse(info) : null
  },

  // 检查是否已认证
  isAuthenticated: (): boolean => {
    return !!localStorage.getItem('auth_token')
  },
}

export default authService
