-- Fix seed activities that V3 added with empty activity_name and zero planned_hours.
-- These were inserted before activity_name/planned_hours columns existed; V3 defaulted them
-- to '' and 0.0, which fails the @NotBlank / @DecimalMin(0.5) backend validations on update.

-- Back-fill activity_name from the first 100 chars of description
UPDATE activities
SET activity_name = LEFT(description, 100)
WHERE activity_name = ''
  AND description IS NOT NULL
  AND description != '';

-- Fix planned_hours: use actual_hours when available, otherwise default to 1.0
-- (0.0 fails the backend @DecimalMin("0.5") constraint)
UPDATE activities
SET planned_hours = CASE
    WHEN actual_hours IS NOT NULL AND actual_hours >= 0.5 THEN actual_hours
    ELSE 1.0
END
WHERE planned_hours = 0.0;
