package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityRepository;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.repository.jpa",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityRepository.class
                }
        )
)
class RepositoryConfiguration {
}
