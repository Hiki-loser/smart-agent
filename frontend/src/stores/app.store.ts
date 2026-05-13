import { defineStore } from 'pinia'
import { ref } from 'vue'
import { v4 as uuidv4 } from 'uuid'
import {
  SIDEBAR_DEFAULT_COLLAPSED,
  DEFAULT_LOCALE,
  DEVICE_ID_STORAGE_KEY,
  LOCALE_STORAGE_KEY,
  THEME_STORAGE_KEY,
  type SupportedLocale,
} from '@/config/app.config'
import { applyTheme } from '@/config/theme.config'

export const useAppStore = defineStore('app', () => {
  const sidebarExpanded = ref(!SIDEBAR_DEFAULT_COLLAPSED)
  const sidebarMobileOpen = ref(false)
  const locale = ref<SupportedLocale>(DEFAULT_LOCALE)
  const theme = ref<'light' | 'dark'>('light')
  const deviceId = ref('')

  function toggleSidebar(): void {
    sidebarExpanded.value = !sidebarExpanded.value
  }

  function toggleMobileSidebar(): void {
    sidebarMobileOpen.value = !sidebarMobileOpen.value
  }

  function setLocale(loc: SupportedLocale): void {
    locale.value = loc
    localStorage.setItem(LOCALE_STORAGE_KEY, loc)
  }

  function setTheme(mode: 'light' | 'dark'): void {
    theme.value = mode
    localStorage.setItem(THEME_STORAGE_KEY, mode)
    applyTheme(mode)
    // Toggle dark class on html element for Element Plus
    document.documentElement.classList.toggle('dark', mode === 'dark')
  }

  function toggleTheme(): void {
    setTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  function initApp(): void {
    // Device ID
    let id = localStorage.getItem(DEVICE_ID_STORAGE_KEY)
    if (!id) {
      id = uuidv4()
      localStorage.setItem(DEVICE_ID_STORAGE_KEY, id)
    }
    deviceId.value = id

    // Locale
    const savedLocale = localStorage.getItem(LOCALE_STORAGE_KEY) as SupportedLocale | null
    if (savedLocale) {
      locale.value = savedLocale
    }

    // Theme
    const savedTheme = localStorage.getItem(THEME_STORAGE_KEY) as 'light' | 'dark' | null
    if (savedTheme) {
      setTheme(savedTheme)
    }
  }

  return {
    sidebarExpanded,
    sidebarMobileOpen,
    locale,
    theme,
    deviceId,
    toggleSidebar,
    toggleMobileSidebar,
    setLocale,
    setTheme,
    toggleTheme,
    initApp,
  }
})
