import axios, { AxiosResponse } from 'axios'

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

const api = axios.create({
  baseURL: '/api',
  timeout: 10000,
})

// 请求拦截器
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('auth_token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// 响应拦截器
api.interceptors.response.use(
  (response: AxiosResponse<ApiResponse>) => {
    // 返回响应体中的数据
    return response.data as unknown as AxiosResponse
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('auth_token')
      localStorage.removeItem('user_info')
      window.location.href = '/login'
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

export default api
