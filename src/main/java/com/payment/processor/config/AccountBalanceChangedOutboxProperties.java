package com.payment.processor.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.outbox.account-balance-changed")
public class AccountBalanceChangedOutboxProperties {

    private int batchSize = 100;
    private int maxInFlight = 1000;
    private Recovery recovery = new Recovery();

    @Getter
    @Setter
    public static class Recovery {

        private long staleAfterMinutes = 5;
        private long fixedDelayMs = 60_000;
    }

}