package com.example.prathiphala_family_league.audit.repository;

import com.example.prathiphala_family_league.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    @Query("SELECT a FROM AuditLog a WHERE (:entityName IS NULL OR a.entityName = :entityName) ORDER BY a.changedAt DESC")
    Page<AuditLog> findWithFilter(@Param("entityName") String entityName, Pageable pageable);
}
