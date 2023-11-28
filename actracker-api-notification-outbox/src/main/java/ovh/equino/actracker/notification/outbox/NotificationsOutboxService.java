package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;

public class NotificationsOutboxService {

    private final NotificationRepository notificationRepository;
    private final NotificationDataSource notificationDataSource;
    private final NotificationPublisher notificationPublisher;

    public NotificationsOutboxService(NotificationRepository notificationRepository,
                                      NotificationDataSource notificationDataSource,
                                      NotificationPublisher notificationPublisher) {

        this.notificationRepository = notificationRepository;
        this.notificationDataSource = notificationDataSource;
        this.notificationPublisher = notificationPublisher;
    }

    public void publishOutboxedNotifications(int messagesCount) {
        notificationDataSource.getPage(messagesCount)
                .forEach(this::publishAndDelete);
    }

    private void publishAndDelete(Notification<?> notification) {
        notificationPublisher.publishNotification(notification);
        notificationRepository.delete(notification.id());
    }
}
