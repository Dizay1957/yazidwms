package com.yazidwms.audit.controller;

import com.yazidwms.audit.service.AuditService;
import com.yazidwms.common.api.ApiResponse;
import com.yazidwms.common.api.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/audit")
public class AuditController {
    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    ResponseEntity<ApiResponse<PageResponse<Map<String, Object>>>> list(@RequestParam(required = false) String entity,
                                                                        @RequestParam(required = false) Long entityId,
                                                                        Pageable pageable) {
        var page = entity != null && entityId != null ? auditService.forEntity(entity, entityId, pageable) : auditService.list(pageable);
        return ResponseEntity.ok(ApiResponse.ok("Audit events fetched", PageResponse.from(page.map(event -> Map.of(
                "id", event.getId(),
                "userId", event.getUser() == null ? "" : event.getUser().getId(),
                "timestamp", event.getTimestamp(),
                "action", event.getAction(),
                "ipAddress", event.getIpAddress() == null ? "" : event.getIpAddress(),
                "entity", event.getEntity(),
                "entityId", event.getEntityId() == null ? "" : event.getEntityId(),
                "details", event.getDetails() == null ? "" : event.getDetails()
        )))));
    }
}
