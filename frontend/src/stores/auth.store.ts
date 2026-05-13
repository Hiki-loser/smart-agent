import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types/auth'
import * as authApi from '@/api/auth'
import * as userApi from '@/api/user'
import { getStoredAuth, setStoredAuth, removeStoredAuth, isTokenExpired, needsTokenRefresh } from '@/utils/token'
import { APP_NAME } from '@/config/app.config'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null)
  const refreshToken = ref<string | null>(null)
  const expiresAt = ref<number | null>(null)
  const user = ref<UserInfo | null>(null)

  const isAuthenticated = computed(() => !!token.value && !isTokenExpired(expiresAt.value))

  function setAuth(accessToken: string, refreshTokenVal: string, expiresIn: number) {
    token.value = accessToken
    refreshToken.value = refreshTokenVal
    // expiresIn is in seconds from backend, convert to absolute timestamp
    expiresAt.value = Math.floor(Date.now() / 1000) + expiresIn

    setStoredAuth({
      accessToken,
      refreshToken: refreshTokenVal,
      expiresAt: expiresAt.value,
    })
  }

  function clearAuth() {
    token.value = null
    refreshToken.value = null
    expiresAt.value = null
    user.value = null
    removeStoredAuth()
  }

  async function login(username: string, password: string, deviceId?: string): Promise<void> {
    const response = await authApi.login({
      username,
      password,
      deviceType: 'web',
      deviceId,
    })
    setAuth(response.accessToken, response.refreshToken, response.expiresIn)
    user.value = response.user
  }

  async function register(username: string, password: string, nickname?: string): Promise<void> {
    const response = await authApi.register({
      username,
      password,
      nickname,
      deviceType: 'web',
    })
    setAuth(response.accessToken, response.refreshToken, response.expiresIn)
    user.value = response.user
  }

  async function logout(): Promise<void> {
    try {
      await userApi.logout()
    } catch {
      // ignore logout API errors
    } finally {
      clearAuth()
    }
  }

  async function fetchCurrentUser(): Promise<void> {
    user.value = await authApi.getCurrentUser()
  }

  async function refreshAccessToken(): Promise<string | null> {
    if (!refreshToken.value) return null
    try {
      const newToken = await authApi.refreshToken(refreshToken.value)
      if (newToken) {
        token.value = newToken
        // Update storage
        const stored = getStoredAuth()
        if (stored) {
          stored.accessToken = newToken
          setStoredAuth(stored)
        }
        return newToken
      }
      return null
    } catch {
      clearAuth()
      return null
    }
  }

  async function updateProfile(data: { nickname?: string; avatar?: string }): Promise<void> {
    const updated = await userApi.updateProfile(data)
    user.value = updated
  }

  async function initAuth(): Promise<void> {
    const stored = getStoredAuth()
    if (!stored) return

    token.value = stored.accessToken
    refreshToken.value = stored.refreshToken
    expiresAt.value = stored.expiresAt

    // If token is expired, try refresh
    if (isTokenExpired(expiresAt.value)) {
      const success = await refreshAccessToken()
      if (!success) {
        clearAuth()
        return
      }
    } else if (needsTokenRefresh(expiresAt.value)) {
      // Proactive refresh in background
      refreshAccessToken()
    }

    // Fetch user info if we have a valid token
    if (token.value) {
      try {
        await fetchCurrentUser()
      } catch {
        clearAuth()
      }
    }
  }

  function setUserTitle() {
    document.title = user.value
      ? `${APP_NAME} - ${user.value.nickname || user.value.username}`
      : APP_NAME
  }

  return {
    token,
    refreshToken,
    expiresAt,
    user,
    isAuthenticated,
    setAuth,
    clearAuth,
    login,
    register,
    logout,
    fetchCurrentUser,
    refreshAccessToken,
    updateProfile,
    initAuth,
    setUserTitle,
  }
})
