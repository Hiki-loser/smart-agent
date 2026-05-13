package com.smartagent.common.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class KnowledgeQueryEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String queryId;
    private Long knowledgeBaseId;
    private Long userId;
    private String query;
    private Double similarityThreshold;
    private Integer topK;
}
