# Family League — Development Task Breakdown
**Total Phases:** 16 | **Total Tasks:** 75 | **Status:** Not Started

> Work through phases sequentially. Each phase depends on the one before it unless noted.  
> Mark tasks `[ ]` → `[~]` (in progress) → `[x]` (done) as you go.

---

## Phase 1 — Project Scaffolding
> Foundation. Everything else depends on this.

### Task 1.1 — Verify / configure Spring Boot parent POM
- [ ] 1.1.1 Confirm `<parent>` uses `spring-boot-starter-parent` version `3.x`
- [ ] 1.1.2 Set `<java.version>17</java.version>` in properties
- [ ] 1.1.3 Set `<packaging>jar</packaging>`
- [ ] 1.1.4 Set `groupId`, `artifactId`, `version`, `name` fields correctly
- [ ] 1.1.5 Verify `mvn clean package` compiles without errors

### Task 1.2 — Add all required dependencies to pom.xml
- [ ] 1.2.1 `spring-boot-starter-web`
- [ ] 1.2.2 `spring-boot-starter-security`
- [ ] 1.2.3 `spring-boot-starter-data-jpa`
- [ ] 1.2.4 `spring-boot-starter-mail`
- [ ] 1.2.5 `spring-boot-starter-validation`
- [ ] 1.2.6 `flyway-core` + `flyway-database-postgresql`
- [ ] 1.2.7 `postgresql` driver (runtime scope)
- [ ] 1.2.8 `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (io.jsonwebtoken)
- [ ] 1.2.9 `lombok` (provided scope) + annotation processor config
- [ ] 1.2.10 `mapstruct` + `mapstruct-processor`
- [ ] 1.2.11 `springdoc-openapi-starter-webmvc-ui`
- [ ] 1.2.12 `spring-boot-starter-test` (test scope)
- [ ] 1.2.13 `testcontainers` BOM + `postgresql` testcontainer (test scope)
- [ ] 1.2.14 Verify `mvn dependency:tree` shows no version conflicts

### Task 1.3 — Set up application.yml with all config keys
- [ ] 1.3.1 Create `src/main/resources/application.yml` — shared defaults:
  - `spring.application.name`
  - `server.port: 8080`
  - `app.jwt.secret`, `app.jwt.expiry-seconds`
  - `app.scheduler.prediction-reminder-cron`, `result-alert-cron`, `reminder-window-hours`
  - `app.prediction.match-lock-offset-hours: 1`, `league-lock-offset-hours: 4`
  - `app.notification.admin-email`, `from-address`
  - `app.async.core-pool-size`, `max-pool-size`, `queue-capacity`, `thread-name-prefix`
  - `spring.mail.*` all pointing to env vars
  - `spring.datasource.*` all pointing to env vars
  - `spring.jpa.hibernate.ddl-auto: validate`
  - `spring.flyway.enabled: true`
  - `logging.file.name`, `logging.level.*`
- [ ] 1.3.2 Create `src/main/resources/application-dev.yml`:
  - Override `spring.flyway.baseline-on-migrate: true`
  - Set `spring.jpa.show-sql: true`
  - Set `logging.level.com.example: DEBUG`
- [ ] 1.3.3 Create `src/main/resources/application-prod.yml`:
  - `server.ssl.*` placeholders
  - `spring.jpa.show-sql: false`
  - Tighter connection pool settings
- [ ] 1.3.4 Create `src/test/resources/application-test.yml`:
  - `spring.flyway.enabled: false` (Testcontainers handles schema)
  - Mock SMTP / `spring.mail.host: localhost`
  - Reduced JWT TTL for tests

### Task 1.4 — Create full package structure
- [ ] 1.4.1 Create package `com.example.prathiphala_family_league.auth` with sub-packages: `controller`, `service`, `dto`, `util`
- [ ] 1.4.2 Create package `...user` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.3 Create package `...league` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.4 Create package `...team` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.5 Create package `...match` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.6 Create package `...prediction` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.7 Create package `...leaderboard` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.8 Create package `...notification` with sub-packages: `controller`, `service`, `repository`, `entity`, `dto`
- [ ] 1.4.9 Create package `...audit` with sub-packages: `controller`, `service`, `repository`, `entity`, `listener`
- [ ] 1.4.10 Create package `...scheduler`
- [ ] 1.4.11 Create package `...common.entity`, `...common.exception`, `...common.response`, `...common.util`
- [ ] 1.4.12 Create package `...config`
- [ ] 1.4.13 Place a `.gitkeep` or placeholder class in each empty package so Git tracks them

### Task 1.5 — Configure Logback (console + rolling file)
- [ ] 1.5.1 Create `src/main/resources/logback-spring.xml`
- [ ] 1.5.2 Add `CONSOLE` appender with pattern: `%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n`
- [ ] 1.5.3 Add `FILE` appender pointing to `logs/family-league.log`
- [ ] 1.5.4 Add `RollingPolicy`: daily rollover, max 30 days history, max 100MB per file
- [ ] 1.5.5 Set root level `INFO`; set `com.example` package level `DEBUG`
- [ ] 1.5.6 Verify app starts and writes to both console and `logs/family-league.log`

---

## Phase 2 — Database Foundation
> Schema versioned and reproducible from a clean clone.

### Task 2.1 — Configure PostgreSQL datasource (env-var driven)
- [ ] 2.1.1 In `application.yml` set:
  ```yaml
  spring:
    datasource:
      url: ${DB_URL}
      username: ${DB_USERNAME}
      password: ${DB_PASSWORD}
      driver-class-name: org.postgresql.Driver
  ```
- [ ] 2.1.2 Set HikariCP pool: `maximum-pool-size: 10`, `minimum-idle: 2`
- [ ] 2.1.3 In `application-dev.yml` add example `.env` comment documenting required env vars
- [ ] 2.1.4 Confirm app fails fast with clear error if DB env vars are missing

### Task 2.2 — Configure Flyway baseline
- [ ] 2.2.1 In `application.yml`:
  ```yaml
  spring:
    flyway:
      enabled: true
      locations: classpath:db/migration
      baseline-on-migrate: false
  ```
- [ ] 2.2.2 In `application-dev.yml` override `baseline-on-migrate: true`
- [ ] 2.2.3 Create directory `src/main/resources/db/migration/`

### Task 2.3 — Write V1__initial_schema.sql
- [ ] 2.3.1 `CREATE TABLE role` (id, role_name, description + BaseEntity columns)
- [ ] 2.3.2 `CREATE TABLE permission` (id, name, description)
- [ ] 2.3.3 `CREATE TABLE role_permission` (role_id, permission_id, PK composite)
- [ ] 2.3.4 `CREATE TABLE users` (all columns, UNIQUE on email)
- [ ] 2.3.5 `CREATE TABLE league` (all columns)
- [ ] 2.3.6 `CREATE TABLE season` (all columns, FK to league)
- [ ] 2.3.7 `CREATE TABLE season_team` (season_id, team_id, PK composite, FKs)
- [ ] 2.3.8 `CREATE TABLE team` (all columns)
- [ ] 2.3.9 `CREATE TABLE player` (all columns, FK to team)
- [ ] 2.3.10 `CREATE TABLE match` (all columns, FKs to season + team x2)
- [ ] 2.3.11 `CREATE TABLE match_result` (all columns, UNIQUE on match_id)
- [ ] 2.3.12 `CREATE TABLE prediction` (all columns, UNIQUE(user_id, match_id))
- [ ] 2.3.13 `CREATE TABLE league_prediction` (all columns, two UNIQUE constraints)
- [ ] 2.3.14 `CREATE TABLE leaderboard` (all columns, UNIQUE(season_id, user_id))
- [ ] 2.3.15 `CREATE TABLE notification` (all columns)
- [ ] 2.3.16 `CREATE TABLE audit_log` (all columns, JSONB for old_value / new_value)
- [ ] 2.3.17 Run Flyway against a local DB and confirm V1 applies cleanly

### Task 2.4 — Write V2__seed_roles_permissions.sql
- [ ] 2.4.1 INSERT roles: `ADMIN`, `USER`
- [ ] 2.4.2 INSERT all 11 permissions: `CREATE_LEAGUE`, `CREATE_SEASON`, `MANAGE_SEASON_TEAMS`, `CREATE_MATCH`, `PUBLISH_RESULT`, `CLOSE_SEASON`, `MANAGE_USERS`, `SEND_NOTIFICATION`, `SUBMIT_PREDICTION`, `VIEW_PREDICTIONS`, `VIEW_ALL_PREDICTIONS`
- [ ] 2.4.3 INSERT `role_permission` rows: map all admin permissions to ADMIN role; `SUBMIT_PREDICTION` and `VIEW_PREDICTIONS` to USER role
- [ ] 2.4.4 Run V2 and confirm roles + permissions are queryable

### Task 2.5 — Write V3__indexes.sql
- [ ] 2.5.1 Index on `audit_log(entity_name, entity_id)`
- [ ] 2.5.2 Index on `audit_log(changed_at DESC)`
- [ ] 2.5.3 Index on `prediction(match_id)` — for bulk scoring queries
- [ ] 2.5.4 Index on `prediction(user_id)` — for user history queries
- [ ] 2.5.5 Index on `leaderboard(season_id, rank_position ASC)` — for leaderboard fetch order
- [ ] 2.5.6 Index on `match(season_id, start_time ASC)` — for scheduler queries
- [ ] 2.5.7 Index on `match(prediction_lock_time)` — for reminder scheduler
- [ ] 2.5.8 Index on `notification(user_id, type, status)` — for idempotency check
- [ ] 2.5.9 Run V3 and confirm all indexes exist

---

## Phase 3 — Core Framework
> Shared plumbing used by every feature.

### Task 3.1 — BaseEntity (@MappedSuperclass)
- [ ] 3.1.1 Create `common/entity/BaseEntity.java` annotated `@MappedSuperclass` and `@EntityListeners(AuditingEntityListener.class)`
- [ ] 3.1.2 Fields: `id` (`@Id @GeneratedValue IDENTITY`), `createdAt` (`@CreatedDate`), `updatedAt` (`@LastModifiedDate`), `createdBy` (`@CreatedBy`), `updatedBy` (`@LastModifiedBy`), `isDeleted` (default false), `deletedAt`, `deletedBy`
- [ ] 3.1.3 Add `@EnableJpaAuditing(auditorAwareRef = "auditorProvider")` to main application class or a `JpaConfig`
- [ ] 3.1.4 Verify Hibernate generates the correct DDL for a test entity extending BaseEntity

### Task 3.2 — AuditorAware<Long>
- [ ] 3.2.1 Create `config/AuditorAwareImpl.java` implementing `AuditorAware<Long>`
- [ ] 3.2.2 In `getCurrentAuditor()`: extract `userId` from `SecurityContextHolder` → `Authentication` → principal
- [ ] 3.2.3 Return `Optional.empty()` for unauthenticated/system operations (e.g. Flyway startup)
- [ ] 3.2.4 Register as `@Bean("auditorProvider")`

### Task 3.3 — ApiResponse<T> and PagedResponse<T>
- [ ] 3.3.1 Create `common/response/ApiResponse.java` with fields: `boolean success`, `T data`, `ErrorDetail error`, `LocalDateTime timestamp`
- [ ] 3.3.2 Add static factory: `ApiResponse.success(T data)`
- [ ] 3.3.3 Add static factory: `ApiResponse.error(String code, String message)`
- [ ] 3.3.4 Add static factory: `ApiResponse.validationError(List<FieldError> errors)`
- [ ] 3.3.5 Create `common/response/ErrorDetail.java` with fields: `String code`, `String message`, `String path`, `List<FieldError> fieldErrors`
- [ ] 3.3.6 Create `common/response/PagedResponse.java` wrapping Spring's `Page<T>` into: `content`, `page`, `size`, `totalElements`, `totalPages`

### Task 3.4 — Custom exception hierarchy
- [ ] 3.4.1 Create `common/exception/FamilyLeagueException.java` (abstract base, holds `errorCode`)
- [ ] 3.4.2 `ResourceNotFoundException.java` extends base — HTTP 404
- [ ] 3.4.3 `PredictionWindowClosedException.java` extends base — HTTP 409
- [ ] 3.4.4 `SeasonClosedException.java` extends base — HTTP 403
- [ ] 3.4.5 `DuplicatePredictionException.java` extends base — HTTP 409
- [ ] 3.4.6 `UnauthorisedActionException.java` extends base — HTTP 403
- [ ] 3.4.7 `InvalidSeasonTeamException.java` extends base — HTTP 400 (team not in season)
- [ ] 3.4.8 `InvalidResultException.java` extends base — HTTP 400 (bad result payload)

### Task 3.5 — GlobalExceptionHandler
- [ ] 3.5.1 Create `common/exception/GlobalExceptionHandler.java` annotated `@RestControllerAdvice`
- [ ] 3.5.2 Handle `ResourceNotFoundException` → 404
- [ ] 3.5.3 Handle `PredictionWindowClosedException` → 409
- [ ] 3.5.4 Handle `SeasonClosedException` → 403
- [ ] 3.5.5 Handle `DuplicatePredictionException` → 409
- [ ] 3.5.6 Handle `UnauthorisedActionException` → 403
- [ ] 3.5.7 Handle `InvalidSeasonTeamException` → 400
- [ ] 3.5.8 Handle `InvalidResultException` → 400
- [ ] 3.5.9 Handle `MethodArgumentNotValidException` → 400 with field error list
- [ ] 3.5.10 Handle `AccessDeniedException` (Spring Security) → 403
- [ ] 3.5.11 Handle `Exception` (fallback) → 500; log at ERROR level
- [ ] 3.5.12 All responses use `ApiResponse.error(...)` envelope

### Task 3.6 — AsyncConfig
- [ ] 3.6.1 Create `config/AsyncConfig.java` annotated `@Configuration @EnableAsync`
- [ ] 3.6.2 Define `@Bean("familyLeagueExecutor")` returning `ThreadPoolTaskExecutor`
- [ ] 3.6.3 Bind pool sizes from `app.async.*` config properties
- [ ] 3.6.4 Set `setThreadNamePrefix("fl-async-")`
- [ ] 3.6.5 Set `setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())`

---

## Phase 4 — Authentication & RBAC
> Secure the entire API before building any feature endpoint.

### Task 4.1 — Role, Permission, RolePermission entities + repositories
- [ ] 4.1.1 Create `user/entity/Role.java` extending `BaseEntity` — fields: `roleName`, `description`, `@ManyToMany permissions`
- [ ] 4.1.2 Create `user/entity/Permission.java` — fields: `name`, `description` (no BaseEntity — static seed data)
- [ ] 4.1.3 Create `user/entity/RolePermission.java` — composite PK embedded entity (or manage via `Role.permissions` join table)
- [ ] 4.1.4 Create `user/repository/RoleRepository.java` — `findByRoleName(String name)`
- [ ] 4.1.5 Create `user/repository/PermissionRepository.java` — `findByName(String name)`

### Task 4.2 — User entity + UserRepository
- [ ] 4.2.1 Create `user/entity/User.java` extending `BaseEntity` — all fields matching DATA_MODEL
- [ ] 4.2.2 `@ManyToOne Role role` relationship
- [ ] 4.2.3 Create `user/repository/UserRepository.java`:
  - `Optional<User> findByEmailAndIsDeletedFalse(String email)`
  - `Optional<User> findByIdAndIsDeletedFalse(Long id)`
  - `Page<User> findAllByIsDeletedFalse(Pageable pageable)`

### Task 4.3 — JwtUtil
- [ ] 4.3.1 Create `auth/util/JwtUtil.java`
- [ ] 4.3.2 `generateToken(String email, Long userId, String role, List<String> permissions)` — signs with HS256, includes all claims
- [ ] 4.3.3 `validateToken(String token)` — verifies signature + expiry; returns boolean
- [ ] 4.3.4 `extractEmail(String token)` — parses subject claim
- [ ] 4.3.5 `extractUserId(String token)` — parses `userId` claim as Long
- [ ] 4.3.6 `extractPermissions(String token)` — parses `permissions` claim as `List<String>`
- [ ] 4.3.7 Read `app.jwt.secret` and `app.jwt.expiry-seconds` from `@Value` or `@ConfigurationProperties`

### Task 4.4 — JwtAuthenticationFilter
- [ ] 4.4.1 Create `auth/util/JwtAuthenticationFilter.java` extending `OncePerRequestFilter`
- [ ] 4.4.2 Extract `Authorization: Bearer <token>` header; skip filter if absent
- [ ] 4.4.3 Call `JwtUtil.validateToken()`; if invalid, clear context and continue chain (401 returned by entry point)
- [ ] 4.4.4 Build `UsernamePasswordAuthenticationToken` with permissions as `GrantedAuthority` list
- [ ] 4.4.5 Set token into `SecurityContextHolder`
- [ ] 4.4.6 Log invalid/expired token attempts at WARN level

### Task 4.5 — SecurityConfig
- [ ] 4.5.1 Create `config/SecurityConfig.java` annotated `@Configuration @EnableMethodSecurity`
- [ ] 4.5.2 Define `SecurityFilterChain` bean:
  - Disable CSRF (stateless API)
  - Permit `POST /api/v1/auth/register`, `POST /api/v1/auth/login` without auth
  - Require authentication on all other routes
  - `SessionCreationPolicy.STATELESS`
- [ ] 4.5.3 Add `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`
- [ ] 4.5.4 Configure `AuthenticationEntryPoint` returning 401 JSON (not default redirect)
- [ ] 4.5.5 Configure `AccessDeniedHandler` returning 403 JSON
- [ ] 4.5.6 Register `PasswordEncoder @Bean` using `BCryptPasswordEncoder`
- [ ] 4.5.7 Register `AuthenticationManager @Bean`

### Task 4.6 — CustomUserDetailsService
- [ ] 4.6.1 Create `auth/service/CustomUserDetailsService.java` implementing `UserDetailsService`
- [ ] 4.6.2 `loadUserByUsername(email)` → load `User` from DB; throw `UsernameNotFoundException` if not found or inactive
- [ ] 4.6.3 Map `User.role.permissions` to Spring `GrantedAuthority` list
- [ ] 4.6.4 Return `org.springframework.security.core.userdetails.User` built from entity

### Task 4.7 — AuthController + DTOs
- [ ] 4.7.1 Create request DTO `RegisterRequest` — validated: `@NotBlank name`, `@Email email`, `@Size(min=8) password`
- [ ] 4.7.2 Create request DTO `LoginRequest` — `@Email email`, `@NotBlank password`
- [ ] 4.7.3 Create response DTO `AuthResponse` — `accessToken`, `tokenType: "Bearer"`, `expiresIn`
- [ ] 4.7.4 Create `auth/service/AuthService.java` interface + impl:
  - `register(RegisterRequest)` — check email uniqueness, hash password, assign USER role, save, generate JWT
  - `login(LoginRequest)` — verify credentials, generate JWT
- [ ] 4.7.5 Create `auth/controller/AuthController.java`:
  - `POST /api/v1/auth/register` → 201 + `AuthResponse`
  - `POST /api/v1/auth/login` → 200 + `AuthResponse`
  - `POST /api/v1/auth/logout` → 200 + success message (stateless no-op per DL-008)

---

## Phase 5 — User Management

### Task 5.1 — UserService interface + UserServiceImpl
- [ ] 5.1.1 Create `user/service/UserService.java` interface
- [ ] 5.1.2 `UserProfileResponse getMyProfile(Long userId)`
- [ ] 5.1.3 `UserProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request)` — only name + avatar
- [ ] 5.1.4 `Page<UserSummaryResponse> getAllUsers(Pageable pageable, String search)` — Admin only
- [ ] 5.1.5 `void updateUserStatus(Long userId, boolean isActive)` — Admin only; cannot deactivate self

### Task 5.2 — UserController (self-service endpoints)
- [ ] 5.2.1 Create request DTO `UpdateProfileRequest` — `name`, `avatar` (both optional, at least one required)
- [ ] 5.2.2 Create response DTO `UserProfileResponse` — id, name, email, avatar, role, isActive, createdAt
- [ ] 5.2.3 `GET /api/v1/users/me` — returns own profile; resolves userId from JWT
- [ ] 5.2.4 `PUT /api/v1/users/me` — updates name/avatar; 200 + updated profile

### Task 5.3 — UserController (Admin endpoints)
- [ ] 5.3.1 Create response DTO `UserSummaryResponse` — id, name, email, role, isActive
- [ ] 5.3.2 `GET /api/v1/users` — `@PreAuthorize("hasAuthority('MANAGE_USERS')")`; paginated; search by name/email
- [ ] 5.3.3 `PUT /api/v1/users/{id}/status` — body: `{ "active": true/false }`; 200 on success

---

## Phase 6 — League & Season Management

### Task 6.1 — League entity + service + controller
- [ ] 6.1.1 Create `league/entity/League.java` extending `BaseEntity`
- [ ] 6.1.2 Create `league/repository/LeagueRepository.java` — `findAllByIsDeletedFalse(Pageable)`; search by name
- [ ] 6.1.3 Create request DTO `CreateLeagueRequest` — `@NotBlank name`, `description`
- [ ] 6.1.4 Create response DTO `LeagueResponse`
- [ ] 6.1.5 Create `league/service/LeagueService.java` interface + impl: `create`, `getAll(pageable, search)`, `getById`
- [ ] 6.1.6 Create `league/controller/LeagueController.java`:
  - `POST /api/v1/leagues` — Admin (`CREATE_LEAGUE`)
  - `GET /api/v1/leagues` — authenticated; paginated + search

### Task 6.2 — Season entity + service + controller
- [ ] 6.2.1 Create `league/entity/Season.java` extending `BaseEntity` — all fields; `status` as `SeasonStatus` enum
- [ ] 6.2.2 Create `SeasonStatus` enum: `UPCOMING`, `ACTIVE`, `COMPLETED`, `CLOSED`
- [ ] 6.2.3 Create `league/repository/SeasonRepository.java` — `findByLeagueIdAndIsDeletedFalse(Long, Pageable)`
- [ ] 6.2.4 Create request DTO `CreateSeasonRequest` — `@NotBlank name`, `@NotNull startDate`, `endDate`
- [ ] 6.2.5 Create response DTO `SeasonResponse` — includes status, predictionLockTime
- [ ] 6.2.6 Create `league/service/SeasonService.java` interface + impl: `create`, `getByLeague`, `getById`
- [ ] 6.2.7 Create `league/controller/SeasonController.java`:
  - `POST /api/v1/leagues/{id}/seasons` — Admin (`CREATE_SEASON`)
  - `GET /api/v1/leagues/{id}/seasons` — authenticated; paginated

### Task 6.3 — Season status lifecycle enforcement
- [ ] 6.3.1 `SeasonService.transitionStatus(seasonId, newStatus)` — validates allowed transitions: `UPCOMING→ACTIVE`, `ACTIVE→COMPLETED`, `COMPLETED→CLOSED`; throws `UnauthorisedActionException` for invalid transitions
- [ ] 6.3.2 Create helper `SeasonGuard.assertNotClosed(Season)` — throws `SeasonClosedException` if `status == CLOSED`
- [ ] 6.3.3 Call `SeasonGuard.assertNotClosed()` at the start of every write operation in every service that touches season-owned data

### Task 6.4 — Close season endpoint
- [ ] 6.4.1 `PUT /api/v1/seasons/{id}/close` — Admin (`CLOSE_SEASON`)
- [ ] 6.4.2 Validate current status is `COMPLETED` before allowing close
- [ ] 6.4.3 Set `status = CLOSED`, save
- [ ] 6.4.4 Return 200 + updated `SeasonResponse`

### Task 6.5 — SeasonTeam entity + endpoints
- [ ] 6.5.1 Create `league/entity/SeasonTeam.java` with embedded composite PK (`@EmbeddedId SeasonTeamId`)
- [ ] 6.5.2 Create `league/repository/SeasonTeamRepository.java`:
  - `List<SeasonTeam> findBySeasonId(Long seasonId)`
  - `boolean existsBySeasonIdAndTeamId(Long seasonId, Long teamId)`
- [ ] 6.5.3 Create request DTO `AddSeasonTeamRequest` — `@NotNull teamId`
- [ ] 6.5.4 `SeasonService.addTeamToSeason(seasonId, teamId)` — validates team exists; validates season not CLOSED; prevents duplicate
- [ ] 6.5.5 `SeasonService.getTeamsInSeason(seasonId)` — returns list of `TeamResponse`
- [ ] 6.5.6 Add to `SeasonController`:
  - `POST /api/v1/seasons/{id}/teams` — Admin (`MANAGE_SEASON_TEAMS`)
  - `GET /api/v1/seasons/{id}/teams` — authenticated

---

## Phase 7 — Team & Player Management

### Task 7.1 — Team entity + service + controller
- [ ] 7.1.1 Create `team/entity/Team.java` extending `BaseEntity`
- [ ] 7.1.2 Create `team/repository/TeamRepository.java` — `findAllByIsDeletedFalse(Pageable)`; search by teamName
- [ ] 7.1.3 Create request DTO `CreateTeamRequest` — `@NotBlank teamName`, `shortName`, `logo`
- [ ] 7.1.4 Create response DTO `TeamResponse`
- [ ] 7.1.5 Create `team/service/TeamService.java` interface + impl: `create`, `getAll(pageable, search)`, `getById`, `softDelete`
- [ ] 7.1.6 Create `team/controller/TeamController.java`:
  - `POST /api/v1/teams` — Admin (`CREATE_LEAGUE` permission reused, or define `MANAGE_TEAMS`)
  - `GET /api/v1/teams` — authenticated; paginated + search
  - `DELETE /api/v1/teams/{id}` — Admin; soft delete → 204

### Task 7.2 — Player entity + service + controller
- [ ] 7.2.1 Create `team/entity/Player.java` extending `BaseEntity` — `playerRole` as `PlayerRole` enum
- [ ] 7.2.2 Create `PlayerRole` enum: `BATSMAN`, `BOWLER`, `ALL_ROUNDER`, `WICKET_KEEPER`
- [ ] 7.2.3 Create `team/repository/PlayerRepository.java` — `findByTeamIdAndIsDeletedFalse(Long)`
- [ ] 7.2.4 Create request DTO `CreatePlayerRequest` — `@NotBlank playerName`, `@NotNull playerRole`, `jerseyNumber`, `country`
- [ ] 7.2.5 Create response DTO `PlayerResponse`
- [ ] 7.2.6 Create `team/service/PlayerService.java` interface + impl: `addToTeam`, `getByTeam`, `update`, `softDelete`
- [ ] 7.2.7 Create `team/controller/PlayerController.java`:
  - `POST /api/v1/teams/{id}/players` — Admin
  - `GET /api/v1/teams/{id}/players` — authenticated
  - `PUT /api/v1/teams/{teamId}/players/{playerId}` — Admin
  - `DELETE /api/v1/teams/{teamId}/players/{playerId}` — Admin; soft delete → 204

---

## Phase 8 — Match Scheduling

### Task 8.1 — Match entity + service + controller
- [ ] 8.1.1 Create `match/entity/Match.java` extending `BaseEntity` — `status` as `MatchStatus` enum
- [ ] 8.1.2 Create `MatchStatus` enum: `SCHEDULED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`
- [ ] 8.1.3 Create `match/repository/MatchRepository.java`:
  - `Page<Match> findBySeasonIdAndIsDeletedFalse(Long seasonId, Pageable pageable)`
  - `List<Match> findByPredictionLockTimeBetweenAndIsDeletedFalse(LocalDateTime from, LocalDateTime to)` — for reminder scheduler
  - `List<Match> findByStatusAndMatchResultIsNullAndIsDeletedFalse(MatchStatus status)` — for result-alert scheduler
  - `Optional<Match> findFirstBySeasonIdAndIsDeletedFalseOrderByStartTimeAsc(Long seasonId)` — for season lock time calc
- [ ] 8.1.4 Create request DTO `CreateMatchRequest` — `@NotNull team1Id`, `@NotNull team2Id`, `venue`, `@NotNull startTime`, `matchNumber`
- [ ] 8.1.5 Create response DTO `MatchResponse` — includes predictionLockTime, status
- [ ] 8.1.6 Create `match/service/MatchService.java` interface + impl: `create`, `getBySeason`, `getById`, `update`
- [ ] 8.1.7 Create `match/controller/MatchController.java`:
  - `POST /api/v1/seasons/{id}/matches` — Admin (`CREATE_MATCH`)
  - `GET /api/v1/seasons/{id}/matches` — authenticated; paginated; filter by status; sort by startTime

### Task 8.2 — Auto-set match.prediction_lock_time
- [ ] 8.2.1 In `MatchService.create()`: set `predictionLockTime = startTime - matchLockOffsetHours` (from config)
- [ ] 8.2.2 Validate `team1Id != team2Id`; throw `InvalidSeasonTeamException` if same
- [ ] 8.2.3 Validate `team1Id` exists in `season_team` for the season; same for `team2Id`
- [ ] 8.2.4 Validate `startTime` is in the future

### Task 8.3 — Auto-update season.prediction_lock_time
- [ ] 8.3.1 After saving a new match, query `MatchRepository.findFirstBySeasonIdOrderByStartTimeAsc(seasonId)`
- [ ] 8.3.2 Compute `season.predictionLockTime = earliestMatch.startTime - leagueLockOffsetHours` (from config)
- [ ] 8.3.3 If the new lock time differs from the stored one, update and save `Season`
- [ ] 8.3.4 Only update if `season.status != CLOSED`

---

## Phase 9 — Prediction Submission

### Task 9.1 — Prediction entity + PredictionRepository
- [ ] 9.1.1 Create `prediction/entity/Prediction.java` extending `BaseEntity` — all fields; `pointsEarned` default 0
- [ ] 9.1.2 Create `prediction/repository/PredictionRepository.java`:
  - `Optional<Prediction> findByUserIdAndMatchIdAndIsDeletedFalse(Long userId, Long matchId)`
  - `List<Prediction> findByMatchIdAndIsDeletedFalse(Long matchId)` — for scoring
  - `List<Prediction> findByMatchIdAndUserIdNotAndIsDeletedFalse(Long matchId, Long userId)` — visibility check

### Task 9.2 — PredictionService
- [ ] 9.2.1 Create `prediction/service/PredictionService.java` interface + impl
- [ ] 9.2.2 `submitOrUpdate(Long userId, Long matchId, SubmitPredictionRequest)`:
  - Load match; validate `predictionLockTime > NOW()` — throw `PredictionWindowClosedException` if not
  - Validate season is not CLOSED
  - Validate predicted teams/player belong to match teams
  - Upsert: find existing prediction or create new
  - Set `submittedAt = NOW()`
- [ ] 9.2.3 `getMyPrediction(Long userId, Long matchId)` — always allowed; 404 if none exists
- [ ] 9.2.4 `getAllPredictions(Long matchId, Long callerUserId)`:
  - If `NOW() < match.predictionLockTime` → return only caller's prediction
  - If `NOW() >= match.predictionLockTime` → return all predictions

### Task 9.3 — PredictionController
- [ ] 9.3.1 Create request DTO `SubmitPredictionRequest` — `@NotNull predictedWinnerTeamId`, `@NotNull predictedTossWinnerId`, `predictedPlayerOfMatchId`
- [ ] 9.3.2 Create response DTO `PredictionResponse`
- [ ] 9.3.3 `POST /api/v1/matches/{id}/predictions` — `SUBMIT_PREDICTION`; 201
- [ ] 9.3.4 `PUT /api/v1/matches/{id}/predictions` — `SUBMIT_PREDICTION`; 200 (delegates to same service method)
- [ ] 9.3.5 `GET /api/v1/matches/{id}/predictions` — authenticated; applies visibility rule
- [ ] 9.3.6 `GET /api/v1/matches/{id}/predictions/me` — authenticated; always returns caller's own

### Task 9.4 — Prediction visibility rule (verified in service)
- [ ] 9.4.1 Unit test: calling `getAllPredictions()` before lock returns only caller's record
- [ ] 9.4.2 Unit test: calling `getAllPredictions()` after lock returns all records

### Task 9.5 — LeaguePrediction entity + service + controller
- [ ] 9.5.1 Create `prediction/entity/LeaguePrediction.java` extending `BaseEntity`
- [ ] 9.5.2 Create `prediction/repository/LeaguePredictionRepository.java`:
  - `List<LeaguePrediction> findByUserIdAndSeasonIdAndIsDeletedFalse(Long userId, Long seasonId)`
  - `boolean existsByUserIdAndSeasonIdAndPredictedRankAndIsDeletedFalse(...)` — duplicate rank check
- [ ] 9.5.3 Create request DTO `SubmitLeaguePredictionRequest` — `List<TeamRankEntry>` where `TeamRankEntry` has `teamId` + `predictedRank`
- [ ] 9.5.4 `LeaguePredictionService.submitOrUpdate(userId, seasonId, request)`:
  - Validate `season.predictionLockTime > NOW()` — throw `PredictionWindowClosedException` if not
  - Load `season_team` for the season → get expected team set (size N)
  - Validate submitted entries: exactly N teams, ranks 1..N with no gaps, no duplicate teams, no duplicate ranks
  - Soft-delete existing entries for this user+season if updating, then insert new
- [ ] 9.5.5 `LeaguePredictionService.getMyLeaguePrediction(userId, seasonId)`
- [ ] 9.5.6 Create `prediction/controller/LeaguePredictionController.java`:
  - `POST /api/v1/seasons/{id}/league-predictions` — 201
  - `PUT /api/v1/seasons/{id}/league-predictions` — 200
  - `GET /api/v1/seasons/{id}/league-predictions/me` — 200

---

## Phase 10 — Result Processing

### Task 10.1 — MatchResult entity + repository
- [ ] 10.1.1 Create `match/entity/MatchResult.java` extending `BaseEntity` — `resultType` as `ResultType` enum
- [ ] 10.1.2 Create `ResultType` enum: `WIN`, `TIE`, `NO_RESULT`
- [ ] 10.1.3 Create `match/repository/MatchResultRepository.java`:
  - `Optional<MatchResult> findByMatchIdAndIsDeletedFalse(Long matchId)`
  - `boolean existsByMatchId(Long matchId)` — idempotency guard

### Task 10.2 — ResultService.publishResult()
- [ ] 10.2.1 Create `match/service/ResultService.java` interface + impl
- [ ] 10.2.2 Validate match exists and is not CLOSED season
- [ ] 10.2.3 Validate no result already published for this match (409 if duplicate)
- [ ] 10.2.4 Validate result payload: if `WIN`, `winningTeamId` + `playerOfMatchId` are required; if `TIE` or `NO_RESULT`, `winningTeamId` must be null
- [ ] 10.2.5 Save `MatchResult`
- [ ] 10.2.6 Update `Match.status = COMPLETED`
- [ ] 10.2.7 Publish `ResultPublishedEvent(matchId, seasonId)` via `ApplicationEventPublisher`
- [ ] 10.2.8 Return immediately (do not wait for async job)

### Task 10.3 — ResultController
- [ ] 10.3.1 Create request DTO `PublishResultRequest` — `@NotNull resultType`, `winningTeamId`, `@NotNull tossWinnerTeamId`, `playerOfMatchId`, `winningMargin`
- [ ] 10.3.2 Create response DTO `MatchResultResponse`
- [ ] 10.3.3 `POST /api/v1/matches/{id}/result` — `@PreAuthorize("hasAuthority('PUBLISH_RESULT')")`; 201

### Task 10.4 — @Async @EventListener — ScoringService
- [ ] 10.4.1 Create `prediction/service/ScoringService.java`
- [ ] 10.4.2 Method `@Async @EventListener calculatePoints(ResultPublishedEvent event)` runs on `familyLeagueExecutor`
- [ ] 10.4.3 Log start and end of calculation at INFO level
- [ ] 10.4.4 Fetch all predictions for the match
- [ ] 10.4.5 For each prediction apply scoring rules (see Task 10.5)
- [ ] 10.4.6 Batch-save all updated predictions
- [ ] 10.4.7 Trigger `LeaderboardService.recalculate(seasonId)` after scoring completes

### Task 10.5 — Scoring logic
- [ ] 10.5.1 WIN: `+1` if `predictedWinnerTeamId == result.winningTeamId`
- [ ] 10.5.2 WIN: `+1` if `predictedTossWinnerId == result.tossWinnerTeamId`
- [ ] 10.5.3 WIN: `+1` if `predictedPlayerOfMatchId == result.playerOfMatchId` (and both non-null)
- [ ] 10.5.4 TIE: `+1` if `predictedWinnerTeamId == match.team1Id OR match.team2Id` (any team predicted as winner scores)
- [ ] 10.5.5 TIE: `+1` if `predictedTossWinnerId == result.tossWinnerTeamId`
- [ ] 10.5.6 TIE: `playerOfMatch = null` → 0 pts for that field
- [ ] 10.5.7 NO_RESULT: all fields = 0 pts; `pointsEarned = 0`
- [ ] 10.5.8 Ensure `pointsEarned` is NEVER read from the request payload; always computed here

---

## Phase 11 — Leaderboard

### Task 11.1 — Leaderboard entity + repository
- [ ] 11.1.1 Create `leaderboard/entity/Leaderboard.java` — does NOT extend BaseEntity; fields: `id`, `seasonId`, `userId`, `totalPoints`, `rankPosition`, `updatedAt`
- [ ] 11.1.2 Create `leaderboard/repository/LeaderboardRepository.java`:
  - `Optional<Leaderboard> findBySeasonIdAndUserId(Long seasonId, Long userId)`
  - `Page<Leaderboard> findBySeasonIdOrderByRankPositionAsc(Long seasonId, Pageable pageable)`
  - Support upsert via `@Query` with `ON CONFLICT (season_id, user_id) DO UPDATE`

### Task 11.2 — LeaderboardService.recalculate(seasonId)
- [ ] 11.2.1 Create `leaderboard/service/LeaderboardService.java` interface + impl
- [ ] 11.2.2 Aggregate `SUM(pointsEarned)` from `prediction` table grouped by `userId` for all non-deleted predictions in the season
- [ ] 11.2.3 Upsert one `Leaderboard` row per user (INSERT or UPDATE on conflict)
- [ ] 11.2.4 After upsert, compute `rankPosition` using DENSE_RANK ordered by `totalPoints DESC` (tied users share same rank)
- [ ] 11.2.5 Update all `rankPosition` values and save
- [ ] 11.2.6 Log "Leaderboard recalculation complete for seasonId={}" at INFO

### Task 11.3 — LeaderboardController
- [ ] 11.3.1 Create response DTO `LeaderboardEntryResponse` — `rankPosition`, `userId`, `userName`, `totalPoints`
- [ ] 11.3.2 `GET /api/v1/seasons/{id}/leaderboard` — authenticated; paginated; sorted by rankPosition ASC; 200

---

## Phase 12 — Notification System

### Task 12.1 — Notification entity + repository
- [ ] 12.1.1 Create `notification/entity/Notification.java` extending `BaseEntity` — `type` as `NotificationType` enum, `status` as `NotificationStatus` enum
- [ ] 12.1.2 `NotificationType` enum: `PREDICTION_REMINDER`, `RESULT_PUBLISHED`, `LEADERBOARD_UPDATE`, `CUSTOM`
- [ ] 12.1.3 `NotificationStatus` enum: `PENDING`, `SENT`, `FAILED`
- [ ] 12.1.4 Create `notification/repository/NotificationRepository.java`:
  - `boolean existsByUserIdAndTypeAndSubjectAndStatus(...)` — idempotency check for schedulers
  - `Page<Notification> findAllByIsDeletedFalse(Pageable pageable)` — admin history

### Task 12.2 — NotificationService.send()
- [ ] 12.2.1 Create `notification/service/NotificationService.java` interface + impl
- [ ] 12.2.2 `send(Long userId, NotificationType type, String subject, String body)`:
  - Create `Notification` with `status = PENDING`, save
  - Build `SimpleMailMessage` or `MimeMessage`
  - Call `JavaMailSender.send()`
  - On success: update `status = SENT`, `sentAt = NOW()`
  - On `MailException`: update `status = FAILED`, log at ERROR level; do NOT re-throw (don't fail the caller)
- [ ] 12.2.3 Inject `JavaMailSender` and `from-address` from config

### Task 12.3 — Email templates
- [ ] 12.3.1 Create `notification/service/EmailTemplateService.java` — builds email subject + body strings from config/template files
- [ ] 12.3.2 Template: `PREDICTION_REMINDER` — "Hi {name}, match {matchName} prediction closes at {lockTime}. Don't forget to predict!"
- [ ] 12.3.3 Template: `RESULT_ALERT` (admin) — "Match {matchName} completed. Please publish the result."
- [ ] 12.3.4 Template: `LEADERBOARD_UPDATE` (admin) — "Leaderboard updated for season {seasonName} after match {matchName}."
- [ ] 12.3.5 All template strings configurable via `application.yml` `app.notification.templates.*`

### Task 12.4 — PredictionReminderScheduler
- [ ] 12.4.1 Create `scheduler/PredictionReminderScheduler.java` annotated `@Component`
- [ ] 12.4.2 `@Scheduled(cron = "${app.scheduler.prediction-reminder-cron}")` on `sendReminders()` method
- [ ] 12.4.3 Log job start at INFO
- [ ] 12.4.4 Find matches where `predictionLockTime` is between `NOW()` and `NOW() + reminderWindowHours`
- [ ] 12.4.5 For each match, find all active users in the season who have NO prediction for this match
- [ ] 12.4.6 For each such user, check `notification` table for existing `PREDICTION_REMINDER` for this match (idempotency) — skip if already sent
- [ ] 12.4.7 Call `NotificationService.send(userId, PREDICTION_REMINDER, subject, body)`
- [ ] 12.4.8 Log job end with count of reminders sent at INFO

### Task 12.5 — ResultAlertScheduler
- [ ] 12.5.1 Create `scheduler/ResultAlertScheduler.java`
- [ ] 12.5.2 `@Scheduled(cron = "${app.scheduler.result-alert-cron}")` on `alertAdmin()` method
- [ ] 12.5.3 Find `COMPLETED` matches with no `match_result` record
- [ ] 12.5.4 For each, check if alert already sent (idempotency via notification table)
- [ ] 12.5.5 Send alert to Admin email (`app.notification.admin-email`)

### Task 12.6 — Post-leaderboard Admin email
- [ ] 12.6.1 At the end of `LeaderboardService.recalculate()`, call `NotificationService.send(adminUserId, LEADERBOARD_UPDATE, ...)`
- [ ] 12.6.2 Resolve admin user ID from `UserRepository.findByRole("ADMIN")`

### Task 12.7 — Bulk notification endpoint
- [ ] 12.7.1 Create request DTO `BulkNotificationRequest` — `List<Long> userIds`, `@NotNull type`, `@NotBlank subject`, `@NotBlank body`
- [ ] 12.7.2 `notification/service/NotificationService.sendBulk(BulkNotificationRequest)` — loop over userIds, call `send()` for each
- [ ] 12.7.3 `POST /api/v1/notifications/bulk` — `@PreAuthorize("hasAuthority('SEND_NOTIFICATION')")`; 200

### Task 12.8 — Notification history endpoint
- [ ] 12.8.1 `GET /api/v1/notifications` — Admin; paginated; filter by `type` and `status` query params
- [ ] 12.8.2 Create response DTO `NotificationResponse`

---

## Phase 13 — Audit System

### Task 13.1 — AuditLog entity + repository
- [ ] 13.1.1 Create `audit/entity/AuditLog.java` — does NOT extend BaseEntity (it IS the trail); all fields from DATA_MODEL; `oldValue`/`newValue` stored as `String` (JSON) mapped via `@Column(columnDefinition = "jsonb")`
- [ ] 13.1.2 Create `audit/repository/AuditLogRepository.java`:
  - `Page<AuditLog> findByEntityNameAndIsDeletedFalse(String entityName, Pageable pageable)` (wait — AuditLog has no isDeleted; query all)
  - `Page<AuditLog> findByEntityName(String entityName, Pageable pageable)`
  - `Page<AuditLog> findAll(Pageable pageable)`

### Task 13.2 — AuditEntityListener
- [ ] 13.2.1 Create `audit/service/AuditSerializer.java` — `serialize(Object entity): String` using Jackson `ObjectMapper`; handle circular refs; exclude sensitive fields (password)
- [ ] 13.2.2 Create `audit/listener/AuditEntityListener.java`
- [ ] 13.2.3 `@PreUpdate`: call `AuditSerializer.serialize(entity)` and store result in `ThreadLocal<String>`
- [ ] 13.2.4 `@PostPersist`: call `AuditLogService.record(entity, INSERT, null, serialized)`
- [ ] 13.2.5 `@PostUpdate`: retrieve old value from ThreadLocal (clear after read); call `AuditLogService.record(entity, UPDATE, oldValue, serialized)`
- [ ] 13.2.6 Add `@EntityListeners(AuditEntityListener.class)` to `BaseEntity` (applies to all domain entities)
- [ ] 13.2.7 Create `audit/service/AuditLogService.java` — `record(...)` method that saves `AuditLog` entity; use a separate `@Transactional(propagation = REQUIRES_NEW)` to not be rolled back with the main tx on failure

### Task 13.3 — AuditLogController
- [ ] 13.3.1 Create response DTO `AuditLogResponse`
- [ ] 13.3.2 `GET /api/v1/audit-logs` — Admin; paginated; optional `?entityName=Match` filter; sorted by `changedAt DESC`

---

## Phase 14 — API Documentation

### Task 14.1 — OpenApiConfig
- [ ] 14.1.1 Create `config/OpenApiConfig.java`
- [ ] 14.1.2 Define `OpenAPI` bean with: API title "Family League API", version "1.0", description from PRD
- [ ] 14.1.3 Add `SecurityScheme`: type HTTP, scheme bearer, bearerFormat JWT, name "bearerAuth"
- [ ] 14.1.4 Add global `SecurityRequirement("bearerAuth")` so all endpoints show the lock icon
- [ ] 14.1.5 Mark `/auth/register` and `/auth/login` as `@SecurityRequirements({})` to opt them out

### Task 14.2 — Annotate all controllers
- [ ] 14.2.1 Add `@Tag(name = "Auth")` to `AuthController`
- [ ] 14.2.2 Add `@Operation` + `@ApiResponse(s)` to every controller method across all 10 controllers
- [ ] 14.2.3 Ensure 401, 403, 404, 409 responses are documented where applicable

### Task 14.3 — Annotate all request/response DTOs
- [ ] 14.3.1 Add `@Schema` with `description` and `example` on all DTO fields
- [ ] 14.3.2 Verify Swagger UI at `/swagger-ui.html` renders correctly and shows all endpoints

### Task 14.4 — Export API collection
- [ ] 14.4.1 Create `docs/` directory
- [ ] 14.4.2 Download OpenAPI JSON from `/api-docs` and commit as `docs/openapi.json`
- [ ] 14.4.3 (Optional) Export Postman collection and commit as `docs/postman-collection.json`

---

## Phase 15 — Testing

### Task 15.1 — Unit test: prediction lock enforcement
- [ ] 15.1.1 Test: prediction submitted 1 min before lock → 201 OK
- [ ] 15.1.2 Test: prediction submitted exactly at lock time → 409 `PREDICTION_WINDOW_CLOSED`
- [ ] 15.1.3 Test: prediction submitted 1 min after lock → 409 `PREDICTION_WINDOW_CLOSED`

### Task 15.2 — Unit test: scoring logic (WIN)
- [ ] 15.2.1 All three fields correct → `pointsEarned = 3`
- [ ] 15.2.2 Only winner correct → `pointsEarned = 1`
- [ ] 15.2.3 Only toss correct → `pointsEarned = 1`
- [ ] 15.2.4 Only player correct → `pointsEarned = 1`
- [ ] 15.2.5 Nothing correct → `pointsEarned = 0`

### Task 15.3 — Unit test: scoring logic (TIE)
- [ ] 15.3.1 User predicted team1 as winner (correct as both qualify) → `+1`
- [ ] 15.3.2 User predicted team2 as winner (also correct) → `+1`
- [ ] 15.3.3 `playerOfMatch = null` → player field always `0`
- [ ] 15.3.4 Toss correctly predicted → `+1`

### Task 15.4 — Unit test: scoring logic (NO_RESULT)
- [ ] 15.4.1 All predictions → `pointsEarned = 0` regardless of what was predicted

### Task 15.5 — Unit test: leaderboard ranking with ties
- [ ] 15.5.1 User A=8, User B=5, User C=5 → ranks: A=1, B=2, C=2
- [ ] 15.5.2 All users same points → all rank 1
- [ ] 15.5.3 Strictly descending → ranks 1, 2, 3 with no sharing

### Task 15.6 — Integration test: Flyway clean migration
- [ ] 15.6.1 Spin up PostgreSQL via Testcontainers
- [ ] 15.6.2 Run Flyway V1 → V2 → V3
- [ ] 15.6.3 Assert all tables exist with correct columns
- [ ] 15.6.4 Assert seed data (roles, permissions, role_permission) is present

### Task 15.7 — Integration test: full match → prediction → result → leaderboard flow
- [ ] 15.7.1 Create league, season, teams, season_team, match
- [ ] 15.7.2 Two users submit predictions
- [ ] 15.7.3 Admin publishes result
- [ ] 15.7.4 Wait for async scoring to complete
- [ ] 15.7.5 Assert correct `pointsEarned` on each prediction
- [ ] 15.7.6 Assert leaderboard shows correct ranks

### Task 15.8 — API test (MockMvc): prediction visibility before/after lock
- [ ] 15.8.1 Before lock: `GET /predictions` returns only caller's prediction
- [ ] 15.8.2 After lock: `GET /predictions` returns all users' predictions

### Task 15.9 — API test: CLOSED season blocks all writes
- [ ] 15.9.1 Close a season
- [ ] 15.9.2 Attempt to schedule a match → 403
- [ ] 15.9.3 Attempt to submit a prediction → 403
- [ ] 15.9.4 Attempt to publish a result → 403

### Task 15.10 — API test: points_earned not accepted via API
- [ ] 15.10.1 Submit prediction with `pointsEarned` in body → verify it is ignored / not saved
- [ ] 15.10.2 Publish result with `pointsEarned` in body → verify 400 or field is ignored

---

## Phase 16 — Final Packaging

### Task 16.1 — README.md
- [ ] 16.1.1 Prerequisites section: Java 17, Maven 3.9, PostgreSQL 15, SMTP credentials
- [ ] 16.1.2 Environment variables table (all 8 vars from TRD §14.3)
- [ ] 16.1.3 "How to run from clean clone" steps: clone → set env vars → `mvn spring-boot:run -Dspring-boot.run.profiles=dev`
- [ ] 16.1.4 "How to run tests": `mvn test` (Testcontainers auto-provisions DB)
- [ ] 16.1.5 Links to: BRD.md, PRD.md, TRD.md, DATA_MODEL.md, TASKS.md, DECISION_LOG.md, AI_PROMPTS.md, docs/openapi.json

### Task 16.2 — DECISION_LOG.md
- [ ] 16.2.1 Create `DECISION_LOG.md` at project root
- [ ] 16.2.2 Extract and consolidate all DL-001 through DL-008 entries from TRD §16 into standalone file
- [ ] 16.2.3 Format consistently: Decision | Alternatives | Rationale | Trade-offs

### Task 16.3 — AI_PROMPTS.md
- [ ] 16.3.1 Create `AI_PROMPTS.md` at project root
- [ ] 16.3.2 List every AI tool used (Claude Code, etc.) with prompt content and purpose
- [ ] 16.3.3 Note what was generated vs what was manually written

### Task 16.4 — Security sweep
- [ ] 16.4.1 `grep -rn "password\|secret\|api_key\|apikey" src/` — verify zero hardcoded values
- [ ] 16.4.2 Confirm all secrets come from `${ENV_VAR}` references in YAML
- [ ] 16.4.3 Check `.gitignore` includes `.env`, `*.log`, `target/`
- [ ] 16.4.4 Verify no personal email addresses or credentials in any committed file

### Task 16.5 — Clean-clone build verification
- [ ] 16.5.1 From a fresh directory, clone the repo
- [ ] 16.5.2 Set all required environment variables
- [ ] 16.5.3 Run `mvn clean install`
- [ ] 16.5.4 Confirm build passes with all tests green
- [ ] 16.5.5 Confirm app starts and `/swagger-ui.html` is reachable
- [ ] 16.5.6 Confirm `logs/family-league.log` is created on startup

---

## Dependency Map

| Phase | Name                   | Depends On   |
|-------|------------------------|--------------|
| 1     | Project Scaffolding    | —            |
| 2     | Database Foundation    | 1            |
| 3     | Core Framework         | 1, 2         |
| 4     | Auth & RBAC            | 3            |
| 5     | User Management        | 4            |
| 6     | League & Season        | 4            |
| 7     | Team & Player          | 4            |
| 8     | Match Scheduling       | 6, 7         |
| 9     | Prediction Submission  | 8            |
| 10    | Result Processing      | 9            |
| 11    | Leaderboard            | 10           |
| 12    | Notifications          | 11           |
| 13    | Audit System           | 3            |
| 14    | API Documentation      | All          |
| 15    | Testing                | All          |
| 16    | Final Packaging        | All          |
