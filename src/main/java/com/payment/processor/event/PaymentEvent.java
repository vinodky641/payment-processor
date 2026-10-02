package com.payment.processor.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
        UUID eventId,
        String paymentId,
        String debitAccountId,
        String creditAccountId,
        BigDecimal amount,
        String currency,
        Instant createdAt,
        UUID initiatedByUserId
) {
}

