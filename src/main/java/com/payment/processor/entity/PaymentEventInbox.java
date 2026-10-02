package com.payment.processor.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.PAYMENTS_INBOX_TABLE_NAME;

@Entity
@Table(name = PAYMENTS_INBOX_TABLE_NAME)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEventInbox {

    @Id
    private UUID eventId;

    @Column(nullable = false, length = 64)
    private String paymentId;

    @Column(nullable = false)
    private Instant receivedAt;

    public PaymentEventInbox(UUID eventId, String paymentId) {
        this.eventId = eventId;
        this.paymentId = paymentId;
        this.receivedAt = Instant.now();
    }
    
}