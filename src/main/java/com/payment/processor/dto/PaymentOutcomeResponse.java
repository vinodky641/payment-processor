package com.payment.processor.dto;

import com.payment.processor.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record PaymentOutcomeResponse(
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
