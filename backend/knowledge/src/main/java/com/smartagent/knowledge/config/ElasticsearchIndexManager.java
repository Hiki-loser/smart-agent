package com.smartagent.knowledge.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Elasticsearch 索引管理器
 * 在应用启动时自动创建/验证 ES 索引的 dense_vector mapping，
 * 确保 kNN 向量搜索正常工作。
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "vector-store.type", havingValue = "elasticsearch")
public class ElasticsearchIndexManager {

    private final ElasticsearchClient elasticsearchClient;

    @Value("${vector-store.index-name:smart_agent_knowledge}")
    private String indexName;

    @Value("${ai.model.embedding-dimension:1024}")
    private Integer embeddingDimension;

    public ElasticsearchIndexManager(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @PostConstruct
    public void initIndex() {
        try {
            boolean exists = elasticsearchClient.indices()
                    .exists(e -> e.index(indexName))
                    .value();

            if (exists) {
                log.info("Elasticsearch index [{}] already exists, skipping creation", indexName);
                return;
            }

            log.info("Creating Elasticsearch index [{}] with dense_vector mapping (dimension={})",
                    indexName, embeddingDimension);

            elasticsearchClient.indices().create(c -> c
                    .index(indexName)
                    .settings(s -> s
                            .numberOfShards("1")
                            .numberOfReplicas("0")
                    )
                    .mappings(m -> m
                            .properties("vector", p -> p
                                    .denseVector(dv -> dv
                                            .dims(embeddingDimension)
                                            .index(true)
                                            .similarity("cosine")
                                    )
                            )
                            .properties("text", p -> p.text(t -> t))
                            .properties("metadata", p -> p.object(o -> o))
                    )
            );

            log.info("Elasticsearch index [{}] created successfully", indexName);
        } catch (Exception e) {
            log.error("Failed to initialize Elasticsearch index [{}]: {}", indexName, e.getMessage(), e);
        }
    }
}
