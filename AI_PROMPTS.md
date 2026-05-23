# AI-Assisted Development Log — Family League Prediction Platform

This document records how AI tooling (Claude Code via Anthropic) was used during the development of this project, what was generated, and what was written or reviewed manually.

---

## Tool Used

**Claude Code** (claude-sonnet-4-6) — Anthropic's CLI coding assistant, running inside the VSCode extension.

---

## Development Approach

The project was built incrementally across 16 phases. Each phase was prompted conversationally, with the human providing the specification documents (BRD, PRD, TRD, DATA_MODEL) as context and directing each implementation step. The AI generated code; the human reviewed, approved, and prompted corrections.

---

## Phase-by-Phase Summary

### Phases 1–3 — Project Scaffolding, Database Foundation, Core Framework
**Prompt intent:** Set up the Spring Boot project with all dependencies, Flyway migrations (V1–V3), `BaseEntity`, `ApiResponse`/`PagedResponse`, exception hierarchy, `GlobalExceptionHandler`, and `AsyncConfig`.

**Generated:** `pom.xml`, `application.yml`, `application-dev.yml`, `application-prod.yml`, `logback-spring.xml`, `V1__initial_schema.sql`, `V2__seed_roles_permissions.sql`, `V3__indexes.sql`, all `common/` classes, `AsyncConfig.java`.

**Manually verified:** Migration SQL correctness, Flyway version ordering, exception HTTP status codes.

---

### Phase 4 — Authentication & RBAC
**Prompt intent:** Implement JWT-based stateless authentication, permission-level RBAC, `JwtUtil`, `JwtAuthenticationFilter`, `SecurityConfig`, `CustomUserDetailsService`, `AuthController`.

**Generated:** All auth package files, `SecurityConfig.java`, `AuditorAwareImpl.java`, `JpaConfig.java`.

**Design decision made by human:** Use `Long userId` as JWT principal (not `UserDetails` string) so `@AuthenticationPrincipal Long userId` works cleanly in controllers.

---

### Phase 5 — User Management
**Prompt intent:** User profile endpoints, admin user list and status management.

**Generated:** `UserController`, `UserService`, `UserServiceImpl`, DTOs.

---

### Phase 6 — League & Season Management
**Prompt intent:** League CRUD, Season CRUD, season lifecycle state machine (`UPCOMING → ACTIVE → COMPLETED → CLOSED`), `SeasonGuard`, `SeasonTeam` composite PK via `@EmbeddedId`.

**Generated:** All league package files, `SeasonGuard.java`, `SeasonTeamId.java`.

---

### Phase 7 — Team & Player Management
**Prompt intent:** Team and Player CRUD; `V4__add_team_player_permissions.sql`.

**Generated:** All team package files, `PlayerRole` enum, `V4` migration.

---

### Phase 8 — Match Scheduling
**Prompt intent:** Match entity, `prediction_lock_time = startTime - 1 hour` auto-set, season `predictionLockTime` recalculation on match add/cancel.

**Generated:** All match package files, `MatchStatus` enum, updated `SeasonServiceImpl`.

---

### Phase 9 — Prediction Submission
**Prompt intent:** Match prediction submit/update (upsert), visibility rule (before lock → own only, after → all), `LeaguePrediction` (full-season team ranking), UNIQUE constraint handling via hard-delete pattern.

**Generated:** All prediction package files, `LeaguePrediction` entity and service.

**Tricky decision:** `LeaguePrediction` uses hard delete before re-insert because DB UNIQUE constraints on `(user_id, season_id, predicted_rank)` do not exclude soft-deleted rows.

---

### Phase 10 — Result Publishing & Scoring
**Prompt intent:** `MatchResult` entity, `ResultService.publishResult()`, `ResultPublishedEvent`, async scoring via `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`.

**Generated:** All result/scoring files, `DuplicateResultException`.

**Tricky decision:** `@TransactionalEventListener(AFTER_COMMIT)` prevents scoring from reading an uncommitted result row.

---

### Phase 11 — Leaderboard
**Prompt intent:** `Leaderboard` entity (no soft delete), PostgreSQL `ON CONFLICT DO UPDATE` upsert, DENSE_RANK in Java, admin notification after recalculation.

**Generated:** All leaderboard package files, `UserPointsProjection` interface.

---

### Phase 12 — Notification System & Schedulers
**Prompt intent:** `NotificationService` (JavaMailSender, PENDING → SENT/FAILED), `EmailTemplateService`, `PredictionReminderScheduler`, `ResultAlertScheduler`, idempotency checks, `@EnableScheduling` added to `AsyncConfig`.

**Generated:** All notification package files, both scheduler classes.

---

### Phase 13 — Audit System
**Prompt intent:** `AuditLog` entity (JSONB columns, no BaseEntity), `AuditEntityListener` with ThreadLocal pre-update snapshot, `AuditLogService` with `REQUIRES_NEW` propagation, `SpringContextHolder` for non-Spring entity listeners, `V5` migration.

**Generated:** All audit package files, `SpringContextHolder.java`, `V5__audit_log_table_and_permission.sql`, updated `BaseEntity`.

**Tricky decision:** JPA entity listeners are not Spring-managed beans; `SpringContextHolder` (static `ApplicationContextAware`) provides a workaround.

---

### Phase 14 — API Documentation
**Prompt intent:** `OpenApiConfig` with global JWT bearer scheme, `@Tag` on all 13 controllers.

**Generated:** `OpenApiConfig.java`, `@Tag` imports/annotations across all controllers.

---

### Phase 15 — Testing
**Prompt intent:** Unit tests for scoring (WIN/TIE/NO_RESULT), DENSE_RANK logic, Flyway migration IT, full end-to-end service-layer flow IT with Testcontainers.

**Generated:** `ScoringServiceTest`, `LeaderboardRankingTest`, `AbstractIntegrationTest`, `FlywayMigrationIT`, `FullFlowIT`, `application-it.yml`.

---

### Phase 16 — Final Packaging
**Prompt intent:** `README.md`, `DECISION_LOG.md`, `AI_PROMPTS.md`, `.gitignore` update, security sweep.

**Generated:** This file and the accompanying documents.

---

## What Was Manually Written / Reviewed

- All specification documents (BRD, PRD, TRD, DATA_MODEL) — written by the human before development
- All architectural decisions (DL-001 through DL-008) — decided by the human, captured in TRD
- Business rule constants (scoring values, lock offsets, scheduler crons) — specified by human in config
- Review and approval of every generated file before proceeding to the next phase

## What Was AI-Generated

- All Java source files under `src/main/java/`
- All Flyway migration SQL files
- All test files under `src/test/`
- `pom.xml`, `application.yml` variants, `logback-spring.xml`
- This documentation set (`README.md`, `DECISION_LOG.md`, `AI_PROMPTS.md`)
