package com.payment.processor.exception;

import lombok.Getter;

@Getter
public class AccountReplicaConflictException extends RuntimeException {

    private final String field;

    public AccountReplicaConflictException(String field, String message) {
        super(message);
        this.field = field;
    }
    
}
