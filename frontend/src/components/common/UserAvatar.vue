<script setup lang="ts">
import { computed } from 'vue'

const props = withDefaults(defineProps<{
  src: string | null
  nickname: string
  size?: 'sm' | 'md' | 'lg' | number
}>(), {
  size: 'md',
})

const sizePx = computed(() => {
  if (typeof props.size === 'number') return props.size
  const map = { sm: 32, md: 48, lg: 64 }
  return map[props.size]
})

const initial = computed(() => {
  return (props.nickname || '?').charAt(0).toUpperCase()
})

const bgColor = computed(() => {
  let hash = 0
  for (let i = 0; i < props.nickname.length; i++) {
    hash = props.nickname.charCodeAt(i) + ((hash << 5) - hash)
  }
  const hue = Math.abs(hash % 360)
  return `hsl(${hue}, 65%, 75%)`
})
</script>

<template>
  <div class="user-avatar" :style="{ width: `${sizePx}px`, height: `${sizePx}px` }">
    <img
      v-if="src"
      :src="src"
 :alt="(nickname || 'User') + ' avatar'"
      class="avatar-img"
      @error="() => {}"
    />
    <div
      v-else
      class="avatar-fallback"
      :style="{ backgroundColor: bgColor }"
    >
      <span class="avatar-initial" :style="{ fontSize: `${sizePx * 0.45}px` }">
        {{ initial }}
      </span>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.user-avatar {
  border-radius: var(--radius-full);
  overflow: hidden;
  flex-shrink: 0;
  position: relative;
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-fallback {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-initial {
  color: white;
  font-weight: 700;
  line-height: 1;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.15);
}
</style>
