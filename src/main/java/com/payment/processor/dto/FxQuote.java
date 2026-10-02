package com.payment.processor.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record FxQuote(
        BigDecimal rate,
        LocalDate date,
        String source
) {
}