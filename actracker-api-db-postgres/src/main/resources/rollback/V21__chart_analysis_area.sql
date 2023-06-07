ALTER TABLE IF EXISTS chart
    DROP COLUMN metric;

UPDATE chart SET group_by='TAG' WHERE group_by='SELF';

DELETE FROM flyway_schema_history WHERE version='21';
