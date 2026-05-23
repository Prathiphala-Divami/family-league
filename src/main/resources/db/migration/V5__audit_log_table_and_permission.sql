-- ============================================================
-- V5__audit_log_table_and_permission.sql
-- Grants VIEW_AUDIT_LOGS permission to the ADMIN role.
-- NOTE: audit_log table and its indexes were created in V1/V3.
-- ============================================================

INSERT INTO permission (name, description) VALUES
    ('VIEW_AUDIT_LOGS', 'Read the audit trail for all entities');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
CROSS JOIN permission p
WHERE r.role_name = 'ADMIN'
  AND p.name = 'VIEW_AUDIT_LOGS';
