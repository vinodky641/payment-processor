package com.payment.processor.repository;

import com.payment.processor.entity.UserCreatedEventInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserCreatedEventInboxRepository extends JpaRepository<UserCreatedEventInbox, UUID> {
    
    boolean existsById(UUID eventId);

}
