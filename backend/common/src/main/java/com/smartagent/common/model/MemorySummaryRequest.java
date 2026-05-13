package com.smartagent.common.model;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class MemorySummaryRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private Long userId;
    private Integer currentRound;
    private List<ContextMessage> messages;
}
