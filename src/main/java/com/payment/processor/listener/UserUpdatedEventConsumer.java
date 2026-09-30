package com.payment.processor.listener;

import com.payment.processor.event.UserUpdatedEvent;
import com.payment.processor.service.UserUpdatedEventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserUpdatedEventConsumer {

    private static final String TOPIC = "users.updated";

    private final UserUpdatedEventHandler eventHandler;

    @KafkaListener(
            topics = TOPIC,
            groupId = "${app.kafka.consumer.user-group-id}",
            containerFactory = "userUpdatedKafkaListenerContainerFactory"
    )
    public void consume(UserUpdatedEvent event) {

        log.debug("Received UserUpdatedEvent. eventId={}, userId={}, sourceVersion={}",
                event.eventId(),
                event.userId(),
                event.sourceVersion()
        );

        eventHandler.handle(event);
    }

}
