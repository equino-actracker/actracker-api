package ovh.equino.actracker.main.springboot.configuration.security;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityNotifier;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.notification.outbox",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityNotifier.class
                }
        )
)
public class NotificationConfiguration {
}
