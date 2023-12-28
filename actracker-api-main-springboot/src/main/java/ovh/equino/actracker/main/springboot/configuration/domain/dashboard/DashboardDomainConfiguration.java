package ovh.equino.actracker.main.springboot.configuration.domain.dashboard;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.dashboard.DashboardFactory;
import ovh.equino.actracker.domain.dashboard.DashboardsAccessibilityVerifierImpl;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.domain.dashboard",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        DashboardFactory.class,
                        DashboardsAccessibilityVerifierImpl.class,
                }
        )
)
class DashboardDomainConfiguration {
}
