package com.yazidwms.audit.repository;

import com.yazidwms.audit.entity.AuditEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditEventRepository extends JpaRepository<AuditEvent, Long> {
    Page<AuditEvent> findByEntityAndEntityId(String entity, Long entityId, Pageable pageable);
}
