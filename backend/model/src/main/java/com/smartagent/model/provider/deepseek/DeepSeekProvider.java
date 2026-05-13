package com.smartagent.model.provider.deepseek;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.smartagent.model.config.AIModelConfig;
import com.smartagent.model.dto.AiStreamUsageMetadata;
import com.smartagent.model.config.DeepSeekModelConfig;
import com.smartagent.model.dto.ModelRequest;
import com.smartagent.model.dto.ModelResponse;
import com.smartagent.model.dto.StreamChunk;
import com.smartagent.model.exception.ModelException;
import com.smartagent.model.provider.ModelProvider;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.core.publisher.SynchronousSink;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class DeepSeekProvider implements ModelProvider {

    private final DeepSeekModelConfig config;
    private final AIModelConfig aiModelConfig;
    private final WebClient webClient;

    public DeepSeekProvider(DeepSeekModelConfig config, AIModelConfig aiModelConfig) {
        this.config = config;
        this.aiModelConfig = aiModelConfig;
        this.webClient = buildWebClient(resolveApiKey());
    }

    @Override
    public String providerName() {
        return "deepseek";
    }

    @Override
    public Set<String> supportedModels() {
        return Set.of(config.getProModelName(), config.getFlashModelName());
    }

    @Override
    public ModelResponse chat(ModelRequest request) {
        Map<String, Object> body = buildRequestBody(request, false);
        DeepSeekChatResponse response = webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .onStatus(status -> status.value() >= 400, resp ->
                        resp.bodyToMono(DeepSeekErrorBody.class)
                                .flatMap(err -> Mono.error(toModelException(resp.statusCode().value(), err)))
                )
                .bodyToMono(DeepSeekChatResponse.class)
                .retryWhen(Retry.backoff(aiModelConfig.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(this::isRetryable))
                .block();

        return mapToResponse(response);
    }

    @Override
    public Flux<StreamChunk> chatStream(ModelRequest request) {
        Map<String, Object> body = buildRequestBody(request, true);
        String modelName = request.getModelName();

        return webClient.post()
                .uri("/v1/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .onStatus(status -> status.value() >= 400, resp ->
                        resp.bodyToMono(DeepSeekErrorBody.class)
                                .flatMap(err -> Mono.error(toModelException(resp.statusCode().value(), err)))
                )
                .bodyToFlux(String.class)
                .retryWhen(Retry.backoff(aiModelConfig.getMaxRetries(), Duration.ofSeconds(1))
                        .filter(this::isRetryable))
                .filter(line -> !line.trim().isEmpty())
                .handle((line, sink) -> {
                    emitStreamChunks(line, modelName, sink);
                });
    }

    void emitStreamChunks(String rawChunk, String modelName, SynchronousSink<StreamChunk> sink) {
        for (String payload : extractPayloads(rawChunk)) {
            if (payload.isBlank()) {
                continue;
            }
            if ("[DONE]".equals(payload)) {
                StreamChunk done = new StreamChunk();
                done.setFinished(true);
                sink.next(done);
                sink.complete();
                return;
            }

            try {
                DeepSeekStreamChunk parsed = parseStreamJson(payload);
                if (parsed.choices != null) {
                    for (DeepSeekStreamChoice choice : parsed.choices) {
                        if (choice.delta == null) {
                            continue;
                        }
                        StreamChunk chunk = new StreamChunk();
                        if (choice.delta.content != null) {
                            chunk.setContent(choice.delta.content);
                        }
                        if (choice.delta.reasoningContent != null) {
                            chunk.setReasoningContent(choice.delta.reasoningContent);
                        }
                        if (parsed.usage != null) {
                            chunk.setUsage(buildUsageMetadata(modelName, parsed.usage));
                        }
                        sink.next(chunk);
                    }
                }
                if (parsed.choices == null && parsed.usage != null) {
                    StreamChunk chunk = new StreamChunk();
                    chunk.setUsage(buildUsageMetadata(modelName, parsed.usage));
                    sink.next(chunk);
                }
            } catch (Exception e) {
                log.warn("Failed to parse SSE payload: {}", payload, e);
            }
        }
    }

    static List<String> extractPayloads(String rawChunk) {
        List<String> payloads = new ArrayList<>();
        if (rawChunk == null || rawChunk.isBlank()) {
            return payloads;
        }

        for (String line : rawChunk.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || trimmed.startsWith(":") || trimmed.startsWith("event:")
                    || trimmed.startsWith("id:") || trimmed.startsWith("retry:")) {
                continue;
            }

            String payload = trimmed.startsWith("data:") ? trimmed.substring(5).trim() : trimmed;
            if (!payload.isEmpty()) {
                payloads.add(payload);
            }
        }

        return payloads;
    }

    // ─── request building ───

    private Map<String, Object> buildRequestBody(ModelRequest request, boolean stream) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", request.getModelName());
        body.put("messages", buildMessages(request.getMessages()));
        body.put("stream", stream);

        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        } else if (config.getDefaultTemperature() != null) {
            body.put("temperature", config.getDefaultTemperature());
        }
        if (request.getMaxTokens() != null) {
            body.put("max_tokens", request.getMaxTokens());
        } else if (config.getDefaultMaxTokens() != null) {
            body.put("max_tokens", config.getDefaultMaxTokens());
        }

        Map<String, Object> options = request.getOptions();
        if (options != null) {
            if (options.containsKey("reasoning_effort")) {
                body.put("reasoning_effort", options.get("reasoning_effort"));
            }
            if (options.containsKey("thinking")) {
                body.put("extra_body", Map.of("thinking",
                        Map.of("type", options.get("thinking"))));
            }
        }

        return body;
    }

    private List<Map<String, Object>> buildMessages(List<ModelRequest.Message> messages) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ModelRequest.Message msg : messages) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("role", msg.role());
            m.put("content", msg.content());
            if (msg.reasoningContent() != null && !msg.reasoningContent().isBlank()) {
                m.put("reasoning_content", msg.reasoningContent());
            }
            result.add(m);
        }
        return result;
    }

    // ─── response mapping ───

    private ModelResponse mapToResponse(DeepSeekChatResponse response) {
        ModelResponse resp = new ModelResponse();
        resp.setModelName(response.model);
        resp.setFinishReason(response.getFinishReason());

        if (response.choices != null && !response.choices.isEmpty()) {
            DeepSeekMessage msg = response.choices.get(0).message;
            if (msg != null) {
                resp.setContent(msg.content);
                resp.setReasoningContent(msg.reasoningContent);
            }
        }
        if (response.usage != null) {
            resp.setPromptTokens(response.usage.promptTokens);
            resp.setCompletionTokens(response.usage.completionTokens);
            resp.setTotalTokens(response.usage.totalTokens);
        }
        return resp;
    }

    // ─── SSE parsing ───

    private AiStreamUsageMetadata buildUsageMetadata(String modelName, DeepSeekUsage usage) {
        AiStreamUsageMetadata meta = new AiStreamUsageMetadata();
        meta.setModelName(modelName);
        meta.setPromptTokens(usage.promptTokens);
        meta.setCompletionTokens(usage.completionTokens);
        meta.setTotalTokens(usage.totalTokens);
        return meta;
    }

    private DeepSeekStreamChunk parseStreamJson(String json) {
        try {
            return com.smartagent.common.utils.JsonUtils.fromJson(json, DeepSeekStreamChunk.class);
        } catch (Exception e) {
            // Fallback: use Jackson directly since JsonUtils may not handle it
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                return mapper.readValue(json, DeepSeekStreamChunk.class);
            } catch (Exception ex) {
                throw new RuntimeException("Failed to parse SSE JSON: " + json, ex);
            }
        }
    }

    // ─── error handling ───

    private ModelException toModelException(int httpStatus, DeepSeekErrorBody err) {
        String code = "DEEPSEEK_" + httpStatus;
        String message = err != null && err.error != null ? err.error.message : "DeepSeek API error, status=" + httpStatus;
        return new ModelException(code, message);
    }

    private boolean isRetryable(Throwable t) {
        if (t instanceof ModelException e) {
            String code = e.getCode();
            return "DEEPSEEK_429".equals(code) || "DEEPSEEK_502".equals(code)
                    || "DEEPSEEK_503".equals(code) || "DEEPSEEK_504".equals(code);
        }
        return t instanceof java.net.ConnectException
                || t instanceof java.util.concurrent.TimeoutException;
    }

    // ─── WebClient setup ───

    private WebClient buildWebClient(String apiKey) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(Duration.ofSeconds(aiModelConfig.getTimeoutSeconds()))
                .headers(headers -> headers.set("Authorization", "Bearer " + apiKey));

        return WebClient.builder()
                .baseUrl(config.getBaseUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private String resolveApiKey() {
        if (config.getApiKey() != null && !config.getApiKey().isBlank()) {
            return config.getApiKey();
        }
        if (aiModelConfig.isApiKeyConfigured()) {
            return aiModelConfig.getApiKey();
        }
        throw new IllegalStateException(
                "DeepSeek API key not configured. Set ai.model.deepseek.api-key or ai.model.api-key");
    }

    // ─── DTOs for DeepSeek API JSON ───

    @Data
    static class DeepSeekChatResponse {
        private String id;
        private String model;
        private List<DeepSeekChoice> choices;
        private DeepSeekUsage usage;

        String getFinishReason() {
            if (choices != null && !choices.isEmpty()) {
                return choices.get(0).finishReason;
            }
            return null;
        }
    }

    @Data
    static class DeepSeekChoice {
        private int index;
        private DeepSeekMessage message;
        private String finishReason;

        @JsonProperty("finish_reason")
        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }

    @Data
    static class DeepSeekMessage {
        private String role;
        private String content;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    @Data
    static class DeepSeekUsage {
        @JsonProperty("prompt_tokens")
        private Integer promptTokens;
        @JsonProperty("completion_tokens")
        private Integer completionTokens;
        @JsonProperty("total_tokens")
        private Integer totalTokens;
    }

    @Data
    static class DeepSeekStreamChunk {
        private List<DeepSeekStreamChoice> choices;
        private DeepSeekUsage usage;
    }

    @Data
    static class DeepSeekStreamChoice {
        private int index;
        private DeepSeekStreamDelta delta;

        @JsonProperty("finish_reason")
        private String finishReason;
    }

    @Data
    static class DeepSeekStreamDelta {
        private String role;
        private String content;

        @JsonProperty("reasoning_content")
        private String reasoningContent;
    }

    @Data
    static class DeepSeekErrorBody {
        private DeepSeekErrorDetail error;
    }

    @Data
    static class DeepSeekErrorDetail {
        private String message;
        private String type;
        private String code;
    }
}
