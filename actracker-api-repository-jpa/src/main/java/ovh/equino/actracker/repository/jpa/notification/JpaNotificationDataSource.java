package ovh.equino.actracker.repository.jpa.notification;

import jakarta.persistence.EntityManager;
import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.notification.outbox.NotificationDataSource;
import ovh.equino.actracker.repository.jpa.JpaDAO;

import java.util.List;

import static java.util.stream.Collectors.toList;

class JpaNotificationDataSource extends JpaDAO implements NotificationDataSource {

    JpaNotificationDataSource(EntityManager entityManager) {
        super(entityManager);
    }

    @Override
    public List<Notification<?>> getPage(int limit) {
        return new SelectNotificationsQuery(entityManager)
                .limit(limit)
                .execute()
                .stream()
                .map(NotificationProjection::toNotification)
                .collect(toList());

    }
}
