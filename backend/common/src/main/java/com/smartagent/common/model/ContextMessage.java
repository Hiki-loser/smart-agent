package com.smartagent.common.model;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record ContextMessage(
        String role,
        String content,
        LocalDateTime createdAt
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
