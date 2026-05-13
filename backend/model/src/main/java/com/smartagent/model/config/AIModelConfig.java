package com.smartagent.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "ai.model")
public class AIModelConfig {

    private String apiKey;
    private String baseUrl;
    private String chatModelName;
    private String streamingChatModelName;
    private String embeddingModelName;
    private Double temperature = 0.7;
    private Long timeoutSeconds = 60L;
    private int maxRetries = 3;
    private long retryDelayMs = 1000L;

    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.isBlank();
    }

    public String resolvedStreamingChatModelName() {
        if (streamingChatModelName == null || streamingChatModelName.isBlank()) {
            return chatModelName;
        }
        return streamingChatModelName;
    }
}
