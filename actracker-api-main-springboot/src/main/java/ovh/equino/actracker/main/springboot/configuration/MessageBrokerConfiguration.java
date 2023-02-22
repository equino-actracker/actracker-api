package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.notification.outbox.NotificationPublisher;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.messagebroker.rabbitmq",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        NotificationPublisher.class
                }
        )
)
class MessageBrokerConfiguration {
}
