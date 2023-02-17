CREATE SEQUENCE IF NOT EXISTS outbox_notification_version_seq INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS outbox_notification (
    id          VARCHAR(36)     UNIQUE NOT NULL ,
    version     INTEGER         NOT NULL        ,
    entity      TEXT
);