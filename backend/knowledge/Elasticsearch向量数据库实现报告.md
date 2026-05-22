# SmartAgent Knowledge 模块 — Elasticsearch 向量数据库实现报告

> **日期**: 2026-05-19  
> **版本**: 1.0.0-SNAPSHOT  
> **模块**: `backend/knowledge`

---

## 目录

- [1. 项目问题排查与修复](#1-项目问题排查与修复)
- [2. Elasticsearch 向量数据库完整实现](#2-elasticsearch-向量数据库完整实现)
- [3. 架构设计](#3-架构设计)
- [4. 文件变更清单](#4-文件变更清单)
- [5. 配置说明](#5-配置说明)
- [6. API 接口变更](#6-api-接口变更)
- [7. 验证步骤](#7-验证步骤)

---

## 1. 项目问题排查与修复

### 1.1 数据库 Schema 缺陷

**问题**: `document` 表缺少 `chunk_count` 和 `error_msg` 字段，但 `KnowledgeBuildPipeline.java` 代码中直接引用这两个字段：

- `document.setChunkCount(segments.size())` — 写入不存在的列
- `document.setErrorMsg(e.getMessage())` — 写入不存在的列

**修复**: 在 `backend/knowledge/src/main/resources/db/schema.sql` 的 `document` 表中补充：

```sql
`chunk_count` INT DEFAULT 0 COMMENT '切片数量',
`error_msg` TEXT DEFAULT NULL COMMENT '错误信息',
```

### 1.2 向量查询返回 Mock 数据

**问题**: `KnowledgeServiceImpl.queryKnowledgeBase()` 方法只发送 MQ 事件，返回硬编码的模拟数据，未真正执行向量检索。方法体内存在 TODO 注释。

**修复**: 重构该方法，注入 `EmbeddingModel` 和 `EmbeddingStore<TextSegment>`，实现完整的 **文本向量化 → 向量搜索 → 结果组装** 流程（详见第2.5节）。

### 1.3 单向量后端锁定

**问题**: `LangChain4jConfig` 仅支持 Milvus，`EmbeddingStore<TextSegment>` bean 硬编码为 `MilvusEmbeddingStore`。

**修复**: 引入 Spring Boot `@ConditionalOnProperty` 条件注入，基于 `vector-store.type` 配置动态选择 Milvus 或 Elasticsearch 后端。

---

## 2. Elasticsearch 向量数据库完整实现

### 2.1 Maven 依赖

**文件**: `backend/knowledge/pom.xml`

新增依赖：

```xml
<dependency>
    <groupId>dev.langchain4j</groupId>
    <artifactId>langchain4j-elasticsearch</artifactId>
    <version>${langchain4j.version}</version>  <!-- 0.35.0 -->
</dependency>
<dependency>
    <groupId>jakarta.json</groupId>
    <artifactId>jakarta.json-api</artifactId>
    <version>2.0.2</version>
</dependency>
```

`langchain4j-elasticsearch:0.35.0` 传递依赖：
- `co.elastic.clients:elasticsearch-java:8.14.3`（新版 ES Java Client）
- `org.elasticsearch.client:elasticsearch-rest-client`（底层 HTTP 传输）

### 2.2 应用配置

**文件**: `backend/knowledge/src/main/resources/application.yml`

新增配置项：

```yaml
vector-store:
  type: ${VECTOR_STORE_TYPE:elasticsearch}     # milvus | elasticsearch
  index-name: ${VECTOR_STORE_INDEX_NAME:smart_agent_knowledge}

elasticsearch:
  uris: ${ELASTICSEARCH_URIS:http://localhost:9200}
  username: ${ELASTICSEARCH_USERNAME:elastic}
  password: ${ELASTICSEARCH_PASSWORD:smart123}
```

### 2.3 Elasticsearch 客户端配置

**新增文件**: `backend/knowledge/src/main/java/com/smartagent/knowledge/config/ElasticsearchConfig.java`

仅在 `vector-store.type=elasticsearch` 时激活，创建三个 bean：

| Bean | 类型 | 说明 |
|------|------|------|
| `restClient` | `org.elasticsearch.client.RestClient` | ES 低级 REST 客户端，配置 Basic Auth |
| `elasticsearchTransport` | `co.elastic.clients.transport.ElasticsearchTransport` | 传输层适配（JacksonJsonpMapper） |
| `elasticsearchClient` | `co.elastic.clients.elasticsearch.ElasticsearchClient` | ES 高级客户端，供 LangChain4j 使用 |

### 2.4 ES 索引自动管理

**新增文件**: `backend/knowledge/src/main/java/com/smartagent/knowledge/config/ElasticsearchIndexManager.java`

应用启动时通过 `@PostConstruct` 自动执行：

1. 检查索引 `smart_agent_knowledge` 是否存在
2. 不存在则创建索引，显式定义 mapping：

```json
{
  "mappings": {
    "properties": {
      "vector": {
        "type": "dense_vector",
        "dims": 1024,
        "index": true,
        "similarity": "cosine"
      },
      "text": { "type": "text" },
      "metadata": { "type": "object" }
    }
  },
  "settings": {
    "number_of_shards": 1,
    "number_of_replicas": 0
  }
}
```

**关键设计决策**：
- `vector` 字段必须显式映射为 `dense_vector`，ES 无法通过动态映射自动推断
- 维度 `dims` 由 `ai.model.embedding-dimension` 配置决定（默认 1024）
- 相似度算法使用 `cosine`，与 Milvus 的 `MetricType.COSINE` 保持一致
- 单分片、零副本适用于开发环境；生产环境应调整

### 2.5 双后端条件注入

**修改文件**: `backend/knowledge/src/main/java/com/smartagent/knowledge/config/LangChain4jConfig.java`

核心改动：

```java
// Milvus 后端（vector-store.type=milvus）
@Bean
@ConditionalOnProperty(name = "vector-store.type", havingValue = "milvus")
public EmbeddingStore<TextSegment> milvusEmbeddingStore() { ... }

// Elasticsearch 后端（vector-store.type=elasticsearch）
@Bean
@ConditionalOnProperty(name = "vector-store.type", havingValue = "elasticsearch")
public EmbeddingStore<TextSegment> elasticsearchEmbeddingStore(
        ElasticsearchClient elasticsearchClient) {
    return ElasticsearchEmbeddingStore.builder()
            .client(elasticsearchClient)
            .indexName(esIndexName)
            .configuration(ElasticsearchConfigurationKnn.builder().build())
            .build();
}
```

`ElasticsearchConfigurationKnn` 使用 ES 的近似 kNN 查询，通过 `KnnQuery` 实现向量相似度搜索。

### 2.6 真正向量查询实现

**修改文件**: `backend/knowledge/src/main/java/com/smartagent/knowledge/service/impl/KnowledgeServiceImpl.java`

`queryKnowledgeBase()` 方法重构前后对比：

| 阶段 | 重构前 | 重构后 |
|------|--------|--------|
| 文本向量化 | ❌ 未实现 | `embeddingModel.embed(query)` |
| 向量搜索 | ❌ Mock 数据 | `embeddingStore.search(request)` |
| 知识库隔离 | ❌ 无 | `IsEqualTo("metadata.knowledgeBaseId", kbId)` |
| 相似度过滤 | ❌ 无效 | `minScore(threshold)` |
| TopK | ❌ 无效 | `maxResults(topK)` |
| 结果组装 | 硬编码 | 从 `EmbeddingMatch` 提取真实片段 |
| MQ 归档 | ✅ 保留 | ✅ 保留（异步非阻塞） |

核心搜索代码：

```java
EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
    .queryEmbedding(queryEmbedding)
    .maxResults(topK)
    .minScore(minScore)
    .filter(new IsEqualTo("metadata.knowledgeBaseId", dto.getKnowledgeBaseId()))
    .build();

List<EmbeddingMatch<TextSegment>> matches = embeddingStore.search(searchRequest).matches();
```

### 2.7 健康检查端点

**新增接口**: `GET /api/knowledge/health`

**修改文件**: `KnowledgeController.java`, `KnowledgeService.java`, `KnowledgeServiceImpl.java`

响应示例：

```json
{
  "code": 200,
  "data": {
    "service": "smart-agent-knowledge",
    "status": "UP",
    "vectorStore": {
      "type": "elasticsearch",
      "embeddingStoreClass": "ElasticsearchEmbeddingStore"
    }
  }
}
```

### 2.8 Docker 部署支持

**新增文件**: `env/Dockerfile.knowledge`

多阶段构建：
- **阶段1**: `maven:3.9-eclipse-temurin-21` — 编译 knowledge 模块（含 common、model 依赖）
- **阶段2**: `eclipse-temurin:21-jre-alpine` — 最小化 JRE 运行镜像

**修改文件**: `env/docker-compose.yml`

新增 `knowledge` 服务定义，关键环境变量：

```yaml
VECTOR_STORE_TYPE: ${VECTOR_STORE_TYPE:-elasticsearch}
ELASTICSEARCH_URIS: ${ELASTICSEARCH_URIS:-http://elasticsearch:9200}
ELASTICSEARCH_USERNAME: ${ELASTICSEARCH_USERNAME:-elastic}
ELASTICSEARCH_PASSWORD: ${ELASTICSEARCH_PASSWORD:-smart123}
```

服务依赖链：`nacos` → `mysql` → `elasticsearch` → `knowledge`

**修改文件**: `env/.env`

新增 `VECTOR_STORE_TYPE=elasticsearch` 环境变量默认值。

---

## 3. 架构设计

### 3.1 数据流

```
用户上传文档
    │
    ▼
KnowledgeController.uploadDocument()
    │
    ├── 1. 保存文件到本地 → DocumentEntity(MySQL)
    ├── 2. 发送 DocumentUploadEvent → RocketMQ
    │
    ▼
DocumentProcessConsumer (MQ 消费)
    │
    ▼
KnowledgeBuildPipeline.process()
    │
    ├── 3. DocumentParser.parse()        ← 文档 → 纯文本
    ├── 4. TextSplitter.split()          ← 纯文本 → TextSegment[]
    ├── 5. EmbeddingModel.embedAll()     ← TextSegment → Embedding[]
    ├── 6. EmbeddingStore.addAll()       ← Embedding → Milvus/ES
    └── 7. DocumentEntity.status=2       ← 标记完成

用户提问
    │
    ▼
KnowledgeController.queryKnowledge() ──→ KnowledgeServiceImpl.queryKnowledgeBase()
    │
    ├── 1. EmbeddingModel.embed(query)           ← 文本 → 向量
    ├── 2. EmbeddingStore.search(request)        ← 向量 → TopK 片段
    │      └── 按 knowledgeBaseId 过滤，按相似度阈值裁剪
    └── 3. 组装 QueryResultVO → 返回前端
```

### 3.2 向量后端切换

```
                    ┌──────────────────────────┐
                    │   EmbeddingStore          │
                    │   <TextSegment>           │
                    └──────────┬───────────────┘
                               │ @ConditionalOnProperty
              ┌────────────────┼────────────────┐
              ▼                                 ▼
    vector-store.type=milvus       vector-store.type=elasticsearch
    ┌──────────────────────┐       ┌──────────────────────────┐
    │ MilvusEmbeddingStore │       │ ElasticsearchEmbedding   │
    │ - host:19530         │       │ Store                    │
    │ - IVF_FLAT/COSINE    │       │ - ES 8.12+               │
    │ - 需要 etcd+MinIO    │       │ - kNN 近似搜索           │
    └──────────────────────┘       │ - dense_vector + cosine  │
                                   └──────────────────────────┘
```

### 3.3 Elasticsearch 向量存储原理

1. **索引创建时**：`ElasticsearchIndexManager` 显式映射 `vector` 字段为 `dense_vector(1024, cosine)`
2. **写入时**：`ElasticsearchEmbeddingStore.addAll()` → `bulk index`，将 float[] 向量写入 `vector` 字段
3. **搜索时**：`ElasticsearchConfigurationKnn.internalSearch()` → ES `knn` query，在 `vector` 字段上执行近似最近邻搜索
4. **过滤时**：LangChain4j 的 `IsEqualTo` filter 被 `ElasticsearchMetadataFilterMapper` 转换为 ES `term` query，作为 knn 的 `filter` 子句

---

## 4. 文件变更清单

### 新增文件（4个）

| 文件 | 说明 |
|------|------|
| `backend/knowledge/src/main/java/com/smartagent/knowledge/config/ElasticsearchConfig.java` | ES 客户端配置（RestClient、Transport、ElasticsearchClient） |
| `backend/knowledge/src/main/java/com/smartagent/knowledge/config/ElasticsearchIndexManager.java` | ES 索引自动创建与 mapping 管理 |
| `env/Dockerfile.knowledge` | Knowledge 服务 Docker 镜像构建 |
| `backend/knowledge/Elasticsearch向量数据库实现报告.md` | 本文档 |

### 修改文件（9个）

| 文件 | 变更摘要 |
|------|----------|
| `backend/knowledge/pom.xml` | 新增 `langchain4j-elasticsearch`、`jakarta.json-api` 依赖 |
| `backend/knowledge/src/main/resources/application.yml` | 新增 `vector-store`、`elasticsearch` 配置段 |
| `backend/knowledge/src/main/resources/db/schema.sql` | `document` 表新增 `chunk_count`、`error_msg` 字段 |
| `backend/knowledge/src/main/java/.../config/LangChain4jConfig.java` | 条件 Bean 注入：Milvus / ES 双后端 |
| `backend/knowledge/src/main/java/.../service/KnowledgeService.java` | 新增 `getVectorStoreInfo()` 接口方法 |
| `backend/knowledge/src/main/java/.../service/impl/KnowledgeServiceImpl.java` | 实现真正向量查询；注入 EmbeddingModel / EmbeddingStore；新增健康信息方法 |
| `backend/knowledge/src/main/java/.../controller/KnowledgeController.java` | 新增 `GET /health` 端点 |
| `env/docker-compose.yml` | 新增 `knowledge` 服务定义 |
| `env/.env` | 新增 `VECTOR_STORE_TYPE` 环境变量 |

---

## 5. 配置说明

### 5.1 向量后端切换

通过 `vector-store.type` 配置控制：

```yaml
# 使用 Elasticsearch（默认）
vector-store:
  type: elasticsearch

# 使用 Milvus
vector-store:
  type: milvus
```

### 5.2 环境变量速查

| 变量 | 默认值 | 说明 |
|------|--------|------|
| `VECTOR_STORE_TYPE` | `elasticsearch` | 向量数据库类型 |
| `VECTOR_STORE_INDEX_NAME` | `smart_agent_knowledge` | ES 索引名 / Milvus Collection 名 |
| `ELASTICSEARCH_URIS` | `http://localhost:9200` | ES 连接地址 |
| `ELASTICSEARCH_USERNAME` | `elastic` | ES 用户名 |
| `ELASTICSEARCH_PASSWORD` | `smart123` | ES 密码 |
| `MILVUS_HOST` | `localhost` | Milvus 主机 |
| `MILVUS_PORT` | `19530` | Milvus 端口 |
| `AI_MODEL_EMBEDDING_DIMENSION` | `1024` | Embedding 向量维度 |

### 5.3 Docker Compose 启动

```bash
# 完整启动（含 Elasticsearch）
docker compose -f env/docker-compose.yml --env-file env/.env up -d

# 仅基础设施 + knowledge
docker compose -f env/docker-compose.yml --env-file env/.env up -d \
  elasticsearch knowledge

# 切换到 Milvus
VECTOR_STORE_TYPE=milvus docker compose -f env/docker-compose.yml \
  --env-file env/.env --profile milvus up -d
```

---

## 6. API 接口变更

### 新增接口

#### `GET /api/knowledge/health` — 健康检查

**无需认证**（建议加入网关白名单）

**响应**:

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "service": "smart-agent-knowledge",
    "status": "UP",
    "vectorStore": {
      "type": "elasticsearch",
      "embeddingStoreClass": "ElasticsearchEmbeddingStore"
    }
  }
}
```

### 修复接口

#### `POST /api/knowledge/query` — 知识库查询

重构前返回 mock 数据，重构后执行真正的向量相似度搜索。

**请求体**（不变）：

```json
{
  "knowledgeBaseId": 1234567890,
  "query": "什么是 RAG？",
  "similarityThreshold": 0.7,
  "topK": 5
}
```

**响应**（数据结构不变，内容变为真实搜索结果）：

```json
{
  "code": 200,
  "data": {
    "queryId": "uuid",
    "query": "什么是 RAG？",
    "answer": "找到 3 个相关片段",
    "documentFragments": [
      {
        "documentId": 111,
        "documentName": "AI入门指南.pdf",
        "content": "RAG（Retrieval-Augmented Generation）是...",
        "similarity": 0.92,
        "pageNumber": 3
      }
    ],
    "executionTime": 245
  }
}
```

---

## 7. 验证步骤

### 7.1 本地开发验证

```bash
# 1. 启动 ES（Docker）
docker compose -f env/docker-compose.yml --env-file env/.env up -d elasticsearch

# 2. 验证 ES 可用
curl -u elastic:smart123 http://localhost:9200/_cluster/health

# 3. 启动 knowledge 服务
cd backend/knowledge
mvn spring-boot:run

# 4. 检查索引自动创建
curl -u elastic:smart123 http://localhost:9200/smart_agent_knowledge/_mapping

# 5. 健康检查
curl http://localhost:8080/api/knowledge/health
```

### 7.2 功能验证流程

```
1. POST /api/knowledge/bases      → 创建知识库
2. POST /api/knowledge/bases/{id}/documents → 上传 PDF/TXT 文档
3. 等待 MQ 异步处理完成（检查 document 表 status=2）
4. POST /api/knowledge/query      → 用文档相关内容提问
5. 验证返回的 documentFragments 包含正确文档片段
```

### 7.3 切换到 Milvus 验证

```bash
# 修改 application-local.yml
vector-store:
  type: milvus

# 启动 Milvus
docker compose -f env/docker-compose.yml --profile milvus up -d

# 重启 knowledge 服务，功能应保持一致
```

---

## 附录：LangChain4j EmbeddingStore 接口抽象

整个向量存储操作均通过 LangChain4j 的 `EmbeddingStore<TextSegment>` 接口进行，与具体后端（Milvus/ES）解耦：

```java
public interface EmbeddingStore<TextSegment> {
    String add(Embedding embedding, TextSegment segment);
    void addAll(List<Embedding> embeddings, List<TextSegment> segments);
    List<EmbeddingMatch<TextSegment>> search(EmbeddingSearchRequest request);
    // ...
}
```

这意味着 `KnowledgeBuildPipeline` 和 `KnowledgeServiceImpl` 无需任何修改即可在 Milvus 和 Elasticsearch 之间切换 —— 只需更改配置中的 `vector-store.type`。

---

> **实现完成日期**: 2026-05-19  
> **影响范围**: `backend/knowledge` 模块  
> **兼容性**: 向后兼容，默认值切换到 `elasticsearch`，原 `milvus` 可通过配置启用
