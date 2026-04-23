<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, type Router } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router: Router = useRouter()
const authStore = useAuthStore()

const isLoggedIn = computed(() => authStore.isLoggedIn)

const handleLogout = async (): Promise<void> => {
  await authStore.logout()
  router.push('/')
}
</script>

<template>
  <header class="app-header">
    <div class="header-container">
      <router-link
        to="/"
        class="logo"
      >
        <span class="logo-text">Template</span>
      </router-link>
      <nav class="header-nav">
        <router-link
          to="/"
          class="nav-link"
        >
          首页
        </router-link>
        <router-link
          v-if="!isLoggedIn"
          to="/login"
          class="nav-link"
        >
          登录
        </router-link>
        <template v-else>
          <router-link
            to="/dashboard"
            class="nav-link"
          >
            仪表盘
          </router-link>
          <el-button
            type="danger"
            size="small"
            @click="handleLogout"
          >
            退出
          </el-button>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  background-color: #FFFFFF;
  border-bottom: 1px solid #E5E5E5;
  position: sticky;
  top: 0;
  z-index: 1000;
}

.header-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  height: 64px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  text-decoration: none;
}

.logo-text {
  font-size: 20px;
  font-weight: 700;
  color: #000000;
}

.header-nav {
  display: flex;
  align-items: center;
  gap: 24px;
}

.nav-link {
  color: #333333;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.nav-link:hover {
  color: #2563EB;
}

.nav-link.router-link-active {
  color: #2563EB;
}
</style>
