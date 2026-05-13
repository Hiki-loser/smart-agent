package com.smartagent.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ModelRequest {

    private String modelName;

    private List<Message> messages;

    private Map<String, Object> options;

    private Double temperature;

    private Integer maxTokens;

    public record Message(String role, String content, String reasoningContent) {}
}
