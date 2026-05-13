# SmartAgent 博客后端 API 接口文档

> **版本**: 1.0.0-SNAPSHOT  
> **基础URL**: `http://localhost:8080`  
> **最后更新**: 2026-05-08

---

## 目录

- [1. 系统概述](#1-系统概述)
- [2. 通用说明](#2-通用说明)
- [3. 用户模块 API](#3-用户模块-api)
  - [3.1 用户注册](#31-用户注册)
  - [3.2 用户登录](#32-用户登录)
  - [3.3 获取当前用户信息](#33-获取当前用户信息)
  - [3.4 更新用户信息](#34-更新用户信息)
  - [3.5 用户登出](#35-用户登出)
  - [3.6 刷新令牌](#36-刷新令牌)
  - [3.7 创建 API Key](#37-创建-api-key)
  - [3.8 获取 API Key 列表](#38-获取-api-key-列表)
  - [3.9 吊销 API Key](#39-吊销-api-key)
- [4. 聊天模块 API](#4-聊天模块-api)
  - [4.1 创建会话](#41-创建会话)
  - [4.2 发送消息（SSE 流式）](#42-发送消息sse-流式)
  - [4.3 获取会话列表](#43-获取会话列表)
  - [4.4 获取会话历史消息](#44-获取会话历史消息)
- [5. 错误码参考](#5-错误码参考)
- [6. 认证与安全](#6-认证与安全)
- [7. 架构说明](#7-架构说明)

---

## 1. 系统概述

SmartAgent 博客后端是一个基于 **Spring Boot 3.2.5** + **Spring Cloud 2023.0.1** 的微服务系统，采用 Java 21。主要提供用户认证管理、AI 聊天会话管理及流式消息推送功能。

### 技术栈

| 组件 | 技术 |
|------|------|
| 框架 | Spring Boot 3.2.5, Spring Cloud 2023.0.1 |
| 语言 | Java 21 |
| 服务注册 | Nacos |
| 网关 | Spring Cloud Gateway (端口 8080) |
| 数据库 | MySQL + MyBatis Plus 3.5.6 |
| 缓存 | Redis (Lettuce) |
| 消息队列 | RocketMQ |
| 搜索引擎 | Elasticsearch |
| 认证 | JWT (jjwt 0.11.5) |
| API 文档 | Knife4j 4.5.0 (OpenAPI 3) |

### 模块组成

| 模块 | 服务名 | 端口 | 说明 |
|------|--------|------|------|
| gateway | smart-agent-gateway | 8080 | API 网关，统一入口，认证过滤，限流 |
| user | smart-agent-user | 8081 | 用户服务，注册/登录/令牌管理 |
| chat | smart-agent-chat | 8082 | 聊天服务，会话管理，SSE 流式消息 |
| common | — | — | 公共模块，通用模型/工具/异常处理 |

---

## 2. 通用说明

### 2.1 请求格式

- **Content-Type**: `application/json` (除特殊情况外)
- **字符编码**: `UTF-8`
- **认证方式**: 请求头携带 JWT Token（白名单接口除外）

### 2.2 通用响应结构 `ApiResponse<T>`

所有非流式接口统一返回以下 JSON 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {},
  "timestamp": "2026-05-08T12:00:00",
  "traceId": "550e8400-e29b-41d4-a716-446655440000"
}
```

| 字段 | 类型 | 说明 |
|------|------|------|
| `code` | int | 业务状态码，200 表示成功 |
| `message` | string | 返回消息描述 |
| `data` | T | 响应数据，具体结构见各接口 |
| `timestamp` | string | 响应时间戳 (ISO 8601) |
| `traceId` | string | 链路追踪 ID (UUID) |

### 2.3 认证请求头

需要认证的接口必须携带：

```
Authorization: Bearer <access_token>
```

### 2.4 网关白名单

以下路径无需认证：

- `POST /api/user/login`
- `POST /api/user/register`
- `POST /api/user/refresh`
- `/swagger-ui/**`
- `/v3/api-docs/**`
- `/doc.html`

---

## 3. 用户模块 API

> **服务**: smart-agent-user  
> **路径前缀**: `/api/user`

### 3.1 用户注册

注册新用户账号。

```
POST /api/user/register
```

**认证**: 无需认证（白名单）

**请求体** (`application/json`):

```json
{
  "username": "string",
  "password": "string",
  "nickname": "string",
  "deviceType": "string"
}
```

| 字段 | 类型 | 必填 | 约束 | 说明 |
|------|------|------|------|------|
| `username` | string | 是 | 4-64 字符 | 用户名，不可重复 |
| `password` | string | 是 | 6-20 字符 | 密码，BCrypt 加密存储 |
| `nickname` | string | 否 | — | 用户昵称 |
| `deviceType` | string | 否 | — | 设备类型，如 `web`、`mobile` |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "john_doe",
    "nickname": "John",
    "avatar": null,
    "status": 1,
    "roleId": 2,
    "createTime": "2026-05-08T10:00:00",
    "updateTime": "2026-05-08T10:00:00"
  }
}
```

**响应字段 `data` — UserVO**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 用户 ID（自增） |
| `username` | string | 用户名 |
| `nickname` | string | 昵称 |
| `avatar` | string | 头像 URL，可空 |
| `status` | int | 状态：1=正常，0=禁用 |
| `roleId` | long | 角色 ID（默认 2=普通用户） |
| `createTime` | string | 创建时间 |
| `updateTime` | string | 更新时间 |

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 400 | 用户名已存在 | 用户名重复 |
| 400 | 用户名不能为空 / 密码不能为空 | 参数校验失败 |
| 400 | 用户名长度必须在4-64之间 / 密码长度必须在6-20之间 | 参数校验失败 |

---

### 3.2 用户登录

用户使用账号密码登录，获取访问令牌。

```
POST /api/user/login
```

**认证**: 无需认证（白名单）

**请求体** (`application/json`):

```json
{
  "username": "string",
  "password": "string",
  "deviceType": "string",
  "deviceId": "string"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 密码 |
| `deviceType` | string | 否 | 设备类型，默认 `web` |
| `deviceId` | string | 否 | 设备唯一标识 |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "tokenType": "Bearer",
    "expiresIn": 3600,
    "user": {
      "id": 1001,
      "username": "john_doe",
      "nickname": "John",
      "avatar": null,
      "status": 1,
      "roleId": 2,
      "createTime": "2026-05-08T10:00:00",
      "updateTime": "2026-05-08T10:00:00"
    }
  }
}
```

**响应字段 `data` — LoginVO**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `accessToken` | string | JWT 访问令牌 |
| `refreshToken` | string | JWT 刷新令牌 |
| `tokenType` | string | 令牌类型，固定为 `Bearer` |
| `expiresIn` | long | 访问令牌有效期（秒），默认 3600（1小时） |
| `user` | object | 用户信息，结构见 UserVO |

> **令牌有效期**：Access Token 1 小时，Refresh Token 7 天。

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 400 | 用户名或密码错误 | 用户名不存在或密码不匹配 |
| 403 | 用户被禁用 | 用户状态为禁用 |

---

### 3.3 获取当前用户信息

根据请求 Token 获取当前登录用户信息。

```
GET /api/user/me
```

**认证**: 需要认证

**请求头**:
```
Authorization: Bearer <access_token>
```

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1001,
    "username": "john_doe",
    "nickname": "John",
    "avatar": "https://example.com/avatar.png",
    "status": 1,
    "roleId": 2,
    "createTime": "2026-05-08T10:00:00",
    "updateTime": "2026-05-08T10:00:00"
  }
}
```

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 401 | 未登录 | Token 无效或已过期 |
| 404 | 用户不存在 | 用户已被删除 |

---

### 3.4 更新用户信息

更新当前登录用户的昵称和头像。

```
PUT /api/user/update
```

**认证**: 需要认证

**请求体** (`application/json`):

```json
{
  "nickname": "string",
  "avatar": "string"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `nickname` | string | 否 | 新昵称，不传则不变 |
| `avatar` | string | 否 | 新头像 URL，不传则不变 |

**成功响应** (200): 返回更新后的 UserVO，结构同 [3.3](#33-获取当前用户信息)。

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 401 | 未登录 | Token 无效或已过期 |
| 404 | 用户不存在 | 用户已被删除 |

---

### 3.5 用户登出

清除当前用户的令牌信息，使 Token 失效。

```
POST /api/user/logout
```

**认证**: 需要认证

**请求头**:
```
Authorization: Bearer <access_token>
Device-Type: web
```

> **注意**: 登出会根据 `Device-Type` 请求头清除对应设备的 Token。默认值为 `web`。

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 3.6 刷新令牌

使用 Refresh Token 换取新的 Access Token。

```
POST /api/user/refresh
```

**认证**: 无需认证（白名单）

**请求参数** (`application/x-www-form-urlencoded`):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `refreshToken` | string | 是 | 登录时获取的 Refresh Token |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

`data` 字段为新的 Access Token 字符串。

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 401 | 无效的刷新令牌 | Refresh Token 无效或已过期 |

---

### 3.7 创建 API Key

为用户创建一个 API Key，可用于第三方系统集成。

```
POST /api/user/api-key
```

**认证**: 需要认证

**请求参数** (`application/x-www-form-urlencoded`):

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `name` | string | 是 | API Key 备注名 |
| `expireDays` | int | 否 | 过期天数，不传或 null 表示永不过期 |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 5001,
    "keyValue": "550e8400-e29b-41d4-a716-446655440000",
    "name": "My API Key",
    "status": 1,
    "expireAt": null,
    "lastUsedAt": null,
    "createTime": "2026-05-08T10:00:00"
  }
}
```

**响应字段 `data` — ApiKeyVO**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | API Key ID |
| `keyValue` | string | API Key 值（UUID 格式），创建后请妥善保存 |
| `name` | string | Key 备注名 |
| `status` | int | 状态：1=有效，0=已吊销 |
| `expireAt` | string | 过期时间，null=永不过期 |
| `lastUsedAt` | string | 最后使用时间 |
| `createTime` | string | 创建时间 |

---

### 3.8 获取 API Key 列表

获取当前用户的所有 API Key。

```
GET /api/user/api-key/list
```

**认证**: 需要认证

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 5001,
      "keyValue": "550e8400-e29b-41d4-a716-446655440000",
      "name": "My API Key",
      "status": 1,
      "expireAt": null,
      "lastUsedAt": "2026-05-01T15:30:00",
      "createTime": "2026-04-15T10:00:00"
    }
  ]
}
```

`data` 为 `ApiKeyVO[]` 数组，按创建时间倒序排列。

---

### 3.9 吊销 API Key

吊销指定的 API Key，使其失效。

```
DELETE /api/user/api-key/{apiKeyId}
```

**认证**: 需要认证

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `apiKeyId` | long | 要吊销的 API Key ID |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 401 | 未登录 | Token 无效或已过期 |
| 404 | API Key不存在 | apiKeyId 对应的 Key 不存在 |
| 403 | 无权操作此API Key | Key 不属于当前用户 |

---

## 4. 聊天模块 API

> **服务**: smart-agent-chat  
> **路径前缀**: `/api/chat`  
> **所有接口均需认证**

### 4.1 创建会话

创建一个新的 AI 聊天会话。

```
POST /api/chat/sessions
```

**认证**: 需要认证

**请求体** (`application/json`):

```json
{
  "title": "string",
  "agentType": "string",
  "knowledgeBaseId": 100
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `title` | string | 否 | 会话标题 |
| `agentType` | string | 否 | 代理类型，决定 AI 行为模式 |
| `knowledgeBaseId` | long | 否 | 关联知识库 ID |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 2001,
    "title": "Spring Boot 性能优化讨论",
    "agentType": "technical",
    "messageCount": 0,
    "roundCount": 0,
    "lastMessageAt": "2026-05-08T12:00:00",
    "shouldCreateNewSession": false,
    "sessionHint": null
  }
}
```

**响应字段 `data` — SessionVO**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `id` | long | 会话 ID（雪花算法） |
| `title` | string | 会话标题 |
| `agentType` | string | 代理类型 |
| `messageCount` | int | 消息总数（用户消息 + AI 回复） |
| `roundCount` | int | 对话轮数 |
| `lastMessageAt` | string | 最后一条消息时间 |
| `shouldCreateNewSession` | boolean | 是否建议新建会话（轮数过多时触发） |
| `sessionHint` | string | 会话建议提示文字 |

---

### 4.2 发送消息（SSE 流式）

向指定会话发送消息，通过 **SSE (Server-Sent Events)** 流式返回 AI 回复内容。

```
POST /api/chat/messages
```

**认证**: 需要认证

**请求体** (`application/json`):

```json
{
  "sessionId": 2001,
  "content": "什么是 Spring AOP？",
  "agentType": "technical"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sessionId` | long | 是 | 会话 ID |
| `content` | string | 是 | 用户消息内容 |
| `agentType` | string | 否 | 代理类型 |

**响应格式**: `text/event-stream` (SSE)

**SSE 事件流说明**:

该接口返回的是 SSE 流式响应（非 JSON），具体行为如下：

1. **正常数据事件**: AI 逐 token 返回文本内容，每个 SSE 事件 `data` 字段为一段文本。
2. **Token 用量元数据**: 流结束时，会发送一个携带 token 用量统计的特殊前缀数据：
   ```
   __SMART_AGENT_USAGE__:{"modelName":"gpt-4","promptTokens":150,"completionTokens":300,"totalTokens":450,"finishReason":"STOP"}
   ```
3. **错误事件**: 发生错误时，`data` 为 JSON：
   ```json
   {"error": "错误描述信息"}
   ```

**业务流程**:

1. 校验会话存在且属于当前用户
2. 构建上下文（含聊天记忆摘要）
3. 保存用户消息到数据库
4. 调用 AI Core 服务（`/api/core/chat/stream`）获取流式响应
5. 逐 token 通过 SSE 转发给客户端
6. 流结束后保存 AI 回复、更新会话统计、触发异步摘要
7. 发送消息归档到 RocketMQ

**SSE 连接超时**: 30 分钟

**错误场景**:

| 错误消息 | 说明 |
|----------|------|
| `{"error": "请求参数不合法"}` | sessionId 或 content 为空 |
| `{"error": "会话不存在"}` | 指定的 sessionId 不存在 |
| `{"error": "无权限访问该会话"}` | 会话不属于当前用户 |
| `{"error": "处理消息失败"}` | 系统内部错误 |

---

### 4.3 获取会话列表

获取当前用户的所有聊天会话，按最后消息时间倒序排列。

```
GET /api/chat/sessions
```

**认证**: 需要认证

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2002,
      "title": "数据库调优",
      "agentType": "dba",
      "messageCount": 15,
      "roundCount": 7,
      "lastMessageAt": "2026-05-08T14:30:00",
      "shouldCreateNewSession": false,
      "sessionHint": null
    },
    {
      "id": 2001,
      "title": "Spring Boot 性能优化讨论",
      "agentType": "technical",
      "messageCount": 420,
      "roundCount": 210,
      "lastMessageAt": "2026-05-08T12:00:00",
      "shouldCreateNewSession": true,
      "sessionHint": "当前会话历史较长，建议新建会话以获得更稳定的回复效果"
    }
  ]
}
```

> **会话轮数警告**: 当 `roundCount >= 200`（可通过 `chat.memory.summary-session-round-warning` 配置）时，`shouldCreateNewSession` 为 `true`，并附带提示文字。

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 500 | 服务器异常 | 服务内部错误 |

---

### 4.4 获取会话历史消息

获取指定会话的完整消息历史（包含用户消息和 AI 回复，按时间正序排列）。

```
GET /api/chat/sessions/{sessionId}/messages
```

**认证**: 需要认证

**路径参数**:

| 参数 | 类型 | 说明 |
|------|------|------|
| `sessionId` | long | 会话 ID |

**成功响应** (200):

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "role": "USER",
      "content": "什么是 Spring AOP？",
      "modelName": null,
      "promptTokens": null,
      "completionTokens": 12,
      "totalTokens": 12,
      "finishReason": null,
      "createdAt": "2026-05-08T12:00:00"
    },
    {
      "role": "ASSISTANT",
      "content": "Spring AOP（面向切面编程）是 Spring 框架的核心特性之一...",
      "modelName": "gpt-4",
      "promptTokens": 150,
      "completionTokens": 300,
      "totalTokens": 450,
      "finishReason": "STOP",
      "createdAt": "2026-05-08T12:00:05"
    }
  ]
}
```

**响应字段 `data[]` — MessageVO**:

| 字段 | 类型 | 说明 |
|------|------|------|
| `role` | string | 消息角色：`USER` 或 `ASSISTANT` |
| `content` | string | 消息文本内容 |
| `modelName` | string | 模型名称，仅 `ASSISTANT` 消息有值 |
| `promptTokens` | int | 输入 Token 数，仅 `ASSISTANT` 消息有值 |
| `completionTokens` | int | 输出 Token 数 |
| `totalTokens` | int | 总 Token 数 |
| `finishReason` | string | 结束原因，如 `STOP`、`ERROR`，仅 `ASSISTANT` 消息有值 |
| `createdAt` | string | 消息创建时间 |

> **权限控制**: 仅返回属于当前用户的会话的消息；若会话不存在或不属于当前用户，返回空数组 `[]`。

**错误响应**:

| code | message | 说明 |
|------|---------|------|
| 404 | 资源不存在 | 会话不存在（chat 模块返回 404） |

---

## 5. 错误码参考

所有接口使用统一的 `ResultCode` 状态码体系：

| code | 枚举值 | message | 说明 |
|------|--------|---------|------|
| 200 | `SUCCESS` | success | 请求成功 |
| 400 | `PARAM_ERROR` | 参数错误 | 请求参数不合法 |
| 401 | `UNAUTHORIZED` | 未登录 | 未携带有效 Token |
| 403 | `FORBIDDEN` | 权限不足 | 无权访问该资源 |
| 404 | `NOT_FOUND` | 资源不存在 | 请求的资源不存在 |
| 405 | `METHOD_NOT_ALLOWED` | 请求方法不支持 | HTTP 方法不支持 |
| 500 | `SERVER_ERROR` | 服务器异常 | 服务内部错误 |

### 全局异常处理

系统通过 `GlobalExceptionHandler` 统一处理异常：

| 异常类型 | 响应 code | 说明 |
|----------|-----------|------|
| `BizException` | 异常中携带的 code | 业务异常，返回具体错误码和消息 |
| `RuntimeException` | 500 | 运行时异常，返回 `SERVER_ERROR` |
| `Exception` | 500 | 其他未捕获异常，返回 `SERVER_ERROR` |

---

## 6. 认证与安全

### 6.1 认证流程

```
Client                    Gateway                    User Service
  |                          |                            |
  |-- POST /api/user/login ->|                            |
  |                          |-- (whitelist, bypass) ---->|
  |                          |                            |-- 验证用户密码
  |                          |                            |-- 生成 JWT Token
  |                          |<---- LoginVO (tokens) -----|
  |<--- 200 + LoginVO -------|                            |
  |                          |                            |
  |-- GET /api/user/me ----->|                            |
  |   Authorization: Bearer  |                            |
  |                          |-- 解析 JWT，提取 userId -->|
  |                          |   X-User-Id: 1001         |
  |                          |<---- UserVO --------------|
  |<--- 200 + UserVO --------|                            |
```

### 6.2 JWT Token 规范

- **签名算法**: HS256
- **Access Token 有效期**: 1 小时
- **Refresh Token 有效期**: 7 天
- **Token 类型**: Bearer
- **存储**: Redis + 数据库 `user_token` 表双写

### 6.3 请求头规范

| Header | 说明 |
|--------|------|
| `Authorization` | JWT Token，格式 `Bearer <token>` |
| `Device-Type` | 设备类型，用于 Token 隔离（`web` / `mobile`） |
| `X-Trace-Id` | 链路追踪 ID，网关自动生成 |

### 6.4 网关安全策略

- **认证过滤**: `AuthGlobalFilter` 对非白名单路径进行 JWT 校验
- **限流**: `RateLimitFilter` 支持用户级和 IP 级限流（Redis 实现）
- **Trace ID**: `TraceIdFilter` 为每个请求生成追踪 ID
- **日志**: `LoggingGlobalFilter` 记录请求/响应日志

### 6.5 密码安全

- 用户密码使用 **BCrypt** 加密存储
- 登录时使用 `BCryptPasswordEncoder.matches()` 验证
- API Key 使用 **UUID** 格式生成

---

## 7. 架构说明

### 7.1 微服务调用链路

```
Browser / Client
      |
  [Gateway :8080]
      |
      |--- /api/user/**  ---> [User Service :8081]
      |                           ├── MySQL: smart_agent_user
      |                           └── Redis: Token 缓存
      |
      |--- /api/chat/**  ---> [Chat Service :8082]
      |                           ├── MySQL: smart-agent-chat
      |                           ├── Redis: 聊天记忆
      |                           ├── RocketMQ: 消息归档
      |                           └── Elasticsearch: 消息归档存储
      |
      |--- /api/core/**  ---> [Core Service :8084]
                                  └── AI 模型调用
```

### 7.2 聊天消息处理流程

```
用户发送消息
    │
    ├── 1. 校验会话 + 权限
    ├── 2. 构建上下文（聊天记忆摘要）
    ├── 3. UserMessageEntity → MySQL
    ├── 4. 更新 Session 统计
    ├── 5. WebClient → Core Service SSE 流式调用
    ├── 6. SSE 逐 token 转发客户端
    ├── 7. LlmMessageEntity → MySQL (流结束后)
    ├── 8. LlmSessionEntity → MySQL
    ├── 9. 异步触发摘要 (if roundCount >= 3)
    └── 10. RocketMQ 消息归档
```

### 7.3 数据库

| 服务 | 数据库 | 说明 |
|------|--------|------|
| user | smart_agent_user | users, user_token, user_login_log, api_key |
| chat | smart-agent-chat | session, user_message, llm_message, llm_session, chat_memory_current, chat_memory_history |

### 7.4 相关接口 (Core Service)

Chat 模块内部通过 `WebClient` 调用 Core Service 的以下接口：

| 接口 | 说明 |
|------|------|
| `POST /api/core/chat/stream` | AI 流式对话（SSE） |
| `POST /api/core/chat/summary/raw` | 对话摘要生成（Feign 调用） |

---

> **Swagger/Knife4j 文档地址**: `http://localhost:8080/doc.html`
