<template>
  <div class="app-container">
    <div class="tech-bg"></div>
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

const applyTheme = () => {
  const theme = localStorage.getItem('cms_theme') || 'light'
  document.documentElement.setAttribute('data-theme', theme)
}

onMounted(() => {
  applyTheme()
})
</script>

<style>
.app-container {
  min-height: 100vh;
}
</style>
