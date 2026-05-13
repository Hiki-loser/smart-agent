package com.smartagent.common.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KnowledgeBaseCreateEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long knowledgeBaseId;
    private Long userId;
    private String name;
    private String description;
}
