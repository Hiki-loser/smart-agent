import { useAppStore } from '@/stores/app.store'

export function useTheme() {
  const appStore = useAppStore()

  function toggleTheme(): void {
    appStore.toggleTheme()
  }

  function setTheme(mode: 'light' | 'dark'): void {
    appStore.setTheme(mode)
  }

  return {
    theme: appStore.theme,
    toggleTheme,
    setTheme,
  }
}
