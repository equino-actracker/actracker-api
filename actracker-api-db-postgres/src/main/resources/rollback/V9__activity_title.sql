ALTER TABLE activity DROP COLUMN title;
DELETE FROM flyway_schema_history WHERE version='9';
