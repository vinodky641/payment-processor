package com.payment.processor.service;

import com.payment.processor.entity.User;
import com.payment.processor.entity.UserUpdatedEventInbox;
import com.payment.processor.event.UserUpdatedEvent;
import com.payment.processor.exception.ReplicaNotReadyException;
import com.payment.processor.repository.UserRepository;
import com.payment.processor.repository.UserUpdatedEventInboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserUpdatedEventHandler {

    private final UserUpdatedEventInboxRepository inboxRepository;
    private final UserRepository userRepository;

    @Transactional
    public void handle(UserUpdatedEvent event) {

        validate(event);
        // Idempotency check
        if (inboxRepository.existsById(event.eventId())) {
            log.debug("Ignoring duplicate UserUpdatedEvent. eventId={}, userId={}",
                    event.eventId(),
                    event.userId()
            );
            return;
        }

        User user = userRepository.findById(event.userId())
                .orElseThrow(() -> new ReplicaNotReadyException(
                                REPLICA_NAME_USER,
                                USER_REPLICA_IS_NOT_AVAILABLE + event.userId()
                        )
                );

        boolean applied = user.applyUpdateIfNewer(
                event.email(),
                event.firstName(),
                event.lastName(),
                event.phoneNumber(),
                event.displayName(),
                event.status(),
                event.emailVerified(),
                event.role(),
                event.updatedAt(),
                event.sourceVersion()
        );

        inboxRepository.save(UserUpdatedEventInbox.processed(
                        event.eventId(),
                        event.userId(),
                        event.sourceVersion(),
                        EVENT_TYPE_USER_UPDATED
                )
        );

        if (applied) {
            log.info("Applied UserUpdatedEvent. eventId={}, userId={}, sourceVersion={}",
                    event.eventId(),
                    event.userId(),
                    event.sourceVersion()
            );
        } else {
            log.info("Ignored stale UserUpdatedEvent. eventId={}, userId={}, sourceVersion={}, currentSourceVersion={}",
                    event.eventId(),
                    event.userId(),
                    event.sourceVersion(),
                    user.getSourceVersion()
            );
        }
    }

    private void validate(UserUpdatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("UserUpdatedEvent must not be null");
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException("eventId must not be null");
        }

        if (event.userId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        if (event.email() == null || event.email().isBlank()) {
            throw new IllegalArgumentException("email must not be blank");
        }

        if (event.firstName() == null || event.firstName().isBlank()) {
            throw new IllegalArgumentException("firstName must not be blank");
        }

        if (event.lastName() == null || event.lastName().isBlank()) {
            throw new IllegalArgumentException("lastName must not be blank");
        }

        if (event.phoneNumber() == null || event.phoneNumber().isBlank()) {
            throw new IllegalArgumentException("phoneNumber must not be blank");
        }

        if (event.displayName() == null || event.displayName().isBlank()) {
            throw new IllegalArgumentException("displayName must not be blank");
        }

        if (event.status() == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        if (event.role() == null) {
            throw new IllegalArgumentException("role must not be null");
        }

        if (event.createdAt() == null) {
            throw new IllegalArgumentException("createdAt must not be null");
        }

        if (event.updatedAt() == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }

        if (event.updatedAt().isBefore(event.createdAt())) {
            throw new IllegalArgumentException("updatedAt must not be before createdAt");
        }

        if (event.sourceVersion() < 0) {
            throw new IllegalArgumentException("sourceVersion must not be negative");
        }
    }

}
