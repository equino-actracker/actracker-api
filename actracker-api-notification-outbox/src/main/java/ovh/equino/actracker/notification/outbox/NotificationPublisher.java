package ovh.equino.actracker.notification.outbox;

public interface NotificationPublisher {

    void publishNotification(Notification notification);
}
