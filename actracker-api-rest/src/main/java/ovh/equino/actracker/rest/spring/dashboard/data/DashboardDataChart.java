package ovh.equino.actracker.rest.spring.dashboard.data;

import java.util.Collection;

record DashboardDataChart(
        String name,
        Collection<DashboardDataBucket> buckets
) {}
