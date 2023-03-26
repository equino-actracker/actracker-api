ALTER TABLE activity DROP COLUMN deleted;
DELETE FROM flyway_schema_history WHERE version='4';