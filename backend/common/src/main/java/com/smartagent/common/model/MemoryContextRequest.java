package com.smartagent.common.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MemoryContextRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private String currentUserMessage;
    private List<ContextMessage> recentMessages;
}
