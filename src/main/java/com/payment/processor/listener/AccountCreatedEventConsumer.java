package com.payment.processor.listener;

import com.payment.processor.event.AccountCreatedEvent;
import com.payment.processor.service.AccountCreatedEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccountCreatedEventConsumer {

    private static final String TOPIC = "accounts.created";

    private final AccountCreatedEventHandler eventHandler;

    @KafkaListener(
            topics = TOPIC,
            groupId = "${app.kafka.consumer.account-group-id}",
            containerFactory = "accountCreatedKafkaListenerContainerFactory"
    )
    public void consume(AccountCreatedEvent event) {

        log.debug("Received AccountCreatedEvent. eventId={}, accountId={}, userId={}, sourceVersion={}",
                event.eventId(),
                event.accountId(),
                event.userId(),
                event.sourceVersion()
        );
        eventHandler.handle(event);
    }

}
