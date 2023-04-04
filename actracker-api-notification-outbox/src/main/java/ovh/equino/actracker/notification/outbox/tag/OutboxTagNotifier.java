package ovh.equino.actracker.notification.outbox.tag;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.tag.TagChangedNotification;
import ovh.equino.actracker.domain.tag.TagNotifier;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;

class OutboxTagNotifier implements TagNotifier {

    private final NotificationsOutboxRepository outbox;

    OutboxTagNotifier(NotificationsOutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Override
    public void notifyChanged(TagChangedNotification tagChangedNotification) {
        Notification<TagChangedNotification> notification = new Notification<>(
                tagChangedNotification.id(),
                tagChangedNotification
        );
        outbox.save(notification);
    }
}
