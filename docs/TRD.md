# Technical Requirements Document (TRD)
**Project:** Family League – Prediction Platform  
**Version:** 1.0  
**Date:** 2026-05-22  
**Status:** Approved  

---

## Table of Contents
1. [Document Control](#1-document-control)
2. [System Overview](#2-system-overview)
3. [Technology Stack](#3-technology-stack)
4. [Architecture](#4-architecture)
5. [Module Structure](#5-module-structure)
6. [Security Design](#6-security-design)
7. [Data Layer Design](#7-data-layer-design)
8. [API Design Standards](#8-api-design-standards)
9. [Async Processing and Scheduling](#9-async-processing-and-scheduling)
10. [Notification System](#10-notification-system)
11. [Audit System](#11-audit-system)
12. [Logging Standards](#12-logging-standards)
13. [Exception Handling](#13-exception-handling)
14. [Configuration Management](#14-configuration-management)
15. [Testing Strategy](#15-testing-strategy)
16. [Decision Log](#16-decision-log)

---

## 1. Document Control

| Attribute    | Detail                                      |
|--------------|---------------------------------------------|
| Document ID  | TRD-FL-001                                  |
| Version      | 1.0                                         |
| Prepared By  | Engineering Team                            |
| Review Date  | 2026-05-22                                  |
| References   | BRD-FL-001, PRD-FL-001, DATA_MODEL v1.1     |

### Revision History

| Version | Date       | Author           | Summary of Changes       |
|---------|------------|------------------|--------------------------|
| 1.0     | 2026-05-22 | Engineering Team | Initial document created |
| 1.1     | 2026-05-23 | Engineering Team | Added MANAGE_SEASON_TEAMS permission; fixed audit listener to use @PreUpdate for old-value capture; added DL-008 JWT logout |

---

## 2. System Overview

Family League is a stateless, RESTful backend service. It exposes a set of role-protected JSON APIs consumed by client applications (Postman during development; a future UI in production). All business logic lives in the service layer. The database is the system of record. Notifications are dispatched asynchronously via a scheduled execution model.

### System Context Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT LAYER                          │
│  Postman / Future Web UI / Mobile App                   │
└──────────────────────────┬──────────────────────────────┘
                           │ HTTPS + JWT
┌──────────────────────────▼──────────────────────────────┐
│                  FAMILY LEAGUE API                       │
│                                                         │
│  ┌─────────────┐  ┌──────────────────┐  ┌────────────┐ │
│  │  Auth       │  │  Business        │  │  Admin     │ │
│  │  Layer      │  │  Service         │  │  Service   │ │
│  │  (JWT)      │  │  Layer           │  │  Layer     │ │
│  └─────────────┘  └────────┬─────────┘  └─────┬──────┘ │
│                            │                   │        │
│  ┌─────────────────────────▼───────────────────▼──────┐ │
│  │              Data Access Layer (JPA)               │ │
│  └────────────────────────────────────────────────────┘ │
│                            │                            │
│  ┌─────────────────────────▼────────────┐              │
│  │   Scheduler / Async Executor         │              │
│  │   (Points, Leaderboard, Email)       │              │
│  └──────────────────────────────────────┘              │
└──────────────────────────┬──────────────────────────────┘
                           │
         ┌─────────────────┴──────────────────┐
         │                                    │
┌────────▼─────────┐                ┌─────────▼────────┐
│   PostgreSQL     │                │  SMTP / Email    │
│   Database       │                │  Provider        │
└──────────────────┘                └──────────────────┘
```

---

## 3. Technology Stack

### 3.1 Core Stack

| Component          | Technology                   | Version   | Justification                                                     |
|--------------------|------------------------------|-----------|-------------------------------------------------------------------|
| Language           | Java                         | 17 LTS    | LTS release; full Spring Boot 3.x support; strong ecosystem       |
| Framework          | Spring Boot                  | 3.x       | Batteries-included; auto-config; production-ready                 |
| Security           | Spring Security              | 6.x       | Industry-standard; integrates natively with JWT and RBAC          |
| ORM                | Spring Data JPA + Hibernate  | 6.x       | Reduces boilerplate; supports entity lifecycle callbacks for audit|
| Validation         | Spring Validation (Jakarta)  | 3.x       | Declarative, annotation-based; integrates with BindingResult      |
| Database           | PostgreSQL                   | 15+       | See Decision Log DL-001                                           |
| DB Migration       | Flyway                       | 9.x       | Version-controlled schema; reproducible from clean clone          |
| Email              | Spring Mail (Jakarta Mail)   | 3.x       | Native Spring integration; configurable SMTP transport            |
| API Documentation  | SpringDoc OpenAPI (Swagger)  | 2.x       | Auto-generates OpenAPI 3.0 spec from annotations                  |
| Build Tool         | Maven                        | 3.9+      | Widely adopted; strong IDE support; predictable dependency management |
| Scheduling         | Spring Scheduler             | Built-in  | No external broker needed for cron-style tasks                    |

### 3.2 Supporting Libraries

| Library             | Purpose                                     |
|---------------------|---------------------------------------------|
| `jjwt` (io.jsonwebtoken) | JWT creation, signing, and validation  |
| `Lombok`            | Boilerplate reduction (getters, builders)   |
| `ModelMapper` / `MapStruct` | DTO ↔ Entity mapping               |
| `Logback`           | Logging to console and file                 |
| `JUnit 5`           | Unit testing                                |
| `Mockito`           | Mocking in unit tests                       |
| `Testcontainers`    | Integration tests against real PostgreSQL   |

---

## 4. Architecture

### 4.1 Layered Architecture

The application follows a strict layered architecture:

```
Controller Layer      → HTTP entry point; request/response mapping; validation trigger
Service Layer         → Business logic; transaction boundaries; event publishing
Repository Layer      → Spring Data JPA repositories; data access only
Domain Layer          → JPA entities; no business logic in entities
DTO Layer             → Request/Response objects; decoupled from entities
Exception Layer       → Custom exceptions; global handler
Config Layer          → Security, scheduler, CORS, OpenAPI configurations
```

### 4.2 Design Principles

| Principle              | Application                                                          |
|------------------------|----------------------------------------------------------------------|
| Interface-driven       | Every service class implements a defined interface (e.g., `MatchService` → `MatchServiceImpl`) |
| Single Responsibility  | Controllers only delegate; services only orchestrate; repos only query |
| Dependency Injection   | All dependencies injected via constructor (not field injection)     |
| Immutable DTOs         | Request and response DTOs use final fields or records where possible|
| Fail-fast validation   | Inputs are validated at the controller boundary before reaching the service |
| No logic in entities   | JPA entities are plain data holders; no service calls, no business rules |

### 4.3 Transaction Management

- All write operations in the service layer are annotated `@Transactional`.
- Read-only queries use `@Transactional(readOnly = true)` for performance.
- Async leaderboard recalculation runs in its own transaction, separate from the result publish transaction.

---

## 5. Module Structure

```
family-league/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/familyleague/
│       │       ├── FamilyLeagueApplication.java
│       │       │
│       │       ├── auth/                         # Authentication (JWT, login, register)
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── dto/
│       │       │   └── util/
│       │       │
│       │       ├── user/                         # User profile management
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── league/                       # League and Season management
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── team/                         # Team and Player management
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── match/                        # Match scheduling and results
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── prediction/                   # Match and league predictions
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── leaderboard/                  # Leaderboard calculation and retrieval
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── notification/                 # Email notifications and history
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── dto/
│       │       │
│       │       ├── audit/                        # Audit log management
│       │       │   ├── controller/
│       │       │   ├── service/
│       │       │   ├── repository/
│       │       │   ├── entity/
│       │       │   └── listener/                 # JPA entity listeners for auto-audit
│       │       │
│       │       ├── scheduler/                    # All scheduled jobs
│       │       │   ├── PredictionReminderScheduler.java
│       │       │   ├── ResultAlertScheduler.java
│       │       │   └── LeaderboardRecalculationJob.java
│       │       │
│       │       ├── common/
│       │       │   ├── entity/
│       │       │   │   └── BaseEntity.java       # @MappedSuperclass with audit fields
│       │       │   ├── exception/
│       │       │   │   ├── GlobalExceptionHandler.java
│       │       │   │   ├── PredictionWindowClosedException.java
│       │       │   │   ├── SeasonClosedException.java
│       │       │   │   └── ResourceNotFoundException.java
│       │       │   ├── response/
│       │       │   │   ├── ApiResponse.java      # Standard response wrapper
│       │       │   │   └── PagedResponse.java
│       │       │   └── util/
│       │       │       └── DateTimeUtils.java
│       │       │
│       │       └── config/
│       │           ├── SecurityConfig.java
│       │           ├── JwtConfig.java
│       │           ├── SchedulerConfig.java
│       │           ├── OpenApiConfig.java
│       │           └── AsyncConfig.java
│       │
│       └── resources/
│           ├── application.yml
│           ├── application-dev.yml
│           ├── application-prod.yml
│           └── db/migration/
│               ├── V1__initial_schema.sql
│               ├── V2__seed_roles_permissions.sql
│               └── V3__indexes.sql
│
└── src/
    └── test/
        ├── java/
        │   └── com/familyleague/
        │       ├── auth/
        │       ├── match/
        │       ├── prediction/
        │       ├── leaderboard/
        │       └── integration/
        └── resources/
            └── application-test.yml
```

---

## 6. Security Design

### 6.1 Authentication Flow

```
POST /auth/login
    │
    ▼
Validate credentials → BCrypt.matches(password, stored_hash)
    │
    ▼
Load user + role + permissions from DB
    │
    ▼
Generate JWT:
    {
      "sub":         "user@example.com",
      "userId":      42,
      "role":        "USER",
      "permissions": ["SUBMIT_PREDICTION", "VIEW_PREDICTIONS"],
      "iat":         <issued-at>,
      "exp":         <expiry>
    }
    │
    ▼
Return { "accessToken": "eyJ...", "expiresIn": 3600 }
```

### 6.2 Request Authorization Flow

```
Incoming Request → JwtAuthenticationFilter
    │
    ├── Extract Bearer token from Authorization header
    ├── Validate signature and expiry
    └── Load SecurityContext with UserDetails + GrantedAuthorities
    │
    ▼
Spring Security FilterChain
    │
    ▼
@PreAuthorize("hasAuthority('PUBLISH_RESULT')")  → permission-level guard
```

### 6.3 JWT Configuration

| Parameter          | Value / Source                                     |
|--------------------|----------------------------------------------------|
| Algorithm          | HS256 (HMAC SHA-256)                               |
| Secret Key         | From `app.jwt.secret` in `application.yml` (externally injected via env) |
| Access Token TTL   | Configurable via `app.jwt.expiry-seconds` (default: 3600) |
| Token location     | `Authorization: Bearer <token>` header             |
| Stateless sessions | `SessionCreationPolicy.STATELESS`                  |

### 6.4 Role and Permission Matrix

| Permission                | ADMIN | USER |
|---------------------------|-------|------|
| `CREATE_LEAGUE`           | ✓     |      |
| `CREATE_SEASON`           | ✓     |      |
| `MANAGE_SEASON_TEAMS`     | ✓     |      |
| `CREATE_MATCH`            | ✓     |      |
| `PUBLISH_RESULT`          | ✓     |      |
| `CLOSE_SEASON`            | ✓     |      |
| `MANAGE_USERS`            | ✓     |      |
| `SEND_NOTIFICATION`       | ✓     |      |
| `SUBMIT_PREDICTION`       |       | ✓    |
| `VIEW_PREDICTIONS`        |       | ✓    |
| `VIEW_ALL_PREDICTIONS`    | ✓     | *    |

> \* Users gain `VIEW_ALL_PREDICTIONS` access at the service layer only after the prediction lock time has passed – it is not a static permission assignment.

### 6.5 HTTPS

Local development certificates are acceptable. Production deployments must use valid TLS certificates. Spring Boot's embedded Tomcat is configured via `server.ssl.*` properties in `application-prod.yml`.

---

## 7. Data Layer Design

### 7.1 BaseEntity

All domain entities extend `BaseEntity`:

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @CreatedBy
    private Long createdBy;

    @LastModifiedBy
    private Long updatedBy;

    @Column(nullable = false)
    private boolean isDeleted = false;

    private LocalDateTime deletedAt;

    private Long deletedBy;
}
```

Enable JPA auditing via `@EnableJpaAuditing` and implement `AuditorAware<Long>` to return the current authenticated user ID from the `SecurityContext`.

### 7.2 Soft Delete Pattern

Soft deletes are applied via a service-layer method:

```java
entity.setDeleted(true);
entity.setDeletedAt(LocalDateTime.now());
entity.setDeletedBy(currentUserId);
repository.save(entity);
```

All repository queries filter on `is_deleted = false` by default via `@Where(clause = "is_deleted = false")` on the entity, or via Specification predicates.

### 7.3 Flyway Migrations

Migrations live in `src/main/resources/db/migration/` following the naming convention `V{version}__{description}.sql`.

| Migration File                   | Contents                                              |
|----------------------------------|-------------------------------------------------------|
| `V1__initial_schema.sql`         | All CREATE TABLE statements (full schema from schema.sql) |
| `V2__seed_roles_permissions.sql` | INSERT for roles, permissions, role_permission        |
| `V3__indexes.sql`                | Performance indexes (audit_log, prediction, leaderboard) |

### 7.4 Repositories

All repositories extend `JpaRepository<T, Long>` and `JpaSpecificationExecutor<T>` for dynamic query support (pagination, filtering, sorting).

Example:

```java
public interface MatchRepository
    extends JpaRepository<Match, Long>, JpaSpecificationExecutor<Match> {

    List<Match> findBySeasonIdAndIsDeletedFalse(Long seasonId);

    Optional<Match> findByIdAndIsDeletedFalse(Long id);

    List<Match> findByPredictionLockTimeBetweenAndIsDeletedFalse(
        LocalDateTime from, LocalDateTime to);
}
```

### 7.5 Pagination, Sort, and Search

All list endpoints accept:

| Parameter   | Type    | Description                                 |
|-------------|---------|---------------------------------------------|
| `page`      | int     | Zero-based page number (default: 0)         |
| `size`      | int     | Page size (default: 20, max: 100)           |
| `sort`      | string  | Field and direction (e.g., `startTime,asc`) |
| `search`    | string  | Optional keyword filter (entity-specific)   |

Implemented via `Pageable` from `PageRequest.of(page, size, Sort.by(...))`.

---

## 8. API Design Standards

### 8.1 Request and Response Structure

All responses are wrapped in a standard envelope:

```json
// Success (single resource)
{
  "success": true,
  "data": { ... },
  "timestamp": "2026-05-22T10:00:00Z"
}

// Success (paginated list)
{
  "success": true,
  "data": {
    "content": [ ... ],
    "page": 0,
    "size": 20,
    "totalElements": 85,
    "totalPages": 5
  },
  "timestamp": "2026-05-22T10:00:00Z"
}

// Error
{
  "success": false,
  "error": {
    "code": "PREDICTION_WINDOW_CLOSED",
    "message": "Predictions for this match closed at 2026-04-05T13:00:00Z",
    "path": "/api/v1/matches/42/predictions"
  },
  "timestamp": "2026-05-22T10:00:00Z"
}
```

### 8.2 HTTP Status Codes

| Status | When Used                                                 |
|--------|-----------------------------------------------------------|
| 200    | Successful GET, successful PUT                            |
| 201    | Successful resource creation (POST)                       |
| 204    | Successful delete (soft delete)                           |
| 400    | Request validation failure (missing/invalid fields)       |
| 401    | Missing or invalid JWT token                              |
| 403    | Valid token but insufficient permissions; closed season   |
| 404    | Resource not found                                        |
| 409    | Business rule conflict (prediction window closed, duplicate) |
| 500    | Unhandled server error                                    |

### 8.3 API Versioning

All endpoints are prefixed with `/api/v1/` to allow non-breaking future versioning.

### 8.4 DTO Validation

Request DTOs use Jakarta Bean Validation annotations:

```java
public class SubmitPredictionRequest {

    @NotNull(message = "Predicted winner team is required")
    private Long predictedWinnerTeamId;

    @NotNull(message = "Predicted toss winner is required")
    private Long predictedTossWinnerId;

    private Long predictedPlayerOfMatchId; // optional
}
```

Validation errors return HTTP 400 with a structured list of field errors.

### 8.5 OpenAPI Documentation

SpringDoc OpenAPI auto-generates documentation at `/swagger-ui.html` and `/api-docs`. All controllers, DTOs, and significant fields must be annotated with `@Operation`, `@ApiResponse`, and `@Schema` where the generated documentation would otherwise be ambiguous.

---

## 9. Async Processing and Scheduling

### 9.1 Async Configuration

Spring's `@EnableAsync` is configured with a custom `ThreadPoolTaskExecutor`:

```yaml
app:
  async:
    core-pool-size: 4
    max-pool-size: 10
    queue-capacity: 100
    thread-name-prefix: "fl-async-"
```

### 9.2 Leaderboard Recalculation

Triggered immediately after result publication:

```
ResultService.publishResult()
    │
    ▼
Save MatchResult entity
    │
    ▼
ApplicationEventPublisher.publishEvent(new ResultPublishedEvent(matchId))
    │
    ▼
@Async @EventListener
LeaderboardService.recalculateForMatch(matchId)
    │
    ├── Fetch all predictions for this match
    ├── Compare with result → calculate points_earned per prediction
    ├── Update prediction.points_earned
    ├── Aggregate total points per user for the season
    ├── Upsert leaderboard rows
    └── Recalculate and assign rank_position values
    │
    ▼
NotificationService.notifyAdminLeaderboardUpdated(seasonId)
```

### 9.3 Scheduled Jobs

All schedules are configurable via `application.yml`. The cron expressions below are defaults.

| Job                           | Default Schedule         | Description                                         |
|-------------------------------|--------------------------|-----------------------------------------------------|
| `PredictionReminderScheduler` | Every 30 minutes         | Finds matches approaching lock; emails unpredicted users |
| `ResultAlertScheduler`        | Every 60 minutes         | Finds completed matches with no result; emails Admin |

```yaml
app:
  scheduler:
    prediction-reminder-cron: "0 0/30 * * * *"
    result-alert-cron: "0 0 * * * *"
    reminder-window-hours: 2  # send reminder if lock is within 2 hours
```

### 9.4 Idempotency

Scheduled jobs must be idempotent:
- Reminders track sent notifications in the `notification` table; a user does not receive duplicate reminders for the same match.
- Leaderboard recalculation uses upsert (`MERGE` / `ON CONFLICT DO UPDATE`) – running it twice produces the same result.

---

## 10. Notification System

### 10.1 Email Flow

```
NotificationService.send(userId, type, subject, body)
    │
    ├── Create Notification entity with status = PENDING
    └── Save to DB
    │
    ▼
JavaMailSender.send(message)
    │
    ├── Success → update status = SENT, sent_at = now()
    └── Failure → update status = FAILED, log error
```

### 10.2 Bulk Notification

`POST /notifications/bulk` accepts:

```json
{
  "userIds": [1, 2, 3, 5],
  "type": "CUSTOM",
  "subject": "League starts tomorrow!",
  "body": "Don't forget to submit your league predictions."
}
```

Each email is sent and stored as an individual `Notification` record.

### 10.3 Email Templates

All email bodies are defined as configurable templates (either in properties or in template files using Thymeleaf/plain text), not hardcoded strings.

### 10.4 SMTP Configuration

```yaml
spring:
  mail:
    host: ${MAIL_HOST:smtp.gmail.com}
    port: ${MAIL_PORT:587}
    username: ${MAIL_USERNAME}
    password: ${MAIL_PASSWORD}
    properties:
      mail.smtp.auth: true
      mail.smtp.starttls.enable: true
```

All SMTP credentials are injected via environment variables – never hardcoded.

---

## 11. Audit System

### 11.1 Auto-Audit via JPA Listener

A JPA entity listener uses `@PreUpdate` to snapshot the old state **before** the flush, then `@PostPersist` / `@PostUpdate` to write the `AuditLog` record with both old and new values.

```java
@Component
public class AuditEntityListener {

    // ThreadLocal holds the pre-update snapshot so @PostUpdate can read it
    private static final ThreadLocal<String> oldValueHolder = new ThreadLocal<>();

    @PreUpdate
    public void captureOldValue(BaseEntity entity) {
        // Snapshot current (pre-flush) state before Hibernate writes the row
        oldValueHolder.set(AuditSerializer.serialize(entity));
    }

    @PostPersist
    public void onInsert(BaseEntity entity) {
        AuditLogService.record(entity, AuditAction.INSERT, null, AuditSerializer.serialize(entity));
    }

    @PostUpdate
    public void onUpdate(BaseEntity entity) {
        String oldValue = oldValueHolder.get();
        oldValueHolder.remove();
        AuditLogService.record(entity, AuditAction.UPDATE, oldValue, AuditSerializer.serialize(entity));
    }
}
```

> **Why `@PreUpdate` is required:** JPA fires `@PostUpdate` after Hibernate has already merged the new state into the entity, so the "before" values are gone. `@PreUpdate` fires before the SQL UPDATE is issued — the entity still holds the old field values at that point, making it the only safe place to take the snapshot.

### 11.2 Audit Record Contents

| Field         | Source                                                   |
|---------------|----------------------------------------------------------|
| `entity_name` | Simple class name of the entity (e.g., `Match`)         |
| `entity_id`   | Entity primary key                                       |
| `action`      | INSERT / UPDATE / SOFT_DELETE                           |
| `old_value`   | JSON serialisation of entity state before change (null on INSERT) |
| `new_value`   | JSON serialisation of entity state after change          |
| `changed_by`  | Authenticated user ID from SecurityContext              |
| `changed_at`  | `LocalDateTime.now()` at time of write                  |

### 11.3 Limitations

- Batch operations (e.g., bulk leaderboard upserts) generate a single audit entry per row.
- Audit records themselves are never modified or deleted.

---

## 12. Logging Standards

### 12.1 Log Levels

| Level  | Used For                                                               |
|--------|------------------------------------------------------------------------|
| ERROR  | Exceptions that affect the outcome of a request or job; email failures |
| WARN   | Degraded state; retryable issues; non-critical violations              |
| INFO   | Business events: user registered, result published, job started/ended  |
| DEBUG  | Diagnostic detail: SQL queries, token parsing, lock time evaluations   |

### 12.2 Log Destinations

```yaml
logging:
  file:
    name: logs/family-league.log
  level:
    root: INFO
    com.familyleague: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

Both console and file output are mandatory. Log file rolling is configured via Logback (`logback-spring.xml`) with daily rotation and 30-day retention.

### 12.3 What Must Be Logged

| Event                                    | Level |
|------------------------------------------|-------|
| Application startup / shutdown           | INFO  |
| User login (success and failure)         | INFO  |
| Result published                         | INFO  |
| Prediction window rejection              | WARN  |
| Leaderboard recalculation started/ended  | INFO  |
| Email sent successfully                  | INFO  |
| Email delivery failure                   | ERROR |
| Scheduled job execution start/end        | INFO  |
| Unhandled exception                      | ERROR |

---

## 13. Exception Handling

### 13.1 Custom Exception Hierarchy

```
RuntimeException
    └── FamilyLeagueException         # base
            ├── ResourceNotFoundException       (404)
            ├── PredictionWindowClosedException (409)
            ├── SeasonClosedException           (403)
            ├── DuplicatePredictionException    (409)
            └── UnauthorisedActionException     (403)
```

### 13.2 Global Exception Handler

`@RestControllerAdvice` intercepts all exceptions and maps them to the standard error response:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> handle(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(ApiResponse.error(ex.getCode(), ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handle(MethodArgumentNotValidException ex) {
        // collect all field errors into structured list
        return ResponseEntity.status(400).body(ApiResponse.validationError(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return ResponseEntity.status(500).body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
```

---

## 14. Configuration Management

### 14.1 Configuration File Structure

```yaml
# application.yml (shared defaults)
spring:
  application:
    name: family-league

server:
  port: 8080

app:
  jwt:
    secret: ${JWT_SECRET}
    expiry-seconds: 3600

  scheduler:
    prediction-reminder-cron: "0 0/30 * * * *"
    result-alert-cron: "0 0 * * * *"
    reminder-window-hours: 2

  prediction:
    match-lock-offset-hours: 1
    league-lock-offset-hours: 4

  notification:
    admin-email: ${ADMIN_EMAIL}
    from-address: ${MAIL_FROM}
```

### 14.2 Environment-Specific Profiles

| Profile | File                    | Purpose                           |
|---------|-------------------------|-----------------------------------|
| default | `application.yml`       | Shared cross-environment settings |
| dev     | `application-dev.yml`   | Local development overrides       |
| prod    | `application-prod.yml`  | Production: SSL, pool sizes, etc. |
| test    | `application-test.yml`  | Test DB, mock mail, reduced TTL   |

### 14.3 Secrets Management

No secrets appear in committed files. All sensitive values are injected via environment variables:

| Env Variable   | Purpose                     |
|----------------|-----------------------------|
| `JWT_SECRET`   | JWT signing key             |
| `DB_URL`       | PostgreSQL connection URL   |
| `DB_USERNAME`  | Database user               |
| `DB_PASSWORD`  | Database password           |
| `MAIL_USERNAME`| SMTP username               |
| `MAIL_PASSWORD`| SMTP password               |
| `MAIL_FROM`    | Sender email address        |
| `ADMIN_EMAIL`  | Admin alert recipient       |

---

## 15. Testing Strategy

### 15.1 Test Pyramid

| Layer              | Framework              | What is tested                                      |
|--------------------|------------------------|-----------------------------------------------------|
| Unit tests         | JUnit 5 + Mockito      | Service logic, lock enforcement, scoring rules      |
| Integration tests  | Testcontainers + JPA   | Repository queries, DB constraints, Flyway migrations |
| API / E2E tests    | MockMvc or RestAssured | Full request-response flow per endpoint             |

### 15.2 Key Test Cases (Must have)

| Test Case                                      | Type        |
|------------------------------------------------|-------------|
| Prediction rejected after lock time            | Unit + API  |
| Points correctly calculated for WIN result     | Unit        |
| Points correctly calculated for TIE result     | Unit        |
| Points_earned cannot be submitted via API      | API         |
| Other users' predictions hidden before lock    | API         |
| Other users' predictions visible after lock    | API         |
| Leaderboard UNIQUE constraint enforced         | Integration |
| League prediction duplicate rank rejected      | API + DB    |
| Season CLOSED blocks all writes               | API         |
| Flyway migration runs clean from scratch       | Integration |

---

## 16. Decision Log

### DL-001: PostgreSQL over MySQL

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | Use PostgreSQL as the primary database.                                             |
| Alternatives considered | MySQL 8.x                                                          |
| Rationale  | PostgreSQL natively supports `JSONB` (required for audit log old/new value storage with indexing capability). PostgreSQL has stronger constraint enforcement, full support for partial indexes, and more mature Hibernate dialect. MySQL `JSON` type lacks the query operators available in JSONB. PostgreSQL is the standard choice for Spring Boot + JPA projects in the enterprise. |
| Trade-offs | MySQL may have slightly easier managed hosting options on some cloud providers. This is outweighed by the feature advantages. |

### DL-002: Soft Delete for All Domain Entities

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | No domain entity is permanently deleted. All deletes set `is_deleted = true`.       |
| Rationale  | Requirement explicitly states: "No records get deleted permanently". Soft delete preserves referential integrity, maintains a full history for audit purposes, and allows recovery of accidentally deleted data. |
| Trade-offs | Queries must always filter on `is_deleted = false`. This is enforced via `@Where` on entities and tested. Accumulation of soft-deleted rows over years may require archival. |

### DL-003: Prediction Lock Stored Explicitly

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | `prediction_lock_time` is stored as a column on both `match` and `season`, not derived at query time. |
| Rationale  | Deriving lock time at query time (`start_time - INTERVAL '1 hour'`) creates a risk of inconsistency if the business rule changes after predictions are already submitted. Storing it explicitly: (a) allows DB-level check constraints, (b) allows schedulers to query it directly without computation, (c) makes the lock time immutable once set – matches the spirit of the requirement that the lock must be reliably enforced. |
| Trade-offs | Requires the application to compute and store the lock time on match creation. Service layer responsibility to keep it consistent with start_time. |

### DL-004: Permission-Level RBAC over Role-Level

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | Spring Security `@PreAuthorize` uses named permissions (e.g., `PUBLISH_RESULT`), not role names (e.g., `ROLE_ADMIN`). |
| Rationale  | Permission-level control allows future granular assignment without code changes (e.g., a semi-admin who can publish results but not close seasons). Roles alone create a rigid all-or-nothing access model. |
| Trade-offs | Requires the JWT to carry permissions, increasing token size slightly. Manageable. |

### DL-005: Async Leaderboard Recalculation via Application Events

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | Leaderboard recalculation is triggered by a Spring `ApplicationEvent` published after result save, processed by an `@Async @EventListener`. |
| Rationale  | Requirement explicitly states leaderboard calculation must be async. Using Spring's built-in event mechanism avoids introducing an external message broker (Kafka, RabbitMQ) which would add significant operational overhead for a family-scale platform. The event model is testable and observable within the same JVM. |
| Trade-offs | If the application restarts between result publish and recalculation completion, the event is lost. For this scale and use case, this is an acceptable risk. A future version could use an outbox pattern if reliability requires it. |

### DL-006: Flyway for Database Migrations

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | All schema changes are managed via Flyway migration scripts.                        |
| Rationale  | Requirement: "App starts successfully from a clean clone following README steps." Flyway guarantees a reproducible schema on any environment. Migration scripts double as documentation of schema evolution. |
| Trade-offs | Developers must write migration files for every schema change instead of letting Hibernate auto-generate DDL. This discipline is intentional. |

### DL-007: Constructor Injection over Field Injection

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | All Spring beans use constructor injection (`@RequiredArgsConstructor` via Lombok, or explicit constructors). |
| Rationale  | Constructor injection makes dependencies explicit, supports immutability, and makes unit testing easier (no Spring context needed for instantiation). Field injection with `@Autowired` hides dependencies and cannot be used without a Spring context. |
| Trade-offs | Slightly more verbose than `@Autowired` on fields. Lombok's `@RequiredArgsConstructor` eliminates the verbosity. |

### DL-008: JWT Logout via Client-Side Token Discard

| Attribute  | Detail                                                                              |
|------------|-------------------------------------------------------------------------------------|
| Decision   | `POST /auth/logout` is a no-op on the server. The client discards the token. No server-side token blacklist is maintained. |
| Alternatives considered | In-memory or Redis-backed token blacklist keyed on JWT `jti` (JWT ID) claim. |
| Rationale  | The platform is stateless by design (`SessionCreationPolicy.STATELESS`). Tokens have a short TTL (default 1 hour, configurable). For a private family-scale platform, the security risk of a stolen token being replayed within its TTL window is acceptable. A token blacklist would require shared state across restarts (Redis or DB), adding operational overhead disproportionate to the risk. User story US-A4 ("Log out and invalidate my session") is satisfied by the client discarding the token; the API endpoint exists purely as a clean integration point for future client applications. |
| Trade-offs | A stolen token remains valid until expiry. Mitigated by short TTL and HTTPS-only transport. If stricter invalidation is required in a future version, adding a `jti` blacklist table is a non-breaking change. |
