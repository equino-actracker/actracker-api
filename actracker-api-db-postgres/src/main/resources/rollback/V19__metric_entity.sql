ALTER TABLE metric DROP COLUMN IF EXISTS deleted;
ALTER TABLE metric DROP COLUMN IF EXISTS creator_id;

DELETE FROM flyway_schema_history WHERE version='19';
