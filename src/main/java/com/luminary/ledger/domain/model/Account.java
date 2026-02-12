package com.luminary.ledger.domain.model;

import com.luminary.ledger.domain.enums.AccountType;
import com.luminary.ledger.domain.vo.AccountName;

import java.time.LocalDateTime;

public class Account {

    private final Long id;
    private final AccountName name;
    private final AccountType type;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Account(Long id, AccountName name, AccountType type, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Account create(AccountName name, AccountType type) {
        LocalDateTime now = LocalDateTime.now();
        return new Account(null, name, type, now, now);
    }

    public Long getId() {
        return id;
    }

    public AccountName getName() {
        return name;
    }

    public AccountType getType() {
        return type;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
