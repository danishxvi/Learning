package com.danish.spring.injection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// SETTER INJECTION - the right tool specifically for OPTIONAL dependencies, where the
// class should work whether or not the dependency is present. `required = false` tells
// Spring "call this setter if a matching bean exists; otherwise, skip it silently" -
// with plain constructor injection, a missing dependency is always a startup failure.
@Component
public class ReportGenerator {

    private AuditLogger auditLogger; // stays null in this lesson - no AuditLogger bean exists

    @Autowired(required = false)
    public void setAuditLogger(AuditLogger auditLogger) {
        this.auditLogger = auditLogger;
    }

    public void generateReport() {
        System.out.println("  Report generated.");
        if (auditLogger != null) {
            auditLogger.log("report-generated");
        } else {
            System.out.println("  (no AuditLogger bean was present - setter was simply never called)");
        }
    }
}
