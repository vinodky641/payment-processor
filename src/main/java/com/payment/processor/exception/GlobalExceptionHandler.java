package com.payment.processor.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

import static com.payment.processor.constant.PaymentProcessorConstants.INTERNAL_SERVER_ERROR;
import static com.payment.processor.constant.PaymentProcessorConstants.VALIDATION_FAILED_ERROR;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // AppIllegalArgumentException exception handler
    @ExceptionHandler(AppIllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleAppIllegalArgumentException(
            AppIllegalArgumentException ex,
            HttpServletRequest request
    ) {
        List<Violation> violations = List.of(new Violation(
                ex.getField(),
                ex.getMessage()
        ));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        VALIDATION_FAILED_ERROR,
                        request.getRequestURI(),
                        violations
                ));
    }

    // AppIllegalStateException handler
    @ExceptionHandler(AppIllegalStateException.class)
    public ResponseEntity<ApiErrorResponse> handleAppIllegalStateException(
            AppIllegalStateException ex,
            HttpServletRequest request
    ) {
        List<Violation> violations = List.of(new Violation(
                ex.getField(),
                ex.getMessage()
        ));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        INTERNAL_SERVER_ERROR,
                        request.getRequestURI(),
                        violations
                ));
    }

    // General exception handler
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        List<Violation> violations = List.of(new Violation(
                "",
                ex.getMessage()
        ));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse(
                        Instant.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        INTERNAL_SERVER_ERROR,
                        request.getRequestURI(),
                        violations
                ));
    }

}
