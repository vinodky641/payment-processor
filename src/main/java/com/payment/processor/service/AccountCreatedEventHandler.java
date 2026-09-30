package com.payment.processor.service;

import com.payment.processor.entity.Account;
import com.payment.processor.entity.AccountCreatedEventInbox;
import com.payment.processor.repository.AccountCreatedEventInboxRepository;
import com.payment.processor.event.AccountCreatedEvent;
import com.payment.processor.exception.ReplicaNotReadyException;
import com.payment.processor.repository.AccountRepository;
import com.payment.processor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCreatedEventHandler {

    private final AccountCreatedEventInboxRepository inboxRepository;
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Transactional
    public void handle(AccountCreatedEvent event) {

        validate(event);
        // Idempotency check
        if (inboxRepository.existsById(event.eventId())) {
            log.debug("Ignoring duplicate AccountCreatedEvent. eventId={}, accountId={}",
                    event.eventId(),
                    event.accountId()
            );
            return;
        }

        // User replica must already exist
        userRepository.findById(event.userId())
                .orElseThrow(() -> new ReplicaNotReadyException(
                                REPLICA_NAME_USER,
                                USER_REPLICA_IS_NOT_AVAILABLE + event.userId()
                        )
                );

        // Check whether Account replica already exists
        Account existingAccount = accountRepository.findByAccountId(event.accountId()).orElse(null);

        if (existingAccount != null) {
            if (!existingAccount.getUserId().equals(event.userId())) {
                throw new IllegalStateException(ACCOUNT_OWNER_MISMATCH_FOR_ACCOUNT_ID + event.accountId());
            }
            log.warn("Account replica already exists for AccountCreatedEvent. eventId={}, accountId={}, userId={}",
                    event.eventId(),
                    event.accountId(),
                    event.userId()
            );
            inboxRepository.save(AccountCreatedEventInbox.processed(
                            event.eventId(),
                            event.accountId(),
                            event.userId(),
                            EVENT_TYPE_ACCOUNT_CREATED,
                            event.sourceVersion()
                    )
            );
            log.info("AccountCreatedEvent marked processed. eventId={}, accountId={}, userId={}",
                    event.eventId(),
                    event.accountId(),
                    event.userId()
            );
            return;
        }

        // Create Account replica
        Account account = Account.builder()
                .accountId(event.accountId())
                .userId(event.userId())
                .accountName(event.accountName())
                .accountType(event.accountType())
                .accountBalance(event.accountBalance())
                .status(event.status())
                .currency(event.currency())
                .openedDate(event.openedDate())
                .sourceVersion(event.sourceVersion())
                .build();

        accountRepository.save(account);

        // Record successful processing
        AccountCreatedEventInbox inbox = AccountCreatedEventInbox.processed(
                event.eventId(),
                event.accountId(),
                event.userId(),
                EVENT_TYPE_ACCOUNT_CREATED,
                event.sourceVersion()
        );

        inboxRepository.save(inbox);
        log.info("Processed AccountCreatedEvent. eventId={}, accountId={}, userId={}, sourceVersion={}",
                event.eventId(),
                event.accountId(),
                event.userId(),
                event.sourceVersion()
        );
    }

    private void validate(AccountCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("AccountCreatedEvent must not be null");
        }

        if (event.eventId() == null) {
            throw new IllegalArgumentException("eventId must not be null");
        }

        if (event.accountId() == null || event.accountId().isBlank()) {
            throw new IllegalArgumentException("accountId must not be blank");
        }

        if (event.userId() == null) {
            throw new IllegalArgumentException("userId must not be null");
        }

        if (event.accountName() == null || event.accountName().isBlank()) {
            throw new IllegalArgumentException("accountName must not be blank");
        }

        if (event.accountType() == null) {
            throw new IllegalArgumentException("accountType must not be null");
        }

        if (event.accountBalance() == null) {
            throw new IllegalArgumentException("accountBalance must not be null");
        }

        if (event.accountBalance().signum() < 0) {
            throw new IllegalArgumentException("accountBalance must not be negative");
        }

        if (event.status() == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        if (event.currency() == null || event.currency().isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }

        if (event.currency().length() != 3) {
            throw new IllegalArgumentException("currency must be exactly 3 characters");
        }

        if (event.openedDate() == null) {
            throw new IllegalArgumentException("openedDate must not be null");
        }

        if (event.sourceVersion() < 0) {
            throw new IllegalArgumentException("sourceVersion must not be negative");
        }
    }

}
