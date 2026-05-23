package com.example.prathiphala_family_league.audit.controller;

import com.example.prathiphala_family_league.audit.dto.AuditLogResponse;
import com.example.prathiphala_family_league.audit.entity.AuditLog;
import com.example.prathiphala_family_league.audit.repository.AuditLogRepository;
import com.example.prathiphala_family_league.common.response.ApiResponse;
import com.example.prathiphala_family_league.common.response.PagedResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('VIEW_AUDIT_LOGS')")
    public ResponseEntity<ApiResponse<PagedResponse<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String entityName,
            @PageableDefault(size = 20, sort = "changedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<AuditLog> page = auditLogRepository.findWithFilter(entityName, pageable);
        Page<AuditLogResponse> mapped = page.map(a -> new AuditLogResponse(
                a.getId(), a.getEntityName(), a.getEntityId(), a.getAction(),
                a.getOldValue(), a.getNewValue(), a.getChangedBy(), a.getChangedAt()));

        return ResponseEntity.ok(ApiResponse.success(new PagedResponse<>(mapped)));
    }
}
