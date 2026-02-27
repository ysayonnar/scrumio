-- ==================== ticket ==================
CREATE TABLE IF NOT EXISTS ticket
(
    id          uuid PRIMARY KEY,
    title       text NOT NULL,
    description text,
    priority    text,
    status      text NOT NULL,
    estimation  int,
    created_at  timestamptz DEFAULT now(),
    updated_at  timestamptz,
    deleted_at  timestamptz,
    sprint_id   uuid,
    project_id  uuid NOT NULL,

    CONSTRAINT sprint_id
        FOREIGN KEY (id)
            REFERENCES sprint (id),
    CONSTRAINT project_id
        FOREIGN KEY (id)
            REFERENCES project (id)
);

-- ==================== sprint ===================
CREATE TABLE IF NOT EXISTS sprint
(
    id              uuid PRIMARY KEY,
    name            text NOT NULL,
    business_goal   text,
    dev_plan        text,
    start_date      DATE NOT NULL,
    end_dat         DATE NOT NULL,
    status          text NOT NULL,
    estimation_type text NOT NULL,
    created_at      timestamptz default now(),
    updated_at      timestamptz,
    deleted_at      timestamptz,
    project_id      uuid NOT NULL,

    CONSTRAINT project_id
        FOREIGN KEY (id)
            REFERENCES project (id)
);

-- ==================== meeting ==================
CREATE TABLE IF NOT EXISTS meeting
(
    id          uuid PRIMARY KEY,
    title       text        NOT NULL,
    description text,
    type        text        NOT NULL,
    starts_at   timestamptz NOT NULL,
    ends_at     timestamptz NOT NULL,
    created_at  timestamptz DEFAULT now(),
    updated_at  timestamptz,
    deleted_at  timestamptz,
    sprint_id   uuid,
    project_id  uuid        NOT NULL,

    CONSTRAINT sprint_id
        FOREIGN KEY (id)
            REFERENCES sprint (id),

    CONSTRAINT project_id
        FOREIGN KEY (id)
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
        FOREIGN KEY (id)
            REFERENCES "user" (id)
);


-- ==================== user ==================
CREATE TABLE IF NOT EXISTS "user"
(
    id            uuid PRIMARY KEY,
    name          text        NOT NULL,
    email         text UNIQUE NOT NULL,
    password_hash text        NOT NULL,
    role          text        NOT NULL,
    created_at    timestamptz DEFAULT now(),
    updated_at    timestamptz,
    deleted_at    timestamptz
);

-- ==================== project_member ==================
CREATE TABLE IF NOT EXISTS project_member
(
    id         uuid PRIMARY KEY,
    role       text NOT NULL,
    created_at timestamptz DEFAULT now(),
    updated_at timestamptz,
    deleted_at timestamptz,
    user_id    uuid NOT NULL,
    project_id uuid NOT NULL,

    CONSTRAINT user_id
        FOREIGN KEY (id)
            REFERENCES "user" (id),

    CONSTRAINT project_id
        FOREIGN KEY (id)
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
        FOREIGN KEY (id)
            REFERENCES ticket (id),

    CONSTRAINT member_id
        FOREIGN KEY (id)
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
        FOREIGN KEY (id)
            REFERENCES meeting (id),

    CONSTRAINT member_id
        FOREIGN KEY (id)
            REFERENCES project_member (id)
);