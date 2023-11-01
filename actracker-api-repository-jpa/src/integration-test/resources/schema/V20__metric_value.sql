CREATE TABLE IF NOT EXISTS metric_value (
    id              VARCHAR(36)     NOT NULL,
    activity_id     VARCHAR(36)     NOT NULL,
    metric_id       VARCHAR(36)     NOT NULL,
    metric_value    NUMERIC(12, 3),         -- Max 999_999_999.999
    PRIMARY KEY (id)
);
