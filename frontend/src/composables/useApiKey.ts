import { ref } from 'vue'
import * as userApi from '@/api/user'
import type { ApiKey } from '@/types/user'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

export function useApiKey() {
  const apiKeys = ref<ApiKey[]>([])
  const loading = ref(false)
  const { t } = useI18n()

  async function fetchApiKeys(): Promise<void> {
    loading.value = true
    try {
      apiKeys.value = await userApi.listApiKeys()
    } catch (err: unknown) {
      const message = (err as { message?: string })?.message || t('error.unknownError')
      ElMessage.error(message)
    } finally {
      loading.value = false
    }
  }

  async function createApiKey(name: string, expireDays?: number): Promise<ApiKey | null> {
    try {
      const key = await userApi.createApiKey(name, expireDays)
      apiKeys.value.unshift(key)
      ElMessage.success(t('apiKey.createSuccess'))
      return key
    } catch (err: unknown) {
      const message = (err as { message?: string })?.message || t('error.unknownError')
      ElMessage.error(message)
      return null
    }
  }

  async function revokeApiKey(id: number): Promise<boolean> {
    try {
      await userApi.deleteApiKey(id)
      const idx = apiKeys.value.findIndex((k) => k.id === id)
      if (idx !== -1) {
        apiKeys.value[idx] = { ...apiKeys.value[idx], status: 0 }
      }
      return true
    } catch (err: unknown) {
      const message = (err as { message?: string })?.message || t('error.unknownError')
      ElMessage.error(message)
      return false
    }
  }

  return {
    apiKeys,
    loading,
    fetchApiKeys,
    createApiKey,
    revokeApiKey,
  }
}
