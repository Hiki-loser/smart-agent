<script setup lang="ts">
import { ref, nextTick, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import AgentTypeSelector from './AgentTypeSelector.vue'

const props = defineProps<{
  disabled?: boolean
  isStreaming?: boolean
}>()

const emit = defineEmits<{
  send: [content: string, agentType: string]
  stop: []
}>()

const { t } = useI18n()

const input = ref('')
const agentType = ref('default')
const textareaRef = ref<HTMLTextAreaElement | null>(null)

function handleSend() {
  const text = input.value.trim()
  if (!text || props.disabled) return
  emit('send', text, agentType.value)
  input.value = ''
  nextTick(() => {
    textareaRef.value?.focus()
  })
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    handleSend()
  }
}

function autoResize(e: Event) {
  const el = e.target as HTMLTextAreaElement
  el.style.height = 'auto'
  el.style.height = Math.min(el.scrollHeight, 120) + 'px'
}

// Focus on mount
onMounted(() => {
  nextTick(() => textareaRef.value?.focus())
})
</script>

<template>
  <div class="chat-input-wrapper">
    <div class="chat-input-bar">
      <AgentTypeSelector
        v-model="agentType"
        :disabled="disabled || isStreaming"
      />

      <div class="input-area">
        <textarea
          ref="textareaRef"
          v-model="input"
          class="chat-textarea"
          :placeholder="(t('chat.inputPlaceholder') as string)"
          :disabled="disabled"
          rows="1"
          @keydown="handleKeydown"
          @input="autoResize"
        />
      </div>

      <button
        v-if="!isStreaming"
        class="send-btn"
        :disabled="disabled || !input.trim()"
        @click="handleSend"
        :title="(t('chat.send') as string)"
      >
        <span class="send-icon">➤</span>
      </button>

      <button
        v-else
        class="stop-btn"
        @click="emit('stop')"
        :title="(t('chat.stopGeneration') as string)"
      >
        <span class="stop-icon">■</span>
      </button>
    </div>

    <p class="input-hint">Enter {{ t('chat.send') }}, Shift+Enter ↵</p>
  </div>
</template>

<style lang="scss" scoped>
.chat-input-wrapper {
  padding: 16px;
  background-color: var(--color-bg-secondary);
  border-top: 1px solid var(--color-border-light);
}

.chat-input-bar {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  padding: 8px 12px;
  background-color: var(--color-bg-input);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-lg);
  transition: border-color var(--transition-fast), box-shadow var(--transition-fast);

  &:focus-within {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px rgba(124, 111, 247, 0.12);
  }
}

.input-area {
  flex: 1;
  min-width: 0;
}

.chat-textarea {
  width: 100%;
  border: none;
  background: transparent;
  resize: none;
  font-size: var(--font-size-base);
  line-height: 1.5;
  color: var(--color-text-primary);
  outline: none;
  padding: 4px 0;
  max-height: 120px;
  font-family: var(--font-family);

  &::placeholder {
    color: var(--color-text-muted);
  }
}

.send-btn,
.stop-btn {
  width: 36px;
  height: 36px;
  border-radius: var(--radius-full);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  flex-shrink: 0;
  transition: all var(--transition-fast);
}

.send-btn {
  background: var(--color-primary);
  color: white;

  .send-icon {
    font-size: 16px;
    transform: rotate(-30deg);
  }

  &:hover:not(:disabled) {
    background: var(--color-primary-dark);
    box-shadow: var(--shadow-md);
    transform: scale(1.05);
  }

  &:disabled {
    background: var(--color-text-muted);
    cursor: not-allowed;
    opacity: 0.5;
  }
}

.stop-btn {
  background: var(--color-danger);
  color: white;

  .stop-icon {
    font-size: 14px;
  }

  &:hover {
    background: var(--color-danger);
    opacity: 0.9;
  }
}

.input-hint {
  margin-top: 6px;
  text-align: right;
  font-size: 0.7rem;
  color: var(--color-text-muted);
}
</style>
