package com.smartagent.model.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ai.model.deepseek")
public class DeepSeekModelConfig {

    private String apiKey;

    private String baseUrl = "https://api.deepseek.com";

    private String proModelName = "deepseek-v4-pro";

    private String flashModelName = "deepseek-v4-flash";

    private Double defaultTemperature = 0.7;

    private Integer defaultMaxTokens = 4096;
}
