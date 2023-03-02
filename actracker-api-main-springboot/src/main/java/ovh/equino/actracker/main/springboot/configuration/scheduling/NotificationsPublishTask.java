package ovh.equino.actracker.main.springboot.configuration.scheduling;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxService;

import static java.util.concurrent.TimeUnit.SECONDS;

class NotificationsPublishTask {

    @Autowired
    private NotificationsOutboxService notificationsService;

    @Scheduled(initialDelay = 20, fixedDelay = 10, timeUnit = SECONDS)
    void publishNotifications() {
        notificationsService.publishOutboxedNotifications(3);
    }
}
