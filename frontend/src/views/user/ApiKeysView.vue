<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useApiKey } from '@/composables/useApiKey'
import { copyToClipboard } from '@/utils/dom'
import { formatDateTime } from '@/utils/format'
import EmptyState from '@/components/common/EmptyState.vue'
import ConfirmDialog from '@/components/common/ConfirmDialog.vue'
import { ElMessage } from 'element-plus'
import type { ApiKey } from '@/types/user'

const { t } = useI18n()
const { apiKeys, loading, fetchApiKeys, createApiKey, revokeApiKey } = useApiKey()

const createDialogVisible = ref(false)
const revokeTarget = ref<ApiKey | null>(null)
const revokeDialogVisible = ref(false)
const newKeyValue = ref<string | null>(null)
const creating = ref(false)

const createForm = reactive({
  name: '',
  expireDays: undefined as number | undefined,
})

onMounted(() => {
  fetchApiKeys()
})

async function handleCreate() {
  if (!createForm.name.trim()) return
  creating.value = true
  const key = await createApiKey(createForm.name.trim(), createForm.expireDays)
  creating.value = false
  if (key) {
    newKeyValue.value = key.keyValue
  }
}

function closeCreateDialog() {
  createDialogVisible.value = false
  newKeyValue.value = null
  createForm.name = ''
  createForm.expireDays = undefined
}

function showRevokeDialog(key: ApiKey) {
  revokeTarget.value = key
  revokeDialogVisible.value = true
}

async function handleRevoke() {
  if (!revokeTarget.value) return
  const success = await revokeApiKey(revokeTarget.value.id)
  revokeDialogVisible.value = false
  revokeTarget.value = null
  if (success) {
    await fetchApiKeys()
  }
}

async function handleCopyKey() {
  if (!newKeyValue.value) return
  const success = await copyToClipboard(newKeyValue.value)
  if (success) ElMessage.success(t('apiKey.copySuccess'))
}

function formatExpireAt(date: string | null): string {
  if (!date) return t('apiKey.neverExpire')
  return formatDateTime(date) ?? t('apiKey.neverExpire')
}
</script>

<template>
  <div class="api-keys-view">
    <div class="api-keys-container">
      <!-- Header -->
      <div class="api-keys-header">
        <h2 class="page-title">{{ t('apiKey.title') }}</h2>
        <el-button type="primary" @click="createDialogVisible = true">
          {{ t('apiKey.create') }}
        </el-button>
      </div>

      <!-- Empty state -->
      <EmptyState
        v-if="!loading && apiKeys.length === 0"
        :title="(t('apiKey.noKeys') as string)"
        :description="(t('apiKey.noKeysHint') as string)"
        :action-text="(t('apiKey.create') as string)"
        @action="createDialogVisible = true"
      />

      <!-- Key list -->
      <div v-else class="api-keys-list">
        <div
          v-for="key in apiKeys"
          :key="key.id"
          class="api-key-card"
          :class="{ revoked: key.status === 0 }"
        >
          <div class="key-info">
            <div class="key-name-row">
              <span class="key-name">{{ key.name }}</span>
              <el-tag
                :type="key.status === 1 ? 'success' : 'info'"
                size="small"
                effect="plain"
              >
                {{ key.status === 1 ? t('apiKey.statusActive') : t('apiKey.statusRevoked') }}
              </el-tag>
            </div>

            <div class="key-meta">
              <span>{{ t('apiKey.expireAt') }}: {{ formatExpireAt(key.expireAt) }}</span>
              <span>|</span>
              <span>{{ t('apiKey.lastUsed') }}: {{ key.lastUsedAt ? formatDateTime(key.lastUsedAt) : t('apiKey.neverUsed') }}</span>
              <span>|</span>
              <span>{{ t('apiKey.createdAt') }}: {{ formatDateTime(key.createTime) }}</span>
            </div>
          </div>

          <div class="key-actions" v-if="key.status === 1">
            <el-button
              type="danger"
              text
              size="small"
              @click="showRevokeDialog(key)"
            >
              {{ t('apiKey.revoke') }}
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- Create Dialog -->
    <el-dialog
      v-model="createDialogVisible"
      :title="(t('apiKey.create') as string)"
      :width="460"
      @close="closeCreateDialog"
    >
      <!-- Form before creation -->
      <div v-if="!newKeyValue">
        <el-form label-position="top">
          <el-form-item :label="t('apiKey.keyName')">
            <el-input
              v-model="createForm.name"
              :placeholder="(t('apiKey.keyNamePlaceholder') as string)"
            />
          </el-form-item>
          <el-form-item :label="t('apiKey.expireDays')">
            <el-input-number
              v-model="createForm.expireDays"
              :min="1"
              :max="3650"
              :placeholder="(t('apiKey.expireDaysHint') as string)"
              class="w-full"
            />
          </el-form-item>
        </el-form>
      </div>

      <!-- Show key value after creation -->
      <div v-else class="new-key-display">
        <div class="new-key-warning">
          <span>⚠️</span>
          <p>{{ t('apiKey.keyShownOnce') }}</p>
        </div>
        <div class="new-key-value">
          <code>{{ newKeyValue }}</code>
        </div>
        <el-button type="primary" @click="handleCopyKey">
          {{ t('apiKey.copyKey') }}
        </el-button>
      </div>

      <template #footer>
        <template v-if="!newKeyValue">
          <el-button @click="closeCreateDialog">{{ t('common.cancel') }}</el-button>
          <el-button type="primary" :loading="creating" @click="handleCreate">
            {{ t('common.create') }}
          </el-button>
        </template>
        <el-button v-else @click="closeCreateDialog">{{ t('common.close') }}</el-button>
      </template>
    </el-dialog>

    <!-- Revoke confirmation -->
    <ConfirmDialog
      :visible="revokeDialogVisible"
      :title="(t('apiKey.revoke') as string)"
      :message="(t('apiKey.revokeConfirm') as string)"
      dialog-type="danger"
      @confirm="handleRevoke"
      @cancel="revokeDialogVisible = false"
    />
  </div>
</template>

<style lang="scss" scoped>
.api-keys-view {
  height: 100%;
  overflow-y: auto;
}

.api-keys-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px;
}

.api-keys-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 24px;
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
}

.api-keys-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.api-key-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-sm);
  transition: all var(--transition-fast);

  &:hover {
    box-shadow: var(--shadow-md);
  }

  &.revoked {
    opacity: 0.6;
  }
}

.key-info {
  flex: 1;
  min-width: 0;
}

.key-name-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}

.key-name {
  font-weight: 600;
  color: var(--color-text-primary);
}

.key-meta {
  display: flex;
  gap: 8px;
  font-size: 0.8rem;
  color: var(--color-text-muted);
  flex-wrap: wrap;
}

.key-actions {
  flex-shrink: 0;
  margin-left: 16px;
}

// New key display
.new-key-display {
  text-align: center;
}

.new-key-warning {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: var(--font-size-sm);
  color: var(--color-warning);
}

.new-key-value {
  background: var(--color-bg-input);
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: 12px;
  margin-bottom: 16px;
  word-break: break-all;

  code {
    font-family: var(--font-family-mono);
    font-size: 0.85rem;
    color: var(--color-primary);
  }
}

.w-full {
  width: 100%;
}
</style>
