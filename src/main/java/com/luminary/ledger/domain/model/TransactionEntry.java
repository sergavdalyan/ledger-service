package com.luminary.ledger.domain.model;

import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.domain.vo.Money;

import java.time.LocalDateTime;

public class TransactionEntry {

    private final Long id;
    private final Long accountId;
    private final EntryType entryType;
    private final Money amount;
    private final LocalDateTime createdAt;

    public TransactionEntry(Long id, Long accountId, EntryType entryType, Money amount, LocalDateTime createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = createdAt;
    }

    public static TransactionEntry create(Long accountId, EntryType entryType, Money amount) {
        return new TransactionEntry(null, accountId, entryType, amount, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public EntryType getEntryType() {
        return entryType;
    }

    public Money getAmount() {
        return amount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
