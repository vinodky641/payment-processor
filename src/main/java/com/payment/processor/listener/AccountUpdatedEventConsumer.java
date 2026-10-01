package com.payment.processor.listener;

import com.payment.processor.event.AccountUpdatedEvent;
import com.payment.processor.service.AccountUpdatedEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountUpdatedEventConsumer {

    private static final String TOPIC = "accounts.updated";
    private final AccountUpdatedEventHandler handler;

    @KafkaListener(
            topics = TOPIC,
            groupId = "${app.kafka.consumer.account-group-id}",
            containerFactory = "accountUpdatedKafkaListenerContainerFactory"
    )
    public void consume(AccountUpdatedEvent event) {

        log.debug("Received AccountUpdatedEvent. eventId={}, accountId={}, userId={}, sourceVersion={}",
                event.eventId(),
                event.accountId(),
                event.userId(),
                event.sourceVersion()
        );
        handler.handle(event);
    }

}
