package com.luminary.ledger.api.dto;

import java.math.BigDecimal;

public record TransactionEntryResponse(
        Long id,
        Long accountId,
        String type,
        BigDecimal amount
) {
}
