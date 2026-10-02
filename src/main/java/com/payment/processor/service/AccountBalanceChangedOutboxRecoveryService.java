package com.payment.processor.service;

import com.payment.processor.config.AccountBalanceChangedOutboxProperties;
import com.payment.processor.repository.AccountBalanceChangedOutboxRepository;
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
public class AccountBalanceChangedOutboxRecoveryService {

    private final AccountBalanceChangedOutboxRepository repository;
    private final AccountBalanceChangedOutboxProperties properties;
    private final MeterRegistry meterRegistry;

    @Scheduled(fixedDelayString = TIME_INTERVAL_FOR_RUNNING_STALE_ACCOUNT_BALANCE_CHANGED_RECOVERY)
    @Transactional
    public void recoverStaleEvents() {

        Instant staleBefore = Instant.now().minus(
                properties.getRecovery().getStaleAfterMinutes(),
                ChronoUnit.MINUTES
        );
        int recovered = repository.resetStaleEvents(staleBefore);

        if (recovered > 0) {
            log.warn("Recovered {} stale AccountBalanceChangedOutbox events. staleBefore={}",
                    recovered,
                    staleBefore
            );

            meterRegistry.counter(
                    ACCOUNT_BALANCE_CHANGED_OUTBOX_EVENTS_METRIC_RECOVERED,
                    METRICS_TYPE,
                    EVENT_TYPE_ACCOUNT_BALANCE_CHANGED
            ).increment(recovered);
        }
    }

}
