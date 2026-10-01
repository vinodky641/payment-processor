package com.payment.processor.service;

import com.payment.processor.entity.Account;
import com.payment.processor.entity.AccountUpdatedEventInbox;
import com.payment.processor.event.AccountUpdatedEvent;
import com.payment.processor.exception.AccountReplicaConflictException;
import com.payment.processor.exception.ReplicaNotReadyException;
import com.payment.processor.repository.AccountRepository;
import com.payment.processor.repository.AccountUpdatedEventInboxRepository;
import com.payment.processor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountUpdatedEventHandler {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountUpdatedEventInboxRepository inboxRepository;

    @Transactional
    public void handle(AccountUpdatedEvent event) {

        validate(event);
        // Idempotency check
        if (inboxRepository.existsById(event.eventId())) {
            log.debug("Ignoring duplicate AccountUpdatedEvent. eventId={}, accountId={}",
                    event.eventId(),
                    event.accountId()
            );
            return;
        }

        // Check user exists or not
        verifyUserExists(event);

        // Check Account exists or not
        Account account = accountRepository.findByAccountId(event.accountId())
                .orElseThrow(() -> new ReplicaNotReadyException(
                                REPLICA_NAME_ACCOUNT,
                                ACCOUNT_REPLICA_IS_NOT_AVAILABLE + event.accountId()
                        )
                );

        // Check Account owner is same or not
        verifyAccountOwner(account, event);

        boolean updated = account.applyUpdateIfNewer(event);
        if (updated) {
            accountRepository.save(account);
            log.info("Account replica updated. accountId={}, sourceVersion={}",
                    event.accountId(),
                    event.sourceVersion()
            );
        } else {
            log.debug(
                    "Ignoring stale AccountUpdatedEvent. accountId={}, eventSourceVersion={}, currentSourceVersion={}",
                    event.accountId(),
                    event.sourceVersion(),
                    account.getSourceVersion()
            );
        }

        inboxRepository.save(AccountUpdatedEventInbox.processed(
                        event.eventId(),
                        event.accountId(),
                        event.userId(),
                        EVENT_TYPE_ACCOUNT_UPDATED,
                        event.sourceVersion()
                )
        );
    }

    private void verifyUserExists(AccountUpdatedEvent event) {
        if (!userRepository.existsById(event.userId())) {
            throw new ReplicaNotReadyException(
                    REPLICA_NAME_USER,
                    USER_REPLICA_IS_NOT_AVAILABLE + event.userId()
            );
        }
    }

    private void verifyAccountOwner(Account account, AccountUpdatedEvent event) {

        if (!account.getUserId().equals(event.userId())) {
            throw new AccountReplicaConflictException(
                    REPLICA_CONFLICT_FIELD_NAME_USER_ID,
                    ACCOUNT_OWNER_MISMATCH_FOR_ACCOUNT_ID + event.accountId()
            );
        }
    }

    private void validate(AccountUpdatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("AccountUpdatedEvent must not be null");
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

        if (event.status() == null) {
            throw new IllegalArgumentException("status must not be null");
        }

        if (event.updatedAt() == null) {
            throw new IllegalArgumentException("updatedAt must not be null");
        }

        if (event.sourceVersion() < 0) {
            throw new IllegalArgumentException("sourceVersion must not be negative");
        }
    }

}
