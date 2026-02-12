package com.luminary.ledger.domain.model;

import com.luminary.ledger.domain.enums.EntryType;
import com.luminary.ledger.domain.exception.InsufficientEntriesException;
import com.luminary.ledger.domain.exception.UnbalancedTransactionException;
import com.luminary.ledger.domain.vo.Money;

import java.time.LocalDateTime;
import java.util.List;

public class Transaction {

    private final Long id;
    private final String description;
    private final LocalDateTime date;
    private final List<TransactionEntry> entries;
    private final LocalDateTime createdAt;

    public Transaction(Long id, String description, LocalDateTime date, List<TransactionEntry> entries, LocalDateTime createdAt) {
        this.id = id;
        this.description = description;
        this.date = date;
        this.entries = List.copyOf(entries);
        this.createdAt = createdAt;
    }

    public static Transaction create(String description, LocalDateTime date, List<TransactionEntry> entries) {
        if (entries == null || entries.size() < 2) {
            throw new InsufficientEntriesException();
        }

        Money totalDebits = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.DEBIT)
                .map(TransactionEntry::getAmount)
                .reduce(Money.ZERO, Money::add);

        Money totalCredits = entries.stream()
                .filter(e -> e.getEntryType() == EntryType.CREDIT)
                .map(TransactionEntry::getAmount)
                .reduce(Money.ZERO, Money::add);

        if (!totalDebits.equals(totalCredits)) {
            throw new UnbalancedTransactionException();
        }

        return new Transaction(null, description, date, entries, LocalDateTime.now());
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public List<TransactionEntry> getEntries() {
        return entries;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
