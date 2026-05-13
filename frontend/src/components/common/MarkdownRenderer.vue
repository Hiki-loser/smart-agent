<script setup lang="ts">
import { computed, ref, watch, nextTick } from 'vue'
import { renderMarkdown } from '@/utils/markdown'
import { copyToClipboard } from '@/utils/dom'
import { useI18n } from 'vue-i18n'

const props = withDefaults(defineProps<{
  content: string
  isStreaming?: boolean
}>(), {
  isStreaming: false,
})

const { t } = useI18n()
const containerRef = ref<HTMLElement | null>(null)

const renderedHtml = computed(() => {
  if (!props.content) return ''
  return renderMarkdown(props.content)
})

// Watch for changes and add copy buttons to code blocks
watch(renderedHtml, async () => {
  if (!props.isStreaming) {
    await nextTick()
    addCopyButtons()
  }
})

function addCopyButtons() {
  if (!containerRef.value) return
  const pres = containerRef.value.querySelectorAll('pre')
  pres.forEach((pre) => {
    if (pre.querySelector('.copy-code-btn')) return
    const wrapper = document.createElement('div')
    wrapper.className = 'code-block-wrapper'
    pre.parentNode?.insertBefore(wrapper, pre)
    wrapper.appendChild(pre)

    const btn = document.createElement('button')
    btn.className = 'copy-code-btn'
    btn.textContent = t('common.copy')
    btn.addEventListener('click', async () => {
      const code = pre.querySelector('code')?.textContent || ''
      const success = await copyToClipboard(code)
      btn.textContent = success ? t('common.copied') : t('common.copy')
      setTimeout(() => { btn.textContent = t('common.copy') }, 2000)
    })
    wrapper.appendChild(btn)
  })
}
</script>

<template>
  <div
    ref="containerRef"
    class="markdown-content"
    :class="{ 'is-streaming': isStreaming }"
    v-html="renderedHtml"
  />
  <span v-if="isStreaming" class="streaming-cursor" />
</template>

<style lang="scss" scoped>
.markdown-content {
  &.is-streaming {
    // Last element should have streaming cursor
    :deep(*:last-child)::after {
      // Remove default cursor, handled by parent
    }
  }
}

.streaming-cursor {
  display: inline;
  color: var(--color-primary);
  font-weight: 700;
  animation: typing-cursor 1s step-end infinite;
}

.copy-code-btn {
  // styled in markdown.scss
}
</style>
