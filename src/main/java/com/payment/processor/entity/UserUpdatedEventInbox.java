package com.payment.processor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.USERS_UPDATED_EVENT_INBOX_TABLE_NAME;

@Entity
@Table(
        name = USERS_UPDATED_EVENT_INBOX_TABLE_NAME,
        indexes = {
                @Index(
                        name = "idx_users_updated_event_inbox_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_users_updated_event_inbox_processed_at",
                        columnList = "processed_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdatedEventInbox {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public static UserUpdatedEventInbox processed(
            UUID eventId,
            UUID userId,
            long sourceVersion,
            String eventType
    ) {
        return UserUpdatedEventInbox.builder()
                .eventId(eventId)
                .userId(userId)
                .sourceVersion(sourceVersion)
                .eventType(eventType)
                .processedAt(Instant.now())
                .build();
    }

}
