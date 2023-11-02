CREATE TABLE IF NOT EXISTS tag (
    id                      VARCHAR(36)     UNIQUE NOT NULL ,
    creator_id              VARCHAR(36)     NOT NULL        ,
    name                    TEXT                            ,
    deleted                 BOOLEAN         DEFAULT FALSE   ,
    PRIMARY KEY (id)
);