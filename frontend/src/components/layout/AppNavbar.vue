<script setup lang="ts">
import { useAppStore } from '@/stores/app.store'
import { useI18n } from 'vue-i18n'

const appStore = useAppStore()
const { t } = useI18n()
</script>

<template>
  <header class="app-navbar">
    <div class="navbar-left">
      <el-button
        class="mobile-menu-btn"
        text
        @click="appStore.toggleMobileSidebar()"
      >
        <el-icon><component :is="'Expand'" /></el-icon>
      </el-button>
      <span class="navbar-title">{{ t('app.name') }}</span>
    </div>

    <div class="navbar-right">
      <el-switch
        :model-value="appStore.theme === 'dark'"
        inline-prompt
        :active-icon="'Moon'"
        :inactive-icon="'Sunny'"
        @change="appStore.toggleTheme()"
      />
      <el-select
        :model-value="appStore.locale"
        size="small"
        class="locale-switch"
        @change="appStore.setLocale($event as 'zh-CN' | 'en')"
      >
        <el-option value="zh-CN" :label="t('common.language.zh')" />
        <el-option value="en" :label="t('common.language.en')" />
      </el-select>
    </div>
  </header>
</template>

<style lang="scss" scoped>
.app-navbar {
  display: none;
  align-items: center;
  justify-content: space-between;
  padding: 0 16px;
  height: 56px;
  border-bottom: 1px solid var(--color-border-light);
  background-color: var(--color-bg-secondary);
  flex-shrink: 0;
}

.navbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.navbar-title {
  font-size: 1.125rem;
  font-weight: 600;
  color: var(--color-text-primary);
}

.navbar-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.locale-switch {
  width: 100px;
}

@media (max-width: 768px) {
  .app-navbar {
    display: flex;
  }
}
</style>
