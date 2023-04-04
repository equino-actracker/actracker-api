package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityNotifier;
import ovh.equino.actracker.domain.tag.TagNotifier;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxRepository;
import ovh.equino.actracker.notification.outbox.NotificationsOutboxService;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.notification.outbox",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityNotifier.class,
                        TagNotifier.class
                }
        )
)
class NotificationConfiguration {

    @Bean
    NotificationsOutboxService notificationsOutboxService(
            NotificationsOutboxRepository outboxRepository,
            NotificationPublisher notificationPublisher) {

        return new NotificationsOutboxService(outboxRepository, notificationPublisher);
    }
}
