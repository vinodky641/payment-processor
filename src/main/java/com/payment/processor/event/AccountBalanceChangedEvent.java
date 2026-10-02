package com.payment.processor.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountBalanceChangedEvent(
        UUID eventId,
        String accountId,
        UUID userId,
        BigDecimal previousBalance,
        BigDecimal newBalance,
        String currency,
        String paymentId,
        Instant changedAt,
        long balanceVersion
) {
}
