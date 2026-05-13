<script setup lang="ts">
import { useI18n } from 'vue-i18n'

defineProps<{
  modelValue: string
  disabled?: boolean
}>()

defineEmits<{
  'update:modelValue': [value: string]
}>()

const { t } = useI18n()

const agentTypes = [
  { value: 'default', label: 'Default' },
  { value: 'code', label: 'Code' },
  { value: 'chat', label: 'Chat' },
  { value: 'reasoning', label: 'Reasoning' },
]
</script>

<template>
  <el-select
    :model-value="modelValue"
    :disabled="disabled"
    :placeholder="t('chat.selectAgent')"
    size="small"
    class="agent-selector"
    @update:model-value="$emit('update:modelValue', $event)"
  >
    <el-option
      v-for="agent in agentTypes"
      :key="agent.value"
      :value="agent.value"
      :label="agent.label"
    />
  </el-select>
</template>

<style lang="scss" scoped>
.agent-selector {
  width: 130px;
}
</style>
