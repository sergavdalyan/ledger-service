package com.luminary.ledger.domain.exception;

public class InvalidEntryException extends RuntimeException {

    public InvalidEntryException(String message) {
        super(message);
    }
}
