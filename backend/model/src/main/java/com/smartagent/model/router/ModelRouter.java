package com.smartagent.model.router;

import com.smartagent.model.exception.ModelException;
import com.smartagent.model.provider.ModelProvider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModelRouter {

    private final Map<String, ModelProvider> providerMap;

    public ModelRouter(List<ModelProvider> providers) {
        this.providerMap = new HashMap<>();
        for (ModelProvider provider : providers) {
            for (String modelName : provider.supportedModels()) {
                ModelProvider existing = providerMap.putIfAbsent(modelName, provider);
                if (existing != null) {
                    throw new IllegalStateException(
                            "Duplicate model name '" + modelName + "' registered by both '"
                            + existing.providerName() + "' and '" + provider.providerName() + "'");
                }
            }
        }
    }

    public ModelProvider route(String modelName) {
        if (modelName == null || modelName.isBlank()) {
            if (providerMap.isEmpty()) {
                throw new ModelException("NO_PROVIDER", "No model provider configured");
            }
            return providerMap.values().iterator().next();
        }
        ModelProvider provider = providerMap.get(modelName);
        if (provider == null) {
            throw new ModelException("UNSUPPORTED_MODEL",
                    "No provider found for model '" + modelName + "'. Available: " + providerMap.keySet());
        }
        return provider;
    }
}
