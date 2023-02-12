package ovh.equino.actracker.notification.outbox.activity;

import ovh.equino.actracker.domain.activity.ActivityDto;
import ovh.equino.actracker.domain.activity.ActivityNotifier;
import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.OutboxRepository;

class OutboxActivityNotifier implements ActivityNotifier {

    private final OutboxRepository outbox;

    OutboxActivityNotifier(OutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Override
    public void notifyChanged(ActivityDto activity) {
        Notification notification = new Notification(activity.id(), activity);
        outbox.save(notification);
    }
}
