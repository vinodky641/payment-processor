package com.payment.processor.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Error response returned by the APIs")
public record ApiErrorResponse(

        @Schema(description = "Timestamp when the error occurred", example = "2026-10-07T12:30:45Z")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        Instant timestamp,

        @Schema(description = "HTTP status code", example = "400")
        int status,

        @Schema(description = "Short description of the error", example = "Validation failed")
        String error,

        @Schema(description = "Request path that caused the error", example = "/api/v1/accounts")
        String path,

        @Schema(description = "Detailed validation errors", nullable = true)
        List<Violation> violations
) {
}

