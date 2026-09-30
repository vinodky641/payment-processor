package com.payment.processor.listener;

import com.payment.processor.event.UserCreatedEvent;
import com.payment.processor.service.UserCreatedEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCreatedEventConsumer {

    private static final String TOPIC = "users.created";

    private final UserCreatedEventHandler eventHandler;

    @KafkaListener(
            topics = TOPIC,
            groupId = "${app.kafka.consumer.user-group-id}",
            containerFactory = "userCreatedKafkaListenerContainerFactory"
    )
    public void consume(UserCreatedEvent event) {

        log.debug("Received UserCreatedEvent. eventId={}, userId={}, sourceVersion={}",
                event.eventId(),
                event.userId(),
                event.sourceVersion()
        );

        eventHandler.handle(event);
    }
    
}
