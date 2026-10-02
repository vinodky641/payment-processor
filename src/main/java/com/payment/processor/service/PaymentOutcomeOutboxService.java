package com.payment.processor.service;

import com.payment.processor.entity.PaymentOutcome;
import com.payment.processor.entity.PaymentOutcomeOutbox;
import com.payment.processor.event.PaymentOutcomeEvent;
import com.payment.processor.model.OutboxStatus;
import com.payment.processor.repository.PaymentOutcomeOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.EVENT_TYPE_PAYMENT_OUTCOME;

@Service
@RequiredArgsConstructor
public class PaymentOutcomeOutboxService {

    private final PaymentOutcomeOutboxRepository repository;
    private final ObjectMapper objectMapper;

    public void createOutboxEvent(PaymentOutcome paymentOutcome) {

        UUID eventId = UUID.randomUUID();
        PaymentOutcomeEvent event = new PaymentOutcomeEvent(
                eventId,
                paymentOutcome.getPaymentId(),
                paymentOutcome.getDebitAccountId(),
                paymentOutcome.getCreditAccountId(),
                paymentOutcome.getDebitAmount(),
                paymentOutcome.getDebitCurrency(),
                paymentOutcome.getCreditAmount(),
                paymentOutcome.getCreditCurrency(),
                paymentOutcome.getFxRate(),
                paymentOutcome.getFxRateDate(),
                paymentOutcome.getGbpEquivalent(),
                paymentOutcome.getStatus(),
                paymentOutcome.getReason(),
                paymentOutcome.getProcessedAt(),
                paymentOutcome.getProcessingTimeMs()

        );

        String payload;
        try {
            payload = objectMapper.writeValueAsString(event);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Failed to serialize PaymentOutcomeEvent", ex);
        }

        PaymentOutcomeOutbox outbox = PaymentOutcomeOutbox.builder()
                .eventId(eventId)
                .paymentId(paymentOutcome.getPaymentId())
                .eventType(EVENT_TYPE_PAYMENT_OUTCOME)
                .payload(payload)
                .status(OutboxStatus.PENDING)
                .build();

        repository.save(outbox);
    }

}

