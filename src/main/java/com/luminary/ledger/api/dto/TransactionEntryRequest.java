package com.luminary.ledger.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransactionEntryRequest(
        @NotNull(message = "Account ID is required")
        Long accountId,

        @NotNull(message = "Entry type is required")
        String type,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.0001", message = "Amount must be positive")
        BigDecimal amount
) {
}
