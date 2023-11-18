package ovh.equino.actracker.main.springboot.configuration.search;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivitySearchEngine;
import ovh.equino.actracker.domain.dashboard.DashboardSearchEngine;
import ovh.equino.actracker.domain.tag.TagSearchEngine;
import ovh.equino.actracker.domain.tagset.TagSetSearchEngine;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.search.datasource",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TagSearchEngine.class,
                        ActivitySearchEngine.class,
                        TagSetSearchEngine.class,
                        DashboardSearchEngine.class
                }
        )
)
class DataSourceSearchConfiguration {
}
