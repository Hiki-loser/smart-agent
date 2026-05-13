import type { Router } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useAppStore } from '@/stores/app.store'
import { APP_NAME } from '@/config/app.config'

let initialized = false

export function setupGuards(router: Router): void {
  router.beforeEach(async (to, _from, next) => {
    // App initialization on first visit
    if (!initialized) {
      const authStore = useAuthStore()
      const appStore = useAppStore()
      appStore.initApp()
      await authStore.initAuth()
      initialized = true
    }

    const authStore = useAuthStore()
    const requiresAuth = to.meta.requiresAuth !== false

    // If route requires auth and user is not authenticated
    if (requiresAuth && !authStore.isAuthenticated) {
      return next({ name: 'Login', query: { redirect: to.fullPath } })
    }

    // If on auth page but already logged in
    if (!requiresAuth && authStore.isAuthenticated) {
      return next({ name: 'Chat' })
    }

    next()
  })

  router.afterEach((to) => {
    // Update document title
    const titleKey = to.meta.title as string | undefined
    if (titleKey) {
      // Title will be set by components using i18n
      document.title = `${APP_NAME} - ${titleKey}`
    } else {
      document.title = APP_NAME
    }
  })
}
