package ovh.equino.actracker.main.springboot.configuration.domain.tag;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.tag.MetricFactory;
import ovh.equino.actracker.domain.tag.TagFactory;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.domain.tag",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TagFactory.class,
                        MetricFactory.class
                }
        )
)
class TagDomainConfiguration {
}
