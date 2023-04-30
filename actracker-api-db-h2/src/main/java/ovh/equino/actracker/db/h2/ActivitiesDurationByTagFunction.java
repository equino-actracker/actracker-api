package ovh.equino.actracker.db.h2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ActivitiesDurationByTagFunction {

    private static final String GET_DURATION_BY_TAG_ID = """
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
                            (SELECT * FROM activity WHERE creator_id = ?) a   -- [PARAM 1: userId]
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
            """;

    public static ResultSet execute(Connection connection, String userId) throws SQLException {
        PreparedStatement preparedStatement = connection.prepareStatement(GET_DURATION_BY_TAG_ID);
        preparedStatement.setString(1, userId);
        return preparedStatement.executeQuery();
    }
}
