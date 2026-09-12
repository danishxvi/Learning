package com.danish.spring.transactions;

// A CHECKED exception (extends Exception, not RuntimeException) on purpose - see
// TransferService for why this matters to @Transactional's default rollback behaviour.
public class InsufficientFundsException extends Exception {
    public InsufficientFundsException(String message) {
        super(message);
    }
}
