package ovh.equino.actracker.notification.outbox;

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

    private void publishAndDelete(Notification notification) {
        ActivityChangedNotification activityChangedNotification = new ActivityChangedNotification(
                notification.version(), notification.entity()
        );
        notificationPublisher.publishNotification(activityChangedNotification);
        outboxRepository.delete(notification.id());
    }
}
