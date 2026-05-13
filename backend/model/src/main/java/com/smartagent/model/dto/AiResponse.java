package com.smartagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class AiResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String answer;

    private List<String> sources;

    private AiUsage usage;

    public AiResponse(String answer, List<String> sources, AiUsage usage) {
        this.answer = answer;
        this.sources = sources;
        this.usage = usage;
    }

    public AiResponse() {
    }
}
