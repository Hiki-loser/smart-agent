import axios, { type AxiosError, type InternalAxiosRequestConfig } from 'axios'
import { API_BASE_URL, API_TIMEOUT } from '@/config/api.config'
import { getAccessToken, getRefreshToken, removeStoredAuth } from '@/utils/token'
import { ENDPOINTS } from '@/config/api.config'

// Track refresh state to queue concurrent 401 requests
let isRefreshing = false
let refreshQueue: Array<{
  resolve: (token: string) => void
  reject: (error: Error) => void
}> = []

function queueRefreshRequest(resolve: (token: string) => void, reject: (error: Error) => void) {
  refreshQueue.push({ resolve, reject })
}

function drainRefreshQueue(token: string | null, error: Error | null) {
  refreshQueue.forEach(({ resolve, reject }) => {
    if (token) resolve(token)
    else reject(error!)
  })
  refreshQueue = []
}

const http = axios.create({
  baseURL: API_BASE_URL,
  timeout: API_TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor: inject auth token and device ID
http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = getAccessToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  // Add device ID if available
  const storedDeviceId = localStorage.getItem('smartagent_device_id')
  if (storedDeviceId) {
    config.headers['Device-Type'] = 'web'
    config.headers['X-Device-Id'] = storedDeviceId
  }
  return config
})

// Response interceptor: unwrap ApiResponse, handle 401 refresh
http.interceptors.response.use(
  (response) => {
    const body = response.data
    // Unwrap ApiResponse<T> -> data
    if (body && typeof body.code === 'number') {
      if (body.code === 200) {
        return body.data !== undefined ? body.data : body
      }
      // Business error
      const error = new Error(body.message || 'Unknown error') as Error & {
        code: number
        traceId: string
      }
      error.code = body.code
      error.traceId = body.traceId
      return Promise.reject(error)
    }
    return body
  },
  async (error: AxiosError<{ code?: number; message?: string; traceId?: string }>) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean }

    // Handle 401: attempt token refresh
    if (error.response?.status === 401 && !originalRequest._retry) {
      const refreshToken = getRefreshToken()

      if (!refreshToken) {
        removeStoredAuth()
        window.location.href = '/login'
        return Promise.reject(error)
      }

      if (isRefreshing) {
        // Queue this request until refresh completes
        return new Promise((resolve, reject) => {
          queueRefreshRequest(
            (token: string) => {
              originalRequest.headers.Authorization = `Bearer ${token}`
              resolve(http(originalRequest))
            },
            (err: Error) => {
              reject(err)
            },
          )
        })
      }

      isRefreshing = true
      originalRequest._retry = true

      try {
        const response = await axios.post(
          `${API_BASE_URL}${ENDPOINTS.AUTH.REFRESH}?refreshToken=${encodeURIComponent(refreshToken)}`,
        )
        const data = response.data
        const newAccessToken = typeof data === 'string' ? data : data?.data
        if (!newAccessToken) throw new Error('Refresh returned no token')

        // Update stored auth
        const stored = JSON.parse(localStorage.getItem('smartagent_auth') || '{}')
        stored.accessToken = newAccessToken
        localStorage.setItem('smartagent_auth', JSON.stringify(stored))

        drainRefreshQueue(newAccessToken, null)

        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`
        return http(originalRequest)
      } catch (refreshError) {
        drainRefreshQueue(null, refreshError as Error)
        removeStoredAuth()
        window.location.href = '/login'
        return Promise.reject(refreshError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default http
