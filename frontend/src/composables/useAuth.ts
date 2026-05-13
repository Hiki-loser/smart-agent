import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useAppStore } from '@/stores/app.store'
import { ElMessage } from 'element-plus'
import { useI18n } from 'vue-i18n'

export function useAuth() {
  const router = useRouter()
  const authStore = useAuthStore()
  const appStore = useAppStore()
  const { t } = useI18n()

  async function handleLogin(username: string, password: string): Promise<boolean> {
    try {
      await authStore.login(username, password, appStore.deviceId)
      authStore.setUserTitle()
      ElMessage.success(t('auth.loginSuccess'))
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/chat')
      return true
    } catch (err: unknown) {
      const message = (err as { message?: string })?.message || t('error.unknownError')
      ElMessage.error(message)
      return false
    }
  }

  async function handleRegister(
    username: string,
    password: string,
    nickname?: string,
  ): Promise<boolean> {
    try {
      await authStore.register(username, password, nickname)
      authStore.setUserTitle()
      ElMessage.success(t('auth.registerSuccess'))
      router.push('/chat')
      return true
    } catch (err: unknown) {
      const message = (err as { message?: string })?.message || t('error.unknownError')
      ElMessage.error(message)
      return false
    }
  }

  async function handleLogout(): Promise<void> {
    await authStore.logout()
    router.push('/login')
  }

  return {
    handleLogin,
    handleRegister,
    handleLogout,
  }
}
