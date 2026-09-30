package com.payment.processor.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.USERS_CREATED_EVENT_INBOX_TABLE_NAME;

@Entity
@Table(
        name = USERS_CREATED_EVENT_INBOX_TABLE_NAME,
        indexes = {
                @Index(
                        name = "idx_users_created_event_inbox_user_id",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_users_created_event_inbox_processed_at",
                        columnList = "processed_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCreatedEventInbox {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "user_id", nullable = false, updatable = false)
    private UUID userId;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    public static UserCreatedEventInbox processed(
            UUID eventId,
            UUID userId,
            String eventType
    ) {
        return UserCreatedEventInbox.builder()
                .eventId(eventId)
                .userId(userId)
                .eventType(eventType)
                .processedAt(Instant.now())
                .build();
    }

}
