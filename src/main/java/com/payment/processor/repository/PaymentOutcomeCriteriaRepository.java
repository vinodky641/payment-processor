package com.payment.processor.repository;

import com.payment.processor.dto.PaymentOutcomeResponse;
import com.payment.processor.enums.PaymentStatus;
import org.springframework.data.domain.Page;

public interface PaymentOutcomeCriteriaRepository {

    Page<PaymentOutcomeResponse> searchPaymentOutcomes(PaymentStatus status, String accountId, int page, int size);

}