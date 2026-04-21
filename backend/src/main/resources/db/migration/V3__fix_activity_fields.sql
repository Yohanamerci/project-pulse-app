-- Convert category from MySQL ENUM to VARCHAR so we can change the allowed values
ALTER TABLE activities MODIFY COLUMN category VARCHAR(50) NOT NULL;

-- Remap old category values to new enum names
UPDATE activities SET category = 'BUGFIX' WHERE category = 'BUG_FIX';
UPDATE activities SET category = 'MISCELLANEOUS' WHERE category = 'MEETING';
UPDATE activities SET category = 'MISCELLANEOUS' WHERE category = 'OTHER';

-- Rename hours → actual_hours and widen precision
ALTER TABLE activities CHANGE COLUMN hours actual_hours DECIMAL(5,2) NULL DEFAULT NULL;

-- Add new columns
ALTER TABLE activities ADD COLUMN activity_name VARCHAR(255) NOT NULL DEFAULT '' AFTER id;
ALTER TABLE activities ADD COLUMN planned_hours DECIMAL(5,2) NOT NULL DEFAULT 0.0 AFTER description;
ALTER TABLE activities ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS' AFTER actual_hours;
