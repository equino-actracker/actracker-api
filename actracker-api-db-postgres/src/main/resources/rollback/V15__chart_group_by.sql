ALTER TABLE IF EXISTS chart
    DROP COLUMN group_by;

DROP FUNCTION IF EXISTS activities_duration_by_day;

DELETE FROM flyway_schema_history WHERE version='15';
