package ovh.equino.actracker.domain.dashboard.generation;

import java.util.Collection;

public record DashboardChartData(
        String name,
        Collection<ChartBucketData> buckets
) {
}
