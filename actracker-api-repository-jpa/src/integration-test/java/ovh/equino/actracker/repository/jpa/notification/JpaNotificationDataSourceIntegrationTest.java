package ovh.equino.actracker.repository.jpa.notification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.exception.ParseException;
import ovh.equino.actracker.jpa.JpaIntegrationTest;

import java.sql.SQLException;
import java.util.List;

import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static ovh.equino.actracker.jpa.TestUtil.nextUUID;

abstract class JpaNotificationDataSourceIntegrationTest extends JpaIntegrationTest {

    private static final Notification<?> NOTIFICATION_1 = new Notification<>(nextUUID(), new NotificationData(1));
    private static final Notification<?> NOTIFICATION_2 = new Notification<>(nextUUID(), new NotificationData(2));
    private static final Notification<?> NOTIFICATION_3 = new Notification<>(nextUUID(), new NotificationData(3));

    private JpaNotificationDataSource dataSource;

    @BeforeEach
    void init() throws SQLException, ParseException {
        this.dataSource = new JpaNotificationDataSource(entityManager);
        database().addNotifications(NOTIFICATION_1, NOTIFICATION_2, NOTIFICATION_3);
    }

    @Test
    void shouldFindAllNotifications() {
        inTransaction(() -> {
            List<Notification<?>> foundNotifications = dataSource.getPage(100);
            assertThat(foundNotifications)
                    .usingRecursiveFieldByFieldElementComparatorIgnoringFields("version")
                    .containsAll(of(NOTIFICATION_1, NOTIFICATION_2, NOTIFICATION_3));
        });
    }

    @Test
    void shouldFindFirstPageOfNotifications() {
        int pageSize = 2;
        inTransaction(() -> {
            List<Notification<?>> foundNotifications = dataSource.getPage(pageSize);
            assertThat(foundNotifications).hasSize(pageSize);
        });
    }
}
