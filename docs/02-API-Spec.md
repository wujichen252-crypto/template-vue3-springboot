# API 接口文档

## 基础信息

- 基础路径: `http://localhost:8080/api/v1`
- 数据格式: JSON
- 认证方式: Bearer Token (JWT)

## 统一响应格式

```typescript
interface ApiResult<T> {
  code: number      // 状态码：200成功，400参数错误，401未认证，403无权限，500服务器错误
  data: T          // 响应数据
  msg: string       // 消息
  requestId: string // 请求追踪ID
}
```

## 错误码

| 错误码 | 说明 |
|--------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 401 | 未认证 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

## 接口列表

### 认证模块

#### 1. 用户注册

```
POST /auth/register
```

**请求参数：**

```json
{
  "username": "string",  // 用户名，3-32字符
  "password": "string", // 密码，6-32字符
  "email": "string"     // 邮箱
}
```

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "avatarUrl": null,
    "status": 1
  },
  "msg": "ok",
  "requestId": "abc123"
}
```

---

#### 2. 用户登录

```
POST /auth/login
```

**请求参数：**

```json
{
  "username": "string",
  "password": "string"
}
```

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200000
  },
  "msg": "ok",
  "requestId": "abc123"
}
```

---

#### 3. 刷新 Token

```
POST /auth/refresh
```

**请求头：**

```
X-Refresh-Token: <refresh_token>
```

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIs...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIs...",
    "expiresIn": 7200000
  },
  "msg": "ok",
  "requestId": "abc123"
}
```

---

#### 4. 退出登录

```
POST /auth/logout
```

**需要认证：** Yes

**响应示例：**

```json
{
  "code": 200,
  "data": null,
  "msg": "ok",
  "requestId": "abc123"
}
```

---

### 用户模块

#### 5. 获取当前用户

```
GET /users/me
```

**需要认证：** Yes

**响应示例：**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "avatarUrl": "https://example.com/avatar.png",
    "status": 1
  },
  "msg": "ok",
  "requestId": "abc123"
}
```

---

## 使用示例

### cURL

```bash
# 注册
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456","email":"test@example.com"}'

# 登录
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"test","password":"123456"}'

# 获取当前用户
curl -X GET http://localhost:8080/api/v1/users/me \
  -H "Authorization: Bearer <access_token>"
```

### 前端调用示例

```typescript
import { authApi, userApi } from '@/api/user'

// 登录
const login = async () => {
  const res = await authApi.login({
    username: 'test',
    password: '123456'
  })
  // res.data.accessToken
}

// 获取用户信息
const getUser = async () => {
  const res = await userApi.getCurrentUser()
  // res.data
}
```
