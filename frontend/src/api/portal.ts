import axios, { AxiosResponse } from 'axios'

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

const PORTAL_TOKEN_KEY = 'portal_token'

const portalApi = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器
portalApi.interceptors.request.use((config) => {
  const token = localStorage.getItem(PORTAL_TOKEN_KEY)
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
portalApi.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 返回响应体中的数据
    return response.data as unknown as AxiosResponse
  },
  (error) => {
    if (error.response?.status === 401) {
      // Portal token 过期或无效
      localStorage.removeItem(PORTAL_TOKEN_KEY)
      console.warn('Portal token expired or invalid')
    }
    
    // 返回错误响应
    if (error.response?.data) {
      return Promise.reject(error.response.data)
    }
    
    return Promise.reject({
      code: error.response?.status || 500,
      message: error.message || '请求失败',
      data: null,
    })
  }
)

/**
 * 保存 Portal Token
 */
export function setPortalToken(token: string) {
  localStorage.setItem(PORTAL_TOKEN_KEY, token)
}

/**
 * 获取 Portal Token
 */
export function getPortalToken(): string | null {
  return localStorage.getItem(PORTAL_TOKEN_KEY)
}

/**
 * 清除 Portal Token
 */
export function clearPortalToken() {
  localStorage.removeItem(PORTAL_TOKEN_KEY)
}

export default portalApi
