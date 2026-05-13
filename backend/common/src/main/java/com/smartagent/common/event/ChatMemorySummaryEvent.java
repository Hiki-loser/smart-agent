package com.smartagent.common.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ChatMemorySummaryEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private Long userId;
    private Integer targetRound;
}
