package com.smartagent.model.service;

import com.smartagent.model.dto.ModelRequest;
import com.smartagent.model.dto.ModelResponse;
import com.smartagent.model.dto.StreamChunk;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

public interface ModelService {

    ModelResponse chat(ModelRequest request);

    Flux<StreamChunk> chatStream(ModelRequest request);

    SseEmitter chatStreamAsSse(ModelRequest request);
}
