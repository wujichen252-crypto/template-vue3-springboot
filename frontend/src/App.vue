<script setup lang="ts">
import AppLayout from './components/AppLayout.vue'
import ErrorBoundary from './components/ErrorBoundary.vue'
</script>

<template>
  <ErrorBoundary>
    <AppLayout>
      <router-view />
    </AppLayout>
  </ErrorBoundary>
</template>

<style>
#app {
  width: 100%;
  min-height: 100vh;
  background-color: #F5F5F5;
}
</style>
