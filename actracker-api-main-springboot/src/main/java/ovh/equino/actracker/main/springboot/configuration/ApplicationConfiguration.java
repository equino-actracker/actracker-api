package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.application.tagset.TagSetApplicationService;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.application",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TagSetApplicationService.class
                }
        )
)
class ApplicationConfiguration {
}
