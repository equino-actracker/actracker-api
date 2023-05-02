DROP VIEW IF EXISTS activities_duration_by_tag;

DROP FUNCTION IF EXISTS activities_duration_by_tag;

CREATE OR REPLACE FUNCTION activities_duration_by_tag (
        user_id VARCHAR(36),
        range_start_timestamp TIMESTAMP WITH TIME ZONE,
        range_end_timestamp TIMESTAMP WITH TIME ZONE
    ) RETURNS TABLE (
        tag_id VARCHAR(36),
        tag_duration NUMERIC,
        measured_duration NUMERIC,
        measured_percentage NUMERIC
    ) AS $$

        SELECT
            duration_by_tag.tag_id,
            duration_by_tag.tag_duration,
            total_measured.duration AS measured_duration,
            duration_by_tag.tag_duration / total_measured.duration AS measured_percentage
        FROM (
            SELECT
                t.id AS tag_id,
                COALESCE( EXTRACT(EPOCH FROM SUM(a.end_time - a.start_time)), 0) AS tag_duration
            FROM (
                SELECT
                    id,
                    GREATEST(start_time, $2) AS start_time,
                    LEAST(end_time, $3) AS end_time
                FROM activity
                    WHERE
                        creator_id = $1
                    AND
                        deleted IS FALSE
                    AND
                        start_time <= $3
                    AND
                        end_time >= $2
            ) a
            LEFT JOIN activity_tag at
                ON a.id = at.activity_id
            LEFT JOIN (SELECT id FROM tag WHERE creator_id = $1 AND deleted IS FALSE) t
                ON at.tag_id = t.id
            WHERE t.id IS NOT NULL
            GROUP BY t.id
        ) duration_by_tag
            CROSS JOIN (
                SELECT
                    EXTRACT(EPOCH FROM SUM(a.end_time - a.start_time)) AS duration
                FROM (
                     SELECT
                         id,
                         GREATEST(start_time, $2) AS start_time,
                         LEAST(end_time, $3) AS end_time
                     FROM activity
                         WHERE
                             creator_id = $1
                         AND
                             deleted IS FALSE
                         AND
                             start_time <= $3
                         AND
                             end_time >= $2
                ) a
                LEFT JOIN activity_tag at
                    ON a.id = at.activity_id
                LEFT JOIN (SELECT id FROM tag WHERE creator_id = $1 AND deleted IS FALSE) t
                    ON at.tag_id = t.id
                WHERE t.id IS NOT NULL
            ) total_measured
        WHERE total_measured.duration > 0

$$ LANGUAGE SQL;