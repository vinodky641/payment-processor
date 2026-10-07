package com.payment.processor.entity;

import com.payment.processor.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.PAYMENTS_OUTCOME_TABLE_NAME;

@Entity
@Table(
        name = PAYMENTS_OUTCOME_TABLE_NAME,
        indexes = {
                @Index(
                        name = "idx_payments_outcome_status_processed_at",
                        columnList = "status,processed_at"
                ),
                @Index(
                        name = "idx_payments_outcome_debit_processed_at",
                        columnList = "debit_account_id,processed_at"
                ),
                @Index(
                        name = "idx_payments_outcome_credit_processed_at",
                        columnList = "credit_account_id,processed_at"
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentOutcome {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 64)
    private String paymentId;

    @Column(nullable = false, length = 64)
    private String debitAccountId;

    @Column(nullable = false, length = 64)
    private String creditAccountId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal debitAmount;

    @Column(nullable = false, length = 3)
    private String debitCurrency;

    @Column(precision = 19, scale = 4)
    private BigDecimal creditAmount;

    @Column(nullable = false, length = 3)
    private String creditCurrency;

    @Column(precision = 20, scale = 10)
    private BigDecimal fxRate;

    private LocalDate fxRateDate;

    @Setter
    @Column(precision = 19, scale = 4)
    private BigDecimal gbpEquivalent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private Instant processedAt;

    @Column(nullable = false)
    private Long processingTimeMs;


    public PaymentOutcome(
            String paymentId,
            String debitAccountId,
            String creditAccountId,
            BigDecimal debitAmount,
            String debitCurrency,
            BigDecimal creditAmount,
            String creditCurrency,
            BigDecimal fxRate,
            LocalDate fxRateDate,
            PaymentStatus status,
            String reason,
            long processingTimeMs) {

        this.id = UUID.randomUUID();
        this.paymentId = paymentId;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.debitAmount = debitAmount;
        this.debitCurrency = debitCurrency;
        this.creditAmount = creditAmount;
        this.creditCurrency = creditCurrency;
        this.fxRate = fxRate;
        this.fxRateDate = fxRateDate;
        this.gbpEquivalent = null;
        this.status = status;
        this.reason = reason;
        this.processedAt = Instant.now();
        this.processingTimeMs = processingTimeMs;
    }

}

