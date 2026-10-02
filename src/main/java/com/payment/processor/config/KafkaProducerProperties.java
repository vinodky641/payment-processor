package com.payment.processor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka.producer")
public class KafkaProducerProperties {

    private String acks;

    private boolean enableIdempotence;

    private int retries;

    private int maxInFlightRequestsPerConnection;

    private String compressionType;

    private int lingerMs;

    private int batchSize;

    private int requestTimeoutMs;

    private int deliveryTimeoutMs;

    private boolean addTypeInfoHeaders;

}

