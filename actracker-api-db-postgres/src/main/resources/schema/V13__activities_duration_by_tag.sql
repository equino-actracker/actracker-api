CREATE OR REPLACE VIEW activities_duration_by_tag AS

SELECT
    duration_by_tag.tag_id,
    duration_by_tag.tag_name,
    duration_by_tag.tag_duration,
    total_measured.duration AS measured_duration, -- remove
    EXTRACT(EPOCH FROM duration_by_tag.tag_duration)/EXTRACT(EPOCH FROM total_measured.duration) AS measured_percentage

FROM (
    SELECT
        t.id AS tag_id,
        t.name AS tag_name, -- remove
        SUM(a.end_time - a.start_time) AS tag_duration
    FROM
        activity a
        LEFT JOIN activity_tag at
            ON a.id = at.activity_id
        LEFT JOIN tag t
            ON at.tag_id = t.id

        GROUP BY t.id
) duration_by_tag
    CROSS JOIN (
        SELECT
            SUM(activity.end_time - activity.start_time) AS duration
        FROM activity
    ) total_measured
;