# SmartAgent Docker 部署指南

## 架构概览

```
浏览器 (http://localhost)
    │
    ▼
┌──────────────────────────────────────┐
│  Nginx (frontend 容器 :80)           │
│  - 静态文件: /usr/share/nginx/html   │
│  - API代理: /api/* → gateway:8080    │
│  - SSE支持, CORS头注入               │
└──────────┬───────────────────────────┘
           │ /api/*
           ▼
┌──────────────────────────────────────┐
│  Spring Cloud Gateway (:8080)        │
│  - 认证拦截 (JWT)                    │
│  - 路由分发                          │
│  /api/user/* → user:8081             │
│  /api/chat/* → chat:8082             │
└──┬──────────────┬────────────────────┘
   │              │
   ▼              ▼
┌──────┐    ┌──────────┐
│ User │    │  Chat    │
│:8081 │    │  :8082   │
└──┬───┘    └──┬───┬───┘
   │           │   │
   ▼           ▼   ▼
┌──────┐  ┌──────┐ ┌─────────────┐
│MySQL │  │Redis │ │ES/RocketMQ  │
│:3306 │  │:6379 │ │:9200/:9876  │
└──────┘  └──────┘ └─────────────┘
   ▲
   │ (所有服务共享Nacos注册中心)
   ▼
┌──────┐
│Nacos │
│:8848 │
└──────┘
```

**跨域解决原理**: 浏览器只与 Nginx 同一源通信，Nginx 将 `/api/` 请求反向代理到后端网关。浏览器视角下所有请求同源，自然不存在跨域问题。

---

## 快速开始

### 前置要求

- Docker >= 24.0
- Docker Compose >= 2.20
- 可用内存 >= 8GB（推荐 16GB）

### 1. 配置环境变量

```bash
# 查看并修改配置（数据库密码等）
vim env/.env
```

关键配置项：

| 变量 | 默认值 | 说明 |
|---|---|---|
| `JWT_SECRET` | `smart-agent-secret-key-smart-agent-2026` | JWT 签名密钥 |
| `MYSQL_ROOT_PASSWORD` | `root` | MySQL root 密码 |
| `ELASTICSEARCH_PASSWORD` | `smart123` | ES elastic 用户密码 |
| `FRONTEND_PORT` | `80` | 前端对外端口 |

### 2. 构建镜像

```bash
# 构建所有服务（首次较慢，需下载依赖）
bash env/build.sh

# 或只构建特定服务
bash env/build.sh gateway
bash env/build.sh frontend
```

### 3. 启动服务

```bash
# 启动核心服务（MySQL, Redis, Nacos, RocketMQ, ES, 所有微服务, 前端）
bash env/start.sh

# 含 Milvus 向量数据库
bash env/start.sh --profile milvus
```

### 4. 访问

打开浏览器访问 `http://localhost`

### 5. 停止

```bash
# 停止服务（保留数据）
bash env/stop.sh

# 停止并清除所有数据
bash env/stop.sh --volumes
```

---

## 微服务扩容

### 水平扩容聊天服务

聊天服务是无状态的（会话数据在 Redis/MySQL 中），可以安全扩容：

```bash
# 扩容到 3 个实例
docker compose -f env/docker-compose.yml --env-file env/.env up -d --scale chat=3

# Nginx + Gateway + Nacos 自动负载均衡
```

### 扩容用户服务

```bash
docker compose -f env/docker-compose.yml --env-file env/.env up -d --scale user=2
```

### 扩容网关

> **注意**: 网关扩容需配合 Nginx upstream 配置

1. 修改 `env/nginx.conf`，在 `upstream gateway` 块中添加多个 server：

```nginx
upstream gateway {
    server gateway:8080;
    server gateway_2:8080;  # 新增实例
}
```

2. 重新构建前端镜像并启动：

```bash
bash env/build.sh frontend
docker compose -f env/docker-compose.yml --env-file env/.env up -d --scale gateway=2
```

### 查看运行实例

```bash
docker compose -f env/docker-compose.yml ps
```

---

## 端口映射

| 服务 | 容器端口 | 宿主机端口 | 说明 |
|---|---|---|---|
| **frontend** (Nginx) | 80 | `${FRONTEND_PORT:-80}` | **对外统一入口** |
| gateway | 8080 | 8080 | API 网关（调试用） |
| user | 8081 | - | 用户服务（仅内网） |
| chat | 8082 | - | 聊天服务（仅内网） |
| mysql | 3306 | `${MYSQL_PORT:-3306}` | 数据库 |
| redis | 6379 | `${REDIS_PORT:-6379}` | 缓存 |
| nacos | 8848 | `${NACOS_PORT:-8848}` | 注册中心控制台 |
| rocketmq-nameserver | 9876 | `${ROCKETMQ_PORT:-9876}` | MQ 名字服务 |
| elasticsearch | 9200 | `${ES_PORT:-9200}` | 搜索引擎 |
| minio | 9001 | 9001 | MinIO 控制台 (milvus profile) |
| milvus | 19530 | 19530 | 向量数据库 (milvus profile) |

> 提示：将 `xxx_PORT` 设为 `0` 或空字符串可禁用端口映射，仅容器内网访问。

---

## 配置说明

### 环境变量优先级

Spring Boot 配置优先级（从高到低）：
1. Docker 环境变量（`docker-compose.yml` 中 `environment`）
2. `application.yml` 中的 `${VAR:default}` 默认值
3. `application-local.yml`（本地开发用）

### 修改密码

编辑 `env/.env`，修改密码后重启：

```bash
# 修改 MYSQL_ROOT_PASSWORD 和 ELASTICSEARCH_PASSWORD
vim env/.env
bash env/stop.sh
bash env/start.sh
```

> **注意**: MySQL 和 ES 的密码在首次启动时设定，修改后需同时清除数据卷：`bash env/stop.sh --volumes`

### 自定义 JWT 密钥

```bash
# 在 env/.env 中修改
JWT_SECRET=your-custom-secret-key-min-32-chars
```

### 开发模式 vs 生产模式

- **开发模式**: 后端使用 `SPRING_PROFILES_ACTIVE=local`，前端 `npm run dev` 走 Vite proxy
- **生产模式**: 后端使用 `SPRING_PROFILES_ACTIVE=docker`，前端构建为静态文件由 Nginx 托管

后端 `application.yml` 中已用 `${ENV_VAR:localhost_default}` 格式外部化所有基础设施地址，同一份代码兼容两种模式。

---

## 常用操作

### 查看日志

```bash
# 所有服务
docker compose -f env/docker-compose.yml logs -f

# 特定服务
docker compose -f env/docker-compose.yml logs -f gateway
docker compose -f env/docker-compose.yml logs -f chat
```

### 重启单个服务

```bash
docker compose -f env/docker-compose.yml restart chat
```

### 进入容器调试

```bash
docker exec -it smart-agent-gateway sh
docker exec -it smart-agent-mysql mysql -uroot -p
```

### Nacos 控制台

浏览器访问 `http://localhost:8848/nacos`，可查看服务注册状态。

### 数据库初始化

用户服务的 DDL 脚本位于 `backend/user/src/main/resources/db/schema.sql`，已在 MySQL 容器首次启动时自动执行。如需重新初始化：

```bash
bash env/stop.sh --volumes
bash env/start.sh
```

### 重建某个服务（代码有改动时）

```bash
bash env/build.sh chat                                    # 重新构建
docker compose -f env/docker-compose.yml up -d --force-recreate chat  # 重建容器
```

---

## Nginx 配置说明

配置文件: `env/nginx.conf`

关键特性：
- **SPA 路由**: `try_files $uri /index.html` 支持 Vue Router history 模式
- **SSE 流式响应**: `proxy_buffering off` + `proxy_cache off` + `proxy_read_timeout 1800s`
- **CORS 安全兜底**: 注入 `Access-Control-Allow-*` 头
- **Gzip 压缩**: 静态资源开启 gzip
- **WebSocket 支持**: `Upgrade` 和 `Connection` 头透传

---

## 故障排查

### 1. 容器启动失败

```bash
# 查看所有容器状态
docker compose -f env/docker-compose.yml ps

# 查看具体容器日志
docker logs smart-agent-gateway
```

### 2. 后端服务未注册到 Nacos

检查 Nacos 是否健康：
```bash
curl http://localhost:8848/nacos/v1/console/health/readiness
```

确认后端日志中环境变量是否正确：
```bash
docker logs smart-agent-gateway 2>&1 | grep -i "nacos\|error"
```

### 3. 前端请求 404

确认 Nginx 配置中 `try_files` 指令生效。检查 Nginx 日志：
```bash
docker logs smart-agent-frontend
```

### 4. 聊天 SSE 流中断

确认 Nginx 中 `proxy_buffering off` 和 `proxy_read_timeout` 设置足够大。

### 5. MySQL 连接拒绝

刚启动时 MySQL 需要 10-30 秒初始化。等待 healthcheck 通过后后端自动连接。

```bash
# 等待 MySQL 就绪
docker compose -f env/docker-compose.yml ps mysql
```

### 6. 端口冲突

修改 `env/.env` 中的端口变量：

```bash
FRONTEND_PORT=8088   # 改为其他端口
MYSQL_PORT=3307
```

---

## 文件清单

| 文件 | 用途 |
|---|---|
| `env/docker-compose.yml` | Docker 服务编排 |
| `env/nginx.conf` | Nginx 反向代理配置 |
| `env/Dockerfile.gateway` | 网关服务镜像 |
| `env/Dockerfile.user` | 用户服务镜像 |
| `env/Dockerfile.chat` | 聊天服务镜像 |
| `env/Dockerfile.frontend` | 前端镜像 |
| `env/.env` | 环境变量配置 |
| `env/build.sh` | 构建脚本 |
| `env/start.sh` | 启动脚本 |
| `env/stop.sh` | 停止脚本 |
