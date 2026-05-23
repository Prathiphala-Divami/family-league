package com.example.prathiphala_family_league.audit.service;

import com.example.prathiphala_family_league.audit.entity.AuditAction;

public interface AuditLogService {

    void record(String entityName, Long entityId, AuditAction action, String oldValue, String newValue);
}
