# Business Requirements Document (BRD)
**Project:** Family League – Prediction Platform  
**Version:** 1.0  
**Date:** 2026-05-22  
**Status:** Approved  

---

## Table of Contents
1. [Document Control](#1-document-control)
2. [Executive Summary](#2-executive-summary)
3. [Business Context](#3-business-context)
4. [Business Objectives](#4-business-objectives)
5. [Stakeholders and Personas](#5-stakeholders-and-personas)
6. [Scope](#6-scope)
7. [Business Rules](#7-business-rules)
8. [Functional Business Requirements](#8-functional-business-requirements)
9. [Business Constraints](#9-business-constraints)
10. [Assumptions and Dependencies](#10-assumptions-and-dependencies)
11. [Success Criteria](#11-success-criteria)
12. [Glossary](#12-glossary)

---

## 1. Document Control

| Attribute    | Detail                                  |
|--------------|-----------------------------------------|
| Document ID  | BRD-FL-001                              |
| Version      | 1.0                                     |
| Prepared By  | Engineering Team                        |
| Review Date  | 2026-05-22                              |
| Platform     | Family League – Backend Service         |

### Revision History

| Version | Date       | Author           | Summary of Changes       |
|---------|------------|------------------|--------------------------|
| 1.0     | 2026-05-22 | Engineering Team | Initial document created |

---

## 2. Executive Summary

Family League is a backend platform designed to let groups of family and friends engage in friendly competition by predicting outcomes of real-world cricket leagues. Users earn points when their predictions match actual results, compete on leaderboards, and receive timely notifications – all within a safe, role-governed environment.

The platform supports multiple concurrent leagues, each with their own seasons, matches, teams, and players. It is intentionally scoped to the **service (backend) layer only**; no user interface is included in this delivery.

---

## 3. Business Context

### 3.1 Problem Statement

Cricket leagues such as the IPL generate enormous engagement among fans. There is no dedicated, private platform for families and close-knit groups to run their own prediction leagues with structured scoring, leaderboards, and notifications. Existing public platforms lack the privacy, customisability, and administrative control that a private family group requires.

### 3.2 Business Opportunity

By building a self-hosted, configurable prediction platform, families and friend groups can:
- Host prediction competitions during any real-world cricket league.
- Maintain their own leaderboard, scoring, and notification system.
- Control the entire experience through an Admin persona without relying on third-party services.

### 3.3 Platform Boundaries

This platform is **exclusively a backend service**. All interactions occur through API calls. A user interface is explicitly out of scope for this version.

---

## 4. Business Objectives

| ID   | Objective                                                                 | Priority |
|------|---------------------------------------------------------------------------|----------|
| BO-1 | Allow users to predict match outcomes and earn points fairly              | Must     |
| BO-2 | Support multiple simultaneous leagues with independent configurations     | Must     |
| BO-3 | Enforce prediction windows automatically without manual intervention      | Must     |
| BO-4 | Enable an Admin persona to manage all league data and publish results     | Must     |
| BO-5 | Maintain a real-time, accurate leaderboard per season                    | Must     |
| BO-6 | Notify users of approaching deadlines and result updates via email        | Must     |
| BO-7 | Preserve a full audit trail of all data changes                          | Must     |
| BO-8 | Ensure no prediction data is permanently deleted                         | Must     |
| BO-9 | Allow Admin to communicate custom messages to selected users              | Should   |
| BO-10| Support league-level predictions (full team ranking for the season)       | Must     |

---

## 5. Stakeholders and Personas

### 5.1 Personas

#### Admin
The administrator is the platform operator. They are responsible for setting up all league data, publishing match results, and monitoring platform health. Admins have elevated privileges and their actions are audited.

**Responsibilities:**
- Create and manage leagues, seasons, matches, teams, and players.
- Publish match results after the actual outcome is declared.
- Monitor and close seasons upon final result confirmation.
- Send notifications to users.
- Verify and close leagues once completed.

#### User (Participant)
A family member or friend participating in the prediction game. They register, submit predictions, view leaderboards, and receive notifications.

**Responsibilities:**
- Register and maintain their own profile.
- Submit predictions for matches and league-level team rankings.
- View leaderboard standings.
- Receive email reminders and result notifications.

### 5.2 Stakeholder Matrix

| Stakeholder    | Role            | Interest                             | Influence |
|----------------|-----------------|--------------------------------------|-----------|
| Admin          | Platform owner  | Operational control, data accuracy   | High      |
| User           | Participant     | Fair scoring, timely notifications   | Medium    |
| Platform Host  | Infrastructure  | Uptime, security                     | High      |

---

## 6. Scope

### 6.1 In Scope

- User registration, login, and profile management.
- Role-based access control (Admin and User personas).
- League and season lifecycle management.
- Team and player data management.
- Match scheduling with automated prediction lock enforcement.
- Match-level and league-level prediction submission.
- Match result publishing by Admin.
- Automated points calculation and leaderboard recalculation.
- Email notifications (reminders, result alerts, bulk communication).
- Full audit trail of data changes.
- Soft delete for all primary data entities.
- Admin bulk notification capability.
- API collection and documentation.

### 6.2 Out of Scope

- User Interface (web or mobile).
- Third-party OAuth login (may be added in a future version).
- Live score integration (results are manually entered by Admin).
- Payment or subscription management.
- Social features (chat, comments).
- Mobile push notifications.

---

## 7. Business Rules

### 7.1 Prediction Window Rules

| Rule ID | Rule                                                                                        |
|---------|---------------------------------------------------------------------------------------------|
| BR-P1   | Match predictions must be submitted at least **1 hour before** the match start time.        |
| BR-P2   | League-level team ranking predictions must be submitted at least **4 hours before** the first match in the season. |
| BR-P3   | Once the prediction window closes, no amendments are permitted – not even by Admin.          |
| BR-P4   | Users may view **their own** predictions at any time.                                       |
| BR-P5   | Users may view **other users' predictions** only after the prediction window closes.         |

### 7.2 Scoring Rules

| Rule ID | Rule                                                                                        |
|---------|---------------------------------------------------------------------------------------------|
| BR-S1   | Each correct prediction earns the user **1 point**.                                         |
| BR-S2   | Match predictions cover: match winner, toss winner, and player of the match (3 points max per match). |
| BR-S3   | A tie result counts as an official result. Both teams' backers receive the point.           |
| BR-S4   | Points are **never** accepted via API input – they are always system-calculated upon result publication. |
| BR-S5   | Leaderboard ranks are recalculated after every confirmed result publication.                |

### 7.3 Result Processing Rules

| Rule ID | Rule                                                                                        |
|---------|---------------------------------------------------------------------------------------------|
| BR-R1   | Match results are manually published by the Admin after the actual result is declared.      |
| BR-R2   | Leaderboard recalculation is performed in **asynchronous** (async) mode post result publishing. |
| BR-R3   | Admin receives an email notification when leaderboard recalculation is complete.            |
| BR-R4   | League-level final results are updated by Admin when the league concludes.                  |

### 7.4 League Lifecycle Rules

| Rule ID | Rule                                                                                        |
|---------|---------------------------------------------------------------------------------------------|
| BR-L1   | A League is the umbrella entity. A Season is an instance of that league.                   |
| BR-L2   | Teams are independent of leagues – the same team may participate in multiple seasons.       |
| BR-L3   | Seasons transition through: UPCOMING → ACTIVE → COMPLETED → CLOSED.                        |
| BR-L4   | Once a season is **CLOSED**, no amendments are allowed – not even by Admin.                 |
| BR-L5   | Closed seasons remain fully readable and accessible.                                        |
| BR-L6   | League-level team positions are adjusted after each match result.                           |

### 7.5 Notification Rules

| Rule ID | Rule                                                                                        |
|---------|---------------------------------------------------------------------------------------------|
| BR-N1   | Users who have not submitted predictions receive an email reminder before the prediction window closes. |
| BR-N2   | Admin receives email alerts when a result needs to be entered.                              |
| BR-N3   | All email notifications must be stored in the data store with: recipient, type, subject, body, status, and timestamp. |
| BR-N4   | Admin can bulk-communicate by selecting users and event types with custom messaging.        |

### 7.6 Data Integrity Rules

| Rule ID | Rule                                                                                        |
|---------|---------------------------------------------------------------------------------------------|
| BR-D1   | No records are permanently deleted unless a documented justification exists in the Decision Log. |
| BR-D2   | All data changes must be captured as audit records (who changed what, when, old value, new value). |
| BR-D3   | User avatar and display name can be updated by the user themselves.                         |

---

## 8. Functional Business Requirements

### 8.1 User Management

| Req ID  | Requirement                                                                              |
|---------|------------------------------------------------------------------------------------------|
| BR-UM-1 | The system shall allow users to register with a name, email, and password.               |
| BR-UM-2 | The system shall allow users to log in and receive a session token.                      |
| BR-UM-3 | The system shall allow users to update their avatar and display name.                    |
| BR-UM-4 | Admin shall be able to activate or deactivate user accounts.                             |
| BR-UM-5 | The system shall enforce role-based access – Admin routes must not be accessible to regular users. |

### 8.2 League and Season Management

| Req ID  | Requirement                                                                              |
|---------|------------------------------------------------------------------------------------------|
| BR-LM-1 | Admin shall be able to create a League with a name and description.                      |
| BR-LM-2 | Admin shall be able to create a Season under a League with start/end dates.              |
| BR-LM-3 | The system shall automatically set the league prediction lock time to 4 hours before the first match of the season. |
| BR-LM-4 | Admin shall be able to close a season. Closed seasons shall be read-only.                |
| BR-LM-5 | Admin shall be able to add teams to a season.                                            |

### 8.3 Match Management

| Req ID  | Requirement                                                                              |
|---------|------------------------------------------------------------------------------------------|
| BR-MM-1 | Admin shall be able to schedule matches with two teams, venue, and start time.           |
| BR-MM-2 | The system shall automatically set the match prediction lock time to 1 hour before start.|
| BR-MM-3 | The system shall prevent prediction submissions after the lock time.                     |
| BR-MM-4 | Admin shall be able to publish a match result after the actual result is declared.       |
| BR-MM-5 | A result must capture: winner (or tie), toss winner, and player of the match.            |

### 8.4 Prediction Management

| Req ID  | Requirement                                                                              |
|---------|------------------------------------------------------------------------------------------|
| BR-PM-1 | Users shall be able to submit a match prediction covering winner, toss winner, and player of the match. |
| BR-PM-2 | Users shall be able to submit a league-level prediction ranking all teams 1 to N.        |
| BR-PM-3 | Users shall be able to update their prediction until the lock time.                      |
| BR-PM-4 | The system shall reject any prediction submitted after the lock time.                    |
| BR-PM-5 | Users shall be able to view their own predictions at any time.                           |
| BR-PM-6 | Users shall only see other users' predictions after the lock time has passed.            |

### 8.5 Scoring and Leaderboard

| Req ID  | Requirement                                                                              |
|---------|------------------------------------------------------------------------------------------|
| BR-SL-1 | The system shall calculate points automatically when a result is published.              |
| BR-SL-2 | Points shall never be accepted via API – the system is the sole authority.               |
| BR-SL-3 | The leaderboard shall be recalculated asynchronously after each result publication.      |
| BR-SL-4 | Rankings shall be determined solely by total points.                                     |
| BR-SL-5 | The leaderboard shall display one row per user per season.                               |

### 8.6 Notifications

| Req ID  | Requirement                                                                              |
|---------|------------------------------------------------------------------------------------------|
| BR-NF-1 | The system shall send email reminders to users who have not predicted before the lock window closes. |
| BR-NF-2 | Admin shall receive email alerts when results require entry.                             |
| BR-NF-3 | Admin shall receive email confirmation when leaderboard recalculation completes.         |
| BR-NF-4 | All sent emails shall be stored in the notification data store.                          |
| BR-NF-5 | Admin shall be able to send bulk custom emails to selected users.                        |

---

## 9. Business Constraints

| Constraint ID | Constraint                                                                          |
|---------------|-------------------------------------------------------------------------------------|
| BC-1          | The platform is backend-only – no user interface is delivered.                      |
| BC-2          | All communications are via email only (no SMS, push notifications, or in-app alerts).|
| BC-3          | Results are manually entered by Admin – no live data feed integration.              |
| BC-4          | The platform must support HTTPS for all API communications.                         |
| BC-5          | Session management must be JWT-based.                                               |
| BC-6          | No hardcoded credentials or personal data in the codebase or configuration files.  |

---

## 10. Assumptions and Dependencies

### 10.1 Assumptions

| Assumption ID | Assumption                                                                           |
|---------------|--------------------------------------------------------------------------------------|
| AS-1          | Admin users are trusted and known – no self-registration for Admin role.             |
| AS-2          | All match schedules are known in advance and entered by Admin before season start.   |
| AS-3          | Email delivery is handled via a configured SMTP provider.                            |
| AS-4          | All users have valid, reachable email addresses.                                     |
| AS-5          | The platform hosts a single deployment instance per group/family.                    |

### 10.2 Dependencies

| Dependency ID | Dependency                                                                          |
|---------------|-------------------------------------------------------------------------------------|
| DEP-1         | An SMTP server or email service (e.g., Gmail SMTP, SendGrid) must be available.     |
| DEP-2         | A PostgreSQL database instance must be provisioned.                                 |
| DEP-3         | Java 17+ runtime environment must be available.                                     |

---

## 11. Success Criteria

| Criterion ID | Criterion                                                                                 |
|--------------|-------------------------------------------------------------------------------------------|
| SC-1         | All functional business requirements (Section 8) are implemented and verifiable via API. |
| SC-2         | Prediction lock is enforced – no prediction is accepted after the configured window.      |
| SC-3         | Points are calculated correctly and cannot be submitted via API.                          |
| SC-4         | Leaderboard reflects accurate rankings after every result publication.                   |
| SC-5         | All email notifications are delivered and stored in the data store.                      |
| SC-6         | Audit trail records every data change with actor, timestamp, and old/new values.         |
| SC-7         | Application starts cleanly from a fresh clone following README instructions.             |
| SC-8         | No hardcoded credentials or personal data exist anywhere in the repository.              |

---

## 12. Glossary

| Term                    | Definition                                                                              |
|-------------------------|-----------------------------------------------------------------------------------------|
| League                  | The umbrella competition entity (e.g., "IPL 2025"). Multiple seasons may exist under one league. |
| Season                  | A specific instance of a league (e.g., "IPL 2025 Season 1"). Contains matches and teams. |
| Match                   | A single game between two teams within a season.                                        |
| Prediction              | A user's forecast for a match or season outcome submitted before the lock time.         |
| Prediction Lock Time    | The point in time after which predictions can no longer be submitted or amended.        |
| Leaderboard             | A ranked list of users ordered by total points earned within a season.                  |
| Admin                   | A privileged user persona responsible for data management and result publication.       |
| Soft Delete             | Marking a record as deleted without removing it from the database.                      |
| Audit Trail             | A log of all data changes including who changed what and when.                          |
| Points                  | Score units awarded when a prediction matches the actual result.                        |
| Result Type             | Classification of a match outcome: WIN, TIE, or NO_RESULT.                             |
| RBAC                    | Role-Based Access Control – restricting API access based on the user's assigned role.  |
