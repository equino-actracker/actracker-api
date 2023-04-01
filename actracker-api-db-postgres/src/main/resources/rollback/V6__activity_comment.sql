ALTER TABLE activity DROP COLUMN comment;
DELETE FROM flyway_schema_history WHERE version='6';
