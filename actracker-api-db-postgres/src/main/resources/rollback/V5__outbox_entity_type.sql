ALTER TABLE outbox_notification DROP COLUMN entity_type;
DELETE FROM flyway_schema_history WHERE version='5';
