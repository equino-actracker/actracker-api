package ovh.equino.actracker.notification.outbox.tag;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.tag.TagChangedNotification;
import ovh.equino.actracker.domain.tag.TagNotifier;
import ovh.equino.actracker.notification.outbox.NotificationRepository;

class OutboxTagNotifier implements TagNotifier {

    private final NotificationRepository notificationRepository;

    OutboxTagNotifier(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyChanged(TagChangedNotification tagChangedNotification) {
        Notification<TagChangedNotification> notification = new Notification<>(
                tagChangedNotification.id(),
                tagChangedNotification
        );
        notificationRepository.save(notification);
    }
}
