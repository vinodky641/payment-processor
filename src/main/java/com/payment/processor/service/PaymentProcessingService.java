package com.payment.processor.service;

import com.payment.processor.dto.BalanceChange;
import com.payment.processor.dto.FxQuote;
import com.payment.processor.entity.Account;
import com.payment.processor.entity.PaymentEventInbox;
import com.payment.processor.entity.PaymentOutcome;
import com.payment.processor.event.PaymentEvent;
import com.payment.processor.exception.FxRateUnavailableException;
import com.payment.processor.enums.AccountStatus;
import com.payment.processor.enums.PaymentStatus;
import com.payment.processor.repository.AccountRepository;
import com.payment.processor.repository.PaymentEventInboxRepository;
import com.payment.processor.repository.PaymentOutcomeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.*;

@Service
@RequiredArgsConstructor
public class PaymentProcessingService {

    private static final BigDecimal HOLD_GBP = new BigDecimal(HOLD_GBP_THRESHOLD_AMOUNT);
    private final PaymentEventInboxRepository paymentEventInboxRepository;
    private final AccountRepository accountRepository;
    private final PaymentOutcomeRepository paymentOutcomeRepository;
    private final PaymentOutcomeOutboxService paymentOutcomeOutboxService;
    private final AccountBalanceChangedOutboxService accountBalanceChangedOutboxService;
    private final FxRateService fxRateService;
    private final ProcessingMetrics processingMetrics;

    @Transactional
    public void process(UUID eventId, PaymentEvent paymentEvent) {
        long start = System.nanoTime();
        try {
            try {
                paymentEventInboxRepository.saveAndFlush(
                        new PaymentEventInbox(eventId, paymentEvent.paymentId())
                );
            } catch (DataIntegrityViolationException duplicate) {
                return;
            }

            List<Account> locked = accountRepository.lockAccounts(
                    List.of(
                            paymentEvent.debitAccountId(),
                            paymentEvent.creditAccountId()
                    )
            );

            if (locked.size() != 2) {
                persistOutcome(
                        paymentEvent,
                        null,
                        null,
                        null,
                        PaymentStatus.REJECTED,
                        ACCOUNT_NO_LONGER_EXISTS,
                        start,
                        null
                );
                return;
            }

            Account debitAccount = locked.stream()
                    .filter(a -> a.getAccountId().equals(paymentEvent.debitAccountId()))
                    .findFirst()
                    .orElseThrow();

            Account creditAccount = locked.stream()
                    .filter(a -> a.getAccountId().equals(paymentEvent.creditAccountId()))
                    .findFirst().orElseThrow();

            if (debitAccount.getStatus() != AccountStatus.ACTIVE) {
                persistOutcome(
                        paymentEvent,
                        null,
                        creditAccount.getCurrency(),
                        null,
                        PaymentStatus.REJECTED,
                        DEBIT_ACCOUNT_IS_NOT_ACTIVE,
                        start,
                        null
                );
                return;
            }

            if (creditAccount.getStatus() != AccountStatus.ACTIVE) {
                persistOutcome(
                        paymentEvent,
                        null,
                        creditAccount.getCurrency(),
                        null,
                        PaymentStatus.REJECTED,
                        CREDIT_ACCOUNT_IS_NOT_ACTIVE,
                        start,
                        null
                );
                return;
            }

            FxQuote gbpRate;
            try {
                gbpRate = fxRateService.rate(paymentEvent.currency(), CURRENCY_GBP);
            } catch (FxRateUnavailableException ex) {
                persistOutcome(
                        paymentEvent,
                        null,
                        creditAccount.getCurrency(),
                        null,
                        PaymentStatus.REJECTED,
                        ex.getMessage(),
                        start,
                        null
                );
                return;
            }

            BigDecimal gbp = paymentEvent.amount()
                    .multiply(gbpRate.rate())
                    .setScale(2, RoundingMode.HALF_UP);

            if (gbp.compareTo(HOLD_GBP) > 0) {
                persistOutcome(
                        paymentEvent,
                        null,
                        creditAccount.getCurrency(),
                        null,
                        PaymentStatus.HELD,
                        PAYMENT_EXCEEDS_GBP_250_000_THRESHOLD,
                        start,
                        gbp
                );
                return;
            }

            if (!debitAccount.getCurrency().equalsIgnoreCase(paymentEvent.currency())) {
                persistOutcome(
                        paymentEvent,
                        null,
                        creditAccount.getCurrency(),
                        null,
                        PaymentStatus.REJECTED,
                        PAYMENT_CURRENCY_MUST_MATCH_DEBIT_ACCOUNT_CURRENCY,
                        start,
                        null
                );
                return;
            }

            FxQuote creditRate;
            try {
                creditRate = fxRateService.rate(
                        debitAccount.getCurrency(),
                        creditAccount.getCurrency()
                );
            } catch (FxRateUnavailableException ex) {
                persistOutcome(
                        paymentEvent,
                        null,
                        creditAccount.getCurrency(),
                        null,
                        PaymentStatus.REJECTED,
                        ex.getMessage(),
                        start,
                        null
                );
                return;
            }

            BigDecimal creditAmount = paymentEvent.amount()
                    .multiply(creditRate.rate())
                    .setScale(2, RoundingMode.HALF_UP);

            if (debitAccount.getAccountBalance().compareTo(paymentEvent.amount()) < 0) {
                persistOutcome(
                        paymentEvent,
                        creditAmount,
                        creditAccount.getCurrency(),
                        creditRate,
                        PaymentStatus.REJECTED,
                        INSUFFICIENT_FUNDS,
                        start,
                        null
                );
                return;
            }

            BalanceChange debitChange = debitAccount.applyDebitBalanceChange(paymentEvent.amount());
            BalanceChange creditChange = creditAccount.applyCreditBalanceChange(creditAmount);

            accountRepository.save(debitAccount);
            accountRepository.save(creditAccount);

            accountBalanceChangedOutboxService.createOutboxEvent(
                    debitAccount,
                    debitChange,
                    paymentEvent.paymentId()
            );

            accountBalanceChangedOutboxService.createOutboxEvent(
                    creditAccount,
                    creditChange,
                    paymentEvent.paymentId()
            );

            persistOutcome(
                    paymentEvent,
                    creditAmount,
                    creditAccount.getCurrency(),
                    creditRate,
                    PaymentStatus.PROCESSED,
                    PAYMENT_PROCESSED,
                    start,
                    gbp
            );
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private void persistOutcome(
            PaymentEvent paymentEvent,
            BigDecimal creditAmount,
            String creditCurrency,
            FxQuote rate,
            PaymentStatus status,
            String reason,
            long start,
            BigDecimal gbpEquivalent
    ) {

        try {
            long processingTimeMs = elapsedMillis(start);
            PaymentOutcome paymentOutcome = new PaymentOutcome(
                    paymentEvent.paymentId(),
                    paymentEvent.debitAccountId(),
                    paymentEvent.creditAccountId(),
                    paymentEvent.amount(),
                    paymentEvent.currency(),
                    creditAmount,
                    creditCurrency,
                    rate == null ? null : rate.rate(),
                    rate == null ? null : rate.date(),
                    status,
                    reason,
                    processingTimeMs
            );

            paymentOutcome.setGbpEquivalent(gbpEquivalent);
            PaymentOutcome savedPaymentOutcome = paymentOutcomeRepository.save(paymentOutcome);

            paymentOutcomeOutboxService.createOutboxEvent(savedPaymentOutcome);
            processingMetrics.record(status.name(), processingTimeMs);
        } catch (Exception ex) {
            throw new IllegalStateException(COULD_NOT_PERSIST_PAYMENT_OUTCOME, ex);
        }
    }

    private long elapsedMillis(long startNanos) {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

}

