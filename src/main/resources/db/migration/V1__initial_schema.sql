-- ============================================================
-- V1__initial_schema.sql
-- Initial schema for the Family League prediction platform.
-- All domain tables include BaseEntity audit columns.
-- ============================================================

-- ── RBAC ─────────────────────────────────────────────────────

CREATE TABLE role (
    id          BIGSERIAL    PRIMARY KEY,
    role_name   VARCHAR(50)  NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_by  BIGINT,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE TABLE permission (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255)
);

CREATE TABLE role_permission (
    role_id       BIGINT NOT NULL REFERENCES role(id),
    permission_id BIGINT NOT NULL REFERENCES permission(id),
    PRIMARY KEY (role_id, permission_id)
);

-- ── USERS ─────────────────────────────────────────────────────

CREATE TABLE users (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    avatar      VARCHAR(500),
    role_id     BIGINT       NOT NULL REFERENCES role(id),
    is_active   BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_by  BIGINT,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

-- ── LEAGUE HIERARCHY ──────────────────────────────────────────

CREATE TABLE league (
    id          BIGSERIAL    PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_by  BIGINT,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

CREATE TABLE season (
    id                   BIGSERIAL    PRIMARY KEY,
    league_id            BIGINT       NOT NULL REFERENCES league(id),
    name                 VARCHAR(100) NOT NULL,
    start_date           DATE         NOT NULL,
    end_date             DATE,
    status               VARCHAR(30)  NOT NULL DEFAULT 'UPCOMING',
    prediction_lock_time TIMESTAMPTZ,
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ,
    created_by           BIGINT,
    updated_by           BIGINT,
    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at           TIMESTAMPTZ,
    deleted_by           BIGINT,
    CONSTRAINT ck_season_status CHECK (status IN ('UPCOMING', 'ACTIVE', 'COMPLETED', 'CLOSED'))
);

-- ── TEAMS AND PLAYERS ─────────────────────────────────────────

CREATE TABLE team (
    id          BIGSERIAL    PRIMARY KEY,
    team_name   VARCHAR(100) NOT NULL,
    short_name  VARCHAR(10),
    logo        VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ,
    created_by  BIGINT,
    updated_by  BIGINT,
    is_deleted  BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at  TIMESTAMPTZ,
    deleted_by  BIGINT
);

-- Season ↔ Team roster (authoritative list for the season)
-- Admin adds teams here before predictions open.
-- Validates: match teams must be in this table; league predictions
-- must rank exactly the N teams in this table (F-6.6).
CREATE TABLE season_team (
    season_id  BIGINT NOT NULL REFERENCES season(id),
    team_id    BIGINT NOT NULL REFERENCES team(id),
    PRIMARY KEY (season_id, team_id)
);

CREATE TABLE player (
    id            BIGSERIAL    PRIMARY KEY,
    team_id       BIGINT       NOT NULL REFERENCES team(id),
    player_name   VARCHAR(100) NOT NULL,
    player_role   VARCHAR(30),
    jersey_number INT,
    country       VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ,
    created_by    BIGINT,
    updated_by    BIGINT,
    is_deleted    BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at    TIMESTAMPTZ,
    deleted_by    BIGINT,
    CONSTRAINT ck_player_role CHECK (
        player_role IN ('BATSMAN', 'BOWLER', 'ALL_ROUNDER', 'WICKET_KEEPER')
    )
);

-- ── MATCHES ───────────────────────────────────────────────────

CREATE TABLE match (
    id                   BIGSERIAL    PRIMARY KEY,
    season_id            BIGINT       NOT NULL REFERENCES season(id),
    team1_id             BIGINT       NOT NULL REFERENCES team(id),
    team2_id             BIGINT       NOT NULL REFERENCES team(id),
    venue                VARCHAR(255),
    start_time           TIMESTAMPTZ  NOT NULL,
    -- Stored explicitly (= start_time - 1hr); never derived at query time.
    prediction_lock_time TIMESTAMPTZ  NOT NULL,
    match_number         INT,
    status               VARCHAR(30)  NOT NULL DEFAULT 'SCHEDULED',
    created_at           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at           TIMESTAMPTZ,
    created_by           BIGINT,
    updated_by           BIGINT,
    is_deleted           BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at           TIMESTAMPTZ,
    deleted_by           BIGINT,
    CONSTRAINT ck_match_status         CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
    CONSTRAINT ck_match_different_teams CHECK (team1_id <> team2_id)
);

CREATE TABLE match_result (
    id                  BIGSERIAL    PRIMARY KEY,
    match_id            BIGINT       NOT NULL UNIQUE REFERENCES match(id),
    result_type         VARCHAR(20)  NOT NULL,
    winning_team_id     BIGINT       REFERENCES team(id),   -- NULL when TIE or NO_RESULT
    toss_winner_team_id BIGINT       REFERENCES team(id),
    player_of_match_id  BIGINT       REFERENCES player(id), -- NULL when TIE or NO_RESULT
    winning_margin      VARCHAR(100),
    published_by        BIGINT       NOT NULL REFERENCES users(id),
    published_at        TIMESTAMPTZ  NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,
    created_by          BIGINT,
    updated_by          BIGINT,
    is_deleted          BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at          TIMESTAMPTZ,
    deleted_by          BIGINT,
    CONSTRAINT ck_result_type CHECK (result_type IN ('WIN', 'TIE', 'NO_RESULT'))
);

-- ── PREDICTIONS ───────────────────────────────────────────────

CREATE TABLE prediction (
    id                           BIGSERIAL   PRIMARY KEY,
    user_id                      BIGINT      NOT NULL REFERENCES users(id),
    match_id                     BIGINT      NOT NULL REFERENCES match(id),
    predicted_winner_team_id     BIGINT      REFERENCES team(id),
    predicted_toss_winner_id     BIGINT      REFERENCES team(id),
    predicted_player_of_match_id BIGINT      REFERENCES player(id),
    -- System-calculated post result publish; never accepted via API.
    points_earned                INT         NOT NULL DEFAULT 0,
    submitted_at                 TIMESTAMPTZ NOT NULL,
    created_at                   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMPTZ,
    created_by                   BIGINT,
    updated_by                   BIGINT,
    is_deleted                   BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at                   TIMESTAMPTZ,
    deleted_by                   BIGINT,
    CONSTRAINT uq_prediction_user_match UNIQUE (user_id, match_id)
);

-- Per-season full-leaderboard prediction (team finish-order rankings).
CREATE TABLE league_prediction (
    id             BIGSERIAL   PRIMARY KEY,
    user_id        BIGINT      NOT NULL REFERENCES users(id),
    season_id      BIGINT      NOT NULL REFERENCES season(id),
    team_id        BIGINT      NOT NULL REFERENCES team(id),
    predicted_rank INT         NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMPTZ,
    created_by     BIGINT,
    updated_by     BIGINT,
    is_deleted     BOOLEAN     NOT NULL DEFAULT FALSE,
    deleted_at     TIMESTAMPTZ,
    deleted_by     BIGINT,
    -- No duplicate rank slot per user per season
    CONSTRAINT uq_league_pred_rank UNIQUE (user_id, season_id, predicted_rank),
    -- No duplicate team per user per season
    CONSTRAINT uq_league_pred_team UNIQUE (user_id, season_id, team_id)
);

-- ── LEADERBOARD ───────────────────────────────────────────────
-- Aggregate / computed table — not soft-deleted; rows are upserted
-- on each result publish via the async leaderboard recalculation event.

CREATE TABLE leaderboard (
    id            BIGSERIAL   PRIMARY KEY,
    season_id     BIGINT      NOT NULL REFERENCES season(id),
    user_id       BIGINT      NOT NULL REFERENCES users(id),
    total_points  INT         NOT NULL DEFAULT 0,
    rank_position INT,
    updated_at    TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_leaderboard_season_user UNIQUE (season_id, user_id)
);

-- ── NOTIFICATIONS ─────────────────────────────────────────────

CREATE TABLE notification (
    id         BIGSERIAL    PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id),
    type       VARCHAR(50)  NOT NULL,
    subject    VARCHAR(255),
    body       TEXT,
    status     VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    sent_at    TIMESTAMPTZ,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    created_by BIGINT,
    updated_by BIGINT,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    deleted_at TIMESTAMPTZ,
    deleted_by BIGINT,
    CONSTRAINT ck_notification_type   CHECK (type   IN ('PREDICTION_REMINDER', 'RESULT_PUBLISHED', 'LEADERBOARD_UPDATE', 'CUSTOM')),
    CONSTRAINT ck_notification_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

-- ── AUDIT LOG ─────────────────────────────────────────────────
-- Does NOT extend BaseEntity — it IS the audit trail.

CREATE TABLE audit_log (
    id          BIGSERIAL    PRIMARY KEY,
    entity_name VARCHAR(100) NOT NULL,
    entity_id   BIGINT       NOT NULL,
    action      VARCHAR(50)  NOT NULL,
    old_value   JSONB,
    new_value   JSONB,
    changed_by  BIGINT       REFERENCES users(id),
    changed_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_audit_action CHECK (action IN ('INSERT', 'UPDATE', 'SOFT_DELETE'))
);
