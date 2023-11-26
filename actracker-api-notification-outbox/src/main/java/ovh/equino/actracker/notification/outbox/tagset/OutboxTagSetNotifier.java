package ovh.equino.actracker.notification.outbox.tagset;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.tagset.TagSetChangedNotification;
import ovh.equino.actracker.domain.tagset.TagSetNotifier;
import ovh.equino.actracker.notification.outbox.NotificationRepository;

class OutboxTagSetNotifier implements TagSetNotifier {

    private final NotificationRepository notificationRepository;

    OutboxTagSetNotifier(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyChanged(TagSetChangedNotification tagSetChangedNotification) {
        Notification<TagSetChangedNotification> notification = new Notification<>(
                tagSetChangedNotification.id(),
                tagSetChangedNotification
        );
        notificationRepository.save(notification);
    }
}
