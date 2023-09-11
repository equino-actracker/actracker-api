package ovh.equino.actracker.notification.outbox.tagset;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.tagset.TagSetChangedNotification;
import ovh.equino.actracker.domain.tagset.TagSetNotifier;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;

class OutboxTagSetNotifier implements TagSetNotifier {

    private final NotificationsOutboxRepository outbox;

    OutboxTagSetNotifier(NotificationsOutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Override
    public void notifyChanged(TagSetChangedNotification tagSetChangedNotification) {
        Notification<TagSetChangedNotification> notification = new Notification<>(
                tagSetChangedNotification.id(),
                tagSetChangedNotification
        );
        outbox.save(notification);
    }
}
