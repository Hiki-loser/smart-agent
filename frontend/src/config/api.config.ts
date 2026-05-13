const API_BASE_URL = import.meta.env.VITE_API_BASE_URL
const API_TIMEOUT = Number(import.meta.env.VITE_API_TIMEOUT) || 30000
const SSE_TIMEOUT = Number(import.meta.env.VITE_SSE_TIMEOUT) || 1800000

export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/api/user/login',
    REGISTER: '/api/user/register',
    REFRESH: '/api/user/refresh',
  },
  USER: {
    ME: '/api/user/me',
    UPDATE: '/api/user/update',
    LOGOUT: '/api/user/logout',
    API_KEY: '/api/user/api-key',
    API_KEY_LIST: '/api/user/api-key/list',
    API_KEY_DELETE: (id: number) => `/api/user/api-key/${id}`,
  },
  CHAT: {
    SESSIONS: '/api/chat/sessions',
    SESSION_MESSAGES: (sessionId: string) => `/api/chat/sessions/${sessionId}/messages`,
    MESSAGES: '/api/chat/messages',
  },
} as const

export { API_BASE_URL, API_TIMEOUT, SSE_TIMEOUT }
