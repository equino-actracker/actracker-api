package ovh.equino.actracker.main.springboot.configuration.domain.activity;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityFactory;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.domain.activity",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityFactory.class
                }
        )
)
class ActivityDomainConfiguration {
}
