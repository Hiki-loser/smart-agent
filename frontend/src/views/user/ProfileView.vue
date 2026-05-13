<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useI18n } from 'vue-i18n'
import { useAuthStore } from '@/stores/auth.store'
import UserAvatar from '@/components/common/UserAvatar.vue'
import { formatDateTime } from '@/utils/format'
import { ElMessage } from 'element-plus'

const { t } = useI18n()
const authStore = useAuthStore()

const editing = ref(false)
const saving = ref(false)
const formRef = ref()

const form = reactive({
  nickname: '',
  avatar: '',
})

const rules = {
  nickname: [
    { max: 50, message: 'validation.nicknameLength', trigger: 'blur' },
  ],
}

function startEdit() {
  form.nickname = authStore.user?.nickname || ''
  form.avatar = authStore.user?.avatar || ''
  editing.value = true
}

function cancelEdit() {
  editing.value = false
}

async function saveProfile() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await authStore.updateProfile({
      nickname: form.nickname || undefined,
      avatar: form.avatar || undefined,
    })
    authStore.setUserTitle()
    ElMessage.success(t('user.updateSuccess'))
    editing.value = false
  } catch (err: unknown) {
    const message = (err as { message?: string })?.message || t('error.unknownError')
    ElMessage.error(message)
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <div class="profile-view">
    <div class="profile-container">
      <h2 class="page-title">{{ t('user.profile') }}</h2>

      <div class="profile-card">
        <!-- Avatar section -->
        <div class="profile-avatar-section">
          <UserAvatar
            :src="authStore.user?.avatar ?? null"
            :nickname="authStore.user?.nickname || authStore.user?.username || '?'"
            size="lg"
          />
          <h3 class="profile-nickname">
            {{ authStore.user?.nickname || authStore.user?.username }}
          </h3>
          <span class="profile-role" v-if="authStore.user?.roleId === 1">
            Admin
          </span>
        </div>

        <!-- Info section -->
        <div class="profile-info-section">
          <!-- Display mode -->
          <template v-if="!editing">
            <div class="info-grid">
              <div class="info-item">
                <span class="info-label">{{ t('user.username') }}</span>
                <span class="info-value">@{{ authStore.user?.username }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ t('user.nickname') }}</span>
                <span class="info-value">{{ authStore.user?.nickname || '-' }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ t('user.createdAt') }}</span>
                <span class="info-value">{{ formatDateTime(authStore.user?.createTime) }}</span>
              </div>
              <div class="info-item">
                <span class="info-label">{{ t('user.updatedAt') }}</span>
                <span class="info-value">{{ formatDateTime(authStore.user?.updateTime) }}</span>
              </div>
            </div>

            <el-button type="primary" @click="startEdit">
              {{ t('user.editProfile') }}
            </el-button>
          </template>

          <!-- Edit mode -->
          <template v-else>
            <el-form
              ref="formRef"
              :model="form"
              :rules="rules"
              label-position="top"
            >
              <el-form-item :label="t('user.nickname')" prop="nickname">
                <el-input
                  v-model="form.nickname"
                  :placeholder="(t('user.nickname') as string)"
                  size="large"
                />
              </el-form-item>

              <el-form-item :label="t('user.avatar')" prop="avatar">
                <el-input
                  v-model="form.avatar"
                  placeholder="https://..."
                  size="large"
                />
              </el-form-item>

              <div class="edit-actions">
                <el-button @click="cancelEdit">
                  {{ t('common.cancel') }}
                </el-button>
                <el-button
                  type="primary"
                  :loading="saving"
                  @click="saveProfile"
                >
                  {{ t('common.save') }}
                </el-button>
              </div>
            </el-form>
          </template>
        </div>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.profile-view {
  height: 100%;
  overflow-y: auto;
}

.profile-container {
  max-width: 640px;
  margin: 0 auto;
  padding: 32px 24px;
}

.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 24px;
}

.profile-card {
  display: flex;
  gap: 32px;
  background: var(--color-bg-secondary);
  border: 1px solid var(--color-border-light);
  border-radius: var(--radius-lg);
  padding: 32px;
  box-shadow: var(--shadow-sm);
}

.profile-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  min-width: 140px;
}

.profile-nickname {
  font-size: var(--font-size-lg);
  font-weight: 600;
  color: var(--color-text-primary);
}

.profile-role {
  font-size: 0.75rem;
  padding: 2px 10px;
  border-radius: var(--radius-full);
  background-color: var(--color-primary-light);
  color: var(--color-primary);
  font-weight: 600;
}

.profile-info-section {
  flex: 1;
}

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
  margin-bottom: 24px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-label {
  font-size: 0.8rem;
  color: var(--color-text-muted);
  font-weight: 500;
}

.info-value {
  font-size: var(--font-size-base);
  color: var(--color-text-primary);
  font-weight: 500;
}

.edit-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 16px;
}

@media (max-width: 640px) {
  .profile-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }
}
</style>
