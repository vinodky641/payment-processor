package com.payment.processor.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ReportSummary(
        long totalProcessed,
        long totalHeld,
        long totalRejected,
        BigDecimal totalAmountProcessedGbp,
        Instant firstRecordAt,
        Instant lastRecordAt
) {
}
