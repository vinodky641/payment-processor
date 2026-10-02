package com.payment.processor.service;

import com.payment.processor.entity.PaymentOutcomeOutbox;
import com.payment.processor.repository.PaymentOutcomeOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutcomeOutboxClaimService {

    private final PaymentOutcomeOutboxRepository repository;

    @Transactional
    public List<PaymentOutcomeOutbox> claimBatch(int batchSize) {

        if (batchSize <= 0) {
            log.info("Batch size must be greater than 0. Provided batchSize={}", batchSize);
            return List.of();
        }

        List<PaymentOutcomeOutbox> events = repository.findPendingForUpdate(batchSize);
        if (events.isEmpty()) {
            log.debug("No pending payment outcome outbox events found to claim");
            return events;
        }

        events.forEach(PaymentOutcomeOutbox::markProcessing);
        repository.saveAll(events);
        log.debug("Claimed {} payment outcome outbox events", events.size());
        return events;
    }

}
