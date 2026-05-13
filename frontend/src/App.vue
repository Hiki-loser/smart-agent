<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useI18n } from 'vue-i18n'
import AppLayout from '@/components/layout/AppLayout.vue'

const route = useRoute()
const { locale } = useI18n()

const layout = computed(() => route.meta.layout as string | undefined)

// Keep document language in sync with i18n locale
import { watch } from 'vue'
watch(locale, (val) => {
  document.documentElement.lang = val === 'zh-CN' ? 'zh-CN' : 'en'
  localStorage.setItem('smartagent_locale', val)
})
</script>

<template>
  <!-- Blank layout: no sidebar, full screen for auth/error pages -->
  <template v-if="layout === 'blank'">
    <router-view v-slot="{ Component, route }">
      <transition name="page-fade" mode="out-in">
        <component :is="Component" :key="route.path" />
      </transition>
    </router-view>
  </template>

  <!-- Default layout: sidebar + navbar + main content -->
  <AppLayout v-else />
</template>

<style>
.page-fade-enter-active,
.page-fade-leave-active {
  transition: opacity 0.25s ease, transform 0.25s ease;
}

.page-fade-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

.page-fade-leave-to {
  opacity: 0;
  transform: translateY(-10px);
}
</style>
