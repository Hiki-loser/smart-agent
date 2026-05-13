package com.smartagent.model;

import com.smartagent.model.config.AIModelConfig;
import com.smartagent.model.config.DeepSeekModelConfig;
import com.smartagent.model.provider.ModelProvider;
import com.smartagent.model.provider.deepseek.DeepSeekProvider;
import com.smartagent.model.router.ModelRouter;
import com.smartagent.model.service.ModelService;
import com.smartagent.model.service.impl.ModelServiceImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@AutoConfiguration
@ConditionalOnClass(WebClient.class)
@EnableConfigurationProperties(DeepSeekModelConfig.class)
public class ModelAutoConfiguration {

    @Bean
    public DeepSeekProvider deepSeekProvider(DeepSeekModelConfig config, AIModelConfig aiModelConfig) {
        return new DeepSeekProvider(config, aiModelConfig);
    }

    @Bean
    public ModelRouter modelRouter(List<ModelProvider> providers) {
        return new ModelRouter(providers);
    }

    @Bean
    public ModelService modelService(ModelRouter router) {
        return new ModelServiceImpl(router);
    }
}
