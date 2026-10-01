package com.payment.processor.repository;

import com.payment.processor.entity.AccountUpdatedEventInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountUpdatedEventInboxRepository extends JpaRepository<AccountUpdatedEventInbox, UUID> {

    boolean existsById(UUID eventId);

}
