<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  retryText?: string
}>()

defineEmits<{
  retry: []
}>()
</script>

<template>
  <div class="error-state animate-fade-in">
    <div class="error-icon">
      <span class="error-emoji">💫</span>
    </div>
    <h3 class="error-title">{{ title }}</h3>
    <p v-if="description" class="error-description">{{ description }}</p>
    <el-button
      v-if="retryText"
      type="primary"
      @click="$emit('retry')"
    >
      {{ retryText }}
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
.error-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.error-icon {
  margin-bottom: 24px;

  .error-emoji {
    font-size: 56px;
    display: block;
  }
}

.error-title {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.error-description {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: 24px;
  max-width: 300px;
}
</style>
