package com.danish.spring.injection;

// Deliberately NOT annotated @Component - no bean of this type exists anywhere in this
// lesson's context, on purpose, to prove setter injection can leave a dependency unset
// without breaking startup.
public interface AuditLogger {
    void log(String event);
}
