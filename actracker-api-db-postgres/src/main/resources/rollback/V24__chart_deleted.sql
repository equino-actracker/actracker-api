ALTER TABLE IF EXISTS chart DROP COLUMN IF EXISTS deleted;

DELETE FROM flyway_schema_history WHERE version='24';
