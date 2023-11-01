CREATE TABLE IF NOT EXISTS chart (
    id                      VARCHAR(36)     UNIQUE NOT NULL ,
    dashboard_id            VARCHAR(36)     NOT NULL        ,
    name                    TEXT                            ,
    PRIMARY KEY (id)
);