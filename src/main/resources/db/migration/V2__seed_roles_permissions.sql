-- ============================================================
-- V2__seed_roles_permissions.sql
-- Seed RBAC reference data: roles and permissions.
-- ============================================================

-- Roles
INSERT INTO role (role_name, description) VALUES
    ('ADMIN', 'Platform administrator with full access'),
    ('USER',  'Regular member who submits predictions');

-- Permissions (11 total)
INSERT INTO permission (name, description) VALUES
    ('CREATE_LEAGUE',        'Create a new league'),
    ('CREATE_SEASON',        'Create a season under a league'),
    ('MANAGE_SEASON_TEAMS',  'Add or remove teams from a season'),
    ('CREATE_MATCH',         'Create a match within a season'),
    ('PUBLISH_RESULT',       'Publish the result for a completed match'),
    ('CLOSE_SEASON',         'Close a completed season (no further edits)'),
    ('MANAGE_USERS',         'Admin user management — activate / deactivate accounts'),
    ('SEND_NOTIFICATION',    'Send bulk notifications to users via email'),
    ('SUBMIT_PREDICTION',    'Submit a match or league prediction'),
    ('VIEW_PREDICTIONS',     'View own predictions'),
    ('VIEW_ALL_PREDICTIONS', 'View all users'' predictions after the lock window closes');

-- ADMIN gets every permission
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   role r
CROSS  JOIN permission p
WHERE  r.role_name = 'ADMIN';

-- USER gets: SUBMIT_PREDICTION, VIEW_PREDICTIONS
INSERT INTO role_permission (role_id, permission_id)
SELECT r.id, p.id
FROM   role r
JOIN   permission p ON p.name IN ('SUBMIT_PREDICTION', 'VIEW_PREDICTIONS')
WHERE  r.role_name = 'USER';
