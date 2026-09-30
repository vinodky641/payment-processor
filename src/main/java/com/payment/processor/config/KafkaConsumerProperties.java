package com.payment.processor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.kafka.consumer")
public class KafkaConsumerProperties {

    private String userGroupId;

    private String accountGroupId;

    private String balanceGroupId;

    private String paymentGroupId;

    private String autoOffsetReset = "earliest";

    private boolean enableAutoCommit = false;

    private int maxPollRecords = 100;

}
