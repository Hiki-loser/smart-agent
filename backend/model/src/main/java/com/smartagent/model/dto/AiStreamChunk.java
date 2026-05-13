package com.smartagent.model.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AiStreamChunk implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String content;

    private boolean finished;

    public AiStreamChunk(String content, boolean finished) {
        this.content = content;
        this.finished = finished;
    }

    public AiStreamChunk() {
    }
}
