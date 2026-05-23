-- ============================================================
-- V4__add_team_player_permissions.sql
-- Add MANAGE_TEAMS and MANAGE_PLAYERS permissions for Admin role.
-- These were not included in V2 as they were introduced in Phase 7.
-- ============================================================

INSERT INTO permission (name, description) VALUES
    ('MANAGE_TEAMS',   'Create, update, and delete teams'),
    ('MANAGE_PLAYERS', 'Add, update, and remove players from teams');

INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM role r
CROSS JOIN permission p
WHERE r.role_name = 'ADMIN'
  AND p.name IN ('MANAGE_TEAMS', 'MANAGE_PLAYERS');
