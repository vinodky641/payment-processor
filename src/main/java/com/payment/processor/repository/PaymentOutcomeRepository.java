package com.payment.processor.repository;

import com.payment.processor.entity.PaymentOutcome;
import com.payment.processor.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PaymentOutcomeRepository extends JpaRepository<PaymentOutcome, UUID>, PaymentOutcomeCriteriaRepository {

    long countByStatus(PaymentStatus s);

    @Query("select coalesce(sum(p.gbpEquivalent),0) from PaymentOutcome p where p.status='PROCESSED'")
    java.math.BigDecimal sumProcessedAmount();

    @Query("select min(p.processedAt), max(p.processedAt) from PaymentOutcome p")
    Object[] dateRange();

}
