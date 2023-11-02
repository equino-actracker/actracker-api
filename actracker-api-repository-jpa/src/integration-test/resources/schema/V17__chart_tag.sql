CREATE TABLE IF NOT EXISTS chart_tag (
    chart_id        VARCHAR(36)     NOT NULL,
    tag_id          VARCHAR(36)     NOT NULL,
    PRIMARY KEY (chart_id, tag_id)
);
