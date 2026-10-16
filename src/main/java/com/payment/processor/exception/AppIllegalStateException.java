package com.payment.processor.exception;

import lombok.Getter;

@Getter
public class AppIllegalStateException extends IllegalArgumentException {

    private final String field;

    public AppIllegalStateException(String field, String message) {
        super(message);
        this.field = field;
    }

}
