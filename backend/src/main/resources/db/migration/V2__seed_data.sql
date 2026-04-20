-- V2: Sample data for development & testing
-- All sample users share password: Password1!
-- BCrypt hash: $2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC

-- ─── USERS ────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO users (username, email, password, first_name, last_name, role) VALUES
-- Instructors
('dr.wei',     'wei@projectpulse.local',     '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Bingyang',  'Wei',     'INSTRUCTOR'),
('prof.smith', 'smith@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'John',      'Smith',   'INSTRUCTOR'),

-- Students – Team Alpha (ids 3-6)
('alice.j',    'alice@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Alice',     'Johnson', 'STUDENT'),
('bob.m',      'bob@projectpulse.local',     '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Bob',       'Martinez','STUDENT'),
('carol.k',    'carol@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Carol',     'Kim',     'STUDENT'),
('david.p',    'david@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'David',     'Park',    'STUDENT'),

-- Students – Team Beta (ids 7-10)
('eva.r',      'eva@projectpulse.local',     '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Eva',       'Rivera',  'STUDENT'),
('frank.n',    'frank@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Frank',     'Nguyen',  'STUDENT'),
('grace.l',    'grace@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Grace',     'Lee',     'STUDENT'),
('henry.c',    'henry@projectpulse.local',   '$2b$12$7EyzFeRIxfMM4.9oKfzEtOXLmYGrugw9fiD7LRw1A0yBlmAY81wVC', 'Henry',     'Chen',    'STUDENT');

-- ─── SECTIONS ─────────────────────────────────────────────────────────────────

INSERT IGNORE INTO sections (name, semester, year) VALUES
('CS 4391 - 001', 'SPRING', 2026),   -- id 1  (Dr. Wei)
('CS 4391 - 002', 'SPRING', 2026);   -- id 2  (Prof. Smith)

-- ─── RUBRICS & CRITERIA ───────────────────────────────────────────────────────

-- Shared rubric for CS 4391
INSERT IGNORE INTO rubrics (name, description, section_id) VALUES
('CS 4391 Peer Evaluation Rubric', 'Standard weekly peer evaluation rubric for senior design teams', 1);  -- id 1

INSERT IGNORE INTO criteria (rubric_id, name, description, max_score, display_order) VALUES
(1, 'Technical Contribution',   'Quality and quantity of technical work completed this week',     10, 1),
(1, 'Collaboration',            'Willingness to help teammates and communicate effectively',       10, 2),
(1, 'Meeting Attendance',       'Attendance and participation in scheduled team meetings',          5, 3),
(1, 'Task Completion',          'Delivered on assigned tasks and met agreed deadlines',            10, 4),
(1, 'Code Quality / Artifacts', 'Code reviews, documentation, and deliverable quality',           10, 5);

-- Second rubric for section 2
INSERT IGNORE INTO rubrics (name, description, section_id) VALUES
('CS 4391-002 Peer Evaluation Rubric', 'Peer evaluation rubric for section 002', 2);  -- id 2

INSERT IGNORE INTO criteria (rubric_id, name, description, max_score, display_order) VALUES
(2, 'Technical Contribution', 'Technical work delivered this week',         10, 1),
(2, 'Teamwork',               'Collaboration and communication with team',   10, 2),
(2, 'Task Completion',        'Completion of assigned tasks on time',        10, 3);

-- ─── TEAMS ────────────────────────────────────────────────────────────────────

INSERT IGNORE INTO teams (name, section_id, rubric_id) VALUES
('Team Alpha', 1, 1),   -- id 1
('Team Beta',  1, 1),   -- id 2
('Team Gamma', 2, 2);   -- id 3

-- Team Alpha: instructor dr.wei (id=2), students alice(3), bob(4), carol(5), david(6)
INSERT IGNORE INTO team_instructors (team_id, instructor_id) VALUES (1, 2);
INSERT IGNORE INTO team_students (team_id, student_id) VALUES
(1, 3), (1, 4), (1, 5), (1, 6);

-- Team Beta: instructor dr.wei (id=2), students eva(7), frank(8), grace(9), henry(10)
INSERT IGNORE INTO team_instructors (team_id, instructor_id) VALUES (2, 2);
INSERT IGNORE INTO team_students (team_id, student_id) VALUES
(2, 7), (2, 8), (2, 9), (2, 10);

-- Team Gamma: instructor prof.smith (id=3), students alice(3) and eva(7) are NOT in gamma
-- (fresh students for this section — using carol(5) and frank(8) as dual-section demo)
-- Actually Gamma gets no students by default — admin can assign them

-- ─── ACTIVE WEEKS ─────────────────────────────────────────────────────────────

-- Section 1 active weeks (Spring: 1-15 allowed)
INSERT IGNORE INTO active_weeks (section_id, week_number, start_date, end_date, is_active) VALUES
(1, 8,  '2026-03-09', '2026-03-15', FALSE),   -- id 1  past week
(1, 9,  '2026-03-16', '2026-03-22', FALSE),   -- id 2  past week
(1, 10, '2026-03-23', '2026-03-29', TRUE);    -- id 3  ← CURRENT ACTIVE WEEK

-- Section 2 active weeks
INSERT IGNORE INTO active_weeks (section_id, week_number, start_date, end_date, is_active) VALUES
(2, 10, '2026-03-23', '2026-03-29', TRUE);    -- id 4  ← CURRENT ACTIVE WEEK

-- ─── SAMPLE ACTIVITIES (WAR) ──────────────────────────────────────────────────

-- Week 8 activities (past) for Team Alpha
INSERT IGNORE INTO activities (student_id, team_id, week_id, category, description, hours) VALUES
(3, 1, 1, 'DEVELOPMENT',   'Implemented user authentication module and JWT integration',         6.0),
(3, 1, 1, 'MEETING',       'Team standup and sprint planning session',                           1.5),
(4, 1, 1, 'TESTING',       'Wrote unit tests for the auth service — 90% coverage achieved',     4.0),
(4, 1, 1, 'DOCUMENTATION', 'Updated API docs with new auth endpoints and sample requests',       2.0),
(5, 1, 1, 'DESIGN',        'Created wireframes for the dashboard and activity submission pages', 3.5),
(5, 1, 1, 'DEVELOPMENT',   'Built reusable Vue components for navigation and layout',            5.0),
(6, 1, 1, 'DEVELOPMENT',   'Set up MySQL schema and Flyway migration scripts',                   4.5),
(6, 1, 1, 'BUG_FIX',       'Fixed CORS configuration blocking frontend-backend communication',   1.5);

-- Week 9 activities for Team Alpha
INSERT IGNORE INTO activities (student_id, team_id, week_id, category, description, hours) VALUES
(3, 1, 2, 'DEVELOPMENT',   'Connected frontend login page to Spring Boot auth API',              5.0),
(4, 1, 2, 'TESTING',       'Integration tests for sections and teams CRUD endpoints',            4.5),
(5, 1, 2, 'DESIGN',        'Finalized color palette and component library standards',            2.0),
(6, 1, 2, 'DEVELOPMENT',   'Implemented team management endpoints with BR-1 validation',        6.0);

-- Week 10 activities (current active week) for Team Alpha
INSERT IGNORE INTO activities (student_id, team_id, week_id, category, description, hours) VALUES
(3, 1, 3, 'DEVELOPMENT',   'Working on activity submission form and week-selector logic',        4.0),
(4, 1, 3, 'TESTING',       'Testing peer evaluation submission flow end-to-end',                 3.5),
(5, 1, 3, 'DOCUMENTATION', 'Writing project README and API usage guide for teammates',           2.5);

-- Team Beta week 10 activities
INSERT IGNORE INTO activities (student_id, team_id, week_id, category, description, hours) VALUES
(7, 2, 3, 'DEVELOPMENT',   'Built student dashboard and team overview components',               5.0),
(8, 2, 3, 'MEETING',       'Sprint retrospective and backlog grooming with team',                2.0),
(9, 2, 3, 'DEVELOPMENT',   'Integrated rubric criteria display into evaluation form',            4.5),
(10,2, 3, 'BUG_FIX',       'Fixed v-navigation-drawer layout not updating router-view',          3.0);

-- ─── SAMPLE PEER EVALUATIONS ──────────────────────────────────────────────────

-- Week 9 evaluations: Alice (3) evaluates Bob (4), Carol (5), David (6)
INSERT IGNORE INTO peer_evaluations (evaluator_id, evaluatee_id, team_id, week_id) VALUES
(3, 4, 1, 2),   -- id 1  Alice → Bob
(3, 5, 1, 2),   -- id 2  Alice → Carol
(3, 6, 1, 2);   -- id 3  Alice → David

-- Scores for Alice's evaluations (criteria ids 1-5: Tech, Collab, Meetings, Tasks, Code)
INSERT IGNORE INTO evaluation_scores (evaluation_id, criterion_id, score) VALUES
-- Alice → Bob (solid developer)
(1, 1, 9), (1, 2, 8), (1, 3, 5), (1, 4, 9), (1, 5, 8),
-- Alice → Carol (great designer)
(2, 1, 7), (2, 2, 9), (2, 3, 5), (2, 4, 8), (2, 5, 9),
-- Alice → David (strong backend)
(3, 1, 9), (3, 2, 7), (3, 3, 4), (3, 4, 8), (3, 5, 8);

-- Bob (4) evaluates Alice (3), Carol (5), David (6)
INSERT IGNORE INTO peer_evaluations (evaluator_id, evaluatee_id, team_id, week_id) VALUES
(4, 3, 1, 2),   -- id 4  Bob → Alice
(4, 5, 1, 2),   -- id 5  Bob → Carol
(4, 6, 1, 2);   -- id 6  Bob → David

INSERT IGNORE INTO evaluation_scores (evaluation_id, criterion_id, score) VALUES
-- Bob → Alice
(4, 1, 10), (4, 2, 9), (4, 3, 5), (4, 4, 10), (4, 5, 9),
-- Bob → Carol
(5, 1, 8),  (5, 2, 9), (5, 3, 5), (5, 4, 7),  (5, 5, 8),
-- Bob → David
(6, 1, 8),  (6, 2, 8), (6, 3, 4), (6, 4, 8),  (6, 5, 7);
