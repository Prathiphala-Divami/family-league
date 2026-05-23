package com.example.prathiphala_family_league.audit.dto;

import com.example.prathiphala_family_league.audit.entity.AuditAction;

import java.time.Instant;

public record AuditLogResponse(
        Long id,
        String entityName,
        Long entityId,
        AuditAction action,
        String oldValue,
        String newValue,
        Long changedBy,
        Instant changedAt
) {}
