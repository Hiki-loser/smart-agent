package com.smartagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiUsage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int promptTokens;

    private int completionTokens;

    private int totalTokens;

    public AiUsage(int promptTokens, int completionTokens, int totalTokens) {
        this.promptTokens = promptTokens;
        this.completionTokens = completionTokens;
        this.totalTokens = totalTokens;
    }

    public AiUsage() {
    }
}
