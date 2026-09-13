package com.danish.spring.txpropagation;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // NESTED - in THEORY, unlike REQUIRES_NEW (a genuinely separate transaction, its
    // own connection, lesson 33), NESTED runs inside the SAME transaction and
    // connection, using a JDBC SAVEPOINT: if this method's own work fails, only the
    // work done since the savepoint rolls back, while the outer transaction continues.
    // In PRACTICE, with the default JPA transaction manager, this never gets that far -
    // see the .md for the real exception this actually throws, and why.
    @Transactional(propagation = Propagation.NESTED)
    public void recordNested(String message, boolean shouldFail) {
        auditLogRepository.save(new AuditLogEntry(message));
        if (shouldFail) {
            throw new RuntimeException("simulated audit failure - only THIS savepoint's work should roll back");
        }
    }

    // MANDATORY - this method REFUSES to run without an existing transaction already in
    // progress. Unlike REQUIRED (join or create one), this is a way of saying "calling
    // this method outside a transaction is a bug in the caller, not something to
    // silently paper over by starting one."
    @Transactional(propagation = Propagation.MANDATORY)
    public void mustRunInsideAnExistingTransaction() {
        auditLogRepository.save(new AuditLogEntry("this required an existing transaction"));
    }

    // NEVER - the opposite of MANDATORY: this method must NOT run inside a transaction
    // at all, and throws if one is already active when it's called. Genuinely rare, but
    // real - some operations (certain locking or DDL statements on some databases)
    // are only valid outside a transaction.
    @Transactional(propagation = Propagation.NEVER)
    public void mustNeverRunInsideATransaction() {
        auditLogRepository.save(new AuditLogEntry("this ran with NO transaction active"));
    }
}
