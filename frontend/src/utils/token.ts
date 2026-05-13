import { AUTH_STORAGE_KEY, TOKEN_REFRESH_BUFFER_SECONDS } from '@/config/app.config'

interface StoredAuth {
  accessToken: string
  refreshToken: string
  expiresAt: number
}

export function getStoredAuth(): StoredAuth | null {
  try {
    const raw = localStorage.getItem(AUTH_STORAGE_KEY)
    if (!raw) return null
    return JSON.parse(raw) as StoredAuth
  } catch {
    return null
  }
}

export function setStoredAuth(auth: StoredAuth): void {
  localStorage.setItem(AUTH_STORAGE_KEY, JSON.stringify(auth))
}

export function removeStoredAuth(): void {
  localStorage.removeItem(AUTH_STORAGE_KEY)
}

export function isTokenExpired(expiresAt: number | null): boolean {
  if (!expiresAt) return true
  return Date.now() > expiresAt * 1000
}

export function needsTokenRefresh(expiresAt: number | null): boolean {
  if (!expiresAt) return false
  const bufferMs = TOKEN_REFRESH_BUFFER_SECONDS * 1000
  return Date.now() > expiresAt * 1000 - bufferMs
}

export function getAccessToken(): string | null {
  const auth = getStoredAuth()
  return auth?.accessToken ?? null
}

export function getRefreshToken(): string | null {
  const auth = getStoredAuth()
  return auth?.refreshToken ?? null
}

export function getTokenExpiresAt(): number | null {
  const auth = getStoredAuth()
  return auth?.expiresAt ?? null
}
