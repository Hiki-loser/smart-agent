package com.smartagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiStreamUsageMetadata implements Serializable {

    public static final String SSE_USAGE_PREFIX = "__SMART_AGENT_USAGE__:";

    @Serial
    private static final long serialVersionUID = 1L;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private String finishReason;
}
