package com.smartagent.knowledge.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.message.BasicHeader;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 客户端配置
 * 仅在 vector-store.type=elasticsearch 时激活
 *
 * @author SmartAgent
 * @since 1.0.0
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "vector-store.type", havingValue = "elasticsearch", matchIfMissing = false)
public class ElasticsearchConfig {

    @Value("${elasticsearch.uris}")
    private String elasticsearchUris;

    @Value("${elasticsearch.username:elastic}")
    private String username;

    @Value("${elasticsearch.password:smart123}")
    private String password;

    /**
     * 创建 Elasticsearch 低级 REST 客户端
     */
    @Bean(destroyMethod = "close")
    public RestClient restClient() {
        log.info("Creating Elasticsearch RestClient for uri: {}", elasticsearchUris);

        BasicCredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                AuthScope.ANY,
                new UsernamePasswordCredentials(username, password)
        );

        return RestClient.builder(HttpHost.create(elasticsearchUris))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setDefaultHeaders(new Header[]{
                        new BasicHeader("Content-Type", "application/json")
                })
                .build();
    }

    /**
     * 创建 Elasticsearch 传输层
     */
    @Bean(destroyMethod = "close")
    public ElasticsearchTransport elasticsearchTransport(RestClient restClient) {
        log.info("Creating ElasticsearchTransport");
        return new RestClientTransport(restClient, new JacksonJsonpMapper());
    }

    /**
     * 创建 Elasticsearch 高级客户端
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(ElasticsearchTransport transport) {
        log.info("Creating ElasticsearchClient");
        return new ElasticsearchClient(transport);
    }
}
