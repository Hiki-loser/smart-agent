import http from './index'
import { ENDPOINTS } from '@/config/api.config'
import type { LoginRequest, LoginResponse, RegisterRequest, UserInfo } from '@/types/auth'

export async function login(data: LoginRequest): Promise<LoginResponse> {
  return http.post(ENDPOINTS.AUTH.LOGIN, data)
}

export async function register(data: RegisterRequest): Promise<LoginResponse> {
  return http.post(ENDPOINTS.AUTH.REGISTER, data)
}

export async function refreshToken(token: string): Promise<string> {
  return http.post(`${ENDPOINTS.AUTH.REFRESH}?refreshToken=${encodeURIComponent(token)}`)
}

export async function getCurrentUser(): Promise<UserInfo> {
  return http.get(ENDPOINTS.USER.ME)
}
