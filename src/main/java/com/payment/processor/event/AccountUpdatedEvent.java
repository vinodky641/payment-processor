package com.payment.processor.event;

import com.payment.processor.model.AccountStatus;
import com.payment.processor.model.AccountType;

import java.time.Instant;
import java.util.UUID;

public record AccountUpdatedEvent(
        UUID eventId,
        String accountId,
        UUID userId,
        String accountName,
        AccountType accountType,
        AccountStatus status,
        Instant updatedAt,
        long sourceVersion
) {
}
