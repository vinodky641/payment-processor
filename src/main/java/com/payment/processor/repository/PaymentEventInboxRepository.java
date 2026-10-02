package com.payment.processor.repository;

import com.payment.processor.entity.PaymentEventInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentEventInboxRepository extends JpaRepository<PaymentEventInbox, UUID> {

}
