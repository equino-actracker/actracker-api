package ovh.equino.actracker.main.springboot.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import ovh.equino.actracker.domain.activity.ActivityDataSource;
import ovh.equino.actracker.domain.dashboard.DashboardDataSource;
import ovh.equino.actracker.domain.tag.TagDataSource;
import ovh.equino.actracker.domain.tagset.TagSetDataSource;
import ovh.equino.actracker.domain.tenant.TenantDataSource;
import ovh.equino.actracker.notification.outbox.NotificationDataSource;

@Configuration
@ComponentScan(
        basePackages = "ovh.equino.actracker.datasource.jpa",
        includeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        TenantDataSource.class,
                        ActivityDataSource.class,
                        TagDataSource.class,
                        TagSetDataSource.class,
                        DashboardDataSource.class,
                        NotificationDataSource.class,
                }
        )
)
class JpaDataSourceConfiguration {
}
