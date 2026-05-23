# Family League – Data Model
Version 1.2 | Revised from original ERD

---

## Changes from Original Model

| # | Issue | Fix Applied |
|---|-------|-------------|
| 1 | No soft delete on any table | Added `is_deleted`, `deleted_at`, `deleted_by` via `BaseEntity` |
| 2 | RBAC limited to Role only | Added `Permission` and `Role_Permission` tables |
| 3 | `Match_Result` lacks tie handling | Added `result_type` enum column |
| 4 | `Leaderboard` has no uniqueness | Added `UNIQUE(season_id, user_id)` |
| 5 | `League_Prediction` allows duplicate ranks | Added `UNIQUE(user_id, season_id, predicted_rank)` |
| 6 | `Role` uses `role_id` (inconsistent PK naming) | Renamed to `id` across all tables |
| 7 | `Season` had duplicate `id` column in diagram | Removed duplicate |
| 8 | `Player.player_role` typo `BOWLWER` | Fixed to `BOWLER`, added `ALL_ROUNDER`, `WICKET_KEEPER` |
| 9 | No audit fields on most tables | All tables extend `BaseEntity` |
| 10 | `Prediction` allows multiple entries per user per match | Added `UNIQUE(user_id, match_id)` |
| 11 | `League_Prediction` allows same team at two ranks | Added `UNIQUE(user_id, season_id, team_id)` |
| 12 | No authoritative team roster per season | Added `Season_Team` junction table; Admin assigns teams before predictions open |

---

## Base Entity (Abstract – Not a Table)

All domain tables extend this. Implemented in Java as a `@MappedSuperclass`.

| Column | Type | Notes |
|--------|------|-------|
| `id` | BIGINT | PK, auto-generated |
| `created_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() |
| `updated_at` | TIMESTAMP | auto-managed by JPA |
| `created_by` | BIGINT | FK → User.id |
| `updated_by` | BIGINT | FK → User.id |
| `is_deleted` | BOOLEAN | NOT NULL, DEFAULT FALSE |
| `deleted_at` | TIMESTAMP | nullable |
| `deleted_by` | BIGINT | FK → User.id, nullable |

---

## Tables

### Role
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `role_name` | VARCHAR(50) | NOT NULL, UNIQUE |
| `description` | VARCHAR(255) | |
| *+ BaseEntity fields* | | |

Seeded values: `ADMIN`, `USER`

---

### Permission
*New table – required for proper RBAC.*

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE |
| `description` | VARCHAR(255) | |

Seeded values:

| Permission Name | Description |
|----------------|-------------|
| `CREATE_LEAGUE` | Create a new league |
| `CREATE_SEASON` | Create a season under a league |
| `MANAGE_SEASON_TEAMS` | Add teams to a season |
| `CREATE_MATCH` | Create a match |
| `PUBLISH_RESULT` | Publish match result |
| `CLOSE_SEASON` | Close a completed season |
| `MANAGE_USERS` | Admin user management |
| `SEND_NOTIFICATION` | Bulk notify users |
| `SUBMIT_PREDICTION` | Submit match or league prediction |
| `VIEW_PREDICTIONS` | View own predictions |
| `VIEW_ALL_PREDICTIONS` | View others' predictions (post-lock) |

---

### Role_Permission
*Many-to-many junction between Role and Permission.*

| Column | Type | Constraints |
|--------|------|-------------|
| `role_id` | BIGINT | FK → Role.id |
| `permission_id` | BIGINT | FK → Permission.id |
| | | PK(role_id, permission_id) |

---

### User
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `name` | VARCHAR(100) | NOT NULL |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE |
| `password` | VARCHAR(255) | NOT NULL (BCrypt hashed) |
| `avatar` | VARCHAR(500) | nullable |
| `role_id` | BIGINT | FK → Role.id |
| `is_active` | BOOLEAN | DEFAULT TRUE |
| *+ BaseEntity fields* | | |

---

### League
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `name` | VARCHAR(100) | NOT NULL |
| `description` | TEXT | nullable |
| *+ BaseEntity fields* | | |

---

### Season
*An instance of a League. A League can have many Seasons.*

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `league_id` | BIGINT | FK → League.id, NOT NULL |
| `name` | VARCHAR(100) | NOT NULL |
| `start_date` | DATE | NOT NULL |
| `end_date` | DATE | |
| `status` | VARCHAR(30) | NOT NULL – `UPCOMING` / `ACTIVE` / `COMPLETED` / `CLOSED` |
| `prediction_lock_time` | TIMESTAMP | 4 hrs before first match start time; set by system |
| *+ BaseEntity fields* | | |

> `CLOSED` status means no edits allowed, even by Admin.

---

### Team
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `team_name` | VARCHAR(100) | NOT NULL |
| `short_name` | VARCHAR(10) | |
| `logo` | VARCHAR(500) | |
| *+ BaseEntity fields* | | |

> Teams are independent of leagues. Same team can participate in multiple seasons.

---

### Season_Team
*Junction table defining the authoritative set of participating teams for a season. Required before league predictions can be validated.*

| Column | Type | Constraints |
|--------|------|-------------|
| `season_id` | BIGINT | FK → Season.id, NOT NULL |
| `team_id` | BIGINT | FK → Team.id, NOT NULL |
| | | PK(season_id, team_id) |

> Admin adds teams via `POST /api/v1/seasons/{id}/teams`. The system validates league predictions (F-6.6) and match team assignments against this table — a match cannot be scheduled with a team not in `season_team`, and a league prediction must rank exactly the N teams in `season_team` with no gaps or extras.

---

### Player
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `team_id` | BIGINT | FK → Team.id |
| `player_name` | VARCHAR(100) | NOT NULL |
| `player_role` | VARCHAR(30) | `BATSMAN` / `BOWLER` / `ALL_ROUNDER` / `WICKET_KEEPER` |
| `jersey_number` | INT | |
| `country` | VARCHAR(100) | |
| *+ BaseEntity fields* | | |

---

### Match
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `season_id` | BIGINT | FK → Season.id, NOT NULL |
| `team1_id` | BIGINT | FK → Team.id, NOT NULL |
| `team2_id` | BIGINT | FK → Team.id, NOT NULL |
| `venue` | VARCHAR(255) | |
| `start_time` | TIMESTAMP | NOT NULL |
| `prediction_lock_time` | TIMESTAMP | NOT NULL – stored explicitly; system sets to `start_time - 1hr` |
| `match_number` | INT | match sequence within the season |
| `status` | VARCHAR(30) | `SCHEDULED` / `IN_PROGRESS` / `COMPLETED` / `CANCELLED` |
| *+ BaseEntity fields* | | |

> `prediction_lock_time` is persisted (not derived at query time) so DB-level enforcement is possible.

---

### Match_Result
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `match_id` | BIGINT | FK → Match.id, UNIQUE (one result per match) |
| `result_type` | VARCHAR(20) | NOT NULL – `WIN` / `TIE` / `NO_RESULT` |
| `winning_team_id` | BIGINT | FK → Team.id – nullable (null when TIE or NO_RESULT) |
| `toss_winner_team_id` | BIGINT | FK → Team.id |
| `player_of_match_id` | BIGINT | FK → Player.id – nullable (null when TIE or NO_RESULT) |
| `winning_margin` | VARCHAR(100) | e.g., "5 wickets", "20 runs" |
| `published_by` | BIGINT | FK → User.id (Admin who published) |
| `published_at` | TIMESTAMP | |
| *+ BaseEntity fields* | | |

---

### Prediction
*Per-match prediction by a user.*

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK → User.id, NOT NULL |
| `match_id` | BIGINT | FK → Match.id, NOT NULL |
| `predicted_winner_team_id` | BIGINT | FK → Team.id |
| `predicted_toss_winner_id` | BIGINT | FK → Team.id |
| `predicted_player_of_match_id` | BIGINT | FK → Player.id |
| `points_earned` | INT | DEFAULT 0 – system-calculated, never accepted via API |
| `submitted_at` | TIMESTAMP | |
| *+ BaseEntity fields* | | |

**Constraints:**
- `UNIQUE(user_id, match_id)` – one prediction per user per match
- `CHECK(predicted_winner_team_id != predicted_toss_winner_id OR ...)` – application-level validation

---

### League_Prediction
*Per-season full-leaderboard prediction (team rankings) by a user.*

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK → User.id, NOT NULL |
| `season_id` | BIGINT | FK → Season.id, NOT NULL |
| `team_id` | BIGINT | FK → Team.id, NOT NULL |
| `predicted_rank` | INT | NOT NULL (1 to n, n = number of teams) |
| *+ BaseEntity fields* | | |

**Constraints:**
- `UNIQUE(user_id, season_id, predicted_rank)` – one team per rank slot per user
- `UNIQUE(user_id, season_id, team_id)` – one rank per team per user

---

### Leaderboard
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `season_id` | BIGINT | FK → Season.id, NOT NULL |
| `user_id` | BIGINT | FK → User.id, NOT NULL |
| `total_points` | INT | DEFAULT 0 |
| `rank_position` | INT | |
| `updated_at` | TIMESTAMP | |

**Constraints:**
- `UNIQUE(season_id, user_id)` – one leaderboard row per user per season

> Leaderboard does not extend BaseEntity. It is a computed/aggregate view – rows are upserted, not soft-deleted.

---

### Notification
| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `user_id` | BIGINT | FK → User.id, NOT NULL |
| `type` | VARCHAR(50) | `PREDICTION_REMINDER` / `RESULT_PUBLISHED` / `LEADERBOARD_UPDATE` / `CUSTOM` |
| `subject` | VARCHAR(255) | |
| `body` | TEXT | |
| `status` | VARCHAR(30) | `PENDING` / `SENT` / `FAILED` |
| `sent_at` | TIMESTAMP | |
| *+ BaseEntity fields* | | |

> All emails are stored here – who, when, for what, status – as required.

---

### Audit_Log
*Does not extend BaseEntity – it IS the audit trail.*

| Column | Type | Constraints |
|--------|------|-------------|
| `id` | BIGINT | PK |
| `entity_name` | VARCHAR(100) | NOT NULL – e.g., `Match`, `Prediction` |
| `entity_id` | BIGINT | NOT NULL |
| `action` | VARCHAR(50) | `INSERT` / `UPDATE` / `SOFT_DELETE` |
| `old_value` | JSONB | nullable |
| `new_value` | JSONB | nullable |
| `changed_by` | BIGINT | FK → User.id |
| `changed_at` | TIMESTAMP | NOT NULL, DEFAULT NOW() |

---

## Entity Relationship Summary

```
League 1──* Season 1──* Match *──1 Team (team1, team2)
                              1──0..1 Match_Result
Season *──* Team (via Season_Team – authoritative participating team roster)
User 1──* Prediction (per Match)
User 1──* League_Prediction (per Season)
User 1──1 Leaderboard (per Season, via UNIQUE constraint)
Role *──* Permission (via Role_Permission)
User *──1 Role
Team 1──* Player
Match_Result 1──1 Player (player_of_match)
User 1──* Notification
```

---

## Key Design Decisions

| Decision | Justification |
|----------|--------------|
| BaseEntity as @MappedSuperclass | DRY principle – audit fields auto-populated via JPA listeners, not per-table boilerplate |
| `prediction_lock_time` stored explicitly on Match | Allows DB-level constraint enforcement and scheduled jobs without runtime math |
| `Season.prediction_lock_time` separate from Match lock | Business rule: league predictions close 4hrs before first match; match predictions close 1hr before their own start |
| `result_type` on Match_Result | Ties are valid results per scoring rules; nullable `winning_team_id` is clean when no winner |
| Leaderboard NOT soft-deleted | It is a derived aggregate – it gets recalculated on each result publish, not manually deleted |
| `points_earned` on Prediction | System-calculated post result publish; `CHECK` constraint or service layer prevents API input |
| JSONB for Audit_Log values | Flexible schema for any entity; PostgreSQL JSONB supports indexing if needed |
| Separate Permission table | Spring Security `@PreAuthorize` binds to permissions, not roles – finer-grained ACL without code changes |
| Explicit `season_team` table | Teams are independent of leagues but each season has a defined set of participants. `season_team` is the system of record used to: (a) validate that match teams belong to the season, (b) determine N when validating league predictions (F-6.6 — "rank for every team"). Deriving teams from match records would require matches to exist first, blocking league predictions submitted before the full schedule is entered. |

---

## Visibility Rule (No Schema Change Needed)

> "Users can see each other's predictions only after the prediction window closes."

Enforced at **API / service layer**:
- `GET /matches/{id}/predictions` checks `Match.prediction_lock_time < NOW()`
- Returns `403 Forbidden` or empty list if window is still open
- Documented in Decision Log

---

## DBML Schema (for dbdiagram.io)

```dbml
// ============================================================
// Family League — Improved Data Model v1.1
// Paste this at https://dbdiagram.io to render the diagram
// ============================================================

// ── RBAC ────────────────────────────────────────────────────

Table role {
  id            bigint      [pk, increment]
  role_name     varchar(50) [unique, not null]
  description   varchar(255)
  created_at    timestamp   [not null, default: `now()`]
  updated_at    timestamp
  created_by    bigint
  updated_by    bigint
  is_deleted    boolean     [not null, default: false]
  deleted_at    timestamp
  deleted_by    bigint
}

Table permission {
  id            bigint       [pk, increment]
  name          varchar(100) [unique, not null, note: 'e.g. CREATE_MATCH, PUBLISH_RESULT']
  description   varchar(255)
}

Table role_permission {
  role_id       bigint [not null, ref: > role.id]
  permission_id bigint [not null, ref: > permission.id]

  indexes {
    (role_id, permission_id) [pk]
  }
}

// ── USERS ───────────────────────────────────────────────────

Table users {
  id          bigint       [pk, increment]
  name        varchar(100) [not null]
  email       varchar(150) [unique, not null]
  password    varchar(255) [not null, note: 'BCrypt hashed — never plaintext']
  avatar      varchar(500)
  role_id     bigint       [not null, ref: > role.id]
  is_active   boolean      [not null, default: true]
  created_at  timestamp    [not null, default: `now()`]
  updated_at  timestamp
  created_by  bigint
  updated_by  bigint
  is_deleted  boolean      [not null, default: false]
  deleted_at  timestamp
  deleted_by  bigint
}

// ── LEAGUE HIERARCHY ────────────────────────────────────────

Table league {
  id          bigint       [pk, increment]
  name        varchar(100) [not null]
  description text
  created_at  timestamp    [not null, default: `now()`]
  updated_at  timestamp
  created_by  bigint
  updated_by  bigint
  is_deleted  boolean      [not null, default: false]
  deleted_at  timestamp
  deleted_by  bigint
}

Table season {
  id                   bigint       [pk, increment]
  league_id            bigint       [not null, ref: > league.id]
  name                 varchar(100) [not null]
  start_date           date         [not null]
  end_date             date
  status               varchar(30)  [not null, note: 'UPCOMING / ACTIVE / COMPLETED / CLOSED']
  prediction_lock_time timestamp    [note: 'System-set: 4hrs before first match start_time']
  created_at           timestamp    [not null, default: `now()`]
  updated_at           timestamp
  created_by           bigint
  updated_by           bigint
  is_deleted           boolean      [not null, default: false]
  deleted_at           timestamp
  deleted_by           bigint
}

// ── SEASON–TEAM ROSTER ──────────────────────────────────────

Table season_team {
  season_id  bigint [not null, ref: > season.id, note: 'Authoritative list of teams in this season']
  team_id    bigint [not null, ref: > team.id]

  indexes {
    (season_id, team_id) [pk]
  }
}

// ── TEAMS AND PLAYERS ───────────────────────────────────────

Table team {
  id         bigint       [pk, increment]
  team_name  varchar(100) [not null]
  short_name varchar(10)
  logo       varchar(500)
  created_at timestamp    [not null, default: `now()`]
  updated_at timestamp
  created_by bigint
  updated_by bigint
  is_deleted boolean      [not null, default: false]
  deleted_at timestamp
  deleted_by bigint
}

Table player {
  id            bigint       [pk, increment]
  team_id       bigint       [not null, ref: > team.id]
  player_name   varchar(100) [not null]
  player_role   varchar(30)  [note: 'BATSMAN / BOWLER / ALL_ROUNDER / WICKET_KEEPER']
  jersey_number int
  country       varchar(100)
  created_at    timestamp    [not null, default: `now()`]
  updated_at    timestamp
  created_by    bigint
  updated_by    bigint
  is_deleted    boolean      [not null, default: false]
  deleted_at    timestamp
  deleted_by    bigint
}

// ── MATCHES ─────────────────────────────────────────────────

Table match {
  id                   bigint       [pk, increment]
  season_id            bigint       [not null, ref: > season.id]
  team1_id             bigint       [not null, ref: > team.id]
  team2_id             bigint       [not null, ref: > team.id]
  venue                varchar(255)
  start_time           timestamp    [not null]
  prediction_lock_time timestamp    [not null, note: 'Stored explicitly = start_time minus 1hr']
  match_number         int
  status               varchar(30)  [not null, note: 'SCHEDULED / IN_PROGRESS / COMPLETED / CANCELLED']
  created_at           timestamp    [not null, default: `now()`]
  updated_at           timestamp
  created_by           bigint
  updated_by           bigint
  is_deleted           boolean      [not null, default: false]
  deleted_at           timestamp
  deleted_by           bigint
}

Table match_result {
  id                  bigint       [pk, increment]
  match_id            bigint       [unique, not null, ref: - match.id]
  result_type         varchar(20)  [not null, note: 'WIN / TIE / NO_RESULT']
  winning_team_id     bigint       [ref: > team.id, note: 'NULL when TIE or NO_RESULT']
  toss_winner_team_id bigint       [ref: > team.id]
  player_of_match_id  bigint       [ref: > player.id, note: 'NULL when TIE or NO_RESULT']
  winning_margin      varchar(100)
  published_by        bigint       [not null, ref: > users.id]
  published_at        timestamp    [not null]
  created_at          timestamp    [not null, default: `now()`]
  updated_at          timestamp
  created_by          bigint
  updated_by          bigint
  is_deleted          boolean      [not null, default: false]
  deleted_at          timestamp
  deleted_by          bigint
}

// ── PREDICTIONS ─────────────────────────────────────────────

Table prediction {
  id                           bigint    [pk, increment]
  user_id                      bigint    [not null, ref: > users.id]
  match_id                     bigint    [not null, ref: > match.id]
  predicted_winner_team_id     bigint    [ref: > team.id]
  predicted_toss_winner_id     bigint    [ref: > team.id]
  predicted_player_of_match_id bigint    [ref: > player.id]
  points_earned                int       [not null, default: 0, note: 'System-calculated — never accepted via API']
  submitted_at                 timestamp [not null]
  created_at                   timestamp [not null, default: `now()`]
  updated_at                   timestamp
  created_by                   bigint
  updated_by                   bigint
  is_deleted                   boolean   [not null, default: false]
  deleted_at                   timestamp
  deleted_by                   bigint

  indexes {
    (user_id, match_id) [unique, note: 'One prediction per user per match']
  }
}

Table league_prediction {
  id             bigint    [pk, increment]
  user_id        bigint    [not null, ref: > users.id]
  season_id      bigint    [not null, ref: > season.id]
  team_id        bigint    [not null, ref: > team.id]
  predicted_rank int       [not null]
  created_at     timestamp [not null, default: `now()`]
  updated_at     timestamp
  created_by     bigint
  updated_by     bigint
  is_deleted     boolean   [not null, default: false]
  deleted_at     timestamp
  deleted_by     bigint

  indexes {
    (user_id, season_id, predicted_rank) [unique, note: 'No duplicate rank per user per season']
    (user_id, season_id, team_id)        [unique, note: 'No duplicate team per user per season']
  }
}

// ── LEADERBOARD ─────────────────────────────────────────────

Table leaderboard {
  id            bigint    [pk, increment]
  season_id     bigint    [not null, ref: > season.id]
  user_id       bigint    [not null, ref: > users.id]
  total_points  int       [not null, default: 0]
  rank_position int
  updated_at    timestamp [not null]

  indexes {
    (season_id, user_id) [unique, note: 'One row per user per season']
  }
}

// ── NOTIFICATIONS ───────────────────────────────────────────

Table notification {
  id         bigint       [pk, increment]
  user_id    bigint       [not null, ref: > users.id]
  type       varchar(50)  [not null, note: 'PREDICTION_REMINDER / RESULT_PUBLISHED / LEADERBOARD_UPDATE / CUSTOM']
  subject    varchar(255)
  body       text
  status     varchar(30)  [not null, default: 'PENDING', note: 'PENDING / SENT / FAILED']
  sent_at    timestamp
  created_at timestamp    [not null, default: `now()`]
  updated_at timestamp
  created_by bigint
  updated_by bigint
  is_deleted boolean      [not null, default: false]
  deleted_at timestamp
  deleted_by bigint
}

// ── AUDIT ───────────────────────────────────────────────────

Table audit_log {
  id          bigint       [pk, increment]
  entity_name varchar(100) [not null, note: 'e.g. Match, Prediction, Season']
  entity_id   bigint       [not null]
  action      varchar(50)  [not null, note: 'INSERT / UPDATE / SOFT_DELETE']
  old_value   jsonb
  new_value   jsonb
  changed_by  bigint       [ref: > users.id]
  changed_at  timestamp    [not null, default: `now()`]
}

// ── TABLE GROUPS (visual grouping in dbdiagram.io) ──────────

TableGroup "RBAC" {
  role
  permission
  role_permission
}

TableGroup "Users" {
  users
}

TableGroup "League Hierarchy" {
  league
  season
  season_team
  match
  match_result
}

TableGroup "Teams & Players" {
  team
  player
}

TableGroup "Predictions & Leaderboard" {
  prediction
  league_prediction
  leaderboard
}

TableGroup "Support" {
  notification
  audit_log
}
```
