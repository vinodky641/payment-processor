package com.payment.processor.event;

import com.payment.processor.enums.Role;
import com.payment.processor.enums.UserStatus;

import java.time.Instant;
import java.util.UUID;

public record UserUpdatedEvent(
        UUID eventId,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        String phoneNumber,
        String displayName,
        UserStatus status,
        boolean emailVerified,
        Instant createdAt,
        Instant updatedAt,
        Role role,
        long sourceVersion
) {
}
