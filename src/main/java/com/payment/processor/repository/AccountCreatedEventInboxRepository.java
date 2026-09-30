package com.payment.processor.repository;

import com.payment.processor.entity.AccountCreatedEventInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountCreatedEventInboxRepository extends JpaRepository<AccountCreatedEventInbox, UUID> {

    boolean existsById(UUID eventId);

}
