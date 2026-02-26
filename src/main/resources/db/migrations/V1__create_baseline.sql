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
    deleted_at  timestamptz
-- TODO: FK sprint_id
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
    deleted_at      timestamptz
-- TODO: FK project_id
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
    deleted_at  timestamptz
-- TODO: FK sprint_id
);

-- ==================== project ==================
CREATE TABLE IF NOT EXISTS project
(
    id          uuid PRIMARY KEY,
    name        text NOT NULL,
    description text,
    created_at  timestamptz DEFAULT now(),
    updated_at  timestamptz,
    deleted_at  timestamptz
-- TODO: FK owner_id
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

-- TODO: 3 linking tables