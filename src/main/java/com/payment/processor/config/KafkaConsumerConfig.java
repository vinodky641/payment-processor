package com.payment.processor.config;

import com.payment.processor.event.*;
import lombok.AllArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.Map;

import static com.payment.processor.constant.PaymentProcessorConstants.TRUSTED_PACKAGE_OF_PAYMENT_EVENT;

@Configuration
@AllArgsConstructor
@EnableConfigurationProperties(KafkaConsumerProperties.class)
public class KafkaConsumerConfig {

    private final KafkaConsumerProperties properties;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ConsumerFactory<String, UserCreatedEvent> userCreatedConsumerFactory() {

        Map<String, Object> consumerProperties = new HashMap<>();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getUserGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());

        JacksonJsonDeserializer<UserCreatedEvent> valueDeserializer = new JacksonJsonDeserializer<>(
                UserCreatedEvent.class,
                false
        );

        valueDeserializer.addTrustedPackages(TRUSTED_PACKAGE_OF_PAYMENT_EVENT);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> userCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, UserCreatedEvent> userCreatedConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, UserCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(userCreatedConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, UserUpdatedEvent> userUpdatedConsumerFactory() {

        Map<String, Object> consumerProperties = new HashMap<>();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getUserGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());

        JacksonJsonDeserializer<UserUpdatedEvent> valueDeserializer = new JacksonJsonDeserializer<>(
                UserUpdatedEvent.class,
                false
        );

        valueDeserializer.addTrustedPackages(TRUSTED_PACKAGE_OF_PAYMENT_EVENT);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UserUpdatedEvent> userUpdatedKafkaListenerContainerFactory(
            ConsumerFactory<String, UserUpdatedEvent> userUpdatedConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, UserUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(userUpdatedConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, AccountCreatedEvent> accountCreatedConsumerFactory() {

        Map<String, Object> consumerProperties = new HashMap<>();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getAccountGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());

        JacksonJsonDeserializer<AccountCreatedEvent> valueDeserializer = new JacksonJsonDeserializer<>(
                AccountCreatedEvent.class,
                false
        );

        valueDeserializer.addTrustedPackages(TRUSTED_PACKAGE_OF_PAYMENT_EVENT);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountCreatedEvent> accountCreatedKafkaListenerContainerFactory(
            ConsumerFactory<String, AccountCreatedEvent> accountCreatedConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, AccountCreatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(accountCreatedConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, AccountUpdatedEvent> accountUpdatedConsumerFactory() {

        Map<String, Object> consumerProperties = new HashMap<>();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getAccountGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());

        JacksonJsonDeserializer<AccountUpdatedEvent> valueDeserializer = new JacksonJsonDeserializer<>(
                AccountUpdatedEvent.class,
                false
        );

        valueDeserializer.addTrustedPackages(TRUSTED_PACKAGE_OF_PAYMENT_EVENT);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, AccountUpdatedEvent> accountUpdatedKafkaListenerContainerFactory(
            ConsumerFactory<String, AccountUpdatedEvent> accountUpdatedConsumerFactory
    ) {

        ConcurrentKafkaListenerContainerFactory<String, AccountUpdatedEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(accountUpdatedConsumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, PaymentEvent> paymentConsumerFactory() {

        Map<String, Object> consumerProperties = new HashMap<>();

        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, properties.getPaymentGroupId());
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, properties.isEnableAutoCommit());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, properties.getAutoOffsetReset());
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
        consumerProperties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, properties.getMaxPollRecords());

        JacksonJsonDeserializer<PaymentEvent> valueDeserializer = new JacksonJsonDeserializer<>(
                PaymentEvent.class,
                false
        );

        valueDeserializer.addTrustedPackages(TRUSTED_PACKAGE_OF_PAYMENT_EVENT);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(),
                valueDeserializer
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> paymentKafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentEvent> paymentConsumerFactory,
            KafkaTemplate<String, PaymentEvent> template
    ) {

        ConcurrentKafkaListenerContainerFactory<String, PaymentEvent> factory = new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(paymentConsumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.RECORD);
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(
                template,
                (r, e) -> new TopicPartition(
                        r.topic() + ".DLT",
                        r.partition()
                )
        );
        factory.setCommonErrorHandler(
                new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L))
        );
        return factory;
    }

}

