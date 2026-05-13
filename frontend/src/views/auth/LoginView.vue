<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { useAuth } from '@/composables/useAuth'

const router = useRouter()
const { t } = useI18n()
const { handleLogin } = useAuth()

const loginFormRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
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
}

async function onSubmit() {
  const valid = await loginFormRef.value?.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  await handleLogin(form.username, form.password)
  loading.value = false
}

function goToRegister() {
  router.push('/register')
}
</script>

<template>
  <div class="auth-view">
    <!-- Decorative background -->
    <div class="auth-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
      <div class="bg-circle circle-3"></div>
      <div class="bg-sparkle sparkle-1">✦</div>
      <div class="bg-sparkle sparkle-2">✧</div>
      <div class="bg-sparkle sparkle-3">✦</div>
    </div>

    <!-- Login card -->
    <div class="auth-card animate-bounce-in">
      <!-- Logo -->
      <div class="auth-logo">
        <div class="logo-icon">🌸</div>
        <h1 class="auth-app-name gradient-text">{{ t('app.name') }}</h1>
        <p class="auth-slogan">{{ t('app.slogan') }}</p>
      </div>

      <!-- Form -->
      <el-form
        ref="loginFormRef"
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

        <el-form-item :label="t('auth.password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            :placeholder="t('auth.password')"
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
          {{ t('auth.loginButton') }}
        </el-button>
      </el-form>

      <!-- Footer -->
      <div class="auth-footer">
        <span>{{ t('auth.noAccount') }}</span>
        <el-button link type="primary" @click="goToRegister">
          {{ t('auth.goRegister') }}
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

// Decorative background
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
    width: 400px;
    height: 400px;
    background: var(--color-primary);
    top: -100px;
    right: -100px;
    animation: float 6s ease-in-out infinite;
  }

  &.circle-2 {
    width: 300px;
    height: 300px;
    background: var(--color-secondary);
    bottom: -50px;
    left: -50px;
    animation: float 8s ease-in-out infinite reverse;
  }

  &.circle-3 {
    width: 150px;
    height: 150px;
    background: var(--color-accent);
    top: 50%;
    left: 10%;
    animation: float 5s ease-in-out infinite;
  }
}

.bg-sparkle {
  position: absolute;
  font-size: 24px;
  color: var(--color-accent);
  opacity: 0.4;
  animation: sparkle 2s ease-in-out infinite;

  &.sparkle-1 { top: 15%; right: 25%; animation-delay: 0s; }
  &.sparkle-2 { bottom: 20%; left: 30%; animation-delay: 0.7s; }
  &.sparkle-3 { top: 40%; right: 15%; animation-delay: 1.4s; }
}

// Card
.auth-card {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 420px;
  padding: 40px 36px;
  background: var(--color-bg-secondary);
  border-radius: var(--radius-xl);
  box-shadow: var(--shadow-lg);
  backdrop-filter: blur(20px);
}

.auth-logo {
  text-align: center;
  margin-bottom: 32px;

  .logo-icon {
    font-size: 48px;
    margin-bottom: 12px;
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
  margin-bottom: 20px;
}

.auth-submit-btn {
  width: 100%;
  margin-top: 8px;
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
    padding: 32px 20px;
  }
}
</style>
