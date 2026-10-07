package com.payment.processor.entity;

import com.payment.processor.dto.BalanceChange;
import com.payment.processor.event.AccountUpdatedEvent;
import com.payment.processor.enums.AccountStatus;
import com.payment.processor.enums.AccountType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static com.payment.processor.constant.PaymentProcessorConstants.ACCOUNTS_TABLE_NAME;

@Getter
@Entity
@Table(
        name = ACCOUNTS_TABLE_NAME,
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_accounts_account_id",
                        columnNames = "account_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_accounts_account_id_user_id",
                        columnList = "account_id,user_id"
                ),
                @Index(
                        name = "idx_accounts_user_id_status",
                        columnList = "user_id,status"
                )
        }
)
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", updatable = false, nullable = false, unique = true, length = 64)
    private String accountId;

    @Column(nullable = false)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal accountBalance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private LocalDate openedDate;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "source_version", nullable = false)
    private long sourceVersion;

    @Column(name = "balance_version", nullable = false)
    @Builder.Default
    private long balanceVersion = 0L;

    public Account() {
    }

    public Account(
            String accountId,
            String accountName,
            AccountType accountType,
            BigDecimal accountBalance,
            AccountStatus status,
            String currency,
            LocalDate openedDate,
            UUID userId) {

        this.accountId = accountId;
        this.accountName = accountName;
        this.accountType = accountType;
        this.accountBalance = accountBalance;
        this.status = status;
        this.currency = currency;
        this.openedDate = openedDate;
        this.userId = userId;
        this.sourceVersion = 1L;
    }

    public void debit(BigDecimal amount) {
        this.accountBalance = this.accountBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.accountBalance = this.accountBalance.add(amount);
    }

    public void incrementSourceVersion() {
        this.sourceVersion++;
    }

    public boolean applyUpdateIfNewer(AccountUpdatedEvent event) {

        if (event.sourceVersion() <= this.sourceVersion) {
            return false;
        }

        this.accountName = event.accountName();
        this.accountType = event.accountType();
        this.status = event.status();
        this.sourceVersion = event.sourceVersion();

        return true;
    }

    public BalanceChange applyDebitBalanceChange(BigDecimal debitAmount) {

        if (debitAmount == null) {
            throw new IllegalArgumentException("debitAmount must not be null");
        }

        BigDecimal previousBalance = this.accountBalance;

        BigDecimal newBalance = previousBalance.subtract(debitAmount);

        if (newBalance.signum() < 0) {
            throw new IllegalStateException("Account balance cannot become negative");
        }

        this.accountBalance = newBalance;
        this.balanceVersion++;

        return new BalanceChange(
                previousBalance,
                newBalance,
                this.balanceVersion
        );
    }

    public BalanceChange applyCreditBalanceChange(BigDecimal creditAmount) {

        if (creditAmount == null) {
            throw new IllegalArgumentException("creditAmount must not be null");
        }

        BigDecimal previousBalance = this.accountBalance;

        BigDecimal newBalance = previousBalance.add(creditAmount);

        if (newBalance.signum() < 0) {
            throw new IllegalStateException("Account balance cannot become negative");
        }

        this.accountBalance = newBalance;
        this.balanceVersion++;

        return new BalanceChange(
                previousBalance,
                newBalance,
                this.balanceVersion
        );
    }

}


