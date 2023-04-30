DROP VIEW IF EXISTS activities_duration_by_tag;

CREATE OR REPLACE FUNCTION activities_duration_by_tag (
        user_id VARCHAR(36)
    ) RETURNS TABLE (
        tag_id VARCHAR(36),
        tag_name TEXT,
        tag_duration NUMERIC,
        measured_duration NUMERIC,
        measured_percentage NUMERIC
    ) AS $$

        SELECT
            duration_by_tag.tag_id,
            duration_by_tag.tag_name,
            duration_by_tag.tag_duration,
            total_measured.duration AS measured_duration,
            duration_by_tag.tag_duration / total_measured.duration AS measured_percentage
        FROM (
            SELECT
                t.id AS tag_id,
                t.name AS tag_name,
                COALESCE( EXTRACT(EPOCH FROM SUM(a.end_time - a.start_time)), 0) AS tag_duration
            FROM
                (SELECT * FROM activity WHERE creator_id = $1) a
                LEFT JOIN activity_tag at
                    ON a.id = at.activity_id
                LEFT JOIN tag t
                    ON at.tag_id = t.id
                GROUP BY t.id
        ) duration_by_tag
            CROSS JOIN (
                SELECT
                    EXTRACT(EPOCH FROM SUM(activity.end_time - activity.start_time)) AS duration
                FROM activity
            ) total_measured
        WHERE
                duration_by_tag.tag_id IS NOT NULL
            AND
                total_measured.duration > 0

$$ LANGUAGE SQL;