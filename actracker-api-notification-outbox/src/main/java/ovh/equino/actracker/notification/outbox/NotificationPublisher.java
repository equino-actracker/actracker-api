package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.activity.ActivityChangedNotification;

public interface NotificationPublisher {

    // TODO delete
    void publishNotification(ActivityChangedNotification changedNotification);

    void publishNotification(Notification<?> notification);
}
