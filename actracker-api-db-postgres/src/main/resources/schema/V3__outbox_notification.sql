CREATE SEQUENCE IF NOT EXISTS outbox_notification_version_seq INCREMENT BY 1;

CREATE TABLE IF NOT EXISTS outbox_notification (
    id          VARCHAR(36)     UNIQUE NOT NULL ,
    version     INTEGER         NOT NULL        ,
    entity      TEXT
);

CREATE OR REPLACE FUNCTION outbox_notification_created_handler()
    RETURNS TRIGGER
    AS
$$
BEGIN
    NEW.version := nextval('outbox_notification_version_seq');
    RETURN NEW;
END;
$$ LANGUAGE PLPGSQL;

CREATE OR REPLACE TRIGGER outbox_notification_created_trg
    BEFORE INSERT OR UPDATE
    ON outbox_notification
    FOR EACH ROW EXECUTE FUNCTION outbox_notification_created_handler();
