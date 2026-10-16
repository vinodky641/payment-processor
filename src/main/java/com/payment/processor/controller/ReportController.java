package com.payment.processor.controller;

import com.payment.processor.dto.MetricsSummary;
import com.payment.processor.dto.PaymentOutcomePageResponse;
import com.payment.processor.dto.ReportSummary;
import com.payment.processor.enums.PaymentStatus;
import com.payment.processor.exception.ApiErrorResponse;
import com.payment.processor.repository.PaymentOutcomeRepository;
import com.payment.processor.service.ProcessingMetrics;
import com.payment.processor.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Tag(
        name = "Payment Reports",
        description = "Administrative APIs for payment summaries, activity reports, and payment history"
)
public class ReportController {

    private final PaymentOutcomeRepository paymentOutcomeRepository;
    private final ProcessingMetrics processingMetrics;
    private final ReportService reportService;

    @Operation(
            summary = "Get payment processing metrics",
            description = "Returns live in-memory metrics, including total processed, held, and rejected payments and average processing time."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Metrics retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MetricsSummary.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the JWT is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied; ADMIN role is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    // Live in-memory counters: totalProcessed, totalHeld, totalRejected, avgProcessingTimeMs. Updated in real time.
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/metrics/summary")
    public ResponseEntity<MetricsSummary> getMetrics() {
        MetricsSummary metricsSummary = reportService.getMetrics();
        return ResponseEntity.ok(metricsSummary);
    }

    @Operation(
            summary = "Get payment report summary",
            description = "Returns aggregated payment outcome statistics. Total counts by status, total amount processed, date range of records."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Report summary retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ReportSummary.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the JWT is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied; ADMIN role is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error while generating the report",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    // Aggregate query from payments_outcome: total counts by status, total amount processed, date range of records.
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/summary")
    public ResponseEntity<ReportSummary> getSummary() {
        ReportSummary reportSummary = reportService.getSummary();
        return ResponseEntity.ok(reportSummary);
    }

    @Operation(
            summary = "Get paginated payment activity",
            description = """
                    Returns a paginated list of payment outcomes.
                    Supports optional filtering by payment status and account ID.
                    The response includes pagination metadata: page, size, and totalElements.
                    Requires the ADMIN role.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment activity retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentOutcomePageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid pagination parameters or filter values",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the JWT is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied; ADMIN role is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error while retrieving payment activity",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    // Paginated list of payment outcomes. Supports ?status= filter and ?accountId= filter. Returns page, size, totalElements.
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/activity")
    public ResponseEntity<PaymentOutcomePageResponse> getActivity(
            @RequestParam(required = false) PaymentStatus status,
            @RequestParam(required = false) String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaymentOutcomePageResponse response = reportService.getActivity(
                status,
                accountId,
                page,
                size
        );
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Get payment history for an account",
            description = """
                    Returns payment outcomes where the specified account is either the debit account or the credit account.
                    Results are ordered from most recent to oldest. Requires the ADMIN role.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Payment history retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = PaymentOutcomePageResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid account ID or request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication is required or the JWT is invalid",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied; ADMIN role is required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error while retrieving payment history",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiErrorResponse.class)
                    )
            )
    })
    // All payment outcomes where debitAccountId or creditAccountId matches. Most recent first.
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/accounts/{accountId}/history")
    public ResponseEntity<PaymentOutcomePageResponse> getHistory(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PaymentOutcomePageResponse response = reportService.getHistory(accountId, page, size);
        return ResponseEntity.ok(response);
    }

}

