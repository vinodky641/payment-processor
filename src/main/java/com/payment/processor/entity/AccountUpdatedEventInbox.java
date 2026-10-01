package com.payment.processor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.ACCOUNTS_UPDATED_EVENT_INBOX_TABLE_NAME;

@Entity
@Table(
        name = ACCOUNTS_UPDATED_EVENT_INBOX_TABLE_NAME,
        indexes = {
                @Index(
                        name = "idx_accounts_updated_event_inbox_account_id",
                        columnList = "account_id"
                ),
                @Index(
                        name = "idx_accounts_updated_event_inbox_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_accounts_updated_event_inbox_processed_at",
                        columnList = "processed_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountUpdatedEventInbox {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "account_id", nullable = false, updatable = false, length = 50)
    private String accountId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public static AccountUpdatedEventInbox processed(
            UUID eventId,
            String accountId,
            UUID userId,
            String eventType,
            long sourceVersion
    ) {
        return AccountUpdatedEventInbox.builder()
                .eventId(eventId)
                .accountId(accountId)
                .userId(userId)
                .eventType(eventType)
                .sourceVersion(sourceVersion)
                .processedAt(Instant.now())
                .build();
    }

}
