package com.payment.processor.service;

import com.payment.processor.repository.AccountBalanceChangedOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountBalanceChangedOutboxStateService {

    private final AccountBalanceChangedOutboxRepository repository;

    @Transactional
    public void markPublished(UUID eventId) {

        int updatedRows = repository.markPublishedIfProcessing(eventId, Instant.now());
        if (updatedRows == 0) {
            log.warn(
                    "AccountBalanceChangedOutbox was not marked PUBLISHED because it is no longer PROCESSING. eventId={}",
                    eventId
            );
        }
    }

    @Transactional
    public void resetToPending(UUID eventId) {

        int updatedRows = repository.resetToPendingIfProcessing(eventId);
        if (updatedRows == 0) {
            log.warn(
                    "AccountBalanceChangedOutbox was not reset to PENDING because it is no longer PROCESSING. eventId={}",
                    eventId
            );
        }
    }

}