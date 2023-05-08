package ovh.equino.actracker.rest.spring.dashboard.data;

import java.math.BigDecimal;
import java.util.Collection;

record DashboardDataBucket(
    String id,
    Long rangeStartMillis,
    Long rangeEndMillis,
    String type,
    BigDecimal value,
    BigDecimal percentage,
    Collection<DashboardDataBucket> buckets
) {
}
