package com.payment.processor.service;

import com.payment.processor.entity.AccountBalanceChangedOutbox;
import com.payment.processor.repository.AccountBalanceChangedOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBalanceChangedOutboxClaimService {

    private final AccountBalanceChangedOutboxRepository repository;

    @Transactional
    public List<AccountBalanceChangedOutbox> claimBatch(int batchSize) {

        if (batchSize <= 0) {
            log.info("Batch size must be greater than 0. Provided batchSize={}", batchSize);
            return List.of();
        }

        List<AccountBalanceChangedOutbox> events = repository.findPendingForUpdate(batchSize);
        if (events.isEmpty()) {
            log.debug("No pending account balance changed outbox events found to claim");
            return events;
        }

        events.forEach(AccountBalanceChangedOutbox::markProcessing);
        repository.saveAll(events);
        log.debug("Claimed {} account balance changed outbox events", events.size());
        return events;
    }

}
