package com.payment.processor.exception;

public class FxRateUnavailableException extends RuntimeException {
    public FxRateUnavailableException(String m, Throwable c) {
        super(m, c);
    }
}