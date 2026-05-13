package com.smartagent.knowledge.config;

import com.smartagent.model.config.AIModelConfig;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

@Slf4j
@Configuration
public class AiModelFactoryConfig {

    @Bean
    @Primary
    public EmbeddingModel embeddingModel(AIModelConfig config) {
        if (!config.isApiKeyConfigured()) {
            throw new IllegalStateException("ai.model.api-key is required for knowledge embedding model");
        }
        log.info("Initializing knowledge embedding model with baseUrl={}, model={}",
                config.getBaseUrl(), config.getEmbeddingModelName());
        return OpenAiEmbeddingModel.builder()
                .baseUrl(config.getBaseUrl())
                .apiKey(config.getApiKey())
                .modelName(config.getEmbeddingModelName())
                .timeout(Duration.ofSeconds(config.getTimeoutSeconds()))
                .build();
    }
}
