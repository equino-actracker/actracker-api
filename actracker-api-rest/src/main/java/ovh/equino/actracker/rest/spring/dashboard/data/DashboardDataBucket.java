package ovh.equino.actracker.rest.spring.dashboard.data;

import java.math.BigDecimal;
import java.util.Collection;

record DashboardDataBucket(
    String name,
    BigDecimal value,
    BigDecimal percentage,
    Collection<DashboardDataBucket> buckets
) {
}
