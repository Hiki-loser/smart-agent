<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { t } = useI18n()
const { handleRegister } = useAuth()

const registerFormRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  nickname: '',
  password: '',
  confirmPassword: '',
})

const rules = {
  username: [
    { required: true, message: 'auth.usernameRequired', trigger: 'blur' },
    { min: 4, max: 64, message: 'auth.usernameLength', trigger: 'blur' },
  ],
  password: [
    { required: true, message: 'auth.passwordRequired', trigger: 'blur' },
    { min: 6, max: 20, message: 'auth.passwordLength', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: 'auth.passwordRequired', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string, callback: (error?: Error) => void) => {
        if (value !== form.password) {
          callback(new Error('auth.passwordMismatch'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  nickname: [
    { max: 50, message: 'validation.nicknameLength', trigger: 'blur' },
  ],
}

async function onSubmit() {
  const valid = await registerFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  await handleRegister(form.username, form.password, form.nickname || undefined)
  loading.value = false
}

function goToLogin() {
  router.push('/login')
}
</script>

<template>
  <div class="auth-view">
    <!-- Decorative background -->
    <div class="auth-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-sparkle sparkle-1">✦</div>
      <div class="bg-sparkle sparkle-2">✧</div>
    </div>

    <!-- Register card -->
    <div class="auth-card animate-bounce-in">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="logo-icon">🌸</div>
        <h1 class="auth-app-name gradient-text">{{ t('app.name') }}</h1>
        <p class="auth-slogan">{{ t('auth.register') }}</p>
      </div>

      <!-- Form -->
      <el-form
        ref="registerFormRef"
        :model="form"
        :rules="rules"
        label-position="top"
        class="auth-form"
        @submit.prevent="onSubmit"
      >
        <el-form-item :label="t('auth.username')" prop="username">
          <el-input
            v-model="form.username"
            :placeholder="t('auth.username')"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item :label="t('auth.nickname')" prop="nickname">
          <el-input
            v-model="form.nickname"
            :placeholder="t('auth.nickname')"
            size="large"
            prefix-icon="Edit"
          />
        </el-form-item>

        <el-form-item :label="t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="t('auth.password')"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item :label="t('auth.confirmPassword')" prop="confirmPassword">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            :placeholder="t('auth.confirmPassword')"
            size="large"
            prefix-icon="Lock"
            show-password
            @keyup.enter="onSubmit"
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          class="auth-submit-btn"
          :loading="loading"
          @click="onSubmit"
        >
          {{ t('auth.registerButton') }}
        </el-button>
      </el-form>

      <!-- Footer -->
      <div class="auth-footer">
        <span>{{ t('auth.hasAccount') }}</span>
        <el-button link type="primary" @click="goToLogin">
          {{ t('auth.goLogin') }}
        </el-button>
      </div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.auth-view {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-bg-primary) 0%, var(--color-bg-sidebar) 100%);
  position: relative;
  overflow: hidden;
}

.auth-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.bg-circle {
  position: absolute;
  border-radius: 50%;
  opacity: 0.15;

  &.circle-1 {
    width: 350px;
    height: 350px;
    background: var(--color-secondary);
    top: -80px;
    right: -80px;
    animation: float 6s ease-in-out infinite;
  }

  &.circle-2 {
    width: 250px;
    height: 250px;
    background: var(--color-primary);
    bottom: -60px;
    left: -60px;
    animation: float 8s ease-in-out infinite reverse;
  }
}

.bg-sparkle {
  position: absolute;
  font-size: 22px;
  color: var(--color-primary);
  opacity: 0.35;
  animation: sparkle 2s ease-in-out infinite;

  &.sparkle-1 { top: 20%; right: 20%; animation-delay: 0s; }
  &.sparkle-2 { bottom: 25%; left: 25%; animation-delay: 0.7s; }
}

.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 440px;
  padding: 36px 36px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
}

.auth-logo {
  text-align: center;
  margin-bottom: 28px;

  .logo-icon {
    font-size: 44px;
    margin-bottom: 10px;
    display: block;
    animation: float 3s ease-in-out infinite;
  }
}

.auth-app-name {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin-bottom: 4px;
}

.auth-slogan {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

.auth-form {
  margin-bottom: 16px;

  :deep(.el-form-item) {
    margin-bottom: 16px;
  }
}

.auth-submit-btn {
  width: 100%;
  margin-top: 4px;
  height: 48px;
  font-size: 1.05rem;
  font-weight: 600;
  letter-spacing: 2px;
}

.auth-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
}

@media (max-width: 480px) {
  .auth-card {
    margin: 16px;
    padding: 28px 20px;
  }
}
</style>
