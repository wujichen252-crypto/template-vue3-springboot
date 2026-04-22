import axios, { type AxiosInstance, type AxiosError, type InternalAxiosRequestConfig, type AxiosResponse, type CancelTokenSource } from 'axios'
import { ElMessage } from 'element-plus'
import { getAccessToken, getRefreshToken, setAccessToken, removeAccessToken, removeRefreshToken } from './storage'
import router from '../router'

const BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api'

interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
}

const service: AxiosInstance = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

export const createCancelToken = (): CancelTokenSource => {
  return axios.CancelToken.source()
}

service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getAccessToken()
    if (token && config.headers) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error: AxiosError) => {
    return Promise.reject(error)
  }
)

service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  async (error: AxiosError<{ code?: number; msg?: string }>) => {
    if (axios.isCancel(error)) {
      return Promise.reject(error)
    }

    const originalRequest = error.config as CustomAxiosRequestConfig

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true

      const refreshToken = getRefreshToken()
      if (refreshToken) {
        try {
          const res = await axios.post(`${BASE_URL}/v1/auth/refresh`, {}, {
            headers: {
              'X-Refresh-Token': refreshToken
            }
          })
          if (res.data.code === 200) {
            setAccessToken(res.data.data.accessToken)
            if (originalRequest.headers) {
              originalRequest.headers['Authorization'] = `Bearer ${res.data.data.accessToken}`
            }
            return service(originalRequest)
          }
        } catch {
          removeAccessToken()
          removeRefreshToken()
          router.push('/login')
        }
      } else {
        removeAccessToken()
        router.push('/login')
      }
    }

    const msg = error.response?.data?.msg || error.message || '网络错误'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default service
