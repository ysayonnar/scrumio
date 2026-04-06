# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
./gradlew build

# Run application (requires PostgreSQL running)
just run                  # exports .env and runs ./gradlew bootRun

# Tests
./gradlew test
./gradlew test --tests "com.example.scrumio.SomeTest"   # single test
just test                 # alias

# Database
just up                   # docker compose up with build
just down                 # docker compose down
just apply-migrations     # flywayMigrate + flywayInfo + flywayValidate

# Frontend
just front                # cd frontend && npm install && npm run dev

# Concurrency demo
just demo-counter         # 10 parallel counter increments (race condition demo)
just test-async           # JMeter load test for concurrency

# Other
./gradlew clean
```

## Architecture

Spring Boot 4.0.2 / Java 25 REST API with layered architecture:

```
Controller → Service → Repository → Entity
```

Package root: `com.example.scrumio`

- **auth/** — `@RequireAuth` annotation, `AuthInterceptor` (validates cookies via external auth service), `AuthContext` (ThreadLocal userId/role), `AuthClient` (REST client)
- **config/** — Web MVC configuration (registers `AuthInterceptor`)
- **controller/** — REST endpoints (`@RestController`)
- **service/** — Business logic
- **repository/** — Spring Data JPA repositories with custom JPQL (`JOIN FETCH` to prevent N+1)
- **entity/** — JPA entities + enums organized by subdomain (`ticket/`, `sprint/`, `meeting/`, `user/`, `project/`)
- **web/dto/** — Request/response records (Java `record` types, no sub-packages)
- **web/exception/** — `GlobalExceptionHandler` (`@RestControllerAdvice`)
- **mapper/** — `@Component` classes with `toResponse(Entity)` method

## Auth

All endpoints except `POST /api/v1/users` require `@RequireAuth`.

`AuthInterceptor` calls `${auth-service.url}/auth` with request cookies. On success it stores `userId` and `role` in the request attributes. `AuthContext.getUserId()` / `AuthContext.getRole()` retrieve them in services.

Membership is verified via `projectMemberRepository.findActiveByProjectAndUser(projectId, userId)` — throws `ProjectNotFoundException` if the user is not a member.

## Database

PostgreSQL via Flyway migrations in `src/main/resources/db/migrations/`:

- `V1__create_baseline.sql` — PostgreSQL ENUMs + 8 core tables
- `V2__create_indexes.sql` — Indexes on FK columns

All entities extend `BaseEntity` (`@MappedSuperclass`) which provides: `UUID id`, `OffsetDateTime createdAt`, `updatedAt`, `deletedAt` (soft delete). Soft delete: services set `entity.setDeletedAt(OffsetDateTime.now())` then save; repos filter `WHERE deletedAt IS NULL`.

`spring.jpa.hibernate.ddl-auto: validate` — schema must match migrations exactly; Flyway runs on startup.

Local DB: `localhost:5432`, database `scrumio_db`, user/password `admin/admin` (see `infrastructure/docker-compose.yaml`).

## API Endpoints

| Controller | Base path | Notable |
|---|---|---|
| UserController | `/api/v1/users` | `POST /` is public; all others `@RequireAuth` |
| ProjectController | `/api/v1/projects` | all `@RequireAuth` |
| SprintController | `/api/v1/sprints` | `GET /` requires `?project_id=` |
| TicketController | `/api/v1/tickets` | `GET /` requires `?project_id=`; optional `?status=&priority=&sprint_status=&page=&size=`; `/native` uses native query; `/safe` and `/unsafe` are N+1 demos; `POST /bulk` (transactional batch) and `POST /bulk-unsafe` (N+1 demo) |
| MeetingController | `/api/v1/meetings` | `GET /` requires `?project_id=`; `POST /with-members` (safe, `@Transactional`), `POST /with-members-unsafe` (NOT_SUPPORTED, N+1 demo) |
| ProjectMemberController | `/api/v1/projects/{projectId}/members` | OWNER/MANAGER can add/updateRole; OWNER-only remove |
| MemberTicketController | `/api/v1/tickets/{ticketId}/members` | assign/unassign members to tickets |
| ConcurrencyController | `/api/v1/concurrency` | `POST /tasks`, `GET /tasks/{id}`, `POST /counter/increment`, `GET /counter`, `POST /race` — concurrency demos (no auth) |

Response conventions: POST → `201 CREATED`; DELETE → soft-deleted entity `200 OK`.

## Exception Handling

`GlobalExceptionHandler` handles:

- `NotFoundException` subclasses (User/Project/Sprint/Ticket/Meeting/ProjectMember/MemberTicket/Task) → `404`
- `UnauthorizedException` → `401`
- `ServiceUnavailableException` → `503`
- `MethodArgumentNotValidException` → `400`
- `IllegalArgumentException` → `400`
- `HttpMessageNotReadableException` → `400` (malformed JSON)
- `MissingServletRequestParameterException` → `400`
- `MethodArgumentTypeMismatchException` → `400`
- `Exception` (catch-all) → `500`

All return `ErrorResponse(code, message)`.

## Code Style

Do not write comments (`//`, `/* */`, or Javadoc). Code must be self-documented through clear naming.

## Restrictions

Never commit and push code

## Domain Model

Core entities: **User**, **Project**, **Sprint**, **Ticket**, **Meeting**, **ProjectMember**, **MemberTicket**, **MeetingMember**.

Key enums:

- `TicketStatus`: BACKLOG, TODO, IN_PROGRESS, ON_HOLD, ON_REVIEW, DONE
- `TicketPriority`: LOW, MEDIUM, HIGH
- `SprintStatus`: PLANNED, ACTIVE, COMPLETED
- `SprintEstimationType`: STORY_POINTS, HOURS
- `MeetingType`: PLANNING, DAILY, REVIEW, RETROSPECTIVE, SCRUM_POKER, REGULAR
- `UserRole`: ADMIN, MEMBER
- `ProjectMemberRole`: OWNER, STAKEHOLDER, MANAGER, DEVELOPER
