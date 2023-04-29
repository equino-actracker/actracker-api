package ovh.equino.actracker.rest.spring.dashboard.data;

import java.math.BigDecimal;

record DashboardDataBucket(
    String name,
    BigDecimal value,
    BigDecimal percentage
) {
}
