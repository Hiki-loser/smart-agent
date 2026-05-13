<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'

const router = useRouter()
const { t } = useI18n()

function goHome() {
  router.push('/')
}
</script>

<template>
  <div class="error-view">
    <div class="error-content animate-fade-in-up">
      <div class="error-emoji animate-float">🔒</div>
      <h1 class="error-code">403</h1>
      <h2 class="error-title">{{ t('error.403') }}</h2>
      <p class="error-description">{{ t('error.403Hint') }}</p>
      <el-button type="primary" size="large" @click="goHome">
        {{ t('error.goHome') }}
      </el-button>
    </div>

    <div class="error-bg">
      <div class="bg-circle circle-1"></div>
      <div class="bg-circle circle-2"></div>
    </div>
  </div>
</template>

<style lang="scss" scoped>
.error-view {
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-bg-primary);
  position: relative;
  overflow: hidden;
}

.error-content {
  text-align: center;
  z-index: 1;
}

.error-emoji {
  font-size: 80px;
  display: block;
  margin-bottom: 16px;
}

.error-code {
  font-size: 72px;
  font-weight: 800;
  color: var(--color-text-muted);
  line-height: 1;
  margin-bottom: 8px;
  opacity: 0.5;
}

.error-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-text-primary);
  margin-bottom: 8px;
}

.error-description {
  font-size: var(--font-size-sm);
  color: var(--color-text-muted);
  margin-bottom: 32px;
}

.error-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;

  .bg-circle {
    position: absolute;
    border-radius: 50%;
    opacity: 0.08;

    &.circle-1 {
      width: 450px;
      height: 450px;
      background: var(--color-warning);
      top: -120px;
      right: -80px;
    }

    &.circle-2 {
      width: 300px;
      height: 300px;
      background: var(--color-danger);
      bottom: -80px;
      left: -60px;
    }
  }
}
</style>
