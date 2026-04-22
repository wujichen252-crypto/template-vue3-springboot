<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, type Router } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const router: Router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const email = ref('')
const loading = ref(false)

const handleRegister = async (): Promise<void> => {
  if (!username.value || !password.value || !email.value) {
    return
  }
  loading.value = true
  try {
    await authStore.register(username.value, password.value, email.value)
    router.push('/login')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="register-container">
    <div class="register-card">
      <h1 class="register-title">注册</h1>
      <el-form @submit.prevent="handleRegister" class="register-form">
        <el-form-item>
          <el-input
            v-model="username"
            placeholder="用户名"
            size="large"
            prefix-icon="User"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="email"
            type="email"
            placeholder="邮箱"
            size="large"
            prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item>
          <el-input
            v-model="password"
            type="password"
            placeholder="密码"
            size="large"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            native-type="submit"
            class="register-button"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <router-link to="/login">已有账号？去登录</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background-color: #F5F5F5;
}

.register-card {
  width: 100%;
  max-width: 400px;
  padding: 40px;
  background: #FFFFFF;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.register-title {
  font-size: 24px;
  font-weight: 600;
  color: #000000;
  text-align: center;
  margin-bottom: 32px;
}

.register-form {
  margin-top: 16px;
}

.register-button {
  width: 100%;
}

.register-footer {
  text-align: center;
  margin-top: 16px;
}

.register-footer a {
  color: #2563EB;
  text-decoration: none;
}

.register-footer a:hover {
  text-decoration: underline;
}
</style>
