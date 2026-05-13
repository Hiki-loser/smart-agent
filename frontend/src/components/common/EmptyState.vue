<script setup lang="ts">
defineProps<{
  title: string
  description?: string
  actionText?: string
}>()

defineEmits<{
  action: []
}>()
</script>

<template>
  <div class="empty-state animate-fade-in-up">
    <div class="empty-illustration">
      <span class="empty-emoji">🌸</span>
    </div>
    <h3 class="empty-title">{{ title }}</h3>
    <p v-if="description" class="empty-description">{{ description }}</p>
    <el-button
      v-if="actionText"
      type="primary"
      size="large"
      @click="$emit('action')"
    >
      {{ actionText }}
    </el-button>
  </div>
</template>

<style lang="scss" scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 48px 24px;
  text-align: center;
}

.empty-illustration {
  margin-bottom: 24px;

  .empty-emoji {
    font-size: 64px;
    display: block;
    animation: float 3s ease-in-out infinite;
  }
}

.empty-title {
  font-size: var(--font-size-xl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.empty-description {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: 24px;
  max-width: 300px;
  line-height: 1.6;
}
</style>
