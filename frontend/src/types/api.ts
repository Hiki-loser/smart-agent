// Generic API response wrapper (matches backend ApiResponse<T>)
export interface ApiResponse<T = unknown> {
  code: number
  message: string
  data: T
  timestamp: number
  traceId: string
}

// Pagination
export interface PageQuery {
  page: number
  size: number
}

export interface PageResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
}

// Error
export interface ApiError {
  code: number
  message: string
  traceId?: string
}
