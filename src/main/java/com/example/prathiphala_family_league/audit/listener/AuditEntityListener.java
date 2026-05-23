package com.example.prathiphala_family_league.audit.listener;

import com.example.prathiphala_family_league.audit.entity.AuditAction;
import com.example.prathiphala_family_league.audit.service.AuditLogService;
import com.example.prathiphala_family_league.audit.service.AuditSerializer;
import com.example.prathiphala_family_league.common.entity.BaseEntity;
import com.example.prathiphala_family_league.config.SpringContextHolder;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostUpdate;
import jakarta.persistence.PreUpdate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AuditEntityListener {

    // Captures the pre-update JSON snapshot before Hibernate flushes the UPDATE SQL.
    // @PostUpdate reads it and then removes it to prevent memory leaks.
    private static final ThreadLocal<String> PRE_UPDATE_SNAPSHOT = new ThreadLocal<>();

    @PreUpdate
    public void onPreUpdate(Object entity) {
        try {
            AuditSerializer serializer = SpringContextHolder.getBean(AuditSerializer.class);
            PRE_UPDATE_SNAPSHOT.set(serializer.serialize(entity));
        } catch (Exception e) {
            log.warn("PreUpdate snapshot failed for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
            PRE_UPDATE_SNAPSHOT.set(null);
        }
    }

    @PostPersist
    public void onPostPersist(Object entity) {
        if (!(entity instanceof BaseEntity base)) return;
        try {
            AuditSerializer serializer = SpringContextHolder.getBean(AuditSerializer.class);
            AuditLogService auditLogService = SpringContextHolder.getBean(AuditLogService.class);
            String newValue = serializer.serialize(entity);
            auditLogService.record(entity.getClass().getSimpleName(), base.getId(), AuditAction.INSERT, null, newValue);
        } catch (Exception e) {
            log.error("PostPersist audit failed for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
    }

    @PostUpdate
    public void onPostUpdate(Object entity) {
        if (!(entity instanceof BaseEntity base)) return;
        String oldValue = PRE_UPDATE_SNAPSHOT.get();
        PRE_UPDATE_SNAPSHOT.remove();
        try {
            AuditSerializer serializer = SpringContextHolder.getBean(AuditSerializer.class);
            AuditLogService auditLogService = SpringContextHolder.getBean(AuditLogService.class);
            String newValue = serializer.serialize(entity);
            AuditAction action = base.isDeleted() ? AuditAction.SOFT_DELETE : AuditAction.UPDATE;
            auditLogService.record(entity.getClass().getSimpleName(), base.getId(), action, oldValue, newValue);
        } catch (Exception e) {
            log.error("PostUpdate audit failed for {}: {}", entity.getClass().getSimpleName(), e.getMessage());
        }
    }
}
