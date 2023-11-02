CREATE TABLE IF NOT EXISTS tag_set (
    id                      VARCHAR(36)     UNIQUE NOT NULL ,
    creator_id              VARCHAR(36)     NOT NULL        ,
    name                    TEXT                            ,
    deleted                 BOOLEAN         DEFAULT FALSE   ,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS tag_set_tag (
    tag_set_id      VARCHAR(36)     NOT NULL,
    tag_id          VARCHAR(36)     NOT NULL,
    PRIMARY KEY (tag_set_id, tag_id)
);
