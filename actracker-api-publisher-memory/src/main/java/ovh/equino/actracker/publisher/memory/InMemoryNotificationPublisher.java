package ovh.equino.actracker.publisher.memory;

import ovh.equino.actracker.domain.activity.ActivityChangedNotification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

class InMemoryNotificationPublisher implements NotificationPublisher {

    @Override
    public void publishNotification(ActivityChangedNotification changedNotification) {
        System.out.printf("Publishing notification with ID=%s to Memory%n", changedNotification.id());
    }
}
