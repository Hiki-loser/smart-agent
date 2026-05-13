# SmartAgent —  AI Agent 平台

**技术文档 & 接口规范**

`Spring Boot 3` · `LangChain4j` · `Spring Cloud` · `RAG`

> 版本 1.0 — 2025

---

## 目录

- [第一章  项目概述](#第一章--项目概述)
  - [1.1  项目背景与目标](#11--项目背景与目标)
  - [1.2  核心功能一览](#12--核心功能一览)
  - [1.3  技术栈全景](#13--技术栈全景)
- [第二章  Maven 多模块工程结构](#第二章--maven-多模块工程结构)
  - [2.1  顶层模块划分](#21--顶层模块划分)
  - [2.2  父 pom.xml 关键配置说明](#22--父-pomxml-关键配置说明)
- [第三章  smart-agent-common — 公共基础包](#第三章--smart-agent-common--公共基础包)
  - [3.1  模块职责](#31--模块职责)
  - [3.2  包结构](#32--包结构)
  - [3.3  核心类详解](#33--核心类详解)
- [第四章  smart-agent-gateway — API 网关](#第四章--smart-agent-gateway--api-网关)
  - [4.1  模块职责](#41--模块职责)
  - [4.2  包结构](#42--包结构)
  - [4.3  核心类详解](#43--核心类详解)
  - [4.4  application.yml 关键配置](#44--applicationyml-关键配置)
- [第五章  smart-agent-user — 用户与权限服务](#第五章--smart-agent-user--用户与权限服务)
  - [5.1  模块职责](#51--模块职责)
  - [5.2  包结构](#52--包结构)
  - [5.3  数据库表设计](#53--数据库表设计)
  - [5.4  核心类详解](#54--核心类详解)
- [第六章  smart-agent-chat — 对话与会话服务](#第六章--smart-agent-chat--对话与会话服务)
  - [6.1  模块职责](#61--模块职责)
  - [6.2  包结构](#62--包结构)
  - [6.3  数据库表设计](#63--数据库表设计)
  - [6.4  核心类详解](#64--核心类详解)
- [第七章  smart-agent-core — Agent 编排核心](#第七章--smart-agent-core--agent-编排核心重中之重)
  - [7.1  模块职责](#71--模块职责)
  - [7.2  包结构](#72--包结构)
  - [7.3  LangChain4j 核心概念](#73--langchain4j-核心概念必须理解)
  - [7.4  核心类详解](#74--核心类详解)
  - [7.5  Agent 类型对比](#75--agent-类型对比)
- [第八章  smart-agent-knowledge — 知识库服务](#第八章--smart-agent-knowledge--知识库服务)
  - [8.1  模块职责](#81--模块职责)
  - [8.2  包结构](#82--包结构)
  - [8.3  数据库表设计](#83--数据库表设计)
  - [8.4  核心类详解](#84--核心类详解)
- [第九章  smart-agent-tools — 工具扩展包](#第九章--smart-agent-tools--工具扩展包)
  - [9.1  模块职责](#91--模块职责)
  - [9.2  包结构](#92--包结构)
  - [9.3  核心类详解](#93--核心类详解)
- [第十章  RESTful 接口文档](#第十章--restful-接口文档)
  - [10.1  接口规范说明](#101--接口规范说明)
  - [10.2  用户认证接口](#102--用户认证接口)
  - [10.3  对话接口](#103--对话接口)
  - [10.4  知识库接口](#104--知识库接口)
  - [10.5  错误码参考](#105--错误码参考)
- [第十一章  数据库完整设计](#第十一章--数据库完整设计)
  - [11.1  表关系概览](#111--表关系概览)
  - [11.2  索引设计原则](#112--索引设计原则)
  - [11.3  MyBatis-Plus 配置](#113--mybatis-plus-配置)
- [第十二章  开发环境搭建](#第十二章--开发环境搭建零基础完整指南)
  - [12.1  必备软件安装](#121--必备软件安装)
  - [12.2  中间件 Docker Compose 一键启动](#122--中间件-docker-compose-一键启动)
  - [12.3  初始化数据库](#123--初始化数据库)
  - [12.4  配置 API Key](#124--配置-api-key)
  - [12.5  启动顺序](#125--启动顺序)
- [第十三章  开发规范与注意事项](#第十三章--开发规范与注意事项)
  - [13.1  代码规范](#131--代码规范)
  - [13.2  异常处理规范](#132--异常处理规范)
  - [13.3  安全规范](#133--安全规范)
  - [13.4  性能优化要点](#134--性能优化要点面试常问)
  - [13.5  可观测性配置](#135--可观测性配置)
- [第十四章  面试高频考点与参考答案](#第十四章--面试高频考点与参考答案)
  - [14.1  项目介绍模板](#141--项目介绍模板1分钟口述)
  - [14.2  高频技术问题](#142--高频技术问题)

---

## 第一章  项目概述

### 1.1  项目背景与目标

SmartAgent 是一个面向大厂后端实习求职的完整 AI Agent 平台项目，核心目标是帮助开发者在一个真实的生产级项目中，同时掌握 Spring Boot 3、Spring Cloud 微服务、LangChain4j AI 编排、RAG 知识库、向量数据库、消息队列、分布式缓存等主流后端技术栈。

该项目参照字节跳动、阿里、腾讯等大厂的后端系统设计规范，采用分层架构 + 微服务拆分，具备良好的可扩展性、可观测性和工程规范，是目前最能凸显后端求职竞争力的 AI 项目方向。

### 1.2  核心功能一览

- 多轮对话（含历史记忆与会话管理）
- RAG 增强问答（上传私有文档，AI 基于文档回答）
- ReAct Agent（LLM 自主规划并调用工具完成复杂任务）
- 工具插件体系（天气、数据库、代码执行、搜索等可扩展）
- 流式 SSE 推送（逐 Token 实时返回，类 ChatGPT 体验）
- 知识库管理（文档上传、解析、向量化、检索）
- 用户体系（注册/登录/JWT/RBAC 权限/API Key）
- 监控与可观测性（Prometheus + Grafana + SkyWalking 链路追踪）

### 1.3  技术栈全景

| 层次 | 技术选型 | 版本/说明 |
| --- | --- | --- |
| 核心框架 | Spring Boot 3.x | 基于 Java 21，支持虚拟线程 |
| 微服务 | Spring Cloud 2023.x + Nacos + OpenFeign | 服务注册、配置中心、服务调用 |
| 网关 | Spring Cloud Gateway | 动态路由、限流、鉴权拦截 |
| AI 编排 | LangChain4j 0.35+ | Agent / RAG / Memory / Tool 全套 |
| LLM 接入 | OpenAI / DeepSeek / 通义千问 | 通过 LangChain4j 统一适配 |
| 向量数据库 | Milvus 2.x | 存储 Embedding 向量，ANN 检索 |
| 关系型数据库 | MySQL 8.x + MyBatis-Plus 3.x | 主业务数据持久化 |
| 缓存 | Redis 7.x (Redisson) | 会话窗口、热点缓存、分布式锁 |
| 消息队列 | RocketMQ 5.x | 异步知识库构建、解耦 |
| 全文搜索 | Elasticsearch 8.x | 对话历史检索、知识内容搜索 |
| 文件存储 | MinIO | 文档原文件存储 |
| 鉴权 | Sa-Token + JWT | 双 Token 无状态鉴权 |
| 文档解析 | Apache Tika | PDF/Word/Excel/网页解析 |
| API 文档 | Knife4j (Swagger 3) | 自动生成在线接口文档 |
| 监控 | Prometheus + Grafana | 指标采集与可视化 |
| 链路追踪 | SkyWalking | 分布式调用链追踪 |
| 容器化 | Docker Compose / K8s + Helm | 本地开发 & 生产部署 |
| CI/CD | GitHub Actions | 自动化构建测试部署 |

---

## 第二章  Maven 多模块工程结构

### 2.1  顶层模块划分

整个项目采用 Maven 多模块（Multi-Module）结构，父 pom 统一管理依赖版本。各模块可独立打包部署，也可以按微服务方式分开部署。

```
smart-agent/                        ← 父模块（只有 pom.xml，不含业务代码）
├── smart-agent-common/              ← 公共基础包（被所有模块依赖）
├── smart-agent-gateway/             ← API 网关
├── smart-agent-user/                ← 用户与权限服务
├── smart-agent-chat/                ← 对话与会话服务
├── smart-agent-core/                ← Agent 编排核心（最重要）
├── smart-agent-knowledge/           ← 知识库服务
├── smart-agent-tools/               ← 工具扩展包
└── smart-agent-deploy/              ← 部署配置（Docker/K8s 文件）
```

### 2.2  父 pom.xml 关键配置说明

父 pom 使用 `spring-boot-dependencies` 和 `spring-cloud-dependencies` 作为 BOM（依赖管理），所有子模块继承父 pom，不需要重复声明版本号。

```xml
<!-- 父 pom 关键片段 -->
<groupId>com.smartagent</groupId>
<artifactId>smart-agent</artifactId>
<version>1.0.0-SNAPSHOT</version>
<packaging>pom</packaging>

<properties>
  <java.version>21</java.version>
  <spring-boot.version>3.2.5</spring-boot.version>
  <spring-cloud.version>2023.0.1</spring-cloud.version>
  <langchain4j.version>0.35.0</langchain4j.version>
  <mybatis-plus.version>3.5.7</mybatis-plus.version>
</properties>
```

> 💡 Java 21 支持虚拟线程（Virtual Threads），可以极大提升 IO 密集型场景（如 LLM 调用）的并发能力，是 Spring Boot 3.x 的重要新特性。

---

## 第三章  smart-agent-common — 公共基础包

### 3.1  模块职责

common 模块是所有其他模块的基础依赖，不包含业务逻辑，只提供：统一响应结构、全局异常处理、工具类、常量定义、基础注解等。它不依赖任何其他业务模块，但被所有业务模块依赖。

### 3.2  包结构

```
com.smartagent.common/
├── config/          ← 公共自动配置（Jackson、序列化）
├── constant/        ← 系统常量
├── exception/       ← 自定义异常体系
├── handler/         ← 全局异常处理器
├── model/           ← 通用响应模型
├── enums/           ← 业务枚举
├── utils/           ← 工具类
└── annotation/      ← 自定义注解
```

### 3.3  核心类详解

#### 3.3.1  model 包

**`Result<T>`** — `com.smartagent.common.model`

职责: 统一 API 响应体，所有接口的返回值都必须包装在此类中，确保前后端约定一致。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `code` | `private Integer` | 业务状态码，200=成功，非200=失败 |
| `message` | `private String` | 提示信息 |
| `data` | `private T` | 实际数据 |
| `timestamp` | `private Long` | 响应时间戳 |
| `success(T data)` | `static Result<T>` | 成功响应，code=200 |
| `fail(String msg)` | `static Result<T>` | 失败响应，code=500 |
| `fail(int code, String msg)` | `static Result<T>` | 自定义code失败响应 |

**`PageResult<T>`** — `com.smartagent.common.model`

职责: 分页查询统一响应体，包含当前页数据列表和分页信息。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `list` | `private List<T>` | 当前页数据 |
| `total` | `private Long` | 总记录数 |
| `page` | `private Integer` | 当前页码（从1开始） |
| `size` | `private Integer` | 每页条数 |
| `of(IPage<T> page)` | `static PageResult<T>` | 从 MyBatis-Plus IPage 转换 |

#### 3.3.2  exception 包

**`BusinessException`** — `com.smartagent.common.exception`

职责: 业务异常基类，所有业务逻辑错误都应抛出此异常的子类。携带错误码，便于前端区分处理。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `code` | `private final int` | 错误码 |
| `message` | `private final String` | 错误描述 |
| `BusinessException(ErrorCode errorCode)` | 构造方法 | 通过枚举构造 |
| `BusinessException(int code, String message)` | 构造方法 | 通过码值构造 |

**`ErrorCode`** — `com.smartagent.common.enums`

职责: 错误码枚举，集中管理所有业务错误码，便于维护和统一。

| 枚举值 | 码值 | 说明 |
| --- | --- | --- |
| `SUCCESS` | 200 | 操作成功 |
| `PARAM_ERROR` | 400 | 请求参数错误 |
| `UNAUTHORIZED` | 401 | 未登录或Token失效 |
| `FORBIDDEN` | 403 | 无访问权限 |
| `NOT_FOUND` | 404 | 资源不存在 |
| `TOO_MANY_REQUESTS` | 429 | 请求过于频繁 |
| `SYSTEM_ERROR` | 500 | 系统内部错误 |
| `AI_CALL_ERROR` | 5001 | AI服务调用失败 |
| `VECTOR_DB_ERROR` | 5002 | 向量库操作失败 |
| `KNOWLEDGE_PARSE_ERROR` | 5003 | 文档解析失败 |

#### 3.3.3  handler 包

**`GlobalExceptionHandler`** — `com.smartagent.common.handler`

职责: 全局异常拦截器，使用 `@RestControllerAdvice` 注解，统一捕获所有未处理异常，转换为标准 Result 响应，避免把堆栈信息暴露给前端。

| 方法 | 说明 |
| --- | --- |
| `@ExceptionHandler(BusinessException.class)` | 处理业务异常 |
| `@ExceptionHandler(MethodArgumentNotValidException.class)` | 处理参数校验失败 |
| `@ExceptionHandler(Exception.class)` | 兜底处理所有未知异常 |

#### 3.3.4  utils 包

**`SnowflakeIdUtils`** — `com.smartagent.common.utils`

职责: 雪花算法 ID 生成器，生成全局唯一的 Long 类型 ID，适合分布式环境下的主键生成，避免数据库自增 ID 暴露业务量。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `workerId` | `private final long` | 机器ID（0-31） |
| `datacenterId` | `private final long` | 数据中心ID（0-31） |
| `nextId()` | `long` | 生成下一个唯一ID |

**`JwtUtils`** — `com.smartagent.common.utils`

职责: 基于 JJWT 库的 JWT 工具类，负责 Token 的生成、解析和校验。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `SECRET_KEY` | `private static final String` | 从配置读取的签名密钥 |
| `EXPIRE` | `private static final long` | Token 过期时间（毫秒） |
| `generateToken(Long userId, String role)` | `String` | 生成 JWT Token |
| `parseToken(String token)` | `Claims` | 解析 Token，失败抛异常 |
| `isExpired(String token)` | `boolean` | 检查是否过期 |
| `getUserId(String token)` | `Long` | 从 Token 提取用户ID |

**`RedisUtils`** — `com.smartagent.common.utils`

职责: 对 Redisson 客户端的简单封装，提供常用的缓存操作方法，统一序列化方式。

| 方法 | 说明 |
| --- | --- |
| `void set(String key, Object value, long ttl, TimeUnit unit)` | 设置缓存 |
| `Object get(String key)` | 获取缓存 |
| `void del(String key)` | 删除缓存 |
| `boolean setIfAbsent(String key, Object value, long ttl)` | 分布式锁常用 |
| `Long incr(String key)` | 原子递增，限流用 |

#### 3.3.5  annotation 包

**`@Log`** — `com.smartagent.common.annotation`

职责: 自定义操作日志注解，标注在 Controller 方法上，通过 AOP 自动记录接口调用日志（入参、耗时、操作人、结果）。

| 属性 | 类型 | 说明 |
| --- | --- | --- |
| `title()` | `String` | 操作标题，如"用户登录" |
| `type()` | `String` | 操作类型 |

**`LogAspect`** — `com.smartagent.common.handler`

职责: `@Log` 注解的 AOP 切面实现，使用 `@Around` 拦截方法执行，记录操作日志到数据库或日志文件。

| 方法 | 说明 |
| --- | --- |
| `@Around("@annotation(Log)")` | 环绕通知拦截所有 @Log 方法 |
| `void saveLog(JoinPoint jp, Log log, Object result, long cost)` | 保存日志记录 |

---

## 第四章  smart-agent-gateway — API 网关

### 4.1  模块职责

Gateway 是整个系统的唯一入口，所有外部请求都必须经过网关。它承担以下职责：

- 路由转发 — 将请求路由到对应的微服务
- JWT 鉴权过滤 — 校验 Token 有效性
- 限流防刷 — 防止接口被大量请求压垮
- 请求日志记录
- 跨域配置（CORS）

> 💡 网关基于 Spring WebFlux 响应式编程模型，本身是非阻塞的。过滤器链中不要做耗时的阻塞 IO 操作。

### 4.2  包结构

```
com.smartagent.gateway/
├── config/          ← 路由规则、跨域、限流配置
├── filter/          ← 全局过滤器（鉴权、日志、限流）
└── handler/         ← 异常响应处理
```

### 4.3  核心类详解

**`GatewayConfig`** — `com.smartagent.gateway.config`

职责: 路由配置类，通过代码方式定义路由规则（也可通过 Nacos 动态配置）。将不同路径前缀的请求路由到对应的下游服务。

| 方法 | 说明 |
| --- | --- |
| `@Bean RouteLocator customRouteLocator(RouteLocatorBuilder builder)` | 定义路由规则：`/api/user/**` → `lb://smart-agent-user`，`/api/chat/**` → `lb://smart-agent-chat`，`/api/agent/**` → `lb://smart-agent-core`，`/api/knowledge/**` → `lb://smart-agent-knowledge` |

**`AuthGlobalFilter`** — `com.smartagent.gateway.filter`

职责: 全局鉴权过滤器（最重要的过滤器），对所有请求做 JWT 校验。白名单路径（登录、注册）直接放行，其余请求校验 Authorization header 中的 Bearer Token 合法性。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `whiteList` | `private List<String>` | 白名单路径列表（从配置读取） |
| `filter(ServerWebExchange exchange, GatewayFilterChain chain)` | `Mono<Void>` | 1. 检查请求路径是否在白名单 → 是则放行；2. 取出 Authorization header；3. 调用 JwtUtils 解析 Token，失败则返回 401；4. 将 userId/role 注入请求 header 传给下游 |

**`RateLimitFilter`** — `com.smartagent.gateway.filter`

职责: 基于 Redis 令牌桶算法的限流过滤器。使用 Spring Cloud Gateway 内置的 RequestRateLimiter + Redis 实现，按用户 ID 维度限流（如每用户每分钟最多 60 次请求）。

| 成员 | 类型/签名 | 说明 |
| --- | --- | --- |
| `redisRateLimiter` | `private RedisRateLimiter` | 内置 Redis 限流器 |
| `userKeyResolver()` | `KeyResolver` | 按用户ID提取限流 Key |

**`GatewayExceptionHandler`** — `com.smartagent.gateway.handler`

职责: 网关层全局异常处理，实现 `ErrorWebExceptionHandler` 接口，将各种网关异常（路由不存在、服务不可用、限流等）统一转换为标准 JSON 格式返回，避免返回 HTML 错误页。

| 方法 | 说明 |
| --- | --- |
| `Mono<Void> handle(ServerWebExchange exchange, Throwable ex)` | 根据异常类型映射 HTTP 状态码和业务错误码，写入标准 Result JSON 响应 |

### 4.4  application.yml 关键配置

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: user-service
          uri: lb://smart-agent-user          # lb:// 表示负载均衡
          predicates:
            - Path=/api/user/**
          filters:
            - StripPrefix=1                   # 去掉 /api 前缀转发
      default-filters:
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 20   # 每秒补充令牌数
            redis-rate-limiter.burstCapacity: 40   # 令牌桶容量
```

---

## 第五章  smart-agent-user — 用户与权限服务

### 5.1  模块职责

用户服务负责完整的用户生命周期管理：注册、登录、JWT 颁发、Token 刷新、用户信息维护、RBAC 角色权限控制、API Key 管理（供第三方调用）。

### 5.2  包结构

```
com.smartagent.user/
├── controller/      ← 接口层（REST API）
├── service/         ← 业务逻辑层
│   └── impl/
├── mapper/          ← 数据访问层（MyBatis-Plus）
├── entity/          ← 数据库实体类
├── dto/             ← 请求/响应数据传输对象
├── config/          ← Sa-Token、安全配置
└── listener/        ← 事件监听器
```

### 5.3  数据库表设计

#### 5.3.1  `user` 表

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 主键，雪花算法生成 |
| `username` | VARCHAR(64) | 用户名，唯一索引 |
| `email` | VARCHAR(128) | 邮箱，唯一索引 |
| `password` | VARCHAR(256) | BCrypt 加密后的密码 |
| `avatar` | VARCHAR(512) | 头像 URL |
| `role` | VARCHAR(32) | 角色：ADMIN/USER/VIP |
| `status` | TINYINT | 状态：1=正常，0=封禁 |
| `ai_call_count` | INT | 今日 AI 调用次数（限流用） |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 更新时间 |

#### 5.3.2  `api_key` 表

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 主键 |
| `user_id` | BIGINT FK | 所属用户 ID |
| `key_value` | VARCHAR(64) | API Key 值（UUID格式） |
| `name` | VARCHAR(64) | Key 备注名 |
| `status` | TINYINT | 1=有效，0=已吊销 |
| `expire_at` | DATETIME | 过期时间，NULL=永不过期 |
| `last_used_at` | DATETIME | 最后使用时间 |

### 5.4  核心类详解

**`AuthController`** — `com.smartagent.user.controller`

职责: 鉴权相关接口，处理注册、登录、刷新 Token、退出登录。这是无需鉴权的白名单接口。

| 方法 | 请求体 | 返回值 |
| --- | --- | --- |
| `POST /user/auth/register` | `UserRegisterDTO` | `Result<Void>` |
| `POST /user/auth/login` | `UserLoginDTO` | `Result<LoginVO>` |
| `POST /user/auth/refresh` | `refreshToken` | `Result<LoginVO>` |
| `POST /user/auth/logout` | (Token in header) | `Result<Void>` |

**`UserController`** — `com.smartagent.user.controller`

职责: 用户信息管理接口，需要携带有效 JWT Token 才能访问。

| 方法 | 返回值 |
| --- | --- |
| `GET /user/profile` | `Result<UserVO>` — 获取当前用户信息 |
| `PUT /user/profile` | `Result<Void>` — 更新用户信息 |
| `POST /user/api-key` | `Result<ApiKeyVO>` — 创建 API Key |
| `GET /user/api-key/list` | `Result<List<ApiKeyVO>>` |
| `DELETE /user/api-key/{id}` | `Result<Void>` |

**`AuthServiceImpl`** — `com.smartagent.user.service.impl`

职责: 鉴权服务的核心实现类，包含完整的注册登录业务逻辑，是整个用户模块最重要的类。

| 方法 | 流程 |
| --- | --- |
| `LoginVO register(UserRegisterDTO dto)` | 1. 校验用户名/邮箱是否已存在（查库 + 布隆过滤器）；2. BCrypt 加密密码；3. 生成雪花 ID，插入数据库；4. 自动登录，生成双 Token 返回 |
| `LoginVO login(UserLoginDTO dto)` | 1. 按用户名/邮箱查用户实体；2. BCrypt 校验密码；3. 校验账号状态；4. 生成 accessToken（2小时过期）+ refreshToken（7天过期）；5. refreshToken 存 Redis，key=`refresh:userId`；6. 返回 LoginVO |
| `LoginVO refreshToken(String refreshToken)` | 1. 从 Redis 校验 refreshToken 是否有效；2. 生成新的 accessToken；3. 刷新 refreshToken 过期时间（滑动窗口） |

**`UserEntity`** — `com.smartagent.user.entity`

职责: MyBatis-Plus 实体类，对应数据库 `user` 表。使用 `@TableName`、`@TableId` 等注解。

| 注解/字段 | 说明 |
| --- | --- |
| `@TableName("user")` | 映射表名 |
| `@TableId(type=IdType.ASSIGN_ID)` | 雪花 ID |
| `Long id` | 主键 |
| `String username` | 用户名 |
| `String password` | 存 BCrypt Hash，不存明文 |
| `String email` | 邮箱 |
| `String role` | 角色 |
| `Integer status` | 状态 |

**`LoginVO`** — `com.smartagent.user.dto`

职责: 登录成功后返回给前端的视图对象，包含用户基本信息和双 Token。

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | `Long` | 用户 ID |
| `username` | `String` | 用户名 |
| `avatar` | `String` | 头像 URL |
| `role` | `String` | 角色 |
| `accessToken` | `String` | 短期 Token，用于接口鉴权 |
| `refreshToken` | `String` | 长期 Token，用于刷新 accessToken |
| `accessExpire` | `Long` | accessToken 过期时间戳 |

---

## 第六章  smart-agent-chat — 对话与会话服务

### 6.1  模块职责

Chat 服务管理对话会话（Session）的完整生命周期：创建会话、维护会话列表、接收用户消息、触发 Agent 核心处理、以 SSE 流式推送 AI 回复、归档对话历史到 MySQL 和 Elasticsearch。

> SSE（Server-Sent Events）是 HTTP 协议的单向长连接推送机制，服务端可以持续向客户端推送数据，非常适合 LLM 逐 Token 流式输出场景，比 WebSocket 更轻量。

### 6.2  包结构

```
com.smartagent.chat/
├── controller/      ← 接口层
├── service/
│   └── impl/
├── mapper/
├── entity/          ← Session、Message 实体
├── dto/
├── event/           ← 领域事件
└── listener/        ← 消息归档监听器
```

### 6.3  数据库表设计

#### 6.3.1  `session` 表（会话表）

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 会话ID |
| `user_id` | BIGINT FK | 所属用户 |
| `title` | VARCHAR(128) | 会话标题（首条消息自动截取） |
| `agent_type` | VARCHAR(32) | Agent类型：CHAT/RAG/REACT |
| `knowledge_base_id` | BIGINT | 绑定的知识库ID（可为空） |
| `message_count` | INT | 消息总数 |
| `last_message_at` | DATETIME | 最后消息时间（用于排序） |
| `status` | TINYINT | 1=正常，0=已删除 |
| `created_at` | DATETIME | 创建时间 |

#### 6.3.2  `message` 表（消息表）

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 消息ID |
| `session_id` | BIGINT FK | 所属会话，加索引 |
| `role` | VARCHAR(16) | USER / ASSISTANT / TOOL |
| `content` | TEXT | 消息内容（Markdown格式） |
| `tokens` | INT | 消耗 Token 数 |
| `tool_calls` | JSON | 工具调用记录（JSON数组） |
| `finish_reason` | VARCHAR(32) | 结束原因：stop/tool_call/length |
| `created_at` | DATETIME | 创建时间 |

### 6.4  核心类详解

**`ChatController`** — `com.smartagent.chat.controller`

职责: 对话相关 REST 接口，包括会话管理和消息发送。消息发送接口返回 SSE 流（`text/event-stream`）。

| 方法 | 返回值 |
| --- | --- |
| `POST /chat/session` | `Result<SessionVO>` — 创建会话 |
| `GET /chat/session/list` | `Result<PageResult<SessionVO>>` |
| `DELETE /chat/session/{id}` | `Result<Void>` |
| `GET /chat/session/{id}/messages` | `Result<List<MessageVO>>` |
| `POST /chat/message/send` | **关键接口！返回 SseEmitter（SSE流）** |

**`ChatServiceImpl`** — `com.smartagent.chat.service.impl`

职责: 对话服务核心实现，负责协调会话管理、消息存储、SSE 流建立、调用 Agent Core 生成回复。

| 方法 | 流程 |
| --- | --- |
| `SseEmitter sendMessage(Long userId, ChatRequestDTO dto)` | 1. 校验会话存在且属于当前用户；2. 保存用户消息到 MySQL；3. 创建 SseEmitter（超时30分钟）；4. 异步调用 `AgentCoreService.streamChat()` → 逐 Token 回调 → `sseEmitter.send(token)`；5. 回调完成时：合并完整回复 → 异步发 MQ 消息归档；6. 返回 SseEmitter 给 Spring MVC |
| `SessionVO createSession(Long userId, CreateSessionDTO dto)` | 创建新会话，设置 agentType、绑定知识库 |
| `List<MessageVO> getSessionHistory(Long sessionId, Long userId)` | 查 MySQL，限制最近 50 条，用于前端渲染 |

**`MessageArchiveListener`** — `com.smartagent.chat.listener`

职责: 消费 RocketMQ 中 `message-archive-topic` 的消息，将完整的对话消息异步写入 Elasticsearch，支持后续全文检索历史对话。与主流程解耦，不影响 SSE 推送性能。

| 方法 | 流程 |
| --- | --- |
| `@RocketMQMessageListener(topic="message-archive-topic")` → `void onMessage(MessageArchiveEvent event)` | 1. 解析 event 中的 sessionId、完整消息内容；2. 构建 ES document（含用户ID、时间、内容全文）；3. `elasticsearchClient.index()` 写入 ES |

> 💡 SseEmitter 使用注意：必须在独立线程中执行 AI 调用和 `send()` 操作，因为 AI 调用是阻塞的。使用 `@Async` 或 `CompletableFuture` 异步执行，主线程立即返回 SseEmitter 对象给 Spring MVC。

---

## 第七章  smart-agent-core — Agent 编排核心（重中之重）

### 7.1  模块职责

这是整个项目技术含量最高的模块，也是简历上最能凸显技术深度的部分。它使用 LangChain4j 框架实现了三种 AI 能力：普通 Chat（多轮对话）、RAG（检索增强问答）、ReAct Agent（工具调用）。同时管理 Embedding、向量存储、对话记忆等 AI 基础设施。

### 7.2  包结构

```
com.smartagent.core/
├── agent/           ← Agent 定义（LangChain4j @AiService 接口）
├── chain/           ← RAG Pipeline 构建
├── memory/          ← 对话记忆管理
├── service/         ← 对外暴露的 AI 服务
│   └── impl/
├── tool/            ← 内置工具（由 tools 模块提供，这里是注册入口）
├── config/          ← LangChain4j、Milvus、Embedding 配置
└── feign/           ← Feign 客户端（调用 knowledge 服务）
```

### 7.3  LangChain4j 核心概念（必须理解）

| 概念 | 说明 | 对应类/注解 |
| --- | --- | --- |
| ChatLanguageModel | 与 LLM 通信的核心接口，封装 HTTP 调用 | `OpenAiChatModel` / `DeepSeekChatModel` |
| StreamingChatLanguageModel | 流式版本，逐 Token 回调 | `OpenAiStreamingChatModel` |
| EmbeddingModel | 将文本转为向量 | `OpenAiEmbeddingModel` / `BgeEmbeddingModel` |
| EmbeddingStore | 向量存储接口 | `MilvusEmbeddingStore` / `InMemoryEmbeddingStore` |
| ChatMemory | 对话历史管理 | `MessageWindowChatMemory`（窗口记忆） |
| ContentRetriever | RAG 检索器，查向量库返回相关片段 | `EmbeddingStoreContentRetriever` |
| RetrievalAugmentor | RAG 增强器，将检索结果注入 Prompt | `DefaultRetrievalAugmentor` |
| `@AiService` | 定义 AI 接口的注解，LangChain4j 自动实现 | `AgentAssistant.java` 接口 |
| `@Tool` | 将 Spring Bean 方法标记为 LLM 可调用工具 | 工具类方法上使用 |

### 7.4  核心类详解

**`AgentAssistant`** — `com.smartagent.core.agent`

职责: 核心 AI 接口，使用 LangChain4j 的 `@AiService` 注解定义，框架在运行时自动生成实现类，无需手写任何 HTTP 调用代码。这个接口是连接业务代码和 LLM 的桥梁。

```java
@AiService  // LangChain4j 自动注入实现
@SystemMessage("你是一个智能助手，请用中文回答。")  // 系统提示词

public interface AgentAssistant {

    String chat(@MemoryId String sessionId,   // 会话ID，用于隔离记忆
                @UserMessage String userMessage);  // 用户输入

    TokenStream streamChat(@MemoryId String sessionId,
                           @UserMessage String userMessage);  // 流式版本
}
```

**`LangChain4jConfig`** — `com.smartagent.core.config`

职责: 最关键的配置类，负责装配所有 LangChain4j 组件。决定了 AI 调用的模型、记忆方案、RAG 流水线等。

| Bean 方法 | 说明 |
| --- | --- |
| `@Bean ChatLanguageModel chatLanguageModel()` | 读取配置中的 API Key、模型名、baseUrl，构建 OpenAiChatModel 或 DeepSeekChatModel |
| `@Bean StreamingChatLanguageModel streamingChatLanguageModel()` | 流式模型，用于 SSE 推送 |
| `@Bean EmbeddingModel embeddingModel()` | 文本向量化模型（OpenAI text-embedding-3-small 或本地 BGE） |
| `@Bean EmbeddingStore<TextSegment> embeddingStore()` | 连接 Milvus，指定集合名、向量维度（1536 for OpenAI） |
| `@Bean ContentRetriever contentRetriever(...)` | 配置 RAG 检索：TopK=5，最小相似度=0.7 |
| `@Bean ChatMemoryStore chatMemoryStore()` | Redis-backed 记忆存储（自定义实现） |
| `@Bean AgentAssistant agentAssistant()` | 组装 AgentAssistant：注入模型+记忆+RAG+工具列表 |

**`RedisChatMemoryStore`** — `com.smartagent.core.memory`

职责: 自定义 LangChain4j `ChatMemoryStore` 接口的 Redis 实现。默认的 InMemory 实现重启后丢失，Redis 实现让对话记忆持久化，且支持多节点共享。

| 成员 | 说明 |
| --- | --- |
| `KEY_PREFIX = "chat:memory:"` | Redis Key 前缀 |
| `MAX_WINDOW_SIZE = 20` | 保留最近20轮对话 |
| `List<ChatMessage> getMessages(Object memoryId)` | Redis Key: `chat:memory:{sessionId}`，类型为 List，返回最近 MAX_WINDOW_SIZE 条消息 |
| `void updateMessages(Object memoryId, List<ChatMessage> msgs)` | 序列化消息列表（JSON）→ 存入 Redis，设置 TTL 24小时 |
| `void deleteMessages(Object memoryId)` | 清除指定会话记忆 |

**`RagPipelineBuilder`** — `com.smartagent.core.chain`

职责: RAG（检索增强生成）流水线构建器。当用户绑定了知识库的会话发起对话时，使用此类构建带 RAG 增强的 Agent，实现"基于私有文档回答"的功能。

| 方法 | 说明 |
| --- | --- |
| `AgentAssistant buildRagAgent(String knowledgeBaseId)` | 1. 创建带 metadata filter 的 ContentRetriever（只检索该知识库的内容，不跨库混淆）；2. 构建 RetrievalAugmentor，设置 QueryTransformer（查询改写）；3. 创建独立的 AgentAssistant 实例并注入 RAG |
| `String buildRagPrompt(String userQuery, List<TextSegment> segments)` | 将检索到的知识片段格式化后拼接到 Prompt 前缀 |

**`AgentCoreServiceImpl`** — `com.smartagent.core.service.impl`

职责: 对外暴露的 AI 核心服务实现，被 chat 服务通过 OpenFeign 调用，是两个微服务之间的接口层。

| 方法 | 说明 |
| --- | --- |
| `TokenStream streamChat(String sessionId, String userMessage, AgentType type)` | 根据 agentType 选择对应的 AgentAssistant：CHAT → 普通多轮对话，RAG → 带知识库检索的对话，REACT → 带工具调用的 ReAct Agent；调用对应 `agent.streamChat()` 返回 TokenStream |
| `int countTokens(String text)` | 估算文本 Token 数（使用 tiktoken-java 库） |

> 💡 ReAct Agent 的 Tool 调用是完全自动的：LangChain4j 将所有 `@Tool` 方法的 name 和 description 注入 System Prompt，LLM 决定何时调用哪个 Tool，框架自动解析 LLM 的 function_call 响应并执行对应 Java 方法，结果作为 Observation 再次发给 LLM，直到 LLM 返回 STOP 信号。

### 7.5  Agent 类型对比

| 类型 | 触发条件 | 核心区别 | 典型用例 |
| --- | --- | --- | --- |
| CHAT | 普通对话会话 | 只有记忆，无工具无RAG | 日常问答、写作助手 |
| RAG | 绑定了知识库的会话 | 记忆 + 向量检索增强 | 私有文档问答、企业知识库 |
| REACT | 用户开启工具模式 | 记忆 + 自主调用工具（多轮） | 查天气/计算/数据库查询等复杂任务 |

---

## 第八章  smart-agent-knowledge — 知识库服务

### 8.1  模块职责

知识库服务负责私有知识的完整处理流程：接收用户上传的文档（PDF/Word/TXT/网页）→ 解析提取纯文本 → 按语义切片（Chunking） → 调用 Embedding 模型向量化 → 存入 Milvus → 供 RAG 检索使用。这是整个 RAG 系统的"离线"阶段，与用户实时对话的"在线"检索阶段相互独立。

### 8.2  包结构

```
com.smartagent.knowledge/
├── controller/
├── service/
│   └── impl/
├── mapper/
├── entity/
├── dto/
├── parser/          ← 各类型文档解析器
├── splitter/        ← 文本切片策略
├── pipeline/        ← 知识库构建流水线（核心）
└── listener/        ← MQ 消费者（异步构建）
```

### 8.3  数据库表设计

#### 8.3.1  `knowledge_base` 表（知识库表）

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 知识库ID |
| `user_id` | BIGINT FK | 所属用户 |
| `name` | VARCHAR(128) | 知识库名称 |
| `description` | TEXT | 描述 |
| `doc_count` | INT | 文档数量 |
| `status` | TINYINT | 0=初始化，1=就绪，2=更新中 |
| `milvus_collection` | VARCHAR(128) | 对应 Milvus 集合名 |
| `created_at` | DATETIME | 创建时间 |

#### 8.3.2  `knowledge_doc` 表（文档表）

| 字段名 | 类型 | 说明 |
| --- | --- | --- |
| `id` | BIGINT PK | 文档ID |
| `knowledge_base_id` | BIGINT FK | 所属知识库 |
| `file_name` | VARCHAR(256) | 原始文件名 |
| `file_url` | VARCHAR(512) | MinIO 存储路径 |
| `file_type` | VARCHAR(16) | PDF/DOCX/TXT/URL |
| `file_size` | BIGINT | 文件大小（字节） |
| `chunk_count` | INT | 切片数量 |
| `status` | TINYINT | 0=待处理，1=处理中，2=完成，-1=失败 |
| `error_msg` | TEXT | 失败原因 |
| `created_at` | DATETIME | 上传时间 |

### 8.4  核心类详解

**`KnowledgeController`** — `com.smartagent.knowledge.controller`

职责: 知识库管理 REST 接口。

| 方法 | 说明 |
| --- | --- |
| `POST /knowledge/base` | 创建知识库 |
| `GET /knowledge/base/list` | 获取我的知识库列表 |
| `DELETE /knowledge/base/{id}` | 删除知识库（同步删 Milvus） |
| `POST /knowledge/base/{id}/upload` | 上传文档（multipart/form-data） |
| `GET /knowledge/base/{id}/docs` | 文档列表 |
| `DELETE /knowledge/doc/{docId}` | 删除单个文档 |
| `POST /knowledge/base/{id}/rebuild` | 重新构建向量索引 |

**`DocumentParser`** — `com.smartagent.knowledge.parser`

职责: 文档解析策略接口，不同文件类型由不同实现类处理，使用策略模式，通过工厂根据文件扩展名选择对应解析器。

| 接口/实现类 | 说明 |
| --- | --- |
| `String parse(InputStream inputStream)` | 接口方法，返回纯文本 |
| `PdfDocumentParser` | 使用 Apache PDFBox 解析 PDF |
| `WordDocumentParser` | 使用 Apache POI 解析 .docx/.doc |
| `TxtDocumentParser` | 直接读取，处理编码问题 |
| `TikaDocumentParser` | 兜底：其他类型由 Apache Tika 自动识别解析 |
| `UrlDocumentParser` | 抓取网页 HTML，提取正文（用 Jsoup） |

**`TextSplitter`** — `com.smartagent.knowledge.splitter`

职责: 文本切片器，将长文本按策略切成适合向量化的片段（Chunk）。Chunk 太长会超出模型限制，太短则语义不完整，通常 512~1024 Token 为宜。

| 成员 | 说明 |
| --- | --- |
| `CHUNK_SIZE = 512` | 每片 Token 数 |
| `CHUNK_OVERLAP = 64` | 相邻片段重叠 Token 数（防止边界截断语义） |
| `List<TextSegment> split(String text, Map<String,Object> metadata)` | 1. 按段落/句子粗切；2. 合并小片段到 CHUNK_SIZE，超出则新开一片；3. 处理 overlap：每片末尾 64 token 复制到下一片开头；4. 每片携带 metadata（知识库ID、文档ID、页码等） |

**`KnowledgeBuildPipeline`** — `com.smartagent.knowledge.pipeline`

职责: 知识库构建核心流水线，串联"解析 → 切片 → 向量化 → 入库"全流程。被 MQ 消费者异步触发。

| 方法 | 流程 |
| --- | --- |
| `void process(Long docId)` | 1. 查 DB 获取文档信息，更新 status=1(处理中)；2. 从 MinIO 下载文件；3. `DocumentParser.parse()` 提取纯文本；4. `TextSplitter.split()` 切片，附带 metadata；5. `EmbeddingModel.embedAll()` 批量向量化（注意限速）；6. `MilvusEmbeddingStore.addAll()` 批量入库；7. 更新 DB：status=2，chunk_count=N；8. 异常时：更新 status=-1，记录 error_msg |

**`KnowledgeBuildListener`** — `com.smartagent.knowledge.listener`

职责: 消费 RocketMQ `knowledge-build-topic` 的消息，异步触发知识库构建流水线。这样文档上传接口可以立即返回，后台慢慢处理。

```java
@RocketMQMessageListener(topic="knowledge-build-topic", group="knowledge-build-group")
void onMessage(KnowledgeBuildEvent event) {
    // event 包含 docId
    // 调用 KnowledgeBuildPipeline.process(docId)
}
```

> 💡 Milvus 中每条向量记录除了向量值（`float[]`）外，还需要存储 metadata 字段（knowledgeBaseId、docId、chunkText），以便 RAG 检索时按知识库过滤，并把原始文本一起返回给 LLM。

---

## 第九章  smart-agent-tools — 工具扩展包

### 9.1  模块职责

Tools 模块定义了 ReAct Agent 可以调用的所有工具。每个工具是一个普通的 Spring Bean，方法上使用 LangChain4j 的 `@Tool` 注解，注解的 name 和 description 会被框架自动提取，格式化到 LLM 的 System Prompt 中，让 LLM 知道有哪些工具可用、何时该用。

新增工具只需新建一个类，实现方法并加 `@Tool` 注解，再在 `LangChain4jConfig` 中注册进 `AgentAssistant` 即可，完全遵循开闭原则。

### 9.2  包结构

```
com.smartagent.tools/
├── weather/         ← 天气查询工具
├── database/        ← 自然语言转 SQL 工具
├── search/          ← 网络搜索工具
├── calculator/      ← 数学计算工具
└── code/            ← 代码执行工具（沙箱环境）
```

### 9.3  核心类详解

**`WeatherTool`** — `com.smartagent.tools.weather`

职责: 天气查询工具，调用第三方天气 API（如和风天气）获取实时天气。是最简单的工具示例，适合入门理解 `@Tool` 机制。

```java
@Tool("查询指定城市的实时天气情况，包括温度、湿度、天气状况")
String getWeather(String cityName) {
    // 1. 调用天气 API
    // 2. 格式化返回字符串（LLM 会读这个文本）
    // 示例返回："北京天气：晴，气温18°C，湿度45%，东风3级"
}
```

**`DatabaseQueryTool`** — `com.smartagent.tools.database`

职责: NL2SQL 工具，将用户的自然语言问题转换为 SQL 并执行，返回查询结果。这是企业内部数据助手场景的核心能力，也是简历亮点。

```java
@Tool("查询数据库中的业务数据，支持自然语言描述的查询条件")
String queryDatabase(String naturalLanguageQuery) {
    // 1. 调用 schemaProvider 获取表结构（缓存避免频繁调用）
    // 2. 将表结构 + 用户问题发给 LLM，要求只返回 SELECT SQL
    // 3. 安全校验：只允许 SELECT，拒绝 UPDATE/DELETE
    // 4. 执行 SQL，将结果集格式化为 Markdown 表格返回
}
```

> ⚠️ DatabaseQueryTool 必须做严格的 SQL 注入防护和权限校验，只允许 SELECT 语句，并限制查询的表范围。生产环境建议使用只读数据库账号连接。

**`WebSearchTool`** — `com.smartagent.tools.search`

职责: 网络搜索工具，调用搜索引擎 API（如 Tavily、Serper）获取实时互联网信息，弥补 LLM 训练数据过时的问题。

```java
@Tool("搜索互联网上的实时信息，适用于查询新闻、最新数据等")
String searchWeb(String query) {
    // 1. 调用 Tavily Search API
    // 2. 提取 Top 3 结果的标题+摘要+链接
    // 3. 格式化为结构化文本返回给 LLM
}
```

**`CalculatorTool`** — `com.smartagent.tools.calculator`

职责: 数学计算工具，使用 exp4j 或 MathUtils 解析并计算数学表达式，避免 LLM 直接计算大数时的精度问题。

```java
@Tool("计算数学表达式，支持加减乘除、幂运算、三角函数等")
String calculate(String expression) {
    // 表达式示例："(3.14 * 5^2)"
    // 使用 exp4j 库安全解析执行
    // 返回："结果：78.5"
}
```

---

## 第十章  RESTful 接口文档

### 10.1  接口规范说明

| 规范项 | 说明 |
| --- | --- |
| Base URL | `http://localhost:8080/api`（网关统一入口） |
| 请求格式 | `Content-Type: application/json`（文件上传用 `multipart/form-data`） |
| 鉴权方式 | Request Header: `Authorization: Bearer {accessToken}` |
| 响应格式 | 统一 `Result<T>`：`{"code":200,"message":"ok","data":{...},"timestamp":1234567890}` |
| 分页参数 | `?page=1&size=20`（所有分页接口统一约定，页码从1开始） |
| 时间格式 | ISO 8601: `yyyy-MM-dd HH:mm:ss` |
| ID类型 | 所有 ID 均为 Long 类型，JSON 中使用字符串避免精度丢失 |

### 10.2  用户认证接口

#### `POST /api/user/auth/register` — 用户注册

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | String | 是 | 用户名，4-20位字母数字下划线 |
| `email` | String | 是 | 邮箱地址 |
| `password` | String | 是 | 密码，8-32位，需含字母和数字 |
| `code` | String | 是 | 邮箱验证码（6位数字） |

响应示例：

```json
{ "code": 200, "message": "注册成功", "data": null }
```

#### `POST /api/user/auth/login` — 用户登录

请求体：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `account` | String | 是 | 用户名或邮箱 |
| `password` | String | 是 | 密码 |

响应示例：

```json
{
  "code": 200,
  "data": {
    "userId": "1234567890",
    "username": "zhangsan",
    "role": "USER",
    "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
    "accessExpire": 1735689600000
  }
}
```

### 10.3  对话接口

#### `POST /api/chat/session` — 创建新会话

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | String | 否 | 会话标题，默认"新对话" |
| `agentType` | String | 是 | CHAT / RAG / REACT |
| `knowledgeBaseId` | Long | 否 | RAG类型时指定知识库ID |

#### `POST /api/chat/message/send` — 发送消息（SSE 流式接口）

> 此接口是整个项目的核心接口，返回 `Content-Type: text/event-stream`，客户端通过 EventSource 或 fetch API 接收流式推送。

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `sessionId` | Long | 是 | 会话ID |
| `content` | String | 是 | 用户消息内容 |

SSE 响应流格式：

```
data: {"type":"token","content":"你好"}
data: {"type":"token","content":"，我"}
data: {"type":"token","content":"是"}
data: {"type":"tool_call","toolName":"getWeather","input":"北京"}
data: {"type":"tool_result","toolName":"getWeather","output":"北京：晴，18°C"}
data: {"type":"done","messageId":"9876","tokens":256}
```

> 💡 前端接收 SSE 时，`type=token` 直接追加到显示区域；`type=tool_call` 显示"正在调用工具..."提示；`type=done` 表示本次对话结束，此时保存 messageId。

#### `GET /api/chat/session/{id}/messages` — 获取历史消息

| 参数 | 位置 | 类型 | 说明 |
| --- | --- | --- | --- |
| `id` | Path | Long | 会话ID |
| `page` | Query | Int | 页码，默认1 |
| `size` | Query | Int | 每页条数，默认50 |

### 10.4  知识库接口

#### `POST /api/knowledge/base` — 创建知识库

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `name` | String | 是 | 知识库名称，最长128字符 |
| `description` | String | 否 | 知识库描述 |

#### `POST /api/knowledge/base/{id}/upload` — 上传文档

`Content-Type: multipart/form-data`

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `file` | File | 是 | 文档文件，支持 PDF/DOCX/TXT，最大50MB |

响应（立即返回，后台异步处理）：

```json
{ "code": 200, "data": { "docId": "111", "status": "PROCESSING" } }
```

### 10.5  错误码参考

| 错误码 | 说明 | 常见原因 |
| --- | --- | --- |
| 200 | 成功 | — |
| 400 | 请求参数错误 | 缺少必填字段、格式错误 |
| 401 | 未认证 | Token不存在、过期或格式错误 |
| 403 | 无权限 | 操作他人资源、权限不足 |
| 404 | 资源不存在 | 指定ID的资源不存在或已删除 |
| 429 | 请求过于频繁 | 触发限流，稍后重试 |
| 500 | 服务器内部错误 | 系统bug，查看日志排查 |
| 5001 | AI服务调用失败 | LLM API限额、网络异常 |
| 5002 | 向量库操作失败 | Milvus服务异常 |
| 5003 | 文档解析失败 | 文件格式不支持或文件损坏 |

---

## 第十一章  数据库完整设计

### 11.1  表关系概览

整个系统共 7 张核心表，关系如下：

- `user` 表 → `api_key` 表（一对多：一个用户可有多个API Key）
- `user` 表 → `session` 表（一对多：一个用户有多个会话）
- `session` 表 → `message` 表（一对多：一个会话有多条消息）
- `user` 表 → `knowledge_base` 表（一对多：一个用户可创建多个知识库）
- `knowledge_base` 表 → `knowledge_doc` 表（一对多：一个知识库有多个文档）
- `session` 表 → `knowledge_base` 表（多对一：RAG 会话绑定知识库）

### 11.2  索引设计原则

合理的索引设计是大厂面试高频考点。以下是本项目的索引设计说明：

| 表名 | 索引字段 | 索引类型 | 设计原因 |
| --- | --- | --- | --- |
| user | `username` | 唯一索引 | 登录时按用户名查找，要求唯一 |
| user | `email` | 唯一索引 | 邮件登录，要求唯一 |
| session | `user_id, last_message_at` | 联合索引 | 查用户的会话列表，按时间倒序 |
| session | `user_id, status` | 联合索引 | 筛选有效会话（status=1） |
| message | `session_id, created_at` | 联合索引 | 查会话消息列表，按时间排序 |
| knowledge_doc | `knowledge_base_id, status` | 联合索引 | 查知识库下特定状态的文档 |
| api_key | `key_value` | 唯一索引 | API Key 鉴权时快速查找 |

### 11.3  MyBatis-Plus 配置

所有实体类继承公共基类 `BaseEntity`，包含 `id`、`created_at`、`updated_at` 三个公共字段：

```java
@MappedSuperclass
public abstract class BaseEntity {
    @TableId(type = IdType.ASSIGN_ID)  // 雪花ID
    private Long id;

    @TableField(fill = FieldFill.INSERT)  // 插入时自动填充
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入/更新时自动填充
    private LocalDateTime updatedAt;
}
```

`MetaObjectHandler` 实现自动填充逻辑，所有 service 不需要手动设置这两个字段。

---

## 第十二章  开发环境搭建（零基础完整指南）

### 12.1  必备软件安装

| 软件 | 版本要求 | 说明 / 下载地址 |
| --- | --- | --- |
| JDK | 21（LTS） | adoptium.net，推荐 Eclipse Temurin 21 |
| Maven | 3.9+ | 与 IntelliJ IDEA 自带版本一致即可 |
| IntelliJ IDEA | 2024+（Ultimate 版） | 需要 Ultimate 才有 Spring 完整支持 |
| Docker Desktop | 最新版 | 运行中间件，Windows 需开启 WSL2 |
| Git | 2.4+ | 版本管理 |

### 12.2  中间件 Docker Compose 一键启动

在项目根目录 `smart-agent-deploy/docker` 下有 `docker-compose.yml`，包含所有依赖的中间件：

```bash
# 启动所有中间件（MySQL、Redis、RocketMQ、Milvus、ES、Nacos、MinIO）
cd smart-agent-deploy/docker
docker-compose up -d

# 验证全部启动成功
docker-compose ps
```

各中间件访问地址：

| 中间件 | 访问地址 | 默认账密 |
| --- | --- | --- |
| MySQL | `localhost:3306` | root / smart123 |
| Redis | `localhost:6379` | 密码：smart123 |
| Nacos 控制台 | `http://localhost:8848/nacos` | nacos / nacos |
| RocketMQ 控制台 | `http://localhost:9876` | 无需登录 |
| Milvus Attu（可视化） | `http://localhost:8000` | 无需登录 |
| Elasticsearch | `http://localhost:9200` | elastic / smart123 |
| MinIO 控制台 | `http://localhost:9001` | minioadmin / minioadmin |

### 12.3  初始化数据库

```bash
# 连接 MySQL，执行初始化脚本
mysql -h 127.0.0.1 -u root -psmart123 < sql/init.sql
```

### 12.4  配置 API Key

在 `smart-agent-core` 模块的 `application.yml` 中配置 LLM API Key：

```yaml
langchain4j:
  openai:
    api-key: ${OPENAI_API_KEY}   # 推荐用环境变量，不要硬编码
    base-url: https://api.deepseek.com/v1  # 可替换为 DeepSeek
    model-name: deepseek-chat
```

> 💡 国内推荐使用 DeepSeek API，费用极低（约 OpenAI 的 1/10），且支持中文效果很好。

### 12.5  启动顺序

1. 先确认所有 Docker 容器已启动（`docker-compose ps` 全部 Up）
2. 在 Nacos 中导入配置文件（见 `nacos-config` 目录）
3. 启动 `smart-agent-user`（8081 端口）
4. 启动 `smart-agent-knowledge`（8082 端口）
5. 启动 `smart-agent-core`（8083 端口）
6. 启动 `smart-agent-chat`（8084 端口）
7. 最后启动 `smart-agent-gateway`（8080 端口，统一入口）
8. 访问 Knife4j 文档：`http://localhost:8080/doc.html`

---

## 第十三章  开发规范与注意事项

### 13.1  代码规范

- 包名全小写，类名大驼峰，方法名/变量名小驼峰，常量全大写下划线分隔
- Controller 层只做参数校验和转发，不写业务逻辑
- Service 层写业务逻辑，不直接操作 HTTP 请求/响应对象
- 所有接口必须用 `@Validated` 开启参数校验，字段上用 `@NotBlank` `@NotNull` `@Size` 等注解
- 禁止在 for 循环内调用数据库查询（N+1 问题），用 batch 或 JOIN 代替
- 所有对外接口必须加 `@Log` 注解记录操作日志

### 13.2  异常处理规范

- 业务逻辑中通过 `throw new BusinessException(ErrorCode.XXX)` 抛出异常
- `GlobalExceptionHandler` 统一捕获，Controller 层不需要 try-catch
- 不要 catch 住异常后什么都不做（"吃掉"异常），至少要打日志
- 日志级别：正常操作 INFO，业务异常 WARN，系统错误 ERROR

### 13.3  安全规范

- 密码必须 BCrypt 加密，禁止 MD5（彩虹表可破解）
- SQL 必须使用 MyBatis-Plus 的 Wrapper 或 `#{param}` 占位符，禁止字符串拼接
- 用户只能操作自己的资源，Service 层必须校验 userId 是否匹配
- 敏感配置（API Key、数据库密码）使用环境变量或 Nacos 加密配置，不得 commit 到 Git
- 文件上传必须校验类型和大小，防止上传 .exe .jsp 等危险文件

### 13.4  性能优化要点（面试常问）

| 场景 | 优化手段 | 说明 |
| --- | --- | --- |
| 热点数据查询 | Redis 缓存 + 本地 Caffeine 二级缓存 | 用户信息、知识库列表等 |
| 大批量 Embedding | 线程池 + 批量调用 + 指数退避重试 | 防止 API 限速报错 |
| 会话历史读取 | Redis 滑动窗口（只缓存最近N条） | 避免每次读 MySQL 全量数据 |
| 消息归档 | RocketMQ 异步解耦 | 不影响 SSE 主链路响应速度 |
| Milvus 检索 | 向量索引（IVF_FLAT / HNSW）+ metadata filter | 提升检索精度和速度 |
| 数据库 | 分页查询 + 覆盖索引 + 慢查询监控 | 避免全表扫描 |

### 13.5  可观测性配置

接入 SkyWalking Agent 只需启动时加 JVM 参数，无需改动代码：

```bash
-javaagent:/path/to/skywalking-agent.jar
-Dskywalking.agent.service_name=smart-agent-user
-Dskywalking.collector.backend_service=localhost:11800
```

Prometheus 指标暴露（Spring Boot Actuator 自动支持）：

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,prometheus,info,metrics
```

---

## 第十四章  面试高频考点与参考答案

### 14.1  项目介绍模板（1分钟口述）

> 💡 面试官问"介绍一下你的项目"时，参考以下结构：项目是什么 → 技术亮点 → 你负责了什么 → 解决了什么问题。

**参考：**「我做的是一个企业级 AI Agent 平台，核心功能是让用户能上传私有文档，然后基于文档内容进行问答，同时 Agent 还可以自主调用工具完成复杂任务。技术上后端用 Spring Boot 3 + Spring Cloud 微服务架构，AI 能力用 LangChain4j 框架实现了 RAG 检索增强和 ReAct Agent，向量存储用 Milvus，流式推送用 SSE，消息解耦用 RocketMQ，整体架构参考了大厂的生产级规范。」

### 14.2  高频技术问题

**Q1：什么是 RAG？你是怎么实现的？**

> 参考答案：RAG（Retrieval-Augmented Generation，检索增强生成）解决的是 LLM 训练数据截止导致无法回答私有/实时信息的问题。实现分两阶段：离线阶段把文档切片、向量化存入 Milvus；在线阶段用户提问时先把问题向量化，在 Milvus 里做 ANN 近似最近邻搜索，召回最相关的 K 个片段，将这些片段作为上下文拼入 Prompt，让 LLM 基于这些信息回答。我在项目里实现了按知识库 ID 过滤、Top-5 召回、最低相似度阈值 0.7 等策略。

**Q2：ReAct Agent 的工作原理？**

> 参考答案：ReAct（Reasoning + Acting）是让 LLM 交替进行推理和行动的 Agent 框架。流程是：LLM 拿到用户问题先输出 Thought（我需要先查天气），然后输出 Action（调用 getWeather 工具），框架执行工具得到 Observation（北京：晴18度），再把 Observation 发回 LLM，LLM 输出下一步 Thought...直到 LLM 判断信息足够，输出最终 Answer。LangChain4j 框架自动处理这个循环，我只需要定义 @Tool 方法，框架会把方法的 name 和 description 注入 System Prompt，LLM 自动决定何时调用。

**Q3：SSE 和 WebSocket 的区别，为什么选 SSE？**

> 参考答案：WebSocket 是全双工的，客户端和服务端都能主动发消息；SSE 是单向的，只能服务端向客户端推送。对话场景中，AI 回复是单向推送（只需要服务端 → 客户端），SSE 足够了，而且比 WebSocket 更轻量，基于普通 HTTP，不需要协议升级，对网关和负载均衡更友好。实现上用 Spring MVC 的 SseEmitter，将 LangChain4j 的 TokenStream 回调直接转发给 `SseEmitter.send()`。

**Q4：如何保证 Embedding 大批量处理时不失败？**

> 参考答案：调用 Embedding API 有 QPS 限制，批量处理时容易触发限速（429 错误）。我的方案是：线程池控制并发数（最多 5 个并发请求）+ 批量化（每次最多 20 个 chunk 一起发）+ 指数退避重试（Resilience4j 的 Retry，失败后 1s、2s、4s 间隔重试最多 3 次）。同时文档状态机（0待处理→1处理中→2完成/-1失败）保证失败后可以重新触发处理。

**Q5：分布式场景下如何保证会话记忆的一致性？**

> 参考答案：LangChain4j 默认用 InMemoryMessageWindow，多节点部署时各节点记忆不共享。我实现了自定义的 RedisChatMemoryStore，将会话记忆存在 Redis List 中，key 为 `chat:memory:{sessionId}`，所有节点共享同一个 Redis。读取时 LRANGE 取最近 20 条，写入时 RPUSH 追加并 LTRIM 保持窗口大小，设置 24 小时 TTL 自动过期。

---

*— 文档结束 —*
