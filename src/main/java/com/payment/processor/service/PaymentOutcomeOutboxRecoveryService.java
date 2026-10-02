package com.payment.processor.service;

import com.payment.processor.config.PaymentOutcomeOutboxProperties;
import com.payment.processor.repository.PaymentOutcomeOutboxRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentOutcomeOutboxRecoveryService {

    private final PaymentOutcomeOutboxRepository repository;
    private final PaymentOutcomeOutboxProperties properties;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = TIME_INTERVAL_FOR_RUNNING_STALE_PAYMENT_OUTCOME_RECOVERY)
    @Transactional
    public void recoverStaleEvents() {

        Instant staleBefore = Instant.now().minus(
                properties.getRecovery().getStaleAfterMinutes(),
                ChronoUnit.MINUTES
        );
        int recovered = repository.resetStaleEvents(staleBefore);

        if (recovered > 0) {
            log.warn("Recovered {} stale PaymentOutcomeOutbox events. staleBefore={}",
                    recovered,
                    staleBefore
            );

            meterRegistry.counter(
                    PAYMENT_OUTCOME_OUTBOX_EVENTS_METRIC_RECOVERED,
                    METRICS_TYPE,
                    EVENT_TYPE_PAYMENT_OUTCOME
            ).increment(recovered);
        }
    }

}
