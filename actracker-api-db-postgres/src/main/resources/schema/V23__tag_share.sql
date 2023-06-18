CREATE TABLE IF NOT EXISTS tag_share (
    id              VARCHAR(36)     UNIQUE NOT NULL ,
    tag_id          VARCHAR(36)     NOT NULL        ,
    grantee_id      VARCHAR(36)                     ,
    grantee_name    VARCHAR(100)    NOT NULL        ,
    PRIMARY KEY(id)
);
