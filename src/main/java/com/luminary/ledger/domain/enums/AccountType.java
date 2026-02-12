package com.luminary.ledger.domain.enums;

public enum AccountType {
    ASSET,
    LIABILITY,
    REVENUE,
    EXPENSE;

    public boolean isDebitNormal() {
        return this == ASSET || this == EXPENSE;
    }
}
