<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useI18n } from 'vue-i18n'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { useAuth } from '@/composables/useAuth'

defineProps<{
  collapsed: boolean
}>()

const router = useRouter()
const authStore = useAuthStore()
const { t } = useI18n()
const { handleLogout } = useAuth()

const user = authStore.user

function goToProfile() {
  router.push('/profile')
}

function goToApiKeys() {
  router.push('/api-keys')
}
</script>

<template>
  <el-dropdown trigger="click" placement="top-start">
    <div class="user-dropdown-trigger" :class="{ collapsed }">
      <UserAvatar
        :src="user?.avatar ?? null"
        :nickname="user?.nickname || user?.username || '?'"
        size="sm"
      />
      <div v-if="!collapsed" class="user-info">
        <span class="user-nickname">{{ user?.nickname || user?.username }}</span>
        <span class="user-username">@{{ user?.username }}</span>
      </div>
      <el-icon v-if="!collapsed" class="dropdown-arrow">
        <component :is="'ArrowUp'" />
      </el-icon>
    </div>

    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item @click="goToProfile">
          <el-icon><component :is="'User'" /></el-icon>
          {{ t('nav.profile') }}
        </el-dropdown-item>
        <el-dropdown-item @click="goToApiKeys">
          <el-icon><component :is="'Key'" /></el-icon>
          {{ t('nav.apiKeys') }}
        </el-dropdown-item>
        <el-dropdown-item divided @click="handleLogout">
          <el-icon><component :is="'SwitchButton'" /></el-icon>
          {{ t('nav.logout') }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<style lang="scss" scoped>
.user-dropdown-trigger {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: background-color var(--transition-fast);

  &:hover {
    background-color: var(--color-border-light);
  }

  &.collapsed {
    justify-content: center;
    padding: 8px 4px;
  }
}

.user-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;

  .user-nickname {
    font-size: 0.875rem;
    font-weight: 600;
    color: var(--color-text-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }

  .user-username {
    font-size: 0.75rem;
    color: var(--color-text-muted);
  }
}

.dropdown-arrow {
  font-size: 12px;
  color: var(--color-text-muted);
}
</style>
