import axios, { AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'
import { StorageKeys, API_TIMEOUT } from '@/constants'
import i18n from '@/locales'

const { t } = i18n.global

export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

const api = axios.create({
  baseURL: '/api',
  timeout: API_TIMEOUT,
})

// 请求拦截器
api.interceptors.request.use((config) => {
  const token = localStorage.getItem(StorageKeys.AUTH_TOKEN)
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
      // 未登录或token过期
      localStorage.removeItem(StorageKeys.AUTH_TOKEN)
      localStorage.removeItem(StorageKeys.USER_INFO)
      ElMessage.error(t('auth.loginExpired'))
      window.location.href = '/login'
    } else if (error.response?.status === 403) {
      // 无权限
      ElMessage.error(t('auth.noPermission'))
    }
    
    // 返回错误响应
    if (error.response?.data) {
      return Promise.reject(error.response.data)
    }
    
    return Promise.reject({
      code: error.response?.status || 500,
      message: error.message || t('auth.requestFailed'),
      data: null,
    })
  }
)

export default api
