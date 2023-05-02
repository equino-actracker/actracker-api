ALTER TABLE IF EXISTS chart
    DROP COLUMN group_by;

DELETE FROM flyway_schema_history WHERE version='15';
