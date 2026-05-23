-- ============================================================
-- V5__audit_log_table_and_permission.sql
-- Creates the audit_log table and grants VIEW_AUDIT_LOGS to ADMIN.
-- AuditLog does NOT extend BaseEntity — it IS the immutable trail.
-- ============================================================

CREATE TABLE audit_log (
    id          BIGSERIAL PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(20)  NOT NULL,
    old_value   JSONB,
    new_value   JSONB,
    changed_by  BIGINT,
    changed_at  TIMESTAMPTZ  NOT NULL
);

CREATE INDEX idx_audit_log_entity ON audit_log (entity_name, entity_id);
CREATE INDEX idx_audit_log_changed_at ON audit_log (changed_at DESC);

INSERT INTO permission (name, description) VALUES
    ('VIEW_AUDIT_LOGS', 'Read the audit trail for all entities');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
CROSS JOIN permission p
WHERE r.role_name = 'ADMIN'
  AND p.name = 'VIEW_AUDIT_LOGS';
