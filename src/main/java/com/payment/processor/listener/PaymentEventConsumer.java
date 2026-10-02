package com.payment.processor.listener;

import com.payment.processor.event.PaymentEvent;
import com.payment.processor.service.PaymentProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final PaymentProcessingService paymentProcessingService;

    @KafkaListener(
            topics = KAFKA_TOPIC_NAME_PAYMENTS_SUBMITTED,
            groupId = KAFKA_CONSUMER_GROUP_ID,
            containerFactory = KAFKA_LISTENER_CONTAINER_FACTORY
    )
    public void consume(ConsumerRecord<String, PaymentEvent> record) {

        PaymentEvent paymentEvent = record.value();
        log.debug("Received PaymentEvent. eventId={}, paymentId={}, initiatedByUserId={}",
                paymentEvent.eventId(),
                paymentEvent.paymentId(),
                paymentEvent.initiatedByUserId()
        );
        paymentProcessingService.process(paymentEvent.eventId(), paymentEvent);
    }

}
