import axios, { type AxiosInstance, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from './auth'
import { useShopStore } from '@/store/shop'
import { mockAdapter } from '@/mock'

// 统一请求封装：注入后台 JWT + 租户 X-Shop-Id，对齐后端 { code, data, msg } / 401 契约
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API || '/api',
  timeout: 15000
})

// 后端未就绪时走 Mock 数据层（仅开发环境）：后端就绪后将 .env 的 VITE_USE_MOCK 置为 false 即可
if (import.meta.env.VITE_USE_MOCK === 'true') {
  service.defaults.adapter = mockAdapter
}

service.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const headers = config.headers as Record<string, unknown>
  const token = getToken()
  if (token) headers['Authorization'] = `Bearer ${token}`

  const shopStore = useShopStore()
  if (shopStore.currentShopId) headers['X-Shop-Id'] = String(shopStore.currentShopId)
  return config
})

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    // 约定：code === 0 表示成功，直接返回内层 data
    if (res && res.code === 0) {
      return res.data
    }
    if (response.status === 401 || (res && res.code === 401)) {
      ElMessage.error(res?.msg || '登录已过期，请重新登录')
      removeToken()
      window.location.href = '/login'
      return Promise.reject(new Error(res?.msg || '未登录'))
    }
    ElMessage.error(res?.msg || '请求失败')
    return Promise.reject(new Error(res?.msg || 'Error'))
  },
  (error) => {
    ElMessage.error(error?.message || '网络错误')
    return Promise.reject(error)
  }
)

export default service
