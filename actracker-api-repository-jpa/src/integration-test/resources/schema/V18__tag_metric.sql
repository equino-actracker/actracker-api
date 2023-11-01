CREATE TABLE IF NOT EXISTS metric (
    id      VARCHAR(36)     NOT NULL,
    tag_id  VARCHAR(36)     NOT NULL,
    name    TEXT            NOT NULL,
    type    VARCHAR(50)     NOT NULL,
    PRIMARY KEY (id)
);
