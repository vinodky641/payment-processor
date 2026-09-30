package com.payment.processor.service;

import com.payment.processor.entity.User;
import com.payment.processor.entity.UserCreatedEventInbox;
import com.payment.processor.event.UserCreatedEvent;
import com.payment.processor.repository.UserCreatedEventInboxRepository;
import com.payment.processor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.payment.processor.constant.PaymentProcessorConstants.EVENT_TYPE_USER_CREATED;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCreatedEventHandler {

    private final UserCreatedEventInboxRepository inboxRepository;
    private final UserRepository userRepository;

    @Transactional
    public void handle(UserCreatedEvent event) {

        validate(event);
        // Idempotency check
        if (inboxRepository.existsById(event.eventId())) {
            log.debug("Ignoring duplicate UserCreatedEvent. eventId={}, userId={}",
                    event.eventId(),
                    event.userId()
            );
            return;
        }

        User existingUser = userRepository.findById(event.userId()).orElse(null);
        if (existingUser != null) {
            log.warn("User replica already exists for UserCreatedEvent. eventId={}, userId={}",
                    event.eventId(),
                    event.userId()
            );

            // verifying email here for data consistency
            if (event.email().equals(existingUser.getEmail())) {
                inboxRepository.save(UserCreatedEventInbox.processed(
                                event.eventId(),
                                event.userId(),
                                EVENT_TYPE_USER_CREATED
                        )
                );
                log.info("UserCreatedEvent marked processed. eventId={}, userId={}",
                        event.eventId(),
                        event.userId()
                );
            }
            return;
        }

        User user = User.builder()
                .id(event.userId())
                .email(event.email())
                .firstName(event.firstName())
                .lastName(event.lastName())
                .phoneNumber(event.phoneNumber())
                .displayName(event.displayName())
                .status(event.status())
                .emailVerified(event.emailVerified())
                .createdAt(event.createdAt())
                .updatedAt(event.updatedAt())
                .role(event.role())
                .sourceVersion(event.sourceVersion())
                .build();

        userRepository.save(user);
        UserCreatedEventInbox inbox = UserCreatedEventInbox.processed(
                event.eventId(),
                event.userId(),
                EVENT_TYPE_USER_CREATED
        );

        inboxRepository.save(inbox);
        log.info("Processed UserCreatedEvent. eventId={}, userId={}, sourceVersion={}",
                event.eventId(),
                event.userId(),
                event.sourceVersion()
        );
    }

    private void validate(UserCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("UserCreatedEvent must not be null");
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