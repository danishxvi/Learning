package com.danish.spring.transactions;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

// A SEPARATE bean, specifically so a call into it from another bean goes through a real
// Spring proxy - see TransferService for why that distinction matters.
@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    // REQUIRES_NEW - suspends whatever transaction is currently active (if any) and
    // starts a genuinely SEPARATE one, which commits or rolls back independently. An
    // audit trail is the classic use case: you want a record that "an attempt was made"
    // to survive even if the attempt itself gets rolled back.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String message) {
        auditLogRepository.save(new AuditLogEntry(message));
    }
}
