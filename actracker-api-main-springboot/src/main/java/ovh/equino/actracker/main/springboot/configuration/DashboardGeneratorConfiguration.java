package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.dashboard.generation.DashboardGenerationEngine;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.dashboard.generation.repository",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        DashboardGenerationEngine.class
                }
        )
)
class DashboardGeneratorConfiguration {
}
