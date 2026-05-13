package com.smartagent.common.event;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class MessageArchiveEvent implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long sessionId;
    private String content;
    private Long userId;
    private String aiResponse;

    public String getMessage() {
        return content;
    }

    public void setMessage(String message) {
        this.content = message;
    }
}
