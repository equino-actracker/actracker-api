package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface NotificationsOutboxRepository {

    void save(Notification<?> notification);

    Optional<Notification<?>> findById(UUID notificationId);

    void delete(UUID notificationId);

    List<Notification<?>> getPage(int limit);
}
