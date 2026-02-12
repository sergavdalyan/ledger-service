package com.luminary.ledger.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateAccountRequest(
        @NotBlank(message = "Account name is required")
        @Size(max = 255, message = "Account name must not exceed 255 characters")
        String name,

        @NotNull(message = "Account type is required")
        String type
) {
}
