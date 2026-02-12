package com.luminary.ledger.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionResponse(
        Long id,
        String description,
        LocalDateTime date,
        List<TransactionEntryResponse> entries,
        BigDecimal totalAmount,
        LocalDateTime createdAt
) {
}
