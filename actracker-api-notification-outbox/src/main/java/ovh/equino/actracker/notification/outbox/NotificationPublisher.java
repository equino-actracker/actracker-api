package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;

public interface NotificationPublisher {

    void publishNotification(Notification<?> notification);
}
