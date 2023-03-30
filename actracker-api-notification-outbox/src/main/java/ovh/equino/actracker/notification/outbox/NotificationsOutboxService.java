package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.activity.ActivityChangedNotification;

public class NotificationsOutboxService {

    private final NotificationsOutboxRepository outboxRepository;
    private final NotificationPublisher notificationPublisher;

    public NotificationsOutboxService(NotificationsOutboxRepository outboxRepository,
                                      NotificationPublisher notificationPublisher) {

        this.outboxRepository = outboxRepository;
        this.notificationPublisher = notificationPublisher;
    }

    public void publishOutboxedNotifications(int messagesCount) {
        outboxRepository.getPage(messagesCount)
                .forEach(this::publishAndDelete);
    }

    private void publishAndDelete(Notification<?> notification) {
        ActivityChangedNotification activityChangedNotification = new ActivityChangedNotification(
                notification.version(), ((ActivityChangedNotification) notification.data()).activity()
        );
        notificationPublisher.publishNotification(activityChangedNotification);
        notificationPublisher.publishNotification(notification);
        outboxRepository.delete(notification.id());
    }
}
