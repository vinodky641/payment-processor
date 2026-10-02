package com.payment.processor.controller;

import com.payment.processor.dto.MetricsSummary;
import com.payment.processor.dto.PaymentOutcomeResponse;
import com.payment.processor.dto.ReportSummary;
import com.payment.processor.entity.PaymentOutcome;
import com.payment.processor.model.PaymentStatus;
import com.payment.processor.repository.PaymentOutcomeRepository;
import com.payment.processor.service.ProcessingMetrics;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReportController {

    private final PaymentOutcomeRepository paymentOutcomeRepository;
    private final ProcessingMetrics processingMetrics;

    @GetMapping("/metrics/summary")
    public MetricsSummary metrics() {

        return new MetricsSummary(
                processingMetrics.processed(),
                processingMetrics.held(),
                processingMetrics.rejected(),
                processingMetrics.averageProcessingTimeMs()
        );
    }

    @GetMapping("/reports/summary")
    public ReportSummary summary() {

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

    @GetMapping("/reports/activity")
    public Map<String, Object> activity(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<PaymentOutcome> paymentOutcomePage = paymentOutcomeRepository.search(
                status,
                accountId,
                PageRequest.of(
                        page,
                        size,
                        Sort.by(
                                Sort.Direction.DESC,
                                REPORT_ACTIVITY_SEARCH_SORT_BY_PROPERTY
                        )
                )
        );
        return Map.of(
                REPORT_ACTIVITY_PAGE, paymentOutcomePage.getNumber(),
                REPORT_ACTIVITY_SIZE, paymentOutcomePage.getSize(),
                REPORT_ACTIVITY_TOTAL_ELEMENTS, paymentOutcomePage.getTotalElements(),
                REPORT_ACTIVITY_CONTENT, paymentOutcomePage.getContent()
                        .stream()
                        .map(this::map)
                        .toList()
        );
    }

    @GetMapping("/accounts/{accountId}/history")
    public List<PaymentOutcomeResponse> history(@PathVariable String accountId) {
        return paymentOutcomeRepository.findByDebitAccountIdOrCreditAccountIdOrderByProcessedAtDesc(
                        accountId,
                        accountId
                ).stream()
                .map(this::map)
                .toList();
    }

    private PaymentOutcomeResponse map(PaymentOutcome paymentOutcome) {
        return new PaymentOutcomeResponse(
                paymentOutcome.getPaymentId(),
                paymentOutcome.getDebitAccountId(),
                paymentOutcome.getCreditAccountId(),
                paymentOutcome.getDebitAmount(),
                paymentOutcome.getDebitCurrency(),
                paymentOutcome.getCreditAmount(),
                paymentOutcome.getCreditCurrency(),
                paymentOutcome.getFxRate(),
                paymentOutcome.getFxRateDate(),
                paymentOutcome.getGbpEquivalent(),
                paymentOutcome.getStatus(),
                paymentOutcome.getReason(),
                paymentOutcome.getProcessedAt(),
                paymentOutcome.getProcessingTimeMs()
        );
    }

}

