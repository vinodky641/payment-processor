package com.payment.processor.exception;

import lombok.Getter;

@Getter
public class AppIllegalArgumentException extends IllegalArgumentException {

    private final String field;

    public AppIllegalArgumentException(String field, String message) {
        super(message);
        this.field = field;
    }

}
