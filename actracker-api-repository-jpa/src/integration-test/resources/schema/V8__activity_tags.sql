CREATE TABLE IF NOT EXISTS activity_tag (
    activity_id     VARCHAR(36)     NOT NULL,
    tag_id          VARCHAR(36)     NOT NULL,
    PRIMARY KEY (activity_id, tag_id)
);