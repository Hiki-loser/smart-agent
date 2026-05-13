package com.smartagent.model.dto;

import com.smartagent.model.dto.AiStreamUsageMetadata;
import lombok.Data;

@Data
public class StreamChunk {

    private String content;

    private String reasoningContent;

    private boolean finished;

    private AiStreamUsageMetadata usage;
}
