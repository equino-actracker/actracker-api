package ovh.equino.actracker.main.springboot.configuration.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling
class SchedulerConfiguration {

    @Bean
    NotificationsPublishTask notificationsPublishTask() {
        return new NotificationsPublishTask();
    }
}
