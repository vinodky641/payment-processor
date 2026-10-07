package com.payment.processor.repository;

import com.payment.processor.entity.PaymentOutcome;
import com.payment.processor.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentOutcomeRepository extends JpaRepository<PaymentOutcome, UUID> {

    long countByStatus(PaymentStatus s);

    @Query("select coalesce(sum(p.gbpEquivalent),0) from PaymentOutcome p where p.status='PROCESSED'")
    java.math.BigDecimal sumProcessedAmount();

    @Query(
            "select p " +
            "from PaymentOutcome p " +
            "where (:status is null or p.status=:status) and " +
            "(:accountId is null or p.debitAccountId=:accountId or p.creditAccountId=:accountId)"
    )
    Page<PaymentOutcome> search(@Param("status") PaymentStatus status, @Param("accountId") String accountId, Pageable pageable);

    List<PaymentOutcome> findByDebitAccountIdOrCreditAccountIdOrderByProcessedAtDesc(String debit, String credit);

    @Query("select min(p.processedAt), max(p.processedAt) from PaymentOutcome p")
    Object[] dateRange();
}
