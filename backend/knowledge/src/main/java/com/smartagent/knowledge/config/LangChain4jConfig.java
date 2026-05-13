package com.smartagent.knowledge.config;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.milvus.MilvusEmbeddingStore;
import io.milvus.common.clientenum.ConsistencyLevelEnum;
import io.milvus.param.IndexType;
import io.milvus.param.MetricType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库模块的 LangChain4j 基础配置。
 */
@Configuration
public class LangChain4jConfig {

    @Value("${milvus.host:localhost}")
    private String milvusHost;

    @Value("${milvus.port:19530}")
    private Integer milvusPort;

    @Value("${milvus.collection-name:smart_agent_knowledge}")
    private String milvusCollectionName;

    @Value("${ai.model.embedding-dimension:1024}")
    private Integer embeddingDimension;

    @Bean
    public EmbeddingStore<TextSegment> embeddingStore() {
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
}



