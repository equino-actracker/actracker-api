package ovh.equino.actracker.db.h2;

import java.sql.*;

public class ActivitiesDurationByTagFunction {

    private static final String GET_DURATION_BY_TAG_ID = """
        SELECT
            duration_by_tag.tag_id,
            duration_by_tag.tag_duration,
            total_measured.duration AS measured_duration,
            duration_by_tag.tag_duration / total_measured.duration AS measured_percentage
        FROM (
            SELECT
                t.id AS tag_id,
                COALESCE( EXTRACT(EPOCH FROM SUM(a.end_time - a.start_time)), 0) AS tag_duration
            FROM
                (
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
                LEFT JOIN
                    (
                        SELECT id FROM tag WHERE creator_id = $1 AND deleted IS FALSE
                    ) t
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
            """;

    public static ResultSet execute(
            Connection connection,
            String userId, //$1
            Timestamp rangeStartTimestamp, //$2
            Timestamp rangeEndTimestamp // $3

    ) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(GET_DURATION_BY_TAG_ID);
        preparedStatement.setString(1, userId);
        preparedStatement.setTimestamp(2, rangeStartTimestamp);
        preparedStatement.setTimestamp(3, rangeEndTimestamp);
        return preparedStatement.executeQuery();
    }
}
