-- ============================================================
-- V3__indexes.sql
-- Performance indexes for common query patterns.
-- ============================================================

-- Audit log: look up all events for a specific entity
CREATE INDEX idx_audit_log_entity     ON audit_log (entity_name, entity_id);
-- Audit log: time-ordered event feed
CREATE INDEX idx_audit_log_changed_at ON audit_log (changed_at DESC);

-- Prediction: bulk scoring queries after result publish
CREATE INDEX idx_prediction_match_id  ON prediction (match_id);
-- Prediction: user's own prediction history
CREATE INDEX idx_prediction_user_id   ON prediction (user_id);

-- Leaderboard: ordered fetch for a season (rank ascending)
CREATE INDEX idx_leaderboard_season_rank ON leaderboard (season_id, rank_position ASC);

-- Match: scheduler queries — upcoming matches within a season
CREATE INDEX idx_match_season_start ON match (season_id, start_time ASC);
-- Match: reminder scheduler — find matches whose lock time is approaching
CREATE INDEX idx_match_lock_time    ON match (prediction_lock_time);

-- Notification: idempotency check before sending reminders
CREATE INDEX idx_notification_user_type_status ON notification (user_id, type, status);
