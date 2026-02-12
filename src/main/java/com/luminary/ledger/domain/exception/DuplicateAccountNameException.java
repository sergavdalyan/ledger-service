package com.luminary.ledger.domain.exception;

public class DuplicateAccountNameException extends RuntimeException {

    public DuplicateAccountNameException(String name) {
        super("Account already exists with name: " + name);
    }
}
