CREATE TABLE tenant (
    id VARCHAR(36) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE activity (
    id VARCHAR(36) UNIQUE NOT NULL,
    title TEXT,
    creator_id VARCHAR(36) NOT NULL,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    comment TEXT,
    deleted BOOLEAN,
    PRIMARY KEY (id)
);

CREATE TABLE tag (
    id VARCHAR(36) UNIQUE NOT NULL,
    creator_id VARCHAR(36) NOT NULL,
    name TEXT,
    deleted BOOLEAN,
    PRIMARY KEY (id)
);

CREATE TABLE activity_tag (
    activity_id VARCHAR(36) NOT NULL,
    tag_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (activity_id, tag_id)
);

CREATE SEQUENCE outbox_notification_version_seq INCREMENT BY 1;

CREATE TABLE outbox_notification (
    id VARCHAR(36) UNIQUE NOT NULL,
    version INTEGER NOT NULL,   -- Must be declared as 2nd column, outbox_notification_created_trg relies on it
    entity TEXT,
    entity_type TEXT NOT NULL
);

CREATE TRIGGER outbox_notification_created_trg BEFORE INSERT, UPDATE ON outbox_notification FOR EACH ROW CALL "ovh.equino.actracker.db.h2.OutboxNotificationTrigger";