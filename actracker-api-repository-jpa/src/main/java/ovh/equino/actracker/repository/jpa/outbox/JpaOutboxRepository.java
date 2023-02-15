package ovh.equino.actracker.repository.jpa.outbox;

import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.OutboxRepository;
import ovh.equino.actracker.repository.jpa.JpaRepository;

class JpaOutboxRepository extends JpaRepository implements OutboxRepository {

    private final NotificationMapper notificationMapper = new NotificationMapper();

    @Override
    public void save(Notification notification) {
        NotificationEntity notificationEntity = notificationMapper.toEntity(notification);
        System.out.printf("Outboxing entity: ID: %s%nENTITY: %s%n", notificationEntity.id, notificationEntity.entity);
    }
}
