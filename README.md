# SmartAgent — AI Agent 平台

基于 Spring Boot 3 + Spring Cloud 微服务架构的 AI Agent 平台，集成 LangChain4j 实现多轮对话、RAG 知识库问答和 ReAct Agent 工具调用。前端采用 Vue 3 + TypeScript，提供类 ChatGPT 的流式对话体验。

## 核心功能

- **多轮对话** — 会话管理 + AI 记忆压缩，支持 DeepSeek / OpenAI 等模型
- **RAG 增强问答** — 上传私有文档（PDF / DOCX / TXT），解析 → 向量化 → 检索增强生成
- **ReAct Agent** — LLM 自主规划并调用工具（天气查询、数学计算、搜索等）
- **流式 SSE 推送** — 逐 Token 实时返回，类 ChatGPT 体验
- **知识库管理** — 文档上传、解析、向量化存储（Milvus）、语义检索
- **用户体系** — 注册 / 登录 / JWT 双 Token 刷新 / API Key 管理 / RBAC
- **国际化** — 中英文双语界面，通过 JSON locale 文件全量可配置

## 技术栈

### 后端

| 类别 | 技术 | 版本 |
|------|------|------|
| 语言 | Java | 21 |
| 框架 | Spring Boot 3 + Spring Cloud | 3.2.5 / 2023.0.1 |
| AI 编排 | LangChain4j | 0.35.0 |
| ORM | MyBatis-Plus | 3.5.6 |
| 注册中心 | Nacos | 2.x |
| 认证 | JWT (jjwt) | 0.11.5 |
| API 文档 | Knife4j | 4.5.0 |
| 数据库 | MySQL 8 | — |
| 缓存 | Redis | — |
| 消息队列 | RocketMQ | — |
| 向量数据库 | Milvus | — |
| 搜索引擎 | Elasticsearch | — |
| 构建 | Maven | 多模块 |

### 前端

| 类别 | 技术 | 版本 |
|------|------|------|
| 框架 | Vue 3 (Composition API) + TypeScript | 3.5+ |
| 构建 | Vite | 8.x |
| 路由 | Vue Router | 4.x |
| 状态管理 | Pinia | 3.x |
| UI 库 | Element Plus | 2.x |
| 国际化 | vue-i18n | 10.x |
| SSE | @microsoft/fetch-event-source | — |
| Markdown | markdown-it + highlight.js | — |

## 项目结构

```
smart-agent/
├── backend/                    # 后端 Maven 多模块工程
│   ├── common/                 # 公共包（模型、工具、事件、异常处理）
│   ├── gateway/                # API 网关（JWT 鉴权、路由分发、限流）
│   ├── user/                   # 用户服务（注册/登录/JWT/API Key）
│   ├── chat/                   # 对话服务（会话管理、SSE 流式推送）
│   ├── model/                  # AI 模型服务（LangChain4j AiService 接口）
│   ├── memory/                 # 记忆服务（对话记忆存储、摘要压缩）
│   ├── knowledge/              # 知识库服务（文档解析、向量化、检索）
│   └── CLAUDE.md               # Claude Code 项目指南
├── frontend/                   # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 层（Axios 实例、拦截器）
│   │   ├── components/         # 组件（common/chat/layout）
│   │   ├── composables/        # Vue 组合式函数
│   │   ├── config/             # 配置（API、主题、应用常量）
│   │   ├── locales/            # 国际化翻译文件（zh-CN / en）
│   │   ├── router/             # 路由定义 + 鉴权守卫
│   │   ├── stores/             # Pinia 状态管理
│   │   ├── types/              # TypeScript 类型定义
│   │   ├── utils/              # 工具函数
│   │   └── views/              # 页面组件
│   └── README.md
├── env/                        # Docker 部署
│   ├── docker-compose.yml      # 服务编排
│   ├── nginx.conf              # Nginx 反向代理
│   ├── .env                    # 环境变量配置
│   ├── build.sh                # 镜像构建脚本
│   ├── start.sh                # 启动脚本
│   └── stop.sh                 # 停止脚本
├── resume/                     # 简历文档
├── structures.md               # 技术文档 & 接口规范
├── Airepair.md                 # AI 修复报告
└── Aiteaching.md               # 模块架构设计
```

## 架构概览

```
浏览器 (localhost)
    │
    ▼
Nginx (frontend 容器 :80)
    │ /api/*
    ▼
Spring Cloud Gateway (:8080)
    ├── /api/user/**     →  User Service (:8081)  →  MySQL (smart_agent_user)
    ├── /api/chat/**     →  Chat Service (:8082)   →  MySQL (smart-agent-chat)
    │                          │
    │                          ├── →  Model Service (:8085)   →  DeepSeek / OpenAI
    │                          ├── →  Memory Service (:8086)  →  MySQL + Redis
    │                          └── →  RocketMQ (消息归档)
    └── /api/knowledge/** → Knowledge Service (:8083) →  Milvus + Elasticsearch
```

### 微服务模块

| 模块 | 端口 | Nacos 服务名 | 职责 |
|------|------|-------------|------|
| gateway | 8080 | smart-agent-gateway | JWT 鉴权、路由分发、限流、TraceId |
| user | 8081 | smart-agent-user | 用户注册/登录、Token 管理、API Key |
| chat | 8082 | smart-agent-chat | 会话管理、SSE 流式推送、消息持久化 |
| knowledge | 8083 | smart-agent-knowledge | 文档上传/解析、向量化、语义检索 |
| model | 8085 | smart-agent-model | AI 模型调用（LangChain4j AiService） |
| memory | 8086 | smart-agent-memory | 对话记忆存储、异步摘要压缩 |

## 快速开始

### 前置要求

- **后端**: Java 21, Maven 3.8+
- **前端**: Node.js >= 18, npm >= 9
- **中间件**: Docker Compose >= 2.20
- **AI 模型**: DeepSeek 或 OpenAI API Key

### 1. 克隆 & 配置

```bash
git clone <repo-url> smart-agent
cd smart-agent

# 配置环境变量（数据库密码、JWT 密钥等）
vim env/.env
```

### 2. 启动中间件

```bash
# 启动核心中间件（MySQL, Redis, Nacos, RocketMQ, Elasticsearch）
bash env/start.sh

# 含 Milvus 向量数据库（知识库功能需要）
bash env/start.sh --profile milvus
```

### 3. 启动后端

```bash
cd backend

# 设置 AI API Key
export AI_MODEL_API_KEY=your-api-key
export AI_MODEL_BASE_URL=https://api.deepseek.com/v1

# 按顺序启动各模块
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl user
mvn spring-boot:run -pl model
mvn spring-boot:run -pl memory
mvn spring-boot:run -pl chat
mvn spring-boot:run -pl knowledge
```

### 4. 启动前端

```bash
cd frontend
npm install
cp .env.example .env.development
npm run dev
```

### 5. 访问

- 前端界面: http://localhost:5173
- API 网关: http://localhost:8080
- Nacos 控制台: http://localhost:8848/nacos
- API 文档: http://localhost:8080/doc.html

## Docker 一键部署

```bash
# 构建所有镜像
bash env/build.sh

# 启动全部服务
bash env/start.sh

# 访问 http://localhost
```

水平扩容：

```bash
# 扩容聊天服务到 3 个实例
docker compose -f env/docker-compose.yml --env-file env/.env up -d --scale chat=3
```

## API 概览

| 模块 | 路径 | 说明 |
|------|------|------|
| 用户 | `POST /api/user/login` | 登录 |
| 用户 | `POST /api/user/register` | 注册 |
| 用户 | `POST /api/user/refresh` | 刷新 Token |
| 用户 | `POST /api/user/api-keys` | 创建 API Key |
| 对话 | `POST /api/chat/sessions` | 创建会话 |
| 对话 | `POST /api/chat/stream` | 发送消息（SSE 流式） |
| 对话 | `GET /api/chat/sessions` | 会话列表 |
| 对话 | `GET /api/chat/sessions/{id}/messages` | 历史消息 |
| 知识库 | `POST /api/knowledge/bases` | 创建知识库 |
| 知识库 | `POST /api/knowledge/documents` | 上传文档 |

详细接口文档见 [backend/API_DOCUMENTATION.md](backend/API_DOCUMENTATION.md)。

## 相关文档

- [技术文档 & 接口规范](structures.md) — 模块详解、数据库设计、开发环境搭建
- [模块架构设计](Aiteaching.md) — 微服务职责划分与依赖关系
- [后端项目指南](backend/CLAUDE.md) — Claude Code 项目说明
- [前端 README](frontend/README.md) — 前端项目详细说明
- [Docker 部署指南](env/docker.md) — 容器化部署与运维
- [API 接口文档](backend/API_DOCUMENTATION.md) — 完整 REST API 参考

## License

MIT
