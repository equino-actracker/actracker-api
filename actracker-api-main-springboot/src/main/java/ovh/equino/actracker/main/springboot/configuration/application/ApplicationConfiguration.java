package ovh.equino.actracker.main.springboot.configuration.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.application.activity.ActivityApplicationService;
import ovh.equino.actracker.application.dashboard.DashboardApplicationService;
import ovh.equino.actracker.application.tag.TagApplicationService;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.application",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        ActivityApplicationService.class,
                        TagApplicationService.class,
                        DashboardApplicationService.class
                }
        )
)
class ApplicationConfiguration {
}
