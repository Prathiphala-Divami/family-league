# Product Requirements Document (PRD)
**Project:** Family League – Prediction Platform  
**Version:** 1.0  
**Date:** 2026-05-22  
**Status:** Approved  

---

## Table of Contents
1. [Document Control](#1-document-control)
2. [Product Overview](#2-product-overview)
3. [User Personas](#3-user-personas)
4. [User Stories](#4-user-stories)
5. [Feature Requirements](#5-feature-requirements)
6. [API Surface Summary](#6-api-surface-summary)
7. [User Flows](#7-user-flows)
8. [Non-Functional Requirements](#8-non-functional-requirements)
9. [Acceptance Criteria](#9-acceptance-criteria)
10. [Out of Scope](#10-out-of-scope)

---

## 1. Document Control

| Attribute    | Detail                                      |
|--------------|---------------------------------------------|
| Document ID  | PRD-FL-001                                  |
| Version      | 1.0                                         |
| Prepared By  | Engineering Team                            |
| Review Date  | 2026-05-22                                  |
| References   | BRD-FL-001, DATA_MODEL v1.2, TRD-FL-001     |

### Revision History

| Version | Date       | Author           | Summary of Changes       |
|---------|------------|------------------|--------------------------|
| 1.0     | 2026-05-22 | Engineering Team | Initial document created |
| 1.1     | 2026-05-23 | Engineering Team | Added season-team management (F-3.7/F-3.8), PUT prediction endpoint, logout endpoint; updated data model reference to v1.2 |

---

## 2. Product Overview

### 2.1 What is Family League?

Family League is a **REST API backend platform** that enables private groups to run prediction competitions during real-world cricket leagues. Users predict match outcomes, earn points, and compete on leaderboards. An Admin persona governs all data and result entry.

### 2.2 Core Value Proposition

| For         | The platform provides                                              |
|-------------|--------------------------------------------------------------------|
| Users       | A fair, engaging prediction game with automatic scoring            |
| Admin       | Full control over league data, results, and communications         |
| The group   | A private, competitive leaderboard with equal rules for all        |

### 2.3 Key Differentiators

- Prediction windows are **automatically enforced** – no manual locking needed.
- Points are **never manually entered** – the system calculates them on result publish.
- All data is **auditable** – every change is recorded with actor and timestamp.
- Predictions are **visible to peers only after the lock** – no gaming the system.

---

## 3. User Personas

### 3.1 Persona: Admin (Gopal / Rama)

| Attribute    | Detail                                                                  |
|--------------|-------------------------------------------------------------------------|
| Role         | Platform operator                                                       |
| Technical    | Comfortable with REST APIs and tools like Postman                       |
| Goals        | Set up league data quickly; publish results with minimal friction       |
| Pain Points  | Needs to be sure points are fair; wants alerts when action is needed    |
| Key Flows    | Create league → Add season → Schedule matches → Publish results → Close season |

### 3.2 Persona: User (Family Member / Friend)

| Attribute    | Detail                                                                  |
|--------------|-------------------------------------------------------------------------|
| Role         | Prediction participant                                                  |
| Technical    | Non-technical; consumes via a UI (future) or mobile app                |
| Goals        | Submit predictions before deadline; track their standing on leaderboard|
| Pain Points  | Forgetting to predict; not knowing where they stand                    |
| Key Flows    | Register → Log in → Submit predictions → View leaderboard → Get notifications |

---

## 4. User Stories

### 4.1 Authentication and Profile

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-A1    | User   | Register with my name, email, and password         | I can access the platform                           |
| US-A2    | User   | Log in and receive a JWT token                     | I can make authenticated API calls                  |
| US-A3    | User   | Update my display name and avatar                  | My profile reflects who I am                        |
| US-A4    | User   | Log out and invalidate my session                  | My account is secure when I step away               |
| US-A5    | Admin  | Activate or deactivate a user account              | I can manage platform access                        |

### 4.2 League and Season Management (Admin)

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-L1    | Admin  | Create a League with a name and description        | I can organise competitions under a named umbrella  |
| US-L2    | Admin  | Create a Season under a League with start/end dates| I can run a specific edition of the competition     |
| US-L3    | Admin  | Add teams to a season                              | Only participating teams appear in predictions      |
| US-L4    | Admin  | View all seasons and their statuses                | I can monitor the competition lifecycle             |
| US-L5    | Admin  | Close a season after the final result is confirmed | No further changes occur after the competition ends |

### 4.3 Team and Player Management (Admin)

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-T1    | Admin  | Create a team with name, short name, and logo      | Teams are available for season assignment           |
| US-T2    | Admin  | Add players to a team with role and jersey number  | Player-of-the-match predictions are possible        |
| US-T3    | Admin  | Update player details                              | Roster changes during a season are reflected        |

### 4.4 Match Scheduling (Admin)

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-M1    | Admin  | Schedule a match with teams, venue, and start time | Users know when to submit predictions               |
| US-M2    | Admin  | Have the prediction lock time automatically set    | I don't have to manually manage deadlines           |
| US-M3    | Admin  | View all scheduled matches for a season            | I can verify the schedule is correct                |
| US-M4    | Admin  | Update a match before it starts                    | I can correct scheduling errors                     |

### 4.5 Prediction Submission (User)

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-P1    | User   | Predict the match winner before the lock time      | I can earn a point if I am correct                  |
| US-P2    | User   | Predict the toss winner before the lock time       | I can earn a point if I am correct                  |
| US-P3    | User   | Predict the player of the match before the lock    | I can earn a point if I am correct                  |
| US-P4    | User   | Predict the full season team ranking (1 to N)      | I can earn points for the league-level outcome      |
| US-P5    | User   | Update my prediction before the lock time          | I can change my mind if I get new information       |
| US-P6    | User   | View my own predictions at any time                | I can track what I have submitted                   |
| US-P7    | User   | View other users' predictions after lock time      | I can compare strategies after the window closes    |

### 4.6 Result Processing (Admin)

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-R1    | Admin  | Publish the result for a completed match           | Points are calculated and the leaderboard updates   |
| US-R2    | Admin  | Record the winner, toss winner, and player of match| All three prediction categories are scored          |
| US-R3    | Admin  | Record a tie result                                | Tie outcomes are handled correctly                  |
| US-R4    | Admin  | Receive an email when leaderboard recalculation completes | I know the leaderboard is up to date           |

### 4.7 Leaderboard (User and Admin)

| Story ID | As a…      | I want to…                                       | So that…                                            |
|----------|------------|--------------------------------------------------|-----------------------------------------------------|
| US-LB1   | User       | View the current leaderboard for a season        | I know where I stand                                |
| US-LB2   | User       | See my rank, total points, and other participants| I can compare my performance                        |
| US-LB3   | Admin      | See the full leaderboard with all participant data| I can verify the standings are correct             |

### 4.8 Notifications

| Story ID | As a…  | I want to…                                         | So that…                                            |
|----------|--------|----------------------------------------------------|-----------------------------------------------------|
| US-NF1   | User   | Receive an email reminder before the match lock    | I am not penalised for forgetting                   |
| US-NF2   | Admin  | Receive an email when a result needs to be entered | I action it promptly                                |
| US-NF3   | Admin  | Send a custom bulk email to selected users         | I can communicate with the group on demand          |
| US-NF4   | User   | Receive a notification when results are published  | I know when to check the leaderboard                |

---

## 5. Feature Requirements

### F-1: Authentication and Authorisation

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-1.1    | Users register via `POST /auth/register` with name, email, password.                      | Must     |
| F-1.2    | Users log in via `POST /auth/login` and receive a JWT access token.                       | Must     |
| F-1.3    | JWT tokens must expire and must be validated on every protected endpoint.                 | Must     |
| F-1.4    | All Admin endpoints must reject requests from non-Admin tokens with HTTP 403.             | Must     |
| F-1.5    | Passwords must be stored as BCrypt hashes – never plaintext.                              | Must     |
| F-1.6    | A refresh token mechanism should be available to extend sessions without re-login.        | Should   |

### F-2: Profile Management

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-2.1    | Users can update their own name and avatar via `PUT /users/me`.                           | Must     |
| F-2.2    | Users cannot update other users' profiles.                                                | Must     |
| F-2.3    | Admin can view any user profile.                                                          | Must     |
| F-2.4    | Admin can activate or deactivate a user account.                                          | Must     |

### F-3: League and Season Management

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-3.1    | Admin creates a League via `POST /leagues`.                                               | Must     |
| F-3.2    | Admin creates a Season under a League via `POST /leagues/{id}/seasons`.                   | Must     |
| F-3.3    | Season `prediction_lock_time` is automatically set to 4 hours before the first scheduled match start time within that season. | Must |
| F-3.4    | Season status transitions are enforced: UPCOMING → ACTIVE → COMPLETED → CLOSED.          | Must     |
| F-3.5    | A CLOSED season returns HTTP 403 on any write operation.                                  | Must     |
| F-3.6    | All league and season endpoints support pagination, sort, and search.                     | Must     |
| F-3.7    | Admin can add a team to a season via `POST /seasons/{id}/teams`. Only teams in `season_team` may appear in match scheduling and league predictions. | Must |
| F-3.8    | Any authenticated user can list the teams in a season via `GET /seasons/{id}/teams`.      | Must     |

### F-4: Team and Player Management

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-4.1    | Admin creates teams independently of any league via `POST /teams`.                        | Must     |
| F-4.2    | Admin adds players to a team via `POST /teams/{id}/players`.                              | Must     |
| F-4.3    | Player role must be one of: BATSMAN, BOWLER, ALL_ROUNDER, WICKET_KEEPER.                 | Must     |
| F-4.4    | Teams are reusable across multiple seasons.                                               | Must     |
| F-4.5    | Soft delete applies – no team or player is permanently removed.                           | Must     |

### F-5: Match Scheduling

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-5.1    | Admin schedules matches via `POST /seasons/{id}/matches`.                                 | Must     |
| F-5.2    | Prediction lock time is auto-set to `start_time - 1 hour` and stored explicitly.         | Must     |
| F-5.3    | Matches support pagination, sort by start time, and filter by status.                    | Must     |
| F-5.4    | A match cannot have the same team as both team1 and team2.                               | Must     |
| F-5.5    | Both `team1_id` and `team2_id` must exist in `season_team` for the given season; otherwise the request is rejected with HTTP 400. | Must |

### F-6: Prediction Submission

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-6.1    | Users submit match predictions via `POST /matches/{id}/predictions`.                      | Must     |
| F-6.2    | Prediction submission is rejected with HTTP 409 if submitted after `prediction_lock_time`.| Must     |
| F-6.3    | Users update an existing prediction via `PUT /matches/{id}/predictions` before lock.     | Must     |
| F-6.4    | Only one prediction record exists per user per match (upsert pattern).                   | Must     |
| F-6.5    | Users submit league predictions via `POST /seasons/{id}/league-predictions`.              | Must     |
| F-6.6    | League prediction must include a rank for every team in the season (no gaps).            | Must     |
| F-6.7    | Duplicate rank or duplicate team in a league prediction submission is rejected.           | Must     |
| F-6.8    | `GET /matches/{id}/predictions` returns all predictions only after lock time; returns only the authenticated user's prediction before lock. | Must |

### F-7: Result Publishing

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-7.1    | Admin publishes a result via `POST /matches/{id}/result`.                                 | Must     |
| F-7.2    | Result must include `result_type` (WIN / TIE / NO_RESULT).                               | Must     |
| F-7.3    | If `result_type = WIN`, `winning_team_id` and `player_of_match_id` are required.         | Must     |
| F-7.4    | If `result_type = TIE` or `NO_RESULT`, `winning_team_id` must be null.                   | Must     |
| F-7.5    | After result publication, point calculation is triggered asynchronously.                 | Must     |
| F-7.6    | Points are never accepted as part of the result request payload.                         | Must     |
| F-7.7    | Admin receives an email after leaderboard recalculation completes.                       | Must     |

### F-8: Leaderboard

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-8.1    | `GET /seasons/{id}/leaderboard` returns paginated, ranked user standings.                 | Must     |
| F-8.2    | Rankings are recalculated every time a result is published.                               | Must     |
| F-8.3    | Leaderboard reflects total points across all completed matches in the season.             | Must     |
| F-8.4    | Ties in points result in shared rank positions.                                           | Should   |

### F-9: Notifications

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-9.1    | A scheduled job runs before each match lock and sends reminders to users without predictions. | Must  |
| F-9.2    | A scheduled job alerts Admin when a match has completed but no result has been published. | Must     |
| F-9.3    | Admin sends bulk notifications via `POST /notifications/bulk`.                            | Must     |
| F-9.4    | Every sent (or attempted) email is recorded in the `notification` table.                  | Must     |
| F-9.5    | Admin can view notification history via `GET /notifications` with pagination and filters. | Must     |

### F-10: Audit

| Req ID   | Requirement                                                                               | Priority |
|----------|-------------------------------------------------------------------------------------------|----------|
| F-10.1   | Every create, update, and soft-delete operation is written to the `audit_log` table.      | Must     |
| F-10.2   | Audit records include: entity, entity ID, action, old value (JSON), new value (JSON), actor, timestamp. | Must |
| F-10.3   | Admin can view audit logs via `GET /audit-logs` with entity filter and pagination.        | Must     |

---

## 6. API Surface Summary

| Method | Endpoint                                      | Persona | Description                          |
|--------|-----------------------------------------------|---------|--------------------------------------|
| POST   | `/auth/register`                              | Public  | Register a new user                  |
| POST   | `/auth/login`                                 | Public  | Authenticate and receive JWT         |
| POST   | `/auth/logout`                                | User    | Invalidate current session (see DL-008 in TRD) |
| GET    | `/users/me`                                   | User    | View own profile                     |
| PUT    | `/users/me`                                   | User    | Update name and avatar               |
| GET    | `/users`                                      | Admin   | List all users (paginated)           |
| PUT    | `/users/{id}/status`                          | Admin   | Activate / deactivate a user         |
| POST   | `/leagues`                                    | Admin   | Create a league                      |
| GET    | `/leagues`                                    | User    | List leagues (paginated)             |
| POST   | `/leagues/{id}/seasons`                       | Admin   | Create a season                      |
| GET    | `/leagues/{id}/seasons`                       | User    | List seasons for a league            |
| PUT    | `/seasons/{id}/close`                         | Admin   | Close a season                       |
| POST   | `/seasons/{id}/teams`                         | Admin   | Add a team to a season               |
| GET    | `/seasons/{id}/teams`                         | User    | List teams in a season               |
| POST   | `/teams`                                      | Admin   | Create a team                        |
| GET    | `/teams`                                      | User    | List teams (paginated)               |
| POST   | `/teams/{id}/players`                         | Admin   | Add a player to a team               |
| GET    | `/teams/{id}/players`                         | User    | List players in a team               |
| POST   | `/seasons/{id}/matches`                       | Admin   | Schedule a match                     |
| GET    | `/seasons/{id}/matches`                       | User    | List matches in a season             |
| POST   | `/matches/{id}/result`                        | Admin   | Publish match result                 |
| POST   | `/matches/{id}/predictions`                   | User    | Submit a match prediction            |
| PUT    | `/matches/{id}/predictions`                   | User    | Update an existing prediction (before lock) |
| GET    | `/matches/{id}/predictions`                   | User    | View predictions (all users post-lock; own only pre-lock) |
| GET    | `/matches/{id}/predictions/me`                | User    | View own prediction at any time      |
| POST   | `/seasons/{id}/league-predictions`            | User    | Submit league-level team ranking     |
| PUT    | `/seasons/{id}/league-predictions`            | User    | Update league-level prediction (before lock) |
| GET    | `/seasons/{id}/league-predictions/me`         | User    | View own league prediction           |
| GET    | `/seasons/{id}/leaderboard`                   | User    | View leaderboard                     |
| POST   | `/notifications/bulk`                         | Admin   | Send bulk custom notification        |
| GET    | `/notifications`                              | Admin   | View notification history            |
| GET    | `/audit-logs`                                 | Admin   | View audit trail                     |

---

## 7. User Flows

### 7.1 Match Prediction Flow

```
User logs in
    │
    ▼
GET /seasons/{id}/matches  ───  selects an upcoming match
    │
    ▼
POST /matches/{id}/predictions  ───  submits winner + toss + player predictions
    │
    ├── If before prediction_lock_time ──► 201 Created
    └── If after  prediction_lock_time ──► 409 Conflict (window closed)
    │
    ▼
[Time passes – lock time passes]
    │
    ▼
Admin: POST /matches/{id}/result  ───  publishes result
    │
    ▼
[Async job] calculates points for all predictions of this match
    │
    ▼
[Async job] recalculates leaderboard rankings
    │
    ▼
[Async job] emails Admin: "Leaderboard updated for Match X"
```

### 7.2 League Prediction Flow

```
Admin creates season
    │
    ▼
Admin schedules first match (sets match start_time)
    │
    ▼
System auto-sets season.prediction_lock_time = first_match.start_time - 4hrs
    │
    ▼
User: POST /seasons/{id}/league-predictions  ───  submits team rankings 1-N
    │
    ├── If before season.prediction_lock_time ──► 201 Created
    └── If after  season.prediction_lock_time ──► 409 Conflict
```

### 7.3 Prediction Visibility Flow

```
User: GET /matches/{id}/predictions
    │
    ├── If current time < prediction_lock_time ──► Returns ONLY current user's prediction
    └── If current time >= prediction_lock_time ──► Returns ALL users' predictions
```

### 7.4 Notification Reminder Flow

```
[Scheduled Job – runs periodically]
    │
    ▼
Find all matches where prediction_lock_time is within the reminder window
    │
    ▼
For each such match:
    Find users who have NOT submitted a prediction
    │
    ▼
    Send reminder email to each such user
    │
    ▼
    Store each email in notification table (status: SENT or FAILED)
```

---

## 8. Non-Functional Requirements

### 8.1 Performance

| Req ID  | Requirement                                                                       |
|---------|-----------------------------------------------------------------------------------|
| NFR-P1  | API responses (excluding leaderboard recalculation) must return within 2 seconds. |
| NFR-P2  | Leaderboard recalculation must occur asynchronously and not block the result publish API response. |
| NFR-P3  | All list endpoints must support pagination to prevent unbounded responses.         |

### 8.2 Security

| Req ID  | Requirement                                                                       |
|---------|-----------------------------------------------------------------------------------|
| NFR-S1  | All APIs must be served over HTTPS.                                               |
| NFR-S2  | JWT tokens must be signed and validated on every request.                         |
| NFR-S3  | Admin routes must be protected by role-based authorization.                       |
| NFR-S4  | Passwords must never be stored or logged in plaintext.                            |
| NFR-S5  | No credentials or API keys may be hardcoded in the source code.                   |
| NFR-S6  | Prediction lock must be enforced at the service layer (not just client-side).     |

### 8.3 Reliability

| Req ID  | Requirement                                                                       |
|---------|-----------------------------------------------------------------------------------|
| NFR-R1  | Email delivery failures must be captured and stored in the notification table.    |
| NFR-R2  | Failed async jobs (leaderboard calculation, notification sending) must be logged. |
| NFR-R3  | All unhandled exceptions must return a consistent error response structure.       |

### 8.4 Maintainability

| Req ID  | Requirement                                                                       |
|---------|-----------------------------------------------------------------------------------|
| NFR-M1  | Application must log to both console and file at appropriate levels.              |
| NFR-M2  | All configurable values (lock offsets, SMTP settings, etc.) must be in configuration files – not hardcoded. |
| NFR-M3  | Database migrations must be managed by Flyway.                                   |
| NFR-M4  | Code must follow standard Java and Spring Boot conventions.                       |

### 8.5 Observability

| Req ID  | Requirement                                                                       |
|---------|-----------------------------------------------------------------------------------|
| NFR-O1  | Log levels: ERROR for failures, WARN for degraded state, INFO for business events, DEBUG for diagnostic detail. |
| NFR-O2  | All incoming requests must be logged with method, path, and response status.     |
| NFR-O3  | Scheduled job execution must be logged with start time, completion time, and outcome. |

---

## 9. Acceptance Criteria

### AC-1: Prediction Lock Enforcement
- **Given** a match with `prediction_lock_time = T`
- **When** a user submits a prediction at `T + 1 minute`
- **Then** the API returns HTTP 409 with a descriptive error message
- **And** no prediction record is created or modified

### AC-2: Points Calculation
- **Given** a match result is published with `result_type = WIN`, `winning_team_id = X`, `toss_winner_team_id = Y`, `player_of_match_id = Z`
- **When** the async calculation job runs
- **Then** each user who predicted winner = X earns 1 point
- **And** each user who predicted toss winner = Y earns 1 point
- **And** each user who predicted player of match = Z earns 1 point
- **And** no user's prediction record contains a `points_earned` value submitted via API

### AC-3: Leaderboard Accuracy
- **Given** three users with points 8, 5, 5
- **When** the leaderboard is fetched for that season
- **Then** the user with 8 points has `rank_position = 1`
- **And** both users with 5 points share `rank_position = 2`

### AC-4: Prediction Visibility
- **Given** a match whose `prediction_lock_time` has not yet passed
- **When** User A calls `GET /matches/{id}/predictions`
- **Then** only User A's own prediction is returned

- **Given** a match whose `prediction_lock_time` has passed
- **When** User A calls `GET /matches/{id}/predictions`
- **Then** all users' predictions are returned

### AC-5: Closed Season Immutability
- **Given** a season with `status = CLOSED`
- **When** any write operation is attempted (by any persona, including Admin)
- **Then** the API returns HTTP 403 with message indicating the season is closed

### AC-6: Tie Handling
- **Given** a match result with `result_type = TIE`
- **When** points are calculated
- **Then** all users who predicted either team as the match winner each earn 1 point
- **And** `winning_team_id` in `match_result` is NULL

---

## 10. Out of Scope

| Item                                  | Reason                                      |
|---------------------------------------|---------------------------------------------|
| User Interface (web or mobile)        | Explicitly excluded per platform definition |
| Live score or data feed integration   | Results are manually entered by Admin       |
| OAuth (Google, Facebook login)        | Simple JWT auth is sufficient for v1        |
| Push notifications                    | Email is the only notification channel      |
| Payment or subscription               | Not a monetised product                     |
| Multi-language support (i18n)         | Not required for private family use         |
| Advanced analytics or reporting       | Not in scope for v1                         |
