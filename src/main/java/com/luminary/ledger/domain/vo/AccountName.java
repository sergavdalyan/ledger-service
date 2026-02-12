package com.luminary.ledger.domain.vo;

import java.util.Objects;

public final class AccountName {

    private final String value;

    private AccountName(String value) {
        this.value = value;
    }

    public static AccountName of(String value) {
        Objects.requireNonNull(value, "Account name must not be null");
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("Account name must not be blank");
        }
        if (trimmed.length() > 255) {
            throw new IllegalArgumentException("Account name must not exceed 255 characters");
        }
        return new AccountName(trimmed);
    }

    public String value() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountName that = (AccountName) o;
        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
