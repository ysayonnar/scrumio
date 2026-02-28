# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run application (requires PostgreSQL running)
make run                  # or: ./gradlew bootRun

# Tests
./gradlew test
./gradlew test --tests "com.example.scrumio.SomeTest"   # single test

# Database
make postgres-up          # start PostgreSQL container
make postgres-down        # stop PostgreSQL container

# Other
make todos                # find TODO comments in code
./gradlew clean
```

## Architecture

Spring Boot 4.0.2 / Java 25 REST API with layered architecture:

```
Controller → Service → Repository → Entity
```

Package root: `com.example.scrumio`

- **controller/** — REST endpoints (`@RestController`)
- **service/** — Business logic
- **repository/** — Data access layer (currently in-memory `HashMap`; intended to migrate to Spring Data JPA)
- **entity/** — Domain enums organized by subdomain (`ticket/`, `sprint/`, `meeting/`, `user/`, `projectmember/`)
- **web/dto/** — Request/response records (Java `record` types)
- **web/exception/** — `GlobalExceptionHandler` (`@RestControllerAdvice`)
- **mapper/** — Entity ↔ DTO conversion

## Database

PostgreSQL via Flyway migrations in `src/main/resources/db/migrations/`:

- `V1__create_baseline.sql` — PostgreSQL ENUMs + 7 core tables
- `V2__create_indexes.sql` — Indexes on FK columns

All tables have a `deleted_at` field for soft deletes.

`spring.jpa.hibernate.ddl-auto: validate` — schema must match migrations exactly; Flyway runs on startup.

Local DB: `localhost:5432`, database `scrumio_db`, user/password `admin/admin` (see
`infrastructure/docker-compose.yaml`).

## Domain Model

Core entities: **User**, **Project**, **Sprint**, **Ticket**, **Meeting**, **ProjectMember** (join table), plus
`member_ticket` and `meeting_member` junction tables.

Key enums:

- `TicketStatus`: BACKLOG, TODO, IN_PROGRESS, ON_HOLD, ON_REVIEW, DONE
- `TicketPriority`: LOW, MEDIUM, HIGH
- `SprintStatus`: PLANNED, ACTIVE, COMPLETED
- `UserRole`: ADMIN, MEMBER
- `ProjectMemberRole`: OWNER, STAKEHOLDER, MANAGER, DEVELOPER
