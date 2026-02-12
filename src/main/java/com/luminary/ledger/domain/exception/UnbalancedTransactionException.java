package com.luminary.ledger.domain.exception;

public class UnbalancedTransactionException extends RuntimeException {

    public UnbalancedTransactionException() {
        super("Transaction entries must balance: total debits must equal total credits");
    }
}
