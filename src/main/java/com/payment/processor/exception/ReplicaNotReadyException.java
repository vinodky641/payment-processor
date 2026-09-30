package com.payment.processor.exception;

import lombok.Getter;

@Getter
public class ReplicaNotReadyException extends RuntimeException {

    private final String replicaName;

    public ReplicaNotReadyException(String replicaName, String message) {
        super(message);
        this.replicaName = replicaName;
    }
    
}
