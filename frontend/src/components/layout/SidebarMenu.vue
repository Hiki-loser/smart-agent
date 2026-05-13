<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'

defineProps<{
  collapsed: boolean
}>()

const router = useRouter()
const route = useRoute()
const { t } = useI18n()

const menuItems = [
  { key: 'chat', route: '/chat', icon: 'ChatDotRound', label: 'nav.chat' },
  { key: 'profile', route: '/profile', icon: 'User', label: 'nav.profile' },
  { key: 'apiKeys', route: '/api-keys', icon: 'Key', label: 'nav.apiKeys' },
]

function navigateTo(path: string) {
  router.push(path)
}

function isActive(item: typeof menuItems[0]): boolean {
  if (item.key === 'chat') return route.path.startsWith('/chat')
  return route.path === item.route
}
</script>

<template>
  <div class="sidebar-menu">
    <div
      v-for="item in menuItems"
      :key="item.key"
      class="menu-item"
      :class="{ active: isActive(item), collapsed }"
      @click="navigateTo(item.route)"
    >
      <el-icon :size="20">
        <component :is="item.icon" />
      </el-icon>
      <span v-if="!collapsed" class="menu-label">{{ t(item.label) }}</span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.sidebar-menu {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
  color: var(--color-text-secondary);

  .menu-label {
    font-size: 0.9375rem;
    font-weight: 500;
  }

  &:hover {
    background-color: var(--color-primary-light);
    color: var(--color-primary);
  }

  &.active {
    background-color: var(--color-primary);
    color: var(--color-text-on-primary);

    &:hover {
      background-color: var(--color-primary-dark);
    }
  }

  &.collapsed {
    justify-content: center;
    padding: 10px;
  }
}
</style>
