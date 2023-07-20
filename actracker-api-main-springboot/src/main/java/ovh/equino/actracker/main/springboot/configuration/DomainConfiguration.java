package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityService;
import ovh.equino.actracker.domain.dashboard.DashboardService;
import ovh.equino.actracker.domain.tag.TagService;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.domain",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityService.class,
                        TagService.class,
                        DashboardService.class
                }
        )
)
class DomainConfiguration {
}
