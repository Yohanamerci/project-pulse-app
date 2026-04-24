-- V6: Instructor self-registration via invitation email (UC-30)
-- Adds invitation_tokens table and optional middle_initial column to users

ALTER TABLE users ADD COLUMN middle_initial CHAR(1) NULL AFTER first_name;

CREATE TABLE invitation_tokens (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    token       VARCHAR(255) NOT NULL UNIQUE,
    email       VARCHAR(150) NOT NULL,
    used        BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at  TIMESTAMP NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
