# API 文档规范

## 概述

本文档定义了项目 API 的设计、编写和维护规范。

## RESTful 规范

### 基本原则

1. 使用名词而非动词：`/users` 而非 `/getUsers`
2. 使用 HTTP 方法表达操作：
   - `GET` - 查询
   - `POST` - 创建
   - `PUT` - 更新
   - `DELETE` - 删除
3. 使用复数形式：`/users` 而非 `/user`
4. 层级关系用路径表示：`/users/{id}/orders`

### URL 结构

```
/api/v1/{module}/{resource}

/api/v1/auth/register
/api/v1/auth/login
/api/v1/users/me
/api/v1/users/{id}
```

### HTTP 状态码

| 状态码 | 说明 | 使用场景 |
|--------|------|----------|
| 200 | OK | 成功查询/更新 |
| 201 | Created | 成功创建 |
| 204 | No Content | 成功删除 |
| 400 | Bad Request | 参数错误 |
| 401 | Unauthorized | 未认证 |
| 403 | Forbidden | 无权限 |
| 404 | Not Found | 资源不存在 |
| 500 | Internal Server Error | 服务器错误 |

## 统一响应格式

### 成功响应

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "username": "test"
  },
  "msg": "ok",
  "requestId": "abc123def456"
}
```

### 错误响应

```json
{
  "code": 400,
  "data": null,
  "msg": "参数错误: username 不能为空",
  "requestId": "abc123def456"
}
```

## 请求格式

### 请求头

| 头信息 | 说明 | 必需 |
|--------|------|------|
| Content-Type | application/json | 是 |
| Authorization | Bearer {token} | 受保护接口 |
| X-Refresh-Token | {refresh_token} | 刷新 Token |

### 查询参数

```
GET /api/v1/users?page=1&pageSize=10&status=1
```

### 路径参数

```
GET /api/v1/users/{id}
DELETE /api/v1/users/{id}
```

### 请求体 (JSON)

```json
{
  "username": "test",
  "password": "123456",
  "email": "test@example.com"
}
```

## 分页响应

```json
{
  "code": 200,
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 10
  },
  "msg": "ok",
  "requestId": "abc123"
}
```

## 错误码定义

| 错误码 | 说明 | HTTP Status |
|--------|------|-------------|
| 200 | 成功 | 200 |
| 400 | 参数错误 | 400 |
| 401 | 未认证 | 401 |
| 403 | 无权限 | 403 |
| 404 | 资源不存在 | 404 |
| 500 | 服务器内部错误 | 500 |

## API 文档要求

每个 API 文档必须包含：

1. **接口描述**
2. **请求方法** 和 **路径**
3. **请求头**（如有）
4. **请求参数**（路径参数、查询参数、请求体）
5. **响应格式**
6. **示例**（cURL、HTTP、响应）

## 版本管理

- URL 中包含版本号：`/api/v1/`
- 不兼容变更时升级版本：`/api/v2/`
- 旧版本需保留一段时间供迁移

## 认证方式

### JWT Bearer Token

```
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
```

### Refresh Token

```
X-Refresh-Token: eyJhbGciOiJIUzI1NiIs...
```

## 安全性要求

1. 敏感信息不放在 URL 中
2. 密码不在响应中返回
3. Token 不记录日志
4. 防止 SQL 注入
5. 防止 XSS 攻击
