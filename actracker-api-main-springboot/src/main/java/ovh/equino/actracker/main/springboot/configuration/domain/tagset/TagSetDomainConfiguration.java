package ovh.equino.actracker.main.springboot.configuration.domain.tagset;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.tagset.TagSetFactory;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.domain.tagset",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TagSetFactory.class
                }
        )
)
class TagSetDomainConfiguration {
}
