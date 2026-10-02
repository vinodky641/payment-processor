package com.payment.processor.publisher;

import com.payment.processor.config.AccountBalanceChangedOutboxProperties;
import com.payment.processor.config.AccountBalanceChangedTopicProperties;
import com.payment.processor.entity.AccountBalanceChangedOutbox;
import com.payment.processor.event.AccountBalanceChangedEvent;
import com.payment.processor.service.AccountBalanceChangedOutboxClaimService;
import com.payment.processor.service.AccountBalanceChangedOutboxStateService;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountBalanceChangedOutboxPublisher {

    private final AccountBalanceChangedOutboxClaimService claimService;
    private final AccountBalanceChangedOutboxStateService stateService;
    private final KafkaTemplate<String, AccountBalanceChangedEvent> kafkaTemplate;
    private final ObjectMapper mapper;
    private final AccountBalanceChangedOutboxProperties properties;
    private final AccountBalanceChangedTopicProperties accountBalanceChangedTopicProperties;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger inFlight = new AtomicInteger(0);

    private Timer publishTimer;

    @PostConstruct
    public void registerMetrics() {

        Gauge.builder(
                        ACCOUNT_BALANCE_CHANGED_OUTBOX_EVENTS_METRIC_INFLIGHT,
                        inFlight,
                        AtomicInteger::get
                )
                .description("Number of AccountBalanceChanged outbox events currently waiting for Kafka acknowledgement")
                .tag(METRICS_TYPE, EVENT_TYPE_ACCOUNT_BALANCE_CHANGED)
                .register(meterRegistry);

        publishTimer = Timer.builder(
                        ACCOUNT_BALANCE_CHANGED_OUTBOX_EVENTS_METRIC_KAFKA_PUBLISH_LATENCY
                )
                .description("Kafka publish latency for AccountBalanceChanged outbox events")
                .tag(METRICS_TYPE, EVENT_TYPE_ACCOUNT_BALANCE_CHANGED)
                .register(meterRegistry);

    }

    @Scheduled(fixedDelayString = TIME_INTERVAL_FOR_RUNNING_PENDING_ACCOUNT_BALANCE_CHANGED_PUBLISH)
    public void publishPendingEvents() {

        int currentInFlight = inFlight.get();
        int availableCapacity = properties.getMaxInFlight() - currentInFlight;
        if (availableCapacity <= 0) {
            log.debug(
                    "AccountBalanceChanged publisher reached max in-flight capacity. inFlight={}, maxInFlight={}",
                    currentInFlight,
                    properties.getMaxInFlight()
            );
            return;
        }

        int batchSize = Math.min(properties.getBatchSize(), availableCapacity);
        List<AccountBalanceChangedOutbox> accountBalanceChangedOutboxList = claimService.claimBatch(batchSize);
        if (accountBalanceChangedOutboxList.isEmpty()) {
            return;
        }
        meterRegistry.counter(
                ACCOUNT_BALANCE_CHANGED_OUTBOX_EVENTS_METRIC_CLAIMED,
                METRICS_TYPE,
                EVENT_TYPE_ACCOUNT_BALANCE_CHANGED
        ).increment(accountBalanceChangedOutboxList.size());

        for (AccountBalanceChangedOutbox accountBalanceChangedOutbox : accountBalanceChangedOutboxList) {
            publish(accountBalanceChangedOutbox);
        }
    }

    private void publish(AccountBalanceChangedOutbox accountBalanceChangedOutbox) {

        final AccountBalanceChangedEvent accountBalanceChangedEvent;
        try {
            accountBalanceChangedEvent = mapper.readValue(accountBalanceChangedOutbox.getPayload(), AccountBalanceChangedEvent.class);
        } catch (JacksonException ex) {
            log.error(
                    "Failed to deserialize AccountBalanceChangedEvent. eventId={}, accountId={}",
                    accountBalanceChangedOutbox.getEventId(),
                    accountBalanceChangedOutbox.getAccountId(),
                    ex
            );
            stateService.resetToPending(accountBalanceChangedOutbox.getEventId());
            return;
        }

        Timer.Sample timer = Timer.start(meterRegistry);
        inFlight.incrementAndGet();
        try {
            kafkaTemplate.send(
                    accountBalanceChangedTopicProperties.getName(),
                    accountBalanceChangedEvent.accountId(),
                    accountBalanceChangedEvent
            ).whenComplete(
                    (result, ex) -> handleResult(accountBalanceChangedOutbox, timer, result, ex)
            );
        } catch (Exception ex) {
            log.error(
                    "Failure while submitting AccountBalanceChangedEvent to Kafka. eventId={}, accountId={}",
                    accountBalanceChangedOutbox.getEventId(),
                    accountBalanceChangedOutbox.getAccountId(),
                    ex
            );
            stateService.resetToPending(accountBalanceChangedOutbox.getEventId());
            inFlight.decrementAndGet();
            timer.stop(publishTimer);
        }
    }

    private void handleResult(
            AccountBalanceChangedOutbox accountBalanceChangedOutbox,
            Timer.Sample timer,
            SendResult<String, AccountBalanceChangedEvent> result,
            Throwable ex
    ) {
        try {
            if (ex == null) {
                log.debug(
                        "AccountBalanceChangedEvent published successfully. eventId={}, accountId={}, topic={}, partition={}, offset={}",
                        accountBalanceChangedOutbox.getEventId(),
                        accountBalanceChangedOutbox.getAccountId(),
                        accountBalanceChangedTopicProperties.getName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
                stateService.markPublished(accountBalanceChangedOutbox.getEventId());
                meterRegistry.counter(
                        ACCOUNT_BALANCE_CHANGED_OUTBOX_EVENTS_METRIC_PUBLISHED,
                        METRICS_TYPE,
                        EVENT_TYPE_ACCOUNT_BALANCE_CHANGED
                ).increment();
            } else {
                log.error("Failed to publish AccountBalanceChangedEvent. eventId={}, accountId={}",
                        accountBalanceChangedOutbox.getEventId(),
                        accountBalanceChangedOutbox.getAccountId(),
                        ex
                );
                stateService.resetToPending(accountBalanceChangedOutbox.getEventId());
                meterRegistry.counter(
                        ACCOUNT_BALANCE_CHANGED_OUTBOX_EVENTS_METRIC_FAILED,
                        METRICS_TYPE,
                        EVENT_TYPE_ACCOUNT_BALANCE_CHANGED
                ).increment();
            }

        } finally {

            timer.stop(publishTimer);
            inFlight.decrementAndGet();
        }
    }

}
