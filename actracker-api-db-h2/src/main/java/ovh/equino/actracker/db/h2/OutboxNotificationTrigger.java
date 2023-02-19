package ovh.equino.actracker.db.h2;

import org.h2.api.Trigger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class OutboxNotificationTrigger implements Trigger {

    public static final String SELECT_NEXT_VERSION = "SELECT NEXTVAL('outbox_notification_version_seq') AS num";

    @Override
    public void fire(Connection conn, Object[] oldRow, Object[] newRow) throws SQLException {
        ResultSet nextVersion = conn.prepareStatement(SELECT_NEXT_VERSION).executeQuery();
        if(nextVersion.next()) {
            newRow[1] = nextVersion.getInt(1);
        }
    }
}
