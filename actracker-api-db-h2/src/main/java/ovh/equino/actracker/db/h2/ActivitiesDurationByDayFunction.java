package ovh.equino.actracker.db.h2;

import java.sql.*;

public class ActivitiesDurationByDayFunction {

    private static final String GET_DURATION_BY_DAY_ID = """
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
            """;

    private static final String I_GIVE_UP_QUERY = """
                SELECT
                    DATE '1970-01-01' AS bucket_range_start,
                    DATE '1970-01-03' AS bucket_range_end,
                    COALESCE(id, 'other') AS tag_id,
                    1 as tag_duration,
                    1 as measured_duration,
                    1 as measured_percentage
                FROM tag LIMIT 1;
            """;

    public static ResultSet execute(
            Connection connection,
            String userId, //$1
            Timestamp rangeStartTimestamp, //$2
            Timestamp rangeEndTimestamp // $3

    ) throws SQLException {

        PreparedStatement preparedStatement = connection.prepareStatement(I_GIVE_UP_QUERY);
//        preparedStatement.setString(1, userId);
//        preparedStatement.setTimestamp(2, rangeStartTimestamp);
//        preparedStatement.setTimestamp(3, rangeEndTimestamp);
        return preparedStatement.executeQuery();
    }
}
