package ovh.equino.actracker.notification.outbox.dashboard;

import ovh.equino.actracker.domain.Notification;
import ovh.equino.actracker.domain.dashboard.DashboardChangedNotification;
import ovh.equino.actracker.domain.dashboard.DashboardNotifier;
import ovh.equino.actracker.notification.outbox.NotificationRepository;

class OutboxDashboardNotifier implements DashboardNotifier {

    private final NotificationRepository notificationRepository;

    OutboxDashboardNotifier(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void notifyChanged(DashboardChangedNotification dashboardChangedNotification) {
        Notification<DashboardChangedNotification> notification = new Notification<>(
                dashboardChangedNotification.id(),
                dashboardChangedNotification
        );
        notificationRepository.save(notification);
    }
}
