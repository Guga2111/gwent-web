import { useAuthStore } from '@/stores/authStore'
import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'

const client = axios.create({
  baseURL: '/',
  withCredentials: true,
})

client.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

let isRefreshing = false
let failedQueue: Array<{
  resolve: (value: unknown) => void
  reject: (reason: unknown) => void
  config: InternalAxiosRequestConfig
}> = []

function processQueue(error: AxiosError | null) {
  for (const entry of failedQueue) {
    if (error) {
      entry.reject(error)
    } else {
      client.request(entry.config).then(entry.resolve).catch(entry.reject)
    }
  }
  failedQueue = []
}

client.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    if (error.response?.status === 403) {
      useAuthStore.getState().logout()
      window.location.href = '/login'
      return Promise.reject(error)
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject, config: originalRequest })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshResponse = await axios.post('/api/auth/refresh', null, { withCredentials: true })
        const newAccessToken = refreshResponse.headers['authorization']?.replace('Bearer ', '')

        if (newAccessToken) {
          useAuthStore.getState().setToken(newAccessToken)
          originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        }

        processQueue(null)
        return client.request(originalRequest)
      } catch (refreshError) {
        processQueue(refreshError as AxiosError)
        useAuthStore.getState().logout()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default client
