package ovh.equino.actracker.notification.outbox.dashboard;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.dashboard.DashboardChangedNotification;
import ovh.equino.actracker.domain.dashboard.DashboardNotifier;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;

class OutboxDashboardNotifier implements DashboardNotifier {

    private final NotificationsOutboxRepository outbox;

    OutboxDashboardNotifier(NotificationsOutboxRepository outbox) {
        this.outbox = outbox;
    }

    @Override
    public void notifyChanged(DashboardChangedNotification dashboardChangedNotification) {
        Notification<DashboardChangedNotification> notification = new Notification<>(
                dashboardChangedNotification.id(),
                dashboardChangedNotification
        );
        outbox.save(notification);
    }
}
