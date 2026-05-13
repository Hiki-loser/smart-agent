<script setup lang="ts">
import { ref } from 'vue'
import { useI18n } from 'vue-i18n'

defineProps<{
  promptTokens: number
  completionTokens: number
  totalTokens: number
  modelName?: string
  finishReason?: string
}>()

const { t } = useI18n()
const expanded = ref(false)
</script>

<template>
  <div class="token-usage" :class="{ expanded }">
    <button class="token-toggle" @click="expanded = !expanded">
      <span class="token-summary">{{ t('chat.totalTokens') }}: {{ totalTokens.toLocaleString() }}</span>
      <span class="token-arrow">{{ expanded ? '▾' : '▸' }}</span>
    </button>

    <div v-if="expanded" class="token-details animate-fade-in">
      <div class="token-row">
        <span>{{ t('chat.promptTokens') }}</span>
        <span>{{ promptTokens.toLocaleString() }}</span>
      </div>
      <div class="token-row">
        <span>{{ t('chat.completionTokens') }}</span>
        <span>{{ completionTokens.toLocaleString() }}</span>
      </div>
      <div class="token-row">
        <span>{{ t('chat.totalTokens') }}</span>
        <span>{{ totalTokens.toLocaleString() }}</span>
      </div>
      <div v-if="modelName" class="token-row">
        <span>{{ t('chat.modelName') }}</span>
        <span>{{ modelName }}</span>
      </div>
      <div v-if="finishReason" class="token-row">
        <span>{{ t('chat.finishReason') }}</span>
        <span>{{ finishReason }}</span>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.token-usage {
  margin-top: 10px;
  padding-top: 8px;
  border-top: 1px solid var(--color-border-light);
}

.token-toggle {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 0.75rem;
  color: var(--color-text-muted);
  padding: 0;
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-text-secondary);
  }
}

.token-arrow {
  font-size: 10px;
}

.token-details {
  margin-top: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.token-row {
  display: flex;
  justify-content: space-between;
  font-size: 0.75rem;
  color: var(--color-text-muted);

  span:last-child {
    font-weight: 500;
    color: var(--color-text-secondary);
  }
}
</style>
