DROP TRIGGER IF EXISTS outbox_notification_created_trg ON outbox_notification;
DROP FUNCTION IF EXISTS outbox_notification_created_handler;
DROP TABLE IF EXISTS outbox_notification;
DROP SEQUENCE IF EXISTS outbox_notification_version_seq;
DELETE FROM flyway_schema_history WHERE version='3';
