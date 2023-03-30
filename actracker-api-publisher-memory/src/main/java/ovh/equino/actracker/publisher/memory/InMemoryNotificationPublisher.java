package ovh.equino.actracker.publisher.memory;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

class InMemoryNotificationPublisher implements NotificationPublisher {

    @Override
    public void publishNotification(Notification<?> notification) {
        System.out.printf("Publishing Notification with ID=%s to Memory%n", notification.id());
    }
}
