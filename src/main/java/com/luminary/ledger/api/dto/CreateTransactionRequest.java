package com.luminary.ledger.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public record CreateTransactionRequest(
        @NotBlank(message = "Description is required")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @NotNull(message = "Date is required")
        LocalDateTime date,

        @NotEmpty(message = "Entries are required")
        @Size(min = 2, message = "At least two entries are required")
        List<@Valid TransactionEntryRequest> entries
) {
}
