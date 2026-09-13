package com.danish.spring.txpropagation;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLogEntry, Long> {
}
