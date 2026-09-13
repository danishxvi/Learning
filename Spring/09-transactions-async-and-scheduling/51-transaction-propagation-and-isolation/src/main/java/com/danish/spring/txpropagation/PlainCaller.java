package com.danish.spring.txpropagation;

import org.springframework.stereotype.Component;

// Deliberately has NO @Transactional anywhere in this class - exists purely to call
// AuditService.mustRunInsideAnExistingTransaction() with genuinely NO transaction
// active, to trigger MANDATORY's real failure mode.
@Component
public class PlainCaller {

    private final AuditService auditService;

    public PlainCaller(AuditService auditService) {
        this.auditService = auditService;
    }

    public void callMandatoryWithNoTransaction() {
        auditService.mustRunInsideAnExistingTransaction();
    }
}
