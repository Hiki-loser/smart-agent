package com.smartagent.common.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class DocumentUploadEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long documentId;
    private Long knowledgeBaseId;
    private Long userId;
    private String name;
    private String type;
    private Long size;
    private String storagePath;
}
