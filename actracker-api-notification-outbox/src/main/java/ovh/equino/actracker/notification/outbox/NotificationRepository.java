package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    void save(Notification<?> notification);

    Optional<Notification<?>> get(UUID notificationId);

    void delete(UUID notificationId);
}
