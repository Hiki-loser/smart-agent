package com.smartagent.model.provider;

import com.smartagent.model.dto.ModelRequest;
import com.smartagent.model.dto.ModelResponse;
import com.smartagent.model.dto.StreamChunk;
import reactor.core.publisher.Flux;

import java.util.Set;

public interface ModelProvider {

    String providerName();

    Set<String> supportedModels();

    ModelResponse chat(ModelRequest request);

    Flux<StreamChunk> chatStream(ModelRequest request);
}
