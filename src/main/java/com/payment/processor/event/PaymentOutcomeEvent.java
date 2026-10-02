package com.payment.processor.event;

import com.payment.processor.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record PaymentOutcomeEvent(
        UUID eventId,
        String paymentId,
        String debitAccountId,
        String creditAccountId,
        BigDecimal debitAmount,
        String debitCurrency,
        BigDecimal creditAmount,
        String creditCurrency,
        BigDecimal fxRate,
        LocalDate fxRateDate,
        BigDecimal gbpEquivalent,
        PaymentStatus status,
        String reason,
        Instant processedAt,
        Long processingTimeMs
) {
}
