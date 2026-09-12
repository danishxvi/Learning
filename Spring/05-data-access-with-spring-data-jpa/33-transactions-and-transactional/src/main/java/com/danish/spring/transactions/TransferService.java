package com.danish.spring.transactions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public TransferService(AccountRepository accountRepository, AuditLogRepository auditLogRepository,
                            AuditService auditService) {
        this.accountRepository = accountRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    // @Transactional with NO arguments = Propagation.REQUIRED (the default): join the
    // current transaction if one exists, otherwise start a new one. Debiting, crediting,
    // then throwing an UNCHECKED exception - Spring's default rollback rule is "roll
    // back on RuntimeException or Error" - so BOTH writes below are undone.
    @Transactional
    public void transferAndCrash(Long fromId, Long toId, int amountCents) {
        move(fromId, toId, amountCents);
        throw new RuntimeException("simulated failure - watch both balances stay unchanged");
    }

    // THE DEFAULT ROLLBACK RULE'S SURPRISE: a CHECKED exception (extends Exception, not
    // RuntimeException) does NOT trigger a rollback by default. Both writes below are
    // COMMITTED even though this method throws - Spring's default assumes a checked
    // exception represents an expected, recoverable business outcome, not a failure.
    @Transactional
    public void transferWithCheckedFailureDefaultRollback(Long fromId, Long toId, int amountCents)
            throws InsufficientFundsException {
        move(fromId, toId, amountCents);
        throw new InsufficientFundsException("simulated checked failure - watch balances CHANGE anyway");
    }

    // THE FIX: rollbackFor names exactly which exception types SHOULD roll back the
    // transaction, checked or not. Same method body as above - only this attribute
    // differs - and now the same checked exception correctly undoes both writes.
    @Transactional(rollbackFor = InsufficientFundsException.class)
    public void transferWithCheckedFailureExplicitRollback(Long fromId, Long toId, int amountCents)
            throws InsufficientFundsException {
        move(fromId, toId, amountCents);
        throw new InsufficientFundsException("simulated checked failure - watch balances stay unchanged now");
    }

    // Calls AuditService.log() - a SEPARATE bean, so this call goes through a real
    // Spring proxy, and REQUIRES_NEW genuinely suspends this method's transaction and
    // commits the audit entry independently, BEFORE the RuntimeException below rolls
    // back the transfer itself.
    @Transactional
    public void transferWithAuditViaProperBean(Long fromId, Long toId, int amountCents) {
        move(fromId, toId, amountCents);
        auditService.log("Attempted transfer of " + amountCents + " from " + fromId + " to " + toId);
        throw new RuntimeException("simulated failure - the audit entry should SURVIVE this");
    }

    // THE SELF-INVOCATION TRAP: recordAttemptViaSelf is ALSO @Transactional(REQUIRES_NEW),
    // but it is called as `this.recordAttemptViaSelf(...)` - a plain Java method call on
    // the SAME object, not through the Spring proxy that makes @Transactional work at
    // all (the exact same proxy mechanism lesson 09 and lesson 31 both demonstrated).
    // The annotation is silently ignored for this call; it just runs as part of the
    // CALLER's existing transaction, and gets rolled back along with everything else.
    @Transactional
    public void transferWithAuditViaSelfInvocation(Long fromId, Long toId, int amountCents) {
        move(fromId, toId, amountCents);
        this.recordAttemptViaSelf("Attempted transfer of " + amountCents + " from " + fromId + " to " + toId);
        throw new RuntimeException("simulated failure - the 'REQUIRES_NEW' audit entry will NOT survive this");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAttemptViaSelf(String message) {
        auditLogRepository.save(new AuditLogEntry(message));
    }

    private void move(Long fromId, Long toId, int amountCents) {
        Account from = accountRepository.findById(fromId).orElseThrow();
        Account to = accountRepository.findById(toId).orElseThrow();
        from.setBalanceCents(from.getBalanceCents() - amountCents);
        to.setBalanceCents(to.getBalanceCents() + amountCents);
    }
}
