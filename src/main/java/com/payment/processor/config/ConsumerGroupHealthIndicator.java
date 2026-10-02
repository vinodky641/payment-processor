package com.payment.processor.config;

import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.stereotype.Component;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Component(KAFKA_CONSUMER_GROUP)
public class ConsumerGroupHealthIndicator implements HealthIndicator {

    private final KafkaListenerEndpointRegistry registry;

    public ConsumerGroupHealthIndicator(KafkaListenerEndpointRegistry registry) {
        this.registry = registry;
    }

    @Override
    public Health health() {
        boolean running = registry.getListenerContainers()
                .stream()
                .anyMatch(c -> c.isRunning()
                );

        return Health.up()
                .withDetail(KAFKA_CONSUMER_GROUP, KAFKA_CONSUMER_GROUP_ID)
                .withDetail(KAFKA_CONSUMER_GROUP_HEALTH_INDICATOR_ACTIVE_KEY, running)
                .build();
    }
}
