package ovh.equino.actracker.notification.outbox;

import ovh.equino.actracker.domain.activity.ActivityChangedNotification;

public interface NotificationPublisher {

    void publishNotification(ActivityChangedNotification changedNotification);
}
