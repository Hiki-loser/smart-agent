package com.smartagent.knowledge.config;

import dev.langchain4j.data.segment.TextSegment;
import org.elasticsearch.client.RestClient;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchEmbeddingStore;
import dev.langchain4j.store.embedding.elasticsearch.ElasticsearchConfigurationKnn;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库模块的 LangChain4j 向量存储配置
 * 支持 Milvus 和 Elasticsearch 两种向量数据库后端，通过 vector-store.type 配置切换
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Configuration
public class LangChain4jConfig {

    // ==================== Milvus 配置 ====================

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private Integer milvusPort;

    @Value("${milvus.collection-name:smart_agent_knowledge}")
    private String milvusCollectionName;

    @Value("${ai.model.embedding-dimension:1024}")
    private Integer embeddingDimension;

    @Value("${vector-store.index-name:smart_agent_knowledge}")
    private String esIndexName;

    /**
     * Milvus 向量存储 - 仅在 vector-store.type=milvus 时创建
     */
    @Bean
    @ConditionalOnProperty(name = "vector-store.type", havingValue = "milvus")
    public EmbeddingStore<TextSegment> milvusEmbeddingStore() {
        log.info("Initializing Milvus EmbeddingStore: host={}, port={}, collection={}, dimension={}",
                milvusHost, milvusPort, milvusCollectionName, embeddingDimension);
        return MilvusEmbeddingStore.builder()
                .host(milvusHost)
                .port(milvusPort)
                .collectionName(milvusCollectionName)
                .dimension(embeddingDimension)
                .indexType(IndexType.IVF_FLAT)
                .metricType(MetricType.COSINE)
                .consistencyLevel(ConsistencyLevelEnum.STRONG)
                .autoFlushOnInsert(true)
                .retrieveEmbeddingsOnSearch(false)
                .build();
    }

    /**
     * Elasticsearch 向量存储 - 仅在 vector-store.type=elasticsearch 时创建
     */
    @Bean
    @ConditionalOnProperty(name = "vector-store.type", havingValue = "elasticsearch")
    public EmbeddingStore<TextSegment> elasticsearchEmbeddingStore(RestClient restClient) {
        log.info("Initializing Elasticsearch EmbeddingStore: indexName={}, dimension={}",
                esIndexName, embeddingDimension);
        return ElasticsearchEmbeddingStore.builder()
                .restClient(restClient)
                .indexName(esIndexName)
                .configuration(ElasticsearchConfigurationKnn.builder().build())
                .build();
    }
}
