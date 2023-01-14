CREATE TABLE IF NOT EXISTS tenant (
    id                      VARCHAR(36)     UNIQUE NOT NULL ,
    username                VARCHAR(100)    UNIQUE NOT NULL ,
    password                VARCHAR(100)    NOT NULL        ,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS activity (
    id                      VARCHAR(36)     UNIQUE NOT NULL ,
    start_time              TIMESTAMP                       ,
    end_time                TIMESTAMP                       ,
    PRIMARY KEY (id)
);
