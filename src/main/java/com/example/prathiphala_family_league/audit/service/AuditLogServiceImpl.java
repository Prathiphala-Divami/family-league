package com.example.prathiphala_family_league.audit.service;

import com.example.prathiphala_family_league.audit.entity.AuditAction;
import com.example.prathiphala_family_league.audit.entity.AuditLog;
import com.example.prathiphala_family_league.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(String entityName, Long entityId, AuditAction action, String oldValue, String newValue) {
        try {
            AuditLog log = new AuditLog();
            log.setEntityName(entityName);
            log.setEntityId(entityId);
            log.setAction(action);
            log.setOldValue(oldValue);
            log.setNewValue(newValue);
            log.setChangedBy(resolveCurrentUserId());
            log.setChangedAt(Instant.now());
            auditLogRepository.save(log);
        } catch (Exception e) {
            AuditLogServiceImpl.log.error("Failed to persist audit log for {}/{}: {}", entityName, entityId, e.getMessage());
        }
    }

    private Long resolveCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Long userId) {
            return userId;
        }
        return null;
    }
}
