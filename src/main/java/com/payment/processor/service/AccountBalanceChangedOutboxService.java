package com.payment.processor.service;

import com.payment.processor.dto.BalanceChange;
import com.payment.processor.entity.Account;
import com.payment.processor.entity.AccountBalanceChangedOutbox;
import com.payment.processor.event.AccountBalanceChangedEvent;
import com.payment.processor.model.OutboxStatus;
import com.payment.processor.repository.AccountBalanceChangedOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountBalanceChangedOutboxService {

    private final AccountBalanceChangedOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public void createOutboxEvent(Account account, BalanceChange balanceChange, String paymentId) {

        UUID eventId = UUID.randomUUID();
        Instant changedAt = Instant.now();

        AccountBalanceChangedEvent event = new AccountBalanceChangedEvent(
                eventId,
                account.getAccountId(),
                account.getUserId(),
                balanceChange.previousBalance(),
                balanceChange.newBalance(),
                account.getCurrency(),
                paymentId,
                changedAt,
                balanceChange.balanceVersion()
        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize AccountBalanceChangedEvent", ex);
        }

        AccountBalanceChangedOutbox outbox = AccountBalanceChangedOutbox.builder()
                .eventId(eventId)
                .accountId(account.getAccountId())
                .userId(account.getUserId())
                .paymentId(paymentId)
                .previousBalance(balanceChange.previousBalance())
                .newBalance(balanceChange.newBalance())
                .currency(account.getCurrency())
                .changedAt(changedAt)
                .payload(payload)
                .createdAt(Instant.now())
                .balanceVersion(balanceChange.balanceVersion())
                .status(OutboxStatus.PENDING)
                .build();

        repository.save(outbox);
    }

}
