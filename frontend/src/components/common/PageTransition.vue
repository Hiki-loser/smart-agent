<script setup lang="ts">
// Page transition wrapper - provides enter/leave animations for route changes
</script>

<template>
  <div class="page-transition">
    <slot />
  </div>
</template>

<style lang="scss" scoped>
.page-transition {
  width: 100%;
  height: 100%;
}
</style>
