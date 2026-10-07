package com.payment.processor.event;

import com.payment.processor.enums.AccountStatus;
import com.payment.processor.enums.AccountType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record AccountCreatedEvent(
        UUID eventId,
        String accountId,
        UUID userId,
        String accountName,
        AccountType accountType,
        BigDecimal accountBalance,
        AccountStatus status,
        String currency,
        LocalDate openedDate,
        long sourceVersion
) {
}

