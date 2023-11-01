ALTER TABLE IF EXISTS chart
    ADD COLUMN group_by VARCHAR(20);

UPDATE chart SET group_by='TAG' WHERE group_by IS NULL;

ALTER TABLE IF EXISTS chart
    ALTER COLUMN group_by SET NOT NULL;


DROP FUNCTION IF EXISTS activities_duration_by_day;

CREATE OR REPLACE FUNCTION activities_duration_by_day (
        user_id VARCHAR(36),
        range_start_timestamp TIMESTAMP WITH TIME ZONE,
        range_end_timestamp TIMESTAMP WITH TIME ZONE
    ) RETURNS TABLE (
        bucket_range_start TIMESTAMP WITH TIME ZONE,
        bucket_range_end TIMESTAMP WITH TIME ZONE,
        tag_id VARCHAR(36),
        tag_duration NUMERIC,
        measured_duration NUMERIC,
        measured_percentage NUMERIC
    ) AS $$

    SELECT
        bucket_range.start_time,
        bucket_range.end_time,
        by_tag.tag_id,
        by_tag.tag_duration,
        by_tag.measured_duration,
        by_tag.measured_percentage
    FROM (
        SELECT
            date_in_range AS start_time,
            date_in_range + INTERVAL '1' DAY AS end_time
        FROM
            generate_series($2, $3, INTERVAL '1' DAY) AS date_in_range
    )
    AS bucket_range
    CROSS JOIN activities_duration_by_tag($1, bucket_range.start_time, bucket_range.end_time) by_tag


$$ LANGUAGE SQL;