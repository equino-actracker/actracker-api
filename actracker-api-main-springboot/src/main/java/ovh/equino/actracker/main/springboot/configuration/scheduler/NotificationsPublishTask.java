package ovh.equino.actracker.main.springboot.configuration.scheduler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import ovh.equino.actracker.notification.outbox.Notification;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;
import ovh.equino.actracker.notification.outbox.OutboxRepository;

import static java.util.concurrent.TimeUnit.SECONDS;

class NotificationsPublishTask {

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private NotificationPublisher notificationPublisher;

    @Scheduled(initialDelay = 20, fixedDelay = 10, timeUnit = SECONDS)
    void publishNotifications() {

        outboxRepository.getPage(3)
                .forEach(this::publishAndDelete);
    }

    private void publishAndDelete(Notification notification) {
        notificationPublisher.publishNotification(notification);
        outboxRepository.delete(notification.id());
    }
}
