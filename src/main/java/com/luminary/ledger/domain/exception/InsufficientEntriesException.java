package com.luminary.ledger.domain.exception;

public class InsufficientEntriesException extends RuntimeException {

    public InsufficientEntriesException() {
        super("Transaction must have at least two entries");
    }
}
