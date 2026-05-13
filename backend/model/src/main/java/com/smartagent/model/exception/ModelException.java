package com.smartagent.model.exception;

import lombok.Getter;

@Getter
public class ModelException extends RuntimeException {

    private final String code;

    public ModelException(String code, String message) {
        super(message);
        this.code = code;
    }

    public ModelException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }
}
