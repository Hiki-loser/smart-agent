<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app.store'
import { useChatStore } from '@/stores/chat.store'
import { useI18n } from 'vue-i18n'
import AppLogo from '@/components/common/AppLogo.vue'
import SidebarMenu from './SidebarMenu.vue'
import UserDropdown from './UserDropdown.vue'
import SessionListItem from '@/components/chat/SessionListItem.vue'

const router = useRouter()
const appStore = useAppStore()
const chatStore = useChatStore()
const { t } = useI18n()

const expanded = computed(() => appStore.sidebarExpanded)
const sessions = computed(() => chatStore.sessions)
const currentSessionId = computed(() => chatStore.currentSessionId)

async function handleNewSession() {
  const session = await chatStore.createSession(
    t('chat.defaultSessionTitle'),
    'default',
  )
  if (session) {
    router.push(`/chat/${session.id}`)
  }
}

function handleSelectSession(sessionId: string) {
  chatStore.selectSession(sessionId)
  router.push(`/chat/${sessionId}`)
}
</script>

<template>
  <aside class="app-sidebar" :class="{ expanded, collapsed: !expanded }">
    <!-- Logo area -->
    <div class="sidebar-header">
      <AppLogo :collapsed="!expanded" />
    </div>

    <!-- Navigation menu -->
    <nav class="sidebar-nav">
      <SidebarMenu :collapsed="!expanded" />
    </nav>

    <!-- Session list -->
    <div class="sidebar-sessions" v-if="expanded">
      <div class="sessions-header">
        <span class="sessions-title">{{ t('chat.sessionHistory') }}</span>
        <el-button
          size="small"
          type="primary"
          :icon="'Plus'"
          circle
          @click="handleNewSession"
        />
      </div>

      <div class="sessions-list">
        <div v-if="sessions.length === 0" class="sessions-empty">
          {{ t('chat.noSessions') }}
        </div>
        <SessionListItem
          v-for="session in sessions"
          :key="session.id"
          :session="session"
          :active="session.id === currentSessionId"
          @click="handleSelectSession(session.id)"
        />
      </div>
    </div>

    <!-- User area at bottom -->
    <div class="sidebar-footer">
      <UserDropdown :collapsed="!expanded" />
    </div>
  </aside>
</template>

<style lang="scss" scoped>
.app-sidebar {
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg-sidebar);
  border-right: 1px solid var(--color-border-light);
  transition: width var(--transition-normal);
  height: 100vh;
  overflow: hidden;

  &.expanded {
    width: 260px;
  }

  &.collapsed {
    width: 64px;
  }
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-bottom: 1px solid var(--color-border-light);
  min-height: 64px;
}

.sidebar-nav {
  padding: 8px;
}

.sidebar-sessions {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  padding: 0 8px;
}

.sessions-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 4px;
  margin-bottom: 4px;
}

.sessions-title {
  font-size: 12px;
  font-weight: 600;
  color: var(--color-text-muted);
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.sessions-list {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.sessions-empty {
  padding: 16px 8px;
  text-align: center;
  color: var(--color-text-muted);
  font-size: var(--font-size-sm, 0.875rem);
}

.sidebar-footer {
  border-top: 1px solid var(--color-border-light);
  padding: 8px;
}
</style>
