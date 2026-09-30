package com.payment.processor.repository;

import com.payment.processor.entity.UserUpdatedEventInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserUpdatedEventInboxRepository extends JpaRepository<UserUpdatedEventInbox, UUID> {

    boolean existsById(UUID eventId);
    
}
