# Decision Log — Family League Prediction Platform

Architectural decisions made during the design and build of the platform. Each entry records what was decided, why, what alternatives were considered, and what trade-offs were accepted.

---

## DL-001: PostgreSQL over MySQL

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | Use PostgreSQL as the primary database. |
| **Alternatives**       | MySQL 8.x |
| **Rationale**          | PostgreSQL natively supports `JSONB` (required for audit log `old_value`/`new_value` storage with indexing capability). PostgreSQL has stronger constraint enforcement, full support for partial indexes, and a more mature Hibernate dialect. MySQL `JSON` type lacks the query operators available in JSONB. |
| **Trade-offs**         | MySQL may have slightly easier managed hosting on some cloud providers. Outweighed by the feature advantages. |

---

## DL-002: Soft Delete for All Domain Entities

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | No domain entity is permanently deleted. All deletes set `is_deleted = true` and record `deleted_at` + `deleted_by`. |
| **Alternatives**       | Hard delete; archive tables |
| **Rationale**          | Requirement explicitly states "no records get deleted permanently". Soft delete preserves referential integrity, maintains full history for audit purposes, and allows recovery of accidentally deleted data. |
| **Trade-offs**         | All queries must filter on `is_deleted = false`. Accumulation of soft-deleted rows may require archival over years. |

---

## DL-003: Prediction Lock Time Stored Explicitly

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | `prediction_lock_time` is stored as a column on both `match` and `season`, not derived at query time. |
| **Alternatives**       | Derive at query time: `start_time - INTERVAL '1 hour'` |
| **Rationale**          | Deriving lock time at query time creates a risk of inconsistency if the business rule changes after predictions are submitted. Storing it explicitly: (a) allows DB-level check constraints, (b) allows schedulers to query it directly without computation, (c) makes the lock time immutable once set. |
| **Trade-offs**         | The service layer is responsible for computing and keeping `prediction_lock_time` consistent with `start_time`. |

---

## DL-004: Permission-Level RBAC over Role-Level

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | `@PreAuthorize` guards use named permissions (e.g., `PUBLISH_RESULT`), not role names (e.g., `ROLE_ADMIN`). |
| **Alternatives**       | `hasRole('ADMIN')` checks |
| **Rationale**          | Permission-level control allows future granular assignment without code changes (e.g., a semi-admin who can publish results but not close seasons). Roles alone create a rigid all-or-nothing access model. |
| **Trade-offs**         | JWT must carry the full permission list, increasing token size slightly. Acceptable at this scale. |

---

## DL-005: Async Scoring via Spring Application Events

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | Leaderboard recalculation is triggered by a `ResultPublishedEvent` processed by `@Async @TransactionalEventListener(AFTER_COMMIT)`. |
| **Alternatives**       | Kafka, RabbitMQ, synchronous in-request scoring |
| **Rationale**          | Requirement states scoring must be async. Spring's built-in event mechanism avoids introducing an external message broker, which would add significant operational overhead for a family-scale platform. `AFTER_COMMIT` prevents a race condition where the scoring read would see an uncommitted result. |
| **Trade-offs**         | If the application restarts between result publish and scoring completion, the in-flight event is lost. Acceptable at this scale. An outbox pattern could address this if durability is required in future. |

---

## DL-006: Flyway for Schema Migrations

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | All schema changes are managed via versioned Flyway migration scripts. `ddl-auto: validate` in production. |
| **Alternatives**       | Hibernate `ddl-auto: update`; Liquibase |
| **Rationale**          | Requirement: "App starts successfully from a clean clone following README steps." Flyway guarantees a reproducible schema on every environment. Migration scripts double as documentation of schema evolution. |
| **Trade-offs**         | Developers must write a migration file for every schema change instead of relying on Hibernate auto-generation. This discipline is intentional. |

---

## DL-007: Constructor Injection over Field Injection

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | All Spring beans use constructor injection via `@RequiredArgsConstructor` (Lombok). |
| **Alternatives**       | `@Autowired` field injection |
| **Rationale**          | Constructor injection makes dependencies explicit, enforces immutability on injected fields, and makes unit testing straightforward — beans can be instantiated in tests without a Spring context using `new ServiceImpl(mockA, mockB)`. |
| **Trade-offs**         | Slightly more verbose than `@Autowired`. `@RequiredArgsConstructor` eliminates the verbosity entirely. |

---

## DL-008: JWT Logout via Client-Side Token Discard

| Attribute              | Detail |
|------------------------|--------|
| **Decision**           | `POST /api/v1/auth/logout` is a server-side no-op. The client discards the token. No server-side blacklist is maintained. |
| **Alternatives**       | In-memory or Redis-backed blacklist keyed on JWT `jti` claim |
| **Rationale**          | The platform is stateless by design (`SessionCreationPolicy.STATELESS`). Tokens have a short TTL (1 hour default). For a private family-scale platform, the risk of a stolen token being replayed within its TTL window is acceptable. A token blacklist would require shared state across restarts, adding operational overhead disproportionate to the risk. |
| **Trade-offs**         | A stolen token remains valid until expiry. Mitigated by short TTL and HTTPS-only transport. Adding a `jti` blacklist table is a non-breaking future upgrade if stricter invalidation is required. |
