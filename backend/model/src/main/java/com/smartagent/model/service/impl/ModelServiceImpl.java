package com.smartagent.model.service.impl;

import com.smartagent.common.utils.JsonUtils;
import com.smartagent.model.dto.AiStreamUsageMetadata;
import com.smartagent.model.dto.ModelRequest;
import com.smartagent.model.dto.ModelResponse;
import com.smartagent.model.dto.StreamChunk;
import com.smartagent.model.provider.ModelProvider;
import com.smartagent.model.router.ModelRouter;
import com.smartagent.model.service.ModelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class ModelServiceImpl implements ModelService {

    private final ModelRouter router;

    @Override
    public ModelResponse chat(ModelRequest request) {
        ModelProvider provider = router.route(request.getModelName());
        return provider.chat(request);
    }

    @Override
    public Flux<StreamChunk> chatStream(ModelRequest request) {
        ModelProvider provider = router.route(request.getModelName());
        return provider.chatStream(request);
    }

    @Override
    public SseEmitter chatStreamAsSse(ModelRequest request) {
        SseEmitter emitter = new SseEmitter(0L);

        Thread.startVirtualThread(() -> {
            try {
                Flux<StreamChunk> flux = chatStream(request);
                flux.doOnNext(chunk -> {
                    try {
                        if (chunk.getContent() != null) {
                            emitter.send(SseEmitter.event().data(chunk.getContent()));
                        }
                        if (chunk.getUsage() != null) {
                            String payload = JsonUtils.toJson(chunk.getUsage());
                            if (payload != null) {
                                emitter.send(SseEmitter.event()
                                        .data(AiStreamUsageMetadata.SSE_USAGE_PREFIX + payload));
                            }
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .doOnComplete(() -> {
                    try {
                        emitter.complete();
                    } catch (Exception e) {
                        log.error("Failed to complete SSE emitter", e);
                    }
                })
                .doOnError(error -> {
                    log.error("Stream error", error);
                    emitter.completeWithError(error);
                })
                .blockLast();
            } catch (Exception e) {
                log.error("Failed to stream chat", e);
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
