package com.payment.processor.dto;

import java.math.BigDecimal;

public record BalanceChange(
        BigDecimal previousBalance,
        BigDecimal newBalance,
        long balanceVersion
) {
}
