# UserContextUtils 解析

## 目录

- [一、UserContextUtils 如何从请求中获取 UserId](#一usercontextutils-如何从请求中获取-userid)
- [二、（待补充）](#二待补充)
- [三、（待补充）](#三待补充)

---

## 一、UserContextUtils 如何从请求中获取 UserId

### 1.1 核心机制：RequestContextHolder

`UserContextUtils` 位于 `common` 模块，路径为 `common/src/main/java/com/smartagent/common/utils/UserContextUtils.java`。它是一个静态工具类，核心依赖 Spring 提供的 **`RequestContextHolder`** 来获取当前请求上下文。

```java
private static HttpServletRequest currentRequest() {
    ServletRequestAttributes attr =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    return attr != null ? attr.getRequest() : null;
}
```

**`RequestContextHolder` 的工作原理：**

Spring MVC 在处理每个 HTTP 请求时，会通过 `DispatcherServlet` 或 `FrameworkServlet` 将当前的 `ServletRequestAttributes`（封装了 `HttpServletRequest`）绑定到当前线程的 `ThreadLocal` 中。`RequestContextHolder` 内部维护了两个 `ThreadLocal<RequestAttributes>`：

| ThreadLocal | 用途 |
|---|---|
| `requestAttributesHolder` | 当前线程绑定的请求属性 |
| `inheritableRequestAttributesHolder` | 可被子线程继承的请求属性（用于 `@Async` 等场景） |

当调用 `RequestContextHolder.getRequestAttributes()` 时，它从当前线程的 `ThreadLocal` 中取出 `RequestAttributes`。这意味着：

- **必须在请求线程内调用**：如果在非请求线程（如 RocketMQ 消费线程、虚拟线程池中的工作线程）中调用，`ThreadLocal` 中不存在请求属性，方法返回 `null`。
- **与 Servlet 容器耦合**：仅在 Servlet 容器（Tomcat/Undertow）处理的 HTTP 请求上下文中有效。

### 1.2 读取 X-User-Id 请求头

获取到 `HttpServletRequest` 后，直接读取 `X-User-Id` 请求头：

```java
public static Long getUserId() {
    HttpServletRequest request = currentRequest();
    if (request == null) return null;
    String header = request.getHeader("X-User-Id");
    return header != null && !header.isBlank() ? Long.valueOf(header) : null;
}
```

逻辑非常简单：
1. 尝试获取当前请求对象，失败则返回 `null`。
2. 读取 `X-User-Id` 请求头的值。
3. 若值非空，解析为 `Long` 返回；否则返回 `null`。

同理，`getUsername()` 读取的是 `X-Username` 请求头（当前代码中 Gateway 并未注入该头，仅 `LogAspect` 尝试读取，实际值为 `null`）。

### 1.3 X-User-Id 的注入来源：AuthGlobalFilter

`UserContextUtils` 本身并**不负责认证**——它只是被动地从请求头中读取。`X-User-Id` 是由 **Gateway 模块** 的 `AuthGlobalFilter` 注入的。

完整数据流如下：

```
客户端请求
  │
  │  Authorization: Bearer <JWT>
  ▼
┌─────────────────────────────────────────────────────┐
│  Gateway (:8080)                                     │
│  AuthGlobalFilter (GlobalFilter, order=10)           │
│                                                      │
│  1. 检查路径是否在白名单中（login/register/refresh/   │
│     swagger），若是则直接放行，不注入 X-User-Id       │
│                                                      │
│  2. 从 Authorization 头提取 Bearer token             │
│                                                      │
│  3. 调用 JwtUtils.getUserId(token) 解析 JWT，        │
│     获取 userId                                      │
│                                                      │
│  4. 若 token 无效/过期，返回 401 UNAUTHORIZED         │
│                                                      │
│  5. 若有效，通过 request.mutate() 注入 X-User-Id 头:  │
│     request.mutate()                                 │
│       .header("X-User-Id", userId.toString())        │
│       .build()                                       │
│                                                      │
│  6. 将修改后的请求传递给下游微服务                     │
└──────────────────┬──────────────────────────────────┘
                   │
                   │  请求头携带 X-User-Id
                   ▼
┌─────────────────────────────────────────────────────┐
│  下游微服务 (user:8081 / chat:8082 / knowledge:8083) │
│                                                      │
│  Service 层调用 UserContextUtils.getUserId()         │
│    → RequestContextHolder.getRequestAttributes()     │
│    → ServletRequestAttributes.getRequest()           │
│    → request.getHeader("X-User-Id")                 │
│    → 返回 Long 类型的 userId                         │
└─────────────────────────────────────────────────────┘
```

### 1.4 关键设计决策

**为什么用请求头传递 userId 而不是再次解析 JWT？**

| 方案 | 优点 | 缺点 |
|---|---|---|
| Gateway 注入请求头 | 下游服务无需依赖 JWT 解析逻辑、无需持有密钥；解析只发生一次 | 下游服务必须信任 Gateway（内网可接受） |
| 每个服务独立解析 JWT | 服务自治，无单点信任 | 每个服务都要持有密钥、重复解析；耦合 JWT 实现 |

当前架构选择了前者，这符合微服务中"认证在网关统一处理，授权在各服务独立处理"的常见模式。

**为什么 UserContextUtils 不直接用 ThreadLocal 存储 userId 而是每次读请求头？**

当前实现是"无状态"的——每次调用 `getUserId()` 都从 `HttpServletRequest` 重新读取。对比 `user` 模块中的 `UserContext`（使用 `ThreadLocal<Long>` 主动 set/get）：

- `UserContextUtils`（请求头方案）：无需预先 set，网关注入后即可读；但在同一请求中多次调用会重复执行 `request.getHeader()`。
- `UserContext`（ThreadLocal 方案）：需要在过滤器/拦截器中预先 `set(userId)`，但读取成本更低。

实际使用中，`UserContextUtils` 在每个业务方法中通常仅调用一次，重复读取的开销可忽略。

### 1.5 使用示例

在 `ChatServiceImpl` 中：

```java
// 创建会话时关联当前用户
public SessionVO createSession(CreateSessionDTO dto) {
    Long userId = UserContextUtils.getUserId();  // 从 X-User-Id 头获取
    LlmSessionEntity session = new LlmSessionEntity();
    session.setUserId(userId);
    // ...
}

// 发送消息前校验会话归属
public void sendMessage(SendMessageDTO dto) {
    Long currentUserId = UserContextUtils.getUserId();
    LlmSessionEntity session = sessionMapper.selectById(dto.getSessionId());
    if (!session.getUserId().equals(currentUserId)) {
        throw new BusinessException(ResultCode.FORBIDDEN);
    }
    // ...
}
```

### 1.6 注意事项

1. **非请求线程中不可用**：如果在 RocketMQ 消费者、`@Async` 异步方法（未配置 `ThreadPoolTaskExecutor` 的 `TaskDecorator` 传播 RequestAttributes 时）或手动创建的线程中调用 `getUserId()`，将返回 `null`。

2. **白名单路径不受保护**：白名单中的路径（`/api/user/login`、`/api/user/register` 等），Gateway 不会注入 `X-User-Id`，因此这些路径上 `UserContextUtils.getUserId()` 返回 `null`。

3. **Gateway 是唯一信任边界**：下游服务直接信任 `X-User-Id` 请求头的值，不做二次校验。如果外部请求能绕过 Gateway 直接访问下游服务，则可以伪造该请求头。生产环境中应通过网络策略确保下游服务仅接受来自 Gateway 的请求。

---

## 二、（待补充）

---

## 三、（待补充）
