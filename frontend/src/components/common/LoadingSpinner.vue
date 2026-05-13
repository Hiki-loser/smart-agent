<script setup lang="ts">
withDefaults(defineProps<{
  text?: string
  size?: 'sm' | 'md' | 'lg'
}>(), {
  size: 'md',
})
</script>

<template>
  <div class="loading-spinner" :class="`size-${size}`">
    <div class="spinner-ring">
      <div class="ring ring-1"></div>
      <div class="ring ring-2"></div>
      <div class="ring ring-3"></div>
    </div>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<style lang="scss" scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 32px;

  &.size-sm .spinner-ring { width: 24px; height: 24px; }
  &.size-md .spinner-ring { width: 40px; height: 40px; }
  &.size-lg .spinner-ring { width: 64px; height: 64px; }
}

.spinner-ring {
  position: relative;
}

.ring {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  border: 3px solid transparent;
  animation: spin-slow 1.5s linear infinite;

  .ring-1 {
    border-top-color: var(--color-primary);
  }

  .ring-2 {
    border-right-color: var(--color-secondary);
    animation-duration: 2s;
    animation-direction: reverse;
  }

  .ring-3 {
    border-bottom-color: var(--color-accent);
    animation-duration: 1.8s;
  }
}

.loading-text {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  animation: fade-in 0.3s ease;
}
</style>
