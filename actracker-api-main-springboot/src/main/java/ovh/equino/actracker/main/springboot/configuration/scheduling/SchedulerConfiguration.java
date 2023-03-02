package ovh.equino.actracker.main.springboot.configuration.scheduling;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import ovh.equino.actracker.main.springboot.configuration.scheduling.NotificationsPublishTask;

@Configuration
@EnableScheduling
class SchedulerConfiguration {

    @Bean
    NotificationsPublishTask notificationsPublishTask() {
        return new NotificationsPublishTask();
    }
}
