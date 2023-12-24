package ovh.equino.actracker.main.springboot.configuration.domain.tag;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.tag.MetricFactory;
import ovh.equino.actracker.domain.tag.MetricsAccessibilityVerifier;
import ovh.equino.actracker.domain.tag.TagFactory;
import ovh.equino.actracker.domain.tag.TagsAccessibilityVerifier;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.domain.tag",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TagFactory.class,
                        MetricFactory.class,
                        TagsAccessibilityVerifier.class,
                        MetricsAccessibilityVerifier.class,
                }
        )
)
class TagDomainConfiguration {
}
