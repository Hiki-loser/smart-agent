<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  visible: boolean
  title: string
  message: string
  confirmText?: string
  cancelText?: string
  dialogType?: 'info' | 'danger'
}>()

const emit = defineEmits<{
  confirm: []
  cancel: []
}>()

const { t } = useI18n()
</script>

<template>
  <el-dialog
    :model-value="visible"
    :title="title"
    :width="420"
    :close-on-click-modal="false"
    @close="emit('cancel')"
  >
    <p class="confirm-message">{{ message }}</p>

    <template #footer>
      <el-button @click="emit('cancel')">
        {{ cancelText || t('common.cancel') }}
      </el-button>
      <el-button
        :type="dialogType === 'danger' ? 'danger' : 'primary'"
        @click="emit('confirm')"
      >
        {{ confirmText || t('common.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<style lang="scss" scoped>
.confirm-message {
  font-size: var(--font-size-base);
  color: var(--color-text-secondary);
  line-height: 1.6;
}
</style>
