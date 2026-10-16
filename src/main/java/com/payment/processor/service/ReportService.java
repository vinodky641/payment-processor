package com.payment.processor.service;

import com.payment.processor.dto.MetricsSummary;
import com.payment.processor.dto.PaymentOutcomePageResponse;
import com.payment.processor.dto.PaymentOutcomeResponse;
import com.payment.processor.dto.ReportSummary;
import com.payment.processor.enums.PaymentStatus;
import com.payment.processor.exception.AppIllegalArgumentException;
import com.payment.processor.repository.PaymentOutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Instant;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final PaymentOutcomeRepository paymentOutcomeRepository;
    private final ProcessingMetrics processingMetrics;

    public MetricsSummary getMetrics() {

        return new MetricsSummary(
                processingMetrics.processed(),
                processingMetrics.held(),
                processingMetrics.rejected(),
                processingMetrics.averageProcessingTimeMs()
        );
    }

    public ReportSummary getSummary() {

        Object[] dateRange = paymentOutcomeRepository.dateRange();
        return new ReportSummary(
                paymentOutcomeRepository.countByStatus(PaymentStatus.PROCESSED),
                paymentOutcomeRepository.countByStatus(PaymentStatus.HELD),
                paymentOutcomeRepository.countByStatus(PaymentStatus.REJECTED),
                paymentOutcomeRepository.sumProcessedAmount(),
                (Instant) dateRange[0],
                (Instant) dateRange[1]
        );
    }

    @Transactional(readOnly = true)
    public PaymentOutcomePageResponse getActivity(PaymentStatus status, String accountId, int page, int size) {

        if (page < 0) {
            throw new AppIllegalArgumentException(
                    PAGE_FIELD,
                    PAGE_INDEX_MUST_NOT_BE_NEGATIVE
            );
        }

        if (size < 1 || size > 100) {
            throw new AppIllegalArgumentException(
                    SIZE_FIELD,
                    PAGE_SIZE_MUST_BE_BETWEEN_1_AND_100
            );
        }

        Page<PaymentOutcomeResponse> result = paymentOutcomeRepository.searchPaymentOutcomes(
                status,
                accountId,
                page,
                size
        );

        return new PaymentOutcomePageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

    @Transactional(readOnly = true)
    public PaymentOutcomePageResponse getHistory(@PathVariable String accountId, int page, int size) {

        if (accountId == null || accountId.isBlank()) {
            throw new AppIllegalArgumentException(
                    ACCOUNT_ID_FIELD,
                    ACCOUNT_ID_MUST_NOT_BE_NULL_OR_BLANK);
        }

        if (page < 0) {
            throw new AppIllegalArgumentException(
                    PAGE_FIELD,
                    PAGE_INDEX_MUST_NOT_BE_NEGATIVE
            );
        }

        if (size < 1 || size > 100) {
            throw new AppIllegalArgumentException(
                    SIZE_FIELD,
                    PAGE_SIZE_MUST_BE_BETWEEN_1_AND_100
            );
        }

        Page<PaymentOutcomeResponse> result = paymentOutcomeRepository.searchPaymentOutcomes(
                null,
                accountId,
                page,
                size
        );

        return new PaymentOutcomePageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }

}
