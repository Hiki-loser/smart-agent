# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Build all modules
mvn clean compile -U

# Run a single module (each is an independent Spring Boot app)
mvn spring-boot:run -pl gateway
mvn spring-boot:run -pl user
mvn spring-boot:run -pl chat
mvn spring-boot:run -pl core
mvn spring-boot:run -pl knowledge

# Run tests (JUnit 5)
mvn test -pl chat
mvn test -pl chat -Dtest=ChatControllerTest
```

Start required infrastructure first: Nacos (8848), MySQL, Redis, RocketMQ (9876), Milvus (19530), Elasticsearch.

Each module loads its own `application.yml` which imports an `application-local.yml` for local overrides. AI model credentials are injected via env vars (`AI_MODEL_API_KEY`, `AI_MODEL_BASE_URL`, etc.).

## Architecture

A Spring Boot 3.2.5 + Spring Cloud 2023.0.1 multi-module Maven project (Java 21). All request paths are routed through the Gateway (:8080), which injects `X-User-Id` into downstream requests after JWT validation.

### Module call chain

```
Client → Gateway (:8080)
           ├── /api/user/**  →  User Service (:8081)
           ├── /api/chat/**  →  Chat Service (:8082)
           ├── /api/knowledge/** → Knowledge Service (:8083)
           └── /api/core/**  →  Core Service (:8084)
```

### Module responsibilities

- **gateway** — Spring Cloud Gateway. `AuthGlobalFilter` (order=10) validates JWT on all paths except a whitelist (login/register/refresh/swagger). `RateLimitFilter` does Redis-based rate limiting. `TraceIdFilter` generates UUID per request.

- **user** — User registration/login/logout, JWT token issuance/refresh, API key management. Uses Spring Security stateless sessions with a custom `JwtAuthenticationFilter`. Passwords are BCrypt-hashed. Tokens are stored in both MySQL (`user_token`) and Redis.

- **chat** — Session and message management. The core flow in `ChatServiceImpl.sendMessage()`: validates session ownership → builds context with chat memory summary → persists `UserMessageEntity` → calls Core Service SSE endpoint via `WebClient` → relays each token to the client via `SseEmitter` → persists `LlmMessageEntity` and `LlmSessionEntity` on completion → fires `MessageArchiveEvent` to RocketMQ. Runs the entire message loop on a virtual thread. Chat memory is summarized asynchronously when `roundCount >= 3`.

- **core** — AI model calls via LangChain4j 0.35.0. `AgentAssistant` is an `@AiService` interface with separate methods for chat, streamChat, ragChat, reactChat, and summarizeConversation — each reads a different `@SystemMessage(fromResource = "xxx-system-prompt.md")`. `CoreServiceImpl` routes by `AgentType` enum (CHAT/RAG/REACT). RAG mode builds a `RagPipelineBuilder` that uses `MilvusEmbeddingStore` for vector retrieval with a knowledgeBaseId metadata filter. RocketMQ listeners (`ChatMessageListener`, `DocumentUploadListener`, etc.) handle async events.

- **knowledge** — Knowledge base CRUD, document upload/parsing (PDF, DOCX, TXT via `DocumentParserFactory`), and vector search. `KnowledgeBuildPipeline` handles document chunking → embedding → Milvus insert. RocketMQ consumers handle async document processing.

- **common** — Shared models (`ApiResponse`, `PageResponse`, `AiResponse`), `GlobalExceptionHandler` (`@RestControllerAdvice`), `UserContextUtils` (reads `X-User-Id` from request header), `TokenCountUtils`, `RedisUtils`, event POJOs, and `ResultCode` enum.

### RocketMQ topics

- `chat-message-topic` — Chat → Core message archiving
- Knowledge topics for async document processing (`DocumentUploadEvent`, `KnowledgeBaseCreateEvent`, `KnowledgeQueryEvent`)

### Database mapping

| Service | Database |
|---------|----------|
| user | `smart_agent_user` (users, user_token, user_login_log, api_key) |
| chat | `smart-agent-chat` (session, user_message, llm_message, llm_session, chat_memory_current, chat_memory_history) |

### Agent type system prompt mapping

Agent type is resolved case-insensitively in `CoreServiceImpl.resolveAgentType()`, defaults to `CHAT`. Each type maps to a `@SystemMessage` resource file in `core/src/main/resources/`:
- `CHAT` → `chat-system-prompt.md`
- `RAG` → `ragChat-system-prompt.md`
- `REACT` → `reactChat-system-prompt.md`
- Stream → `streamChat-system-prompt.md`
- Summary → `summary-system-prompt.md`
