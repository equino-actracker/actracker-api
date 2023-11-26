package ovh.equino.actracker.notification.outbox.activity;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.activity.ActivityChangedNotification;
import ovh.equino.actracker.domain.activity.ActivityNotifier;
import ovh.equino.actracker.notification.outbox.NotificationRepository;

class OutboxActivityNotifier implements ActivityNotifier {

    private final NotificationRepository notificationRepository;

    OutboxActivityNotifier(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyChanged(ActivityChangedNotification activityChangedNotification) {
        Notification<ActivityChangedNotification> notification = new Notification<>(
                activityChangedNotification.id(),
                activityChangedNotification
        );
        notificationRepository.save(notification);
    }
}
