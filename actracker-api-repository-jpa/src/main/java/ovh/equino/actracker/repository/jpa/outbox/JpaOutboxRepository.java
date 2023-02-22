package ovh.equino.actracker.repository.jpa.outbox;

import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.OutboxRepository;
import ovh.equino.actracker.repository.jpa.JpaRepository;

import java.util.Collections;
import java.util.List;

class JpaOutboxRepository extends JpaRepository implements OutboxRepository {

    private final NotificationMapper notificationMapper = new NotificationMapper();

    @Override
    public void save(Notification notification) {
        NotificationEntity notificationEntity = notificationMapper.toEntity(notification);
        entityManager.merge(notificationEntity);
    }

    @Override
    public List<Notification> getPage(int limit) {
        return Collections.emptyList();
    }
}
