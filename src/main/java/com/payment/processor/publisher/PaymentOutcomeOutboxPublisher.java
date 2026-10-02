package com.payment.processor.publisher;

import com.payment.processor.config.PaymentOutcomeOutboxProperties;
import com.payment.processor.config.PaymentOutcomeTopicProperties;
import com.payment.processor.entity.PaymentOutcomeOutbox;
import com.payment.processor.event.PaymentOutcomeEvent;
import com.payment.processor.service.PaymentOutcomeOutboxClaimService;
import com.payment.processor.service.PaymentOutcomeOutboxStateService;
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
public class PaymentOutcomeOutboxPublisher {

    private final PaymentOutcomeOutboxClaimService claimService;
    private final PaymentOutcomeOutboxStateService stateService;
    private final KafkaTemplate<String, PaymentOutcomeEvent> kafkaTemplate;
    private final ObjectMapper mapper;
    private final PaymentOutcomeOutboxProperties properties;
    private final PaymentOutcomeTopicProperties paymentOutcomeTopicProperties;
    private final MeterRegistry meterRegistry;
    private final AtomicInteger inFlight = new AtomicInteger(0);

    private Timer publishTimer;

    @PostConstruct
    public void registerMetrics() {

        Gauge.builder(
                        PAYMENT_OUTCOME_OUTBOX_EVENTS_METRIC_INFLIGHT,
                        inFlight,
                        AtomicInteger::get
                )
                .description("Number of PaymentOutcome outbox events currently waiting for Kafka acknowledgement")
                .tag(METRICS_TYPE, EVENT_TYPE_PAYMENT_OUTCOME)
                .register(meterRegistry);

        publishTimer = Timer.builder(
                        PAYMENT_OUTCOME_OUTBOX_EVENTS_METRIC_KAFKA_PUBLISH_LATENCY
                )
                .description("Kafka publish latency for PaymentOutcome outbox events")
                .tag(METRICS_TYPE, EVENT_TYPE_PAYMENT_OUTCOME)
                .register(meterRegistry);

    }

    @Scheduled(fixedDelayString = TIME_INTERVAL_FOR_RUNNING_PENDING_PAYMENT_OUTCOME_PUBLISH)
    public void publishPendingEvents() {

        int currentInFlight = inFlight.get();
        int availableCapacity = properties.getMaxInFlight() - currentInFlight;
        if (availableCapacity <= 0) {
            log.debug(
                    "PaymentOutcome publisher reached max in-flight capacity. inFlight={}, maxInFlight={}",
                    currentInFlight,
                    properties.getMaxInFlight()
            );
            return;
        }

        int batchSize = Math.min(properties.getBatchSize(), availableCapacity);
        List<PaymentOutcomeOutbox> paymentOutcomeOutboxList = claimService.claimBatch(batchSize);
        if (paymentOutcomeOutboxList.isEmpty()) {
            return;
        }
        meterRegistry.counter(
                PAYMENT_OUTCOME_OUTBOX_EVENTS_METRIC_CLAIMED,
                METRICS_TYPE,
                EVENT_TYPE_PAYMENT_OUTCOME
        ).increment(paymentOutcomeOutboxList.size());

        for (PaymentOutcomeOutbox paymentOutcomeOutbox : paymentOutcomeOutboxList) {
            publish(paymentOutcomeOutbox);
        }
    }

    private void publish(PaymentOutcomeOutbox paymentOutcomeOutbox) {

        final PaymentOutcomeEvent paymentOutcomeEvent;
        try {
            paymentOutcomeEvent = mapper.readValue(paymentOutcomeOutbox.getPayload(), PaymentOutcomeEvent.class);
        } catch (JacksonException ex) {
            log.error(
                    "Failed to deserialize PaymentOutcomeEvent. eventId={}, paymentId={}",
                    paymentOutcomeOutbox.getEventId(),
                    paymentOutcomeOutbox.getPaymentId(),
                    ex
            );
            stateService.resetToPending(paymentOutcomeOutbox.getEventId());
            return;
        }

        Timer.Sample timer = Timer.start(meterRegistry);
        inFlight.incrementAndGet();
        try {
            kafkaTemplate.send(
                    paymentOutcomeTopicProperties.getName(),
                    paymentOutcomeOutbox.getPaymentId(),
                    paymentOutcomeEvent
            ).whenComplete(
                    (result, ex) -> handleResult(paymentOutcomeOutbox, timer, result, ex)
            );
        } catch (Exception ex) {
            log.error(
                    "Failure while submitting PaymentOutcomeEvent to Kafka. eventId={}, paymentId={}",
                    paymentOutcomeOutbox.getEventId(),
                    paymentOutcomeOutbox.getPaymentId(),
                    ex
            );
            stateService.resetToPending(paymentOutcomeOutbox.getEventId());
            inFlight.decrementAndGet();
            timer.stop(publishTimer);
        }
    }

    private void handleResult(
            PaymentOutcomeOutbox paymentOutcomeOutbox,
            Timer.Sample timer,
            SendResult<String, PaymentOutcomeEvent> result,
            Throwable ex
    ) {
        try {
            if (ex == null) {
                log.debug(
                        "PaymentOutcomeEvent published successfully. eventId={}, paymentId={}, topic={}, partition={}, offset={}",
                        paymentOutcomeOutbox.getEventId(),
                        paymentOutcomeOutbox.getPaymentId(),
                        paymentOutcomeTopicProperties.getName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset()
                );
                stateService.markPublished(paymentOutcomeOutbox.getEventId());
                meterRegistry.counter(
                        PAYMENT_OUTCOME_OUTBOX_EVENTS_METRIC_PUBLISHED,
                        METRICS_TYPE,
                        EVENT_TYPE_PAYMENT_OUTCOME
                ).increment();
            } else {
                log.error("Failed to publish PaymentOutcomeEvent. eventId={}, paymentId={}",
                        paymentOutcomeOutbox.getEventId(),
                        paymentOutcomeOutbox.getPaymentId(),
                        ex
                );
                stateService.resetToPending(paymentOutcomeOutbox.getEventId());
                meterRegistry.counter(
                        PAYMENT_OUTCOME_OUTBOX_EVENTS_METRIC_FAILED,
                        METRICS_TYPE,
                        EVENT_TYPE_PAYMENT_OUTCOME
                ).increment();
            }

        } finally {

            timer.stop(publishTimer);
            inFlight.decrementAndGet();
        }
    }

}
