package com.yazidwms.audit.service;

import com.yazidwms.audit.entity.AuditEvent;
import com.yazidwms.audit.repository.AuditEventRepository;
import com.yazidwms.security.UserPrincipal;
import com.yazidwms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditService {

    private final AuditEventRepository auditEventRepository;

    public AuditService(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    @Transactional
    public void log(String action, String entity, Long entityId, String details, String ipAddress) {
        var event = new AuditEvent();
        event.setUser(currentUser());
        event.setTimestamp(Instant.now());
        event.setAction(action);
        event.setEntity(entity);
        event.setEntityId(entityId);
        event.setDetails(details);
        event.setIpAddress(ipAddress);
        auditEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> list(Pageable pageable) {
        return auditEventRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<AuditEvent> forEntity(String entity, Long entityId, Pageable pageable) {
        return auditEventRepository.findByEntityAndEntityId(entity, entityId, pageable);
    }

    private User currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal principal) {
            return principal.user();
        }
        return null;
    }
}
