ALTER TABLE activity DROP COLUMN creator_id;
DELETE FROM flyway_schema_history WHERE version='2';
