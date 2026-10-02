package com.payment.processor.dto;

public record MetricsSummary(
        long totalProcessed,
        long totalHeld,
        long totalRejected,
        double avgProcessingTimeMs
) {
}

