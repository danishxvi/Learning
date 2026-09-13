package com.danish.spring.txpropagation;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

@Service
public class PropagationService {

    private final AccountRepository accountRepository;
    private final AuditService auditService;

    public PropagationService(AccountRepository accountRepository, AuditService auditService) {
        this.accountRepository = accountRepository;
        this.auditService = auditService;
    }

    // The OUTER transaction. Updates the balance, then calls a NESTED audit write. In
    // theory a NESTED failure only rolls back to its own savepoint, leaving this outer
    // work intact. In practice (see the .md), JpaTransactionManager never gets that
    // far - it rejects NESTED outright because it has no savepoint support to offer.
    @Transactional
    public void updateBalanceWithNestedAudit(Long accountId, int newBalance, boolean failAudit) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setBalanceCents(newBalance);

        try {
            auditService.recordNested("Balance changed to " + newBalance, failAudit);
        } catch (RuntimeException ex) {
            System.out.println("  Caught nested failure: " + ex.getMessage() + " - outer transaction continues");
        }
    }

    // Demonstrates NEVER from WITHIN an active transaction - the opposite failure mode
    // from calling MANDATORY with no transaction at all.
    @Transactional
    public void callNeverPropagationFromInsideATransaction() {
        auditService.mustNeverRunInsideATransaction();
    }
}
