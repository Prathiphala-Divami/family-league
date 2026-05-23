# Family League — Cricket Match Prediction Platform

A Spring Boot REST API for managing cricket prediction leagues. Users submit match and season predictions; an async scoring engine awards points after each result; a leaderboard tracks season standings.

---

## Prerequisites

| Tool          | Version  | Notes                                    |
|---------------|----------|------------------------------------------|
| Java          | 17       | `java -version` must show 17+            |
| Maven         | 3.9+     | Bundled `./mvnw` wrapper can be used     |
| PostgreSQL    | 15+      | Running locally or via Docker            |
| SMTP server   | Any      | MailHog recommended for local dev        |
| Docker        | Any      | Required only for Testcontainers tests   |

---

## Environment Variables

All secrets and environment-specific config come from the shell — nothing is hardcoded.

| Variable        | Description                                   | Example                                         |
|-----------------|-----------------------------------------------|-------------------------------------------------|
| `DB_URL`        | JDBC connection string                        | `jdbc:postgresql://localhost:5432/familyleague` |
| `DB_USERNAME`   | Database username                             | `fluser`                                        |
| `DB_PASSWORD`   | Database password                             | *(your password)*                               |
| `JWT_SECRET`    | HS256 signing key — minimum 32 characters     | *(random 64-char string)*                       |
| `MAIL_HOST`     | SMTP host                                     | `localhost` (MailHog) or `smtp.gmail.com`       |
| `MAIL_PORT`     | SMTP port                                     | `1025` (MailHog) or `587`                       |
| `MAIL_USERNAME` | SMTP username                                 | `test`                                          |
| `MAIL_PASSWORD` | SMTP password                                 | `test`                                          |
| `MAIL_FROM`     | From address for outgoing emails              | `noreply@familyleague.local`                    |
| `ADMIN_EMAIL`   | Admin alert recipient                         | `admin@familyleague.local`                      |

---

## Running Locally (from a clean clone)

### 1 — Start PostgreSQL

```bash
docker run -d --name fl-db -p 5432:5432 \
  -e POSTGRES_DB=familyleague \
  -e POSTGRES_USER=fluser \
  -e POSTGRES_PASSWORD=flpass \
  postgres:15-alpine
```

### 2 — Start MailHog (captures emails in the browser at localhost:8025)

```bash
docker run -d --name fl-mail -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

### 3 — Export environment variables

```bash
export DB_URL="jdbc:postgresql://localhost:5432/familyleague"
export DB_USERNAME=fluser
export DB_PASSWORD=flpass
export JWT_SECRET="replace-this-with-a-random-64-character-secret-string!!"
export MAIL_HOST=localhost
export MAIL_PORT=1025
export MAIL_USERNAME=test
export MAIL_PASSWORD=test
export MAIL_FROM=noreply@familyleague.local
export ADMIN_EMAIL=admin@familyleague.local
```

### 4 — Run the application

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

Flyway will apply all migrations automatically on first start.

### 5 — Verify startup

- API base: `http://localhost:8080/api/v1/`
- Swagger UI: `http://localhost:8080/swagger-ui.html` — click **Authorize** and paste a JWT
- Log file: `logs/family-league.log`

---

## First-User Setup

The registration endpoint assigns the `USER` role by default. To create your first admin:

```bash
# 1. Register
curl -s -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@test.com","password":"admin1234"}'

# 2. Promote to ADMIN in psql
psql $DB_URL -U $DB_USERNAME -c \
  "UPDATE users SET role_id = (SELECT id FROM role WHERE role_name='ADMIN') WHERE email='admin@test.com';"
```

---

## Running Tests

```bash
# Unit tests only (fast — no Docker required)
./mvnw test -Dtest="ScoringServiceTest,LeaderboardRankingTest"

# Integration tests (Docker must be running — Testcontainers auto-provisions PostgreSQL)
./mvnw test -Dtest="FlywayMigrationIT,FullFlowIT"

# Full test suite
./mvnw test
```

---

## Project Documentation

| Document                               | Description                                              |
|----------------------------------------|----------------------------------------------------------|
| [docs/BRD.md](docs/BRD.md)            | Business Requirements Document                           |
| [docs/PRD.md](docs/PRD.md)            | Product Requirements Document                            |
| [docs/TRD.md](docs/TRD.md)            | Technical Requirements Document                          |
| [DATA_MODEL.md](DATA_MODEL.md)         | Full database schema and entity relationships            |
| [TASKS.md](TASKS.md)                   | Phase-by-phase development task breakdown                |
| [DECISION_LOG.md](DECISION_LOG.md)     | Key architectural decisions (DL-001 through DL-008)      |
| [AI_PROMPTS.md](AI_PROMPTS.md)         | AI-assisted development log                              |

---

## API Overview

| Area                | Base Path                                  | Key Permission          |
|---------------------|--------------------------------------------|-------------------------|
| Authentication      | `/api/v1/auth`                             | Public                  |
| Users               | `/api/v1/users`                            | `MANAGE_USERS`          |
| Leagues             | `/api/v1/leagues`                          | `CREATE_LEAGUE`         |
| Seasons             | `/api/v1/leagues/{id}/seasons`             | `CREATE_SEASON`         |
| Teams               | `/api/v1/teams`                            | `MANAGE_TEAMS`          |
| Players             | `/api/v1/teams/{id}/players`               | `MANAGE_PLAYERS`        |
| Matches             | `/api/v1/seasons/{id}/matches`             | `CREATE_MATCH`          |
| Match Results       | `/api/v1/matches/{id}/result`              | `PUBLISH_RESULT`        |
| Predictions         | `/api/v1/matches/{id}/predictions`         | `SUBMIT_PREDICTION`     |
| League Predictions  | `/api/v1/seasons/{id}/league-predictions`  | `SUBMIT_PREDICTION`     |
| Leaderboard         | `/api/v1/seasons/{id}/leaderboard`         | Authenticated           |
| Notifications       | `/api/v1/notifications`                    | `SEND_NOTIFICATION`     |
| Audit Log           | `/api/v1/audit-logs`                       | `VIEW_AUDIT_LOGS`       |

---

## Architecture Notes

- **Stateless JWT auth** — `Authorization: Bearer <token>` on every request
- **Permission-level RBAC** — `@PreAuthorize("hasAuthority('PERMISSION_NAME')")` on all write endpoints
- **Async scoring** — result publish fires a `ResultPublishedEvent`; `@TransactionalEventListener(AFTER_COMMIT)` + `@Async` scores predictions after the result transaction commits
- **Soft delete** — no domain entity is permanently deleted; `is_deleted = true` is the pattern throughout
- **Flyway migrations** — schema is versioned V1 through V5; `ddl-auto: validate`
- **Audit trail** — `AuditEntityListener` on `BaseEntity` records INSERT / UPDATE / SOFT_DELETE to the `audit_log` table with JSONB snapshots
