package com.payment.processor.repository.impl;

import com.payment.processor.dto.PaymentOutcomeResponse;
import com.payment.processor.entity.PaymentOutcome;
import com.payment.processor.enums.PaymentStatus;
import com.payment.processor.repository.PaymentOutcomeCriteriaRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Repository
@RequiredArgsConstructor
public class PaymentOutcomeCriteriaRepositoryImpl implements PaymentOutcomeCriteriaRepository {

    private final EntityManager entityManager;

    @Override
    public Page<PaymentOutcomeResponse> searchPaymentOutcomes(PaymentStatus status, String accountId, int page, int size) {

        int offset = Math.multiplyExact(page, size);
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        // 1. DTO projection query: selects only required columns.
        CriteriaQuery<PaymentOutcomeResponse> query = cb.createQuery(PaymentOutcomeResponse.class);
        Root<PaymentOutcome> root = query.from(PaymentOutcome.class);

        query.select(cb.construct(
                PaymentOutcomeResponse.class,
                root.get(PAYMENT_ID_FIELD),
                root.get(DEBIT_ACCOUNT_ID_FIELD),
                root.get(CREDIT_ACCOUNT_ID_FIELD),
                root.get(DEBIT_AMOUNT_FIELD),
                root.get(DEBIT_CURRENCY_FIELD),
                root.get(CREDIT_AMOUNT_FIELD),
                root.get(CREDIT_CURRENCY_FIELD),
                root.get(FX_RATE_FIELD),
                root.get(FX_RATE_DATE_FIELD),
                root.get(GBP_EQUIVALENT_FIELD),
                root.get(STATUS_FIELD),
                root.get(REASON_FIELD),
                root.get(PROCESSED_AT_FIELD),
                root.get(PROCESSING_TIME_MS_FIELD)
        ));

        List<Predicate> predicates = buildPredicates(cb, root, status, accountId);
        query.where(predicates.toArray(new Predicate[0]));
        query.orderBy(cb.desc(root.get(PROCESSED_AT_FIELD)), cb.desc(root.get(PAYMENT_ID_FIELD)));
        TypedQuery<PaymentOutcomeResponse> typedQuery = entityManager.createQuery(query);
        typedQuery.setFirstResult(offset);
        typedQuery.setMaxResults(size);
        List<PaymentOutcomeResponse> content = typedQuery.getResultList();

        // 2. Count query: uses the same optional filters.
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<PaymentOutcome> countRoot = countQuery.from(PaymentOutcome.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, status, accountId);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        PageRequest pageRequest = PageRequest.of(
                page,
                size,
                Sort.by(
                        Sort.Order.desc(PROCESSED_AT_FIELD),
                        Sort.Order.desc(PAYMENT_ID_FIELD)
                )
        );

        return new PageImpl<>(content, pageRequest, totalElements);
    }

    private List<Predicate> buildPredicates(
            CriteriaBuilder cb,
            Root<PaymentOutcome> root,
            PaymentStatus status,
            String accountId
    ) {
        List<Predicate> predicates = new ArrayList<>();
        if (status != null) {
            predicates.add(cb.equal(root.get(STATUS_FIELD), status));
        }
        if (accountId != null && !accountId.isBlank()) {
            predicates.add(
                    cb.or(
                            cb.equal(
                                    root.get(DEBIT_ACCOUNT_ID_FIELD),
                                    accountId
                            ),
                            cb.equal(
                                    root.get(CREDIT_ACCOUNT_ID_FIELD),
                                    accountId
                            )
                    )
            );
        }
        return predicates;
    }

}
