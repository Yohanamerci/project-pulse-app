-- V1: Baseline schema for Project Pulse
-- Tables: users, sections, rubrics, criteria, teams, team members,
--         active_weeks, activities (WAR), peer_evaluations, evaluation_scores

-- Users (Admin, Instructor, Student share one table with a role discriminator)
CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    first_name  VARCHAR(80)  NOT NULL,
    last_name   VARCHAR(80)  NOT NULL,
    role        ENUM('ADMIN','INSTRUCTOR','STUDENT') NOT NULL,
    enabled     BOOLEAN NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Course Sections
CREATE TABLE sections (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    semester    ENUM('FALL','SPRING') NOT NULL,
    year        INT NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Rubrics (one per section typically, reusable)
CREATE TABLE rubrics (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    section_id  BIGINT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_rubric_section FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE SET NULL
);

-- Criteria (each rubric has multiple criteria)
CREATE TABLE criteria (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    rubric_id   BIGINT NOT NULL,
    name        VARCHAR(150) NOT NULL,
    description TEXT,
    max_score   INT NOT NULL DEFAULT 10,
    display_order INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_criteria_rubric FOREIGN KEY (rubric_id) REFERENCES rubrics(id) ON DELETE CASCADE
);

-- Teams
CREATE TABLE teams (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    section_id  BIGINT NOT NULL,
    rubric_id   BIGINT,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_team_section FOREIGN KEY (section_id) REFERENCES sections(id),
    CONSTRAINT fk_team_rubric  FOREIGN KEY (rubric_id)  REFERENCES rubrics(id) ON DELETE SET NULL
);

-- Team ↔ Student membership
CREATE TABLE team_students (
    team_id     BIGINT NOT NULL,
    student_id  BIGINT NOT NULL,
    PRIMARY KEY (team_id, student_id),
    CONSTRAINT fk_ts_team    FOREIGN KEY (team_id)    REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_ts_student FOREIGN KEY (student_id) REFERENCES users(id)  ON DELETE CASCADE
);

-- Team ↔ Instructor assignment (BR-1: team must have an instructor)
CREATE TABLE team_instructors (
    team_id       BIGINT NOT NULL,
    instructor_id BIGINT NOT NULL,
    PRIMARY KEY (team_id, instructor_id),
    CONSTRAINT fk_ti_team       FOREIGN KEY (team_id)       REFERENCES teams(id) ON DELETE CASCADE,
    CONSTRAINT fk_ti_instructor FOREIGN KEY (instructor_id) REFERENCES users(id)  ON DELETE CASCADE
);

-- Active Weeks (BR-2: Fall 5-15, Spring 1-15)
CREATE TABLE active_weeks (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    section_id  BIGINT NOT NULL,
    week_number INT NOT NULL,
    start_date  DATE NOT NULL,
    end_date    DATE NOT NULL,
    is_active   BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_aw_section FOREIGN KEY (section_id) REFERENCES sections(id) ON DELETE CASCADE,
    CONSTRAINT uq_aw_section_week UNIQUE (section_id, week_number)
);

-- Weekly Activity Reports (WAR) — BR-4: submit only within active week window
CREATE TABLE activities (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id  BIGINT NOT NULL,
    team_id     BIGINT NOT NULL,
    week_id     BIGINT NOT NULL,
    category    ENUM('DEVELOPMENT','TESTING','BUG_FIX','DOCUMENTATION','DESIGN','MEETING','OTHER') NOT NULL,
    description TEXT NOT NULL,
    hours       DECIMAL(4,1) NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_act_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_act_team    FOREIGN KEY (team_id)    REFERENCES teams(id),
    CONSTRAINT fk_act_week    FOREIGN KEY (week_id)    REFERENCES active_weeks(id)
);

-- Peer Evaluations (BR-3: no edit after submit; BR-4: only during active week)
CREATE TABLE peer_evaluations (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluator_id BIGINT NOT NULL,
    evaluatee_id BIGINT NOT NULL,
    team_id      BIGINT NOT NULL,
    week_id      BIGINT NOT NULL,
    submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_pe_evaluator FOREIGN KEY (evaluator_id) REFERENCES users(id),
    CONSTRAINT fk_pe_evaluatee FOREIGN KEY (evaluatee_id) REFERENCES users(id),
    CONSTRAINT fk_pe_team      FOREIGN KEY (team_id)      REFERENCES teams(id),
    CONSTRAINT fk_pe_week      FOREIGN KEY (week_id)      REFERENCES active_weeks(id),
    CONSTRAINT uq_pe_unique UNIQUE (evaluator_id, evaluatee_id, week_id)
);

-- Evaluation Scores (one row per criterion per evaluation)
CREATE TABLE evaluation_scores (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id  BIGINT NOT NULL,
    criterion_id   BIGINT NOT NULL,
    score          INT NOT NULL,
    CONSTRAINT fk_es_evaluation FOREIGN KEY (evaluation_id) REFERENCES peer_evaluations(id) ON DELETE CASCADE,
    CONSTRAINT fk_es_criterion  FOREIGN KEY (criterion_id)  REFERENCES criteria(id),
    CONSTRAINT uq_es_unique UNIQUE (evaluation_id, criterion_id)
);

-- Seed: default admin user (password: Admin1234! — bcrypt hash)
INSERT INTO users (username, email, password, first_name, last_name, role)
VALUES (
    'admin',
    'admin@projectpulse.local',
    '$2a$12$pWkBbNmMcbJCgkQFgTW0cO9QHe9Zv6u3zTgYpSk7zBz3bRLAEiPoe',
    'System',
    'Admin',
    'ADMIN'
);
