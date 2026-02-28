-- =================== enums ====================
CREATE TYPE ticket_priority AS ENUM (
    'LOW',
    'MEDIUM',
    'HIGH'
    );

CREATE TYPE ticket_status AS ENUM (
    'BACKLOG',
    'TODO',
    'IN_PROGRESS',
    'ON_HOLD',
    'ON_REVIEW',
    'DONE'
    );

CREATE TYPE sprint_status AS ENUM (
    'PLANNED',
    'ACTIVE',
    'COMPLETED'
    );

CREATE TYPE sprint_estimation_type AS ENUM (
    'STORY_POINTS',
    'HOURS'
    );

CREATE TYPE meeting_type AS ENUM (
    'PLANNING',
    'DAILY',
    'REVIEW',
    'RETROSPECTIVE',
    'SCRUM_POKER',
    'REGULAR'
    );

CREATE TYPE user_role AS ENUM (
    'ADMIN',
    'MEMBER'
    );

CREATE TYPE project_member_role AS ENUM (
    'OWNER',
    'STAKEHOLDER',
    'MANAGER',
    'DEVELOPER'
    );
-- ==================== ticket ==================
CREATE TABLE IF NOT EXISTS ticket
(
    id          uuid PRIMARY KEY,
    title       text          NOT NULL,
    description text,
    priority    ticket_priority,
    status      ticket_status NOT NULL,
    estimation  int,
    created_at  timestamptz DEFAULT now(),
    updated_at  timestamptz,
    deleted_at  timestamptz,
    sprint_id   uuid,
    project_id  uuid          NOT NULL,

    CONSTRAINT sprint_id
        FOREIGN KEY (sprint_id)
            REFERENCES sprint (id),
    CONSTRAINT project_id
        FOREIGN KEY (project_id)
            REFERENCES project (id)
);

-- ==================== sprint ===================
CREATE TABLE IF NOT EXISTS sprint
(
    id              uuid PRIMARY KEY,
    name            text                   NOT NULL,
    business_goal   text,
    dev_plan        text,
    start_date      DATE                   NOT NULL,
    end_date        DATE                   NOT NULL,
    status          sprint_status          NOT NULL,
    estimation_type sprint_estimation_type NOT NULL,
    created_at      timestamptz default now(),
    updated_at      timestamptz,
    deleted_at      timestamptz,
    project_id      uuid                   NOT NULL,

    CONSTRAINT project_id
        FOREIGN KEY (project_id)
            REFERENCES project (id)
);

-- ==================== meeting ==================
CREATE TABLE IF NOT EXISTS meeting
(
    id          uuid PRIMARY KEY,
    title       text         NOT NULL,
    description text,
    type        meeting_type NOT NULL,
    starts_at   timestamptz  NOT NULL,
    ends_at     timestamptz  NOT NULL,
    created_at  timestamptz DEFAULT now(),
    updated_at  timestamptz,
    deleted_at  timestamptz,
    sprint_id   uuid,
    project_id  uuid         NOT NULL,

    CONSTRAINT sprint_id
        FOREIGN KEY (sprint_id)
            REFERENCES sprint (id),

    CONSTRAINT project_id
        FOREIGN KEY (project_id)
            REFERENCES project (id)
);

-- ==================== project ==================
CREATE TABLE IF NOT EXISTS project
(
    id          uuid PRIMARY KEY,
    name        text NOT NULL,
    description text,
    created_at  timestamptz DEFAULT now(),
    updated_at  timestamptz,
    deleted_at  timestamptz,
    owner_id    uuid NOT NULL,

    CONSTRAINT owner_id
        FOREIGN KEY (owner_id)
            REFERENCES "user" (id)
);


-- ==================== user ==================
CREATE TABLE IF NOT EXISTS "user"
(
    id            uuid PRIMARY KEY,
    name          text        NOT NULL,
    email         text UNIQUE NOT NULL,
    password_hash text        NOT NULL,
    role          user_role   NOT NULL,
    created_at    timestamptz DEFAULT now(),
    updated_at    timestamptz,
    deleted_at    timestamptz
);

-- ==================== project_member ==================
CREATE TABLE IF NOT EXISTS project_member
(
    id         uuid PRIMARY KEY,
    role       project_member_role NOT NULL,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz,
    user_id    uuid                NOT NULL,
    project_id uuid                NOT NULL,

    CONSTRAINT user_id
        FOREIGN KEY (user_id)
            REFERENCES "user" (id),

    CONSTRAINT project_id
        FOREIGN KEY (project_id)
            REFERENCES project (id)
);

-- ==================== member_ticket ==================
CREATE TABLE IF NOT EXISTS member_ticket
(
    id         uuid PRIMARY KEY,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz,
    ticket_id  uuid NOT NULL,
    member_id  uuid NOT NULL,

    CONSTRAINT ticket_id
        FOREIGN KEY (ticket_id)
            REFERENCES ticket (id),

    CONSTRAINT member_id
        FOREIGN KEY (member_id)
            REFERENCES project_member (id)
);

-- ==================== meeting_member ==================
CREATE TABLE IF NOT EXISTS meeting_member
(
    id         uuid PRIMARY KEY,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz,
    meeting_id uuid NOT NULL,
    member_id  uuid NOT NULL,

    CONSTRAINT meeting_id
        FOREIGN KEY (meeting_id)
            REFERENCES meeting (id),

    CONSTRAINT member_id
        FOREIGN KEY (member_id)
            REFERENCES project_member (id)
);