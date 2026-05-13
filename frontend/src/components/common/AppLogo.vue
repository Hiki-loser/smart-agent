<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  collapsed: boolean
}>()

const { t } = useI18n()
</script>

<template>
  <div class="app-logo" :class="{ collapsed }">
    <div class="logo-icon">
      <span class="logo-emoji">🌸</span>
    </div>
    <div v-if="!collapsed" class="logo-text">
      <span class="logo-name">{{ t('app.name') }}</span>
      <span class="logo-slogan">{{ t('app.slogan') }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.app-logo {
  display: flex;
  align-items: center;
  gap: 12px;

  &.collapsed {
    justify-content: center;
  }
}

.logo-icon {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-md);
  background: linear-gradient(135deg, var(--color-primary), var(--color-secondary));
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .logo-emoji {
    font-size: 18px;
    line-height: 1;
  }
}

.logo-text {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.logo-name {
  font-size: 1.125rem;
  font-weight: 700;
  color: var(--color-text-primary);
  line-height: 1.2;
}

.logo-slogan {
  font-size: 0.7rem;
  color: var(--color-text-muted);
  line-height: 1.2;
}
</style>
