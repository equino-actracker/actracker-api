package ovh.equino.actracker.messagebroker.memory;

import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

class InMemoryNotificationPublisher implements NotificationPublisher {

    @Override
    public void publishNotification(Notification notification) {
        System.out.printf("Publishing notification with ID=%s to Memory%n", notification.id());
    }
}
