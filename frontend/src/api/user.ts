import http from './index'
import { ENDPOINTS } from '@/config/api.config'
import type { User, UserUpdateRequest, ApiKey } from '@/types/user'

export async function getMe(): Promise<User> {
  return http.get(ENDPOINTS.USER.ME)
}

export async function updateProfile(data: UserUpdateRequest): Promise<User> {
  return http.put(ENDPOINTS.USER.UPDATE, data)
}

export async function logout(): Promise<void> {
  return http.post(ENDPOINTS.USER.LOGOUT)
}

export async function createApiKey(name: string, expireDays?: number): Promise<ApiKey> {
  const params = new URLSearchParams()
  params.set('name', name)
  if (expireDays) params.set('expireDays', String(expireDays))
  return http.post(`${ENDPOINTS.USER.API_KEY}?${params.toString()}`)
}

export async function listApiKeys(): Promise<ApiKey[]> {
  return http.get(ENDPOINTS.USER.API_KEY_LIST)
}

export async function deleteApiKey(id: number): Promise<void> {
  return http.delete(ENDPOINTS.USER.API_KEY_DELETE(id))
}
