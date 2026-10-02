package com.payment.processor.config;

import lombok.AllArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

import static com.payment.processor.constant.PaymentProcessorConstants.RETENTION_MS;

@Configuration
@AllArgsConstructor
public class KafkaTopicConfig {

    private final AccountBalanceChangedTopicProperties accountBalanceChangedTopicProperties;
    private final PaymentOutcomeTopicProperties paymentOutcomeTopicProperties;
    private final PaymentSubmittedDltTopicProperties paymentSubmittedDltTopicProperties;

    @Bean
    public NewTopic accountBalanceChangedTopic() {
        return TopicBuilder
                .name(accountBalanceChangedTopicProperties.getName())
                .partitions(accountBalanceChangedTopicProperties.getPartitions())
                .replicas(accountBalanceChangedTopicProperties.getReplicationFactor())
                .config(RETENTION_MS, accountBalanceChangedTopicProperties.getRetentionMs())
                .build();
    }

    @Bean
    NewTopic paymentOutcomeTopic() {
        return TopicBuilder
                .name(paymentOutcomeTopicProperties.getName())
                .partitions(paymentOutcomeTopicProperties.getPartitions())
                .replicas(paymentOutcomeTopicProperties.getReplicationFactor())
                .config(RETENTION_MS, paymentOutcomeTopicProperties.getRetentionMs())
                .build();
    }

    @Bean
    NewTopic paymentSubmittedDltTopic() {
        return TopicBuilder
                .name(paymentSubmittedDltTopicProperties.getName())
                .partitions(paymentSubmittedDltTopicProperties.getPartitions())
                .replicas(paymentSubmittedDltTopicProperties.getReplicationFactor())
                .config(RETENTION_MS, paymentSubmittedDltTopicProperties.getRetentionMs())
                .build();
    }

}