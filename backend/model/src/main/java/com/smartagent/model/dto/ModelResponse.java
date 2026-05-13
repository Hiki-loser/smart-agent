package com.smartagent.model.dto;

import lombok.Data;

@Data
public class ModelResponse {

    private String content;

    private String reasoningContent;

    private String modelName;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer totalTokens;

    private String finishReason;
}
