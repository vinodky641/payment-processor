package com.payment.processor.entity;

import com.payment.processor.model.OutboxStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.ACCOUNTS_BALANCE_CHANGED_OUTBOX_TABLE_NAME;

@Entity
@Table(
        name = ACCOUNTS_BALANCE_CHANGED_OUTBOX_TABLE_NAME,
        indexes = {
                @Index(
                        name = "idx_abc_outbox_status",
                        columnList = "status"
                ),
                @Index(
                        name = "idx_abc_outbox_created_at",
                        columnList = "created_at"
                ),
                @Index(
                        name = "idx_abc_outbox_claimed_at",
                        columnList = "claimed_at"
                ),
                @Index(
                        name = "idx_abc_outbox_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "uk_abc_outbox_account_payment",
                        columnList = "account_id, payment_id",
                        unique = true
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountBalanceChangedOutbox {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "account_id", nullable = false, updatable = false, length = 50)
    private String accountId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "payment_id", nullable = false, updatable = false, length = 50)
    private String paymentId;

    @Column(name = "previous_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal previousBalance;

    @Column(name = "new_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal newBalance;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @Lob
    @Column(name = "payload", nullable = false)
    private String payload;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "balance_version", nullable = false)
    private long balanceVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OutboxStatus status;

    @Column(name = "claimed_at")
    private Instant claimedAt;

    @Column(name = "attempt_count", nullable = false)
    @Builder.Default
    private int attemptCount = 0;

    @Column(name = "published_at")
    private Instant publishedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }

    public void markProcessing() {
        this.status = OutboxStatus.PROCESSING;
        this.claimedAt = Instant.now();
        this.attemptCount++;
    }

}