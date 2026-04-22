# 前端开发规范 (Vue 3 + TypeScript)

## 技术栈

- Vue 3.4+ (Composition API, `<script setup>`)
- TypeScript 5.4+
- Vite 5.2+
- Pinia 2.1+
- Vue Router 4.3+
- Element Plus 2.7+
- Tailwind CSS 3.4+

## 项目结构

```
frontend/src/
├── api/            # API 接口封装
├── assets/         # 静态资源
├── components/     # 公共组件 (PascalCase)
├── composables/     # 组合式函数
├── router/         # 路由配置
├── stores/         # Pinia 状态管理
├── types/          # TypeScript 类型定义
├── utils/          # 工具函数
├── views/          # 页面组件
├── App.vue
└── main.ts
```

## 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| 组件 | PascalCase | `AppHeader.vue` |
| 组合式函数 | camelCase, use 前缀 | `useAuth.ts` |
| 变量/函数 | camelCase | `userInfo`, `handleLogin` |
| 常量 | UPPER_SNAKE_CASE | `MAX_RETRY_COUNT` |
| 类型/接口 | PascalCase | `UserResponse` |
| 文件 | kebab-case | `user-api.ts` |

## 代码规范

### 组件规范

```vue
<script setup lang="ts">
// 1. 导入
import { ref, computed, onMounted } from 'vue'
import type { UserResponse } from '@/types/user'

// 2. Props 和 Emit
const props = defineProps<{
  title: string
}>()
const emit = defineEmits<{
  (e: 'update', value: string): void
}>()

// 3.响应式状态
const loading = ref(false)
const userInfo = ref<UserResponse | null>(null)

// 4. 计算属性
const isLoggedIn = computed(() => !!userInfo.value)

// 5. 生命周期
onMounted(async () => {
  await fetchData()
})

// 6. 方法 (camelCase)
const handleSubmit = async (): Promise<void> => {
  // ...
}
</script>

<template>
  <div class="container">
    <!-- ... -->
  </div>
</template>

<style scoped>
.container {
  /* ... */
}
</style>
```

### 类型定义规范

```typescript
// types/user.d.ts
export interface User {
  id: number
  username: string
  email: string
  avatarUrl: string
  status: number
}

// 请求/响应类型
export interface LoginRequest {
  username: string
  password: string
}

export interface TokenResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
}
```

### API 封装规范

```typescript
// api/user.ts
import request from '@/utils/request'
import type { ApiResult } from '@/types/api'
import type { LoginRequest, TokenResponse } from '@/types/user'

export const authApi = {
  login(data: LoginRequest): Promise<ApiResult<TokenResponse>> {
    return request.post('/v1/auth/login', data)
  }
}
```

## 样式规范

### Tailwind CSS 使用

- 使用黑白极简配色：`#000000`, `#FFFFFF`, `#F5F5F5`, `#2563EB`
- 组件样式使用 `<style scoped>`
- 避免内联样式

### 响应式断点

- `sm`: 640px
- `md`: 768px
- `lg`: 1024px
- `xl`: 1280px

## 性能优化

1. **路由懒加载**
   ```typescript
   component: () => import('../views/Dashboard.vue')
   ```

2. **图片懒加载**
   ```vue
   <img v-lazy="src" />
   ```

3. **组件异步导入**
   ```typescript
   const HeavyComponent = defineAsyncComponent(() => import('./HeavyComponent.vue'))
   ```

## 禁止事项

- ❌ 禁止使用 `any` 类型
- ❌ 禁止在模板中使用复杂逻辑（使用计算属性）
- ❌ 禁止修改 props
- ❌ 组件函数不超过 50 行（拆分组件）
- ❌ 不写 JSDoc 注释

## Git 提交规范

```
feat: 新功能
fix: 修复 bug
docs: 文档更新
style: 代码格式（不影响功能）
refactor: 重构
test: 测试
chore: 构建/工具
```
